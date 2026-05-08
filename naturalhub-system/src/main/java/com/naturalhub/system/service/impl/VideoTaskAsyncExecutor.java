package com.naturalhub.system.service.impl;

import com.naturalhub.system.domain.SpeciesVideoTask;
import com.naturalhub.system.mapper.SpeciesVideoTaskMapper;
import com.naturalhub.system.service.IQaHistoryService;
import com.naturalhub.common.utils.qiniu.QiniuUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * 视频生成异步执行器（独立 Bean，保证 @Async 代理生效）
 *
 * @author NaturalHub
 */
@Component
public class VideoTaskAsyncExecutor {

    private static final Logger log = LoggerFactory.getLogger(VideoTaskAsyncExecutor.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String COZE_STREAM_URL  = "https://api.coze.cn/v1/workflow/stream_run";
    private static final String COZE_API_TOKEN   = "pat_WPLAYbcr0fjQxgdLIiJRQpj6AdIpEqbpPlhkXf2Dv2AaTgmesPoKA4XeEsaWlAvn";
    private static final String COZE_WORKFLOW_ID = "7614489912053973034";

    private static final String STATUS_IN_PROGRESS = "2";
    private static final String STATUS_SUCCEEDED   = "3";
    private static final String STATUS_FAILED      = "4";

    /** qa_history answer 占位符 */
    private static final String ANSWER_GENERATING = "[GENERATING]";
    /** qa_history answer 失败前缀 */
    private static final String ANSWER_FAILED_PREFIX = "[FAILED] ";

    @Autowired
    private SpeciesVideoTaskMapper videoTaskMapper;

    @Autowired
    private IQaHistoryService qaHistoryService;

    @Autowired(required = false)
    private QiniuUtil qiniuUtil;

    @Async
    public void execute(SpeciesVideoTask task, OkHttpClient httpClient) {
        Long taskId = task.getId();
        String content = task.getContent();
        updateStatus(taskId, STATUS_IN_PROGRESS, null, null);
        try {
            String videoUrl = callCozeStreamWorkflow(content, httpClient);
            if (videoUrl == null || videoUrl.isEmpty()) {
                throw new RuntimeException("扣子工作流未返回视频URL");
            }
            String finalUrl = uploadVideoToQiniu(videoUrl, httpClient);
            updateStatus(taskId, STATUS_SUCCEEDED, finalUrl, null);
            log.info("视频生成任务完成 taskId={} url={}", taskId, finalUrl);
            // 回填 qa_history answer 为真实 URL
            updateQaHistoryAnswer(task, finalUrl);
        } catch (Exception e) {
            log.error("视频生成任务失败 taskId={}", taskId, e);
            String msg = e.getMessage();
            String truncated = msg != null && msg.length() > 490 ? msg.substring(0, 490) : msg;
            updateStatus(taskId, STATUS_FAILED, null, truncated);
            // 回填 qa_history answer 为失败信息
            updateQaHistoryAnswer(task, ANSWER_FAILED_PREFIX + (msg != null ? msg : "未知错误"));
        }
    }

    /**
     * 回填 qa_history.answer
     * - 成功：answer = 视频 URL
     * - 失败：answer = [FAILED] 原因
     */
    private void updateQaHistoryAnswer(SpeciesVideoTask task, String answer) {
        try {
            if (task.getQaHistoryId() == null) return;
            qaHistoryService.updateAnswerById(task.getQaHistoryId(), answer);
            log.info("已回填 qa_history answer historyId={}", task.getQaHistoryId());
        } catch (Exception e) {
            log.warn("回填 qa_history 失败: {}", e.getMessage());
        }
    }

    private String callCozeStreamWorkflow(String content, OkHttpClient httpClient) throws Exception {
        String bodyJson = MAPPER.writeValueAsString(
            new java.util.LinkedHashMap<String, Object>() {{
                put("workflow_id", COZE_WORKFLOW_ID);
                put("parameters", new java.util.LinkedHashMap<String, Object>() {{
                    put("content", content);
                }});
            }}
        );

        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(mediaType, bodyJson);

        Request request = new Request.Builder()
                .url(COZE_STREAM_URL)
                .post(body)
                .header("Authorization", "Bearer " + COZE_API_TOKEN)
                .header("Content-Type", "application/json")
                .header("Accept", "text/event-stream")
                .build();

        log.info("调用扣子流式工作流，content长度={}", content.length());

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                throw new RuntimeException("扣子接口返回异常: " + response.code());
            }

            String videoUrl = null;
            InputStream is = response.body().byteStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));

            String line;
            String lastEvent = null;
            StringBuilder dataBuffer = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                log.debug("SSE line: {}", line);
                if (line.startsWith("event:")) {
                    lastEvent = line.substring(6).trim();
                    dataBuffer = new StringBuilder();
                } else if (line.startsWith("data:")) {
                    dataBuffer.append(line.substring(5).trim());
                } else if (line.isEmpty() && dataBuffer.length() > 0) {
                    String dataStr = dataBuffer.toString();
                    log.debug("SSE event={} data={}", lastEvent, dataStr);
                    if ("Message".equals(lastEvent) || lastEvent == null) {
                        String extracted = extractVideoUrl(dataStr);
                        if (extracted != null) {
                            videoUrl = extracted;
                            log.info("提取到视频URL: {}", videoUrl);
                        }
                    } else if ("Done".equals(lastEvent)) {
                        log.info("扣子工作流执行完毕");
                        break;
                    } else if ("Error".equals(lastEvent)) {
                        throw new RuntimeException("扣子工作流出错: " + dataStr);
                    }
                    dataBuffer = new StringBuilder();
                }
            }
            return videoUrl;
        }
    }

    private String extractVideoUrl(String dataStr) {
        if (dataStr == null || dataStr.isEmpty() || "[DONE]".equals(dataStr)) return null;
        try {
            JsonNode root = MAPPER.readTree(dataStr);
            if (!root.path("node_is_finish").asBoolean(false)) return null;
            log.debug("End节点 data: {}", dataStr);
            JsonNode contentNode = root.path("content");
            if (!contentNode.isMissingNode() && !contentNode.isNull()) {
                String contentStr = contentNode.isTextual() ? contentNode.asText() : contentNode.toString();
                log.debug("End节点 content 值: {}", contentStr);
                try {
                    JsonNode inner = MAPPER.readTree(contentStr);
                    JsonNode outputNode = inner.path("output");
                    if (outputNode.isTextual()) {
                        String url = outputNode.asText();
                        if (url.startsWith("http")) return url;
                    } else if (outputNode.isObject()) {
                        String url = outputNode.path("url").asText(null);
                        if (url != null && url.startsWith("http")) return url;
                        url = outputNode.path("video_url").asText(null);
                        if (url != null && url.startsWith("http")) return url;
                    }
                    String url = inner.path("video_url").asText(null);
                    if (url != null && url.startsWith("http")) return url;
                    url = inner.path("url").asText(null);
                    if (url != null && url.startsWith("http")) return url;
                } catch (Exception ignored) {
                    if (contentStr.startsWith("http") && (contentStr.contains(".mp4") || contentStr.contains("video"))) {
                        return contentStr;
                    }
                }
            }
        } catch (Exception e) {
            log.debug("解析SSE data失败: {}", e.getMessage());
        }
        return null;
    }

    private String uploadVideoToQiniu(String remoteUrl, OkHttpClient httpClient) throws Exception {
        if (qiniuUtil == null) {
            log.warn("七牛云未配置，直接返回原始URL");
            return remoteUrl;
        }
        Request dlReq = new Request.Builder().url(remoteUrl).build();
        try (Response dlResp = httpClient.newCall(dlReq).execute()) {
            if (!dlResp.isSuccessful() || dlResp.body() == null) {
                throw new RuntimeException("下载视频失败: " + remoteUrl);
            }
            String datePath = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
            String fileName = "species_video/" + datePath + "/"
                    + UUID.randomUUID().toString().replace("-", "") + ".mp4";
            String qiniuUrl = qiniuUtil.uploadFile(dlResp.body().byteStream(), fileName);
            log.info("视频已转存七牛云: {}", qiniuUrl);
            return qiniuUrl;
        }
    }

    private void updateStatus(Long taskId, String status, String videoUrl, String errorMessage) {
        SpeciesVideoTask update = new SpeciesVideoTask();
        update.setId(taskId);
        update.setTaskStatus(status);
        if (videoUrl != null) update.setVideoUrl(videoUrl);
        if (errorMessage != null) update.setErrorMessage(errorMessage);
        videoTaskMapper.updateSpeciesVideoTask(update);
    }
}
