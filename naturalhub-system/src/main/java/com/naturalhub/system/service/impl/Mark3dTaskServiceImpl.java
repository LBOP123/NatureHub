package com.naturalhub.system.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.naturalhub.system.domain.Mark3dTask;
import com.naturalhub.system.mapper.Mark3dTaskMapper;
import com.naturalhub.system.service.IMark3dTaskService;
import com.naturalhub.common.utils.qiniu.QiniuUtil;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class Mark3dTaskServiceImpl implements IMark3dTaskService {

    private static final Logger log = LoggerFactory.getLogger(Mark3dTaskServiceImpl.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String ALGORITHM = "TC3-HMAC-SHA256";
    private static final String SERVICE = "ai3d";
    private static final String VERSION = "2025-05-13";
    private static final String HOST = "ai3d.tencentcloudapi.com";
    private static final String ENDPOINT = "https://ai3d.tencentcloudapi.com";

    private static final String STATUS_PENDING = "1";
    private static final String STATUS_IN_PROGRESS = "2";
    private static final String STATUS_SUCCEEDED = "3";
    private static final String STATUS_FAILED = "4";

    @Value("${hunyuan3d.secret-id}")
    private String secretId;

    @Value("${hunyuan3d.secret-key}")
    private String secretKey;

    @Value("${hunyuan3d.region:ap-guangzhou}")
    private String region;

    @Autowired
    private Mark3dTaskMapper mark3dTaskMapper;

    @Autowired(required = false)
    private QiniuUtil qiniuUtil;
    private OkHttpClient httpClient;

    @PostConstruct
    public void init() {
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(20, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(40, java.util.concurrent.TimeUnit.SECONDS)
                .build();
    }

    @Override
    public Mark3dTask createTask(Mark3dTask task) throws Exception {
        String imageUrl = task.getFrontImageUrl();
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            throw new RuntimeException("请上传有效的图片地址");
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("ImageUrl", imageUrl);
        body.put("ResultFormat", "GLB");
        body.put("EnablePBR", false);

        String resp = sendRequest("SubmitHunyuanTo3DRapidJob", body);
        JsonNode root = MAPPER.readTree(resp);
        JsonNode err = root.path("Response").path("Error");
        if (!err.isMissingNode()) {
            String msg = err.path("Message").asText();
            log.error("创建任务失败: {}", msg);
            throw new RuntimeException("创建任务失败：" + msg);
        }

        String jobId = root.path("Response").path("JobId").asText();
        task.setMeshyTaskId(jobId);
        task.setTaskStatus(STATUS_PENDING);
        task.setProgress(0);
        mark3dTaskMapper.insertMark3dTask(task);
        return task;
    }

    @Override
    public Mark3dTask syncTaskStatus(Long id) throws Exception {
        Mark3dTask local = mark3dTaskMapper.selectMark3dTaskById(id);
        if (local == null) throw new RuntimeException("任务不存在");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("JobId", local.getMeshyTaskId());

        String resp = sendRequest("QueryHunyuanTo3DRapidJob", body);
        JsonNode response = MAPPER.readTree(resp).path("Response");
        JsonNode err = response.path("Error");
        if (!err.isMissingNode()) {
            throw new RuntimeException("查询失败：" + err.path("Message").asText());
        }

        String status = response.path("Status").asText();
        Mark3dTask update = new Mark3dTask();
        update.setId(id);
        update.setTaskStatus(convertStatus(status));

        if ("DONE".equals(status)) {
            update.setProgress(100);
            JsonNode files = response.path("ResultFile3Ds");
            if (files.isArray()) {
                for (JsonNode f : files) {
                    String type = f.path("Type").asText();
                    String url = f.path("Url").asText();
                    String previewUrl = f.path("PreviewImageUrl").asText("");
                    if ("GLB".equalsIgnoreCase(type) && !url.isEmpty()) {
                        update.setModelUrlGlb(url);
                        // 将腾讯云临时GLB转存七牛云，获得永久地址
                        try {
                            String qiniuUrl = uploadUrlToQiniu(url, "glb");
                            update.setModelUrlGlbQiniu(qiniuUrl);
                            log.info("GLB已转存七牛云: {}", qiniuUrl);
                        } catch (Exception e) {
                            log.warn("GLB转存七牛云失败，使用临时地址: {}", e.getMessage());
                        }
                    }
                    if ("OBJ".equalsIgnoreCase(type) && !url.isEmpty()) {
                        update.setModelUrlObj(url);
                    }
                    // 预览图转存七牛云
                    if (!previewUrl.isEmpty() && update.getThumbnailUrl() == null) {
                        update.setThumbnailUrl(previewUrl);
                        try {
                            String qiniuThumb = uploadUrlToQiniu(previewUrl, "png");
                            update.setThumbnailQiniu(qiniuThumb);
                            log.info("预览图已转存七牛云: {}", qiniuThumb);
                        } catch (Exception e) {
                            log.warn("预览图转存七牛云失败: {}", e.getMessage());
                        }
                    }
                }
            }
        }
        mark3dTaskMapper.updateMark3dTask(update);
        return mark3dTaskMapper.selectMark3dTaskById(id);
    }

    /**
     * 从远程URL下载文件并上传到七牛云，返回七牛云永久地址
     * @param remoteUrl 腾讯云临时URL
     * @param ext       文件扩展名（glb/png等）
     */
    private String uploadUrlToQiniu(String remoteUrl, String ext) throws Exception {
        if (qiniuUtil == null) throw new RuntimeException("七牛云未配置");
        // 用 OkHttp 下载临时文件流
        Request dlReq = new Request.Builder().url(remoteUrl).build();
        try (Response dlResp = httpClient.newCall(dlReq).execute()) {
            if (!dlResp.isSuccessful() || dlResp.body() == null) {
                throw new RuntimeException("下载失败: " + remoteUrl);
            }
            java.io.InputStream is = dlResp.body().byteStream();
            String datePath = new java.text.SimpleDateFormat("yyyy/MM/dd").format(new java.util.Date());
            String fileName = "mark3d/" + datePath + "/" + java.util.UUID.randomUUID().toString().replace("-", "") + "." + ext;
            return qiniuUtil.uploadFile(is, fileName);
        }
    }

    private String sendRequest(String action, Map<String, Object> body) throws Exception {
        long timestamp = System.currentTimeMillis() / 1000;
        String date = getDate(timestamp);
        String payload = MAPPER.writeValueAsString(body);

        String canonicalHeaders = "content-type:application/json; charset=utf-8\nhost:" + HOST + "\nx-tc-action:" + action.toLowerCase() + "\n";
        String signedHeaders = "content-type;host;x-tc-action";
        String payloadHash = sha256(payload);

        String canonicalRequest = "POST\n/\n\n" + canonicalHeaders + "\n" + signedHeaders + "\n" + payloadHash;
        String credentialScope = date + "/" + SERVICE + "/tc3_request";
        String hashedRequest = sha256(canonicalRequest);
        String stringToSign = ALGORITHM + "\n" + timestamp + "\n" + credentialScope + "\n" + hashedRequest;

        byte[] kDate = hmac256(("TC3" + secretKey).getBytes(StandardCharsets.UTF_8), date);
        byte[] kService = hmac256(kDate, SERVICE);
        byte[] kSigning = hmac256(kService, "tc3_request");
        String signature = hex(hmac256(kSigning, stringToSign));

        String auth = ALGORITHM
                + " Credential=" + secretId + "/" + credentialScope
                + ", SignedHeaders=" + signedHeaders
                + ", Signature=" + signature;

        RequestBody requestBody = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                payload
        );

        Request request = new Request.Builder()
                .url(ENDPOINT)
                .post(requestBody)
                .header("Authorization", auth)
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Host", HOST)
                .header("X-TC-Action", action)
                .header("X-TC-Version", VERSION)
                .header("X-TC-Timestamp", String.valueOf(timestamp))
                .header("X-TC-Region", region)
                .build();

        try (Response res = httpClient.newCall(request).execute()) {
            return res.body() != null ? res.body().string() : "";
        }
    }

    private String convertStatus(String s) {
        switch (s) {
            case "WAIT": return STATUS_PENDING;
            case "RUN": return STATUS_IN_PROGRESS;
            case "DONE": return STATUS_SUCCEEDED;
            case "FAIL": return STATUS_FAILED;
            default: return STATUS_PENDING;
        }
    }

    private static String sha256(String s) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return hex(md.digest(s.getBytes(StandardCharsets.UTF_8)));
    }

    private static byte[] hmac256(byte[] key, String msg) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        return mac.doFinal(msg.getBytes(StandardCharsets.UTF_8));
    }

    private static String hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    private static String getDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date(timestamp * 1000));
    }

    @Override
    public List<Mark3dTask> selectTaskListByUser(Long userId) {
        Mark3dTask t = new Mark3dTask();
        t.setUserId(userId);
        return mark3dTaskMapper.selectMark3dTaskList(t);
    }

    @Override
    public List<Mark3dTask> selectTaskList(Mark3dTask task) {
        return mark3dTaskMapper.selectMark3dTaskList(task);
    }

    @Override
    public Mark3dTask selectTaskById(Long id) {
        return mark3dTaskMapper.selectMark3dTaskById(id);
    }

    @Override
    public int deleteTaskByIds(Long[] ids) {
        return mark3dTaskMapper.deleteMark3dTaskByIds(ids);
    }

    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", mark3dTaskMapper.countTotal());
        stats.put("today", mark3dTaskMapper.countToday());
        java.util.List<Map<String, Object>> byStatus = mark3dTaskMapper.countByStatus();
        int succeeded = 0, failed = 0, inProgress = 0;
        for (Map<String, Object> row : byStatus) {
            String s = String.valueOf(row.get("status"));
            int cnt = ((Number) row.get("cnt")).intValue();
            if (STATUS_SUCCEEDED.equals(s)) succeeded = cnt;
            else if (STATUS_FAILED.equals(s)) failed = cnt;
            else inProgress += cnt;
        }
        stats.put("succeeded", succeeded);
        stats.put("failed", failed);
        stats.put("inProgress", inProgress);
        int total = mark3dTaskMapper.countTotal();
        stats.put("successRate", total == 0 ? 0.0 : Math.round(succeeded * 100.0 / total * 100) / 100.0);
        return stats;
    }

    @Override
    public java.util.List<com.naturalhub.system.domain.Mark3dTask> selectPublicTasksByUserId(Long userId) {
        return mark3dTaskMapper.selectPublicTasksByUserId(userId);
    }

    @Override
    public int updateTaskPublic(Long id, Integer isPublic) {
        return mark3dTaskMapper.updateTaskPublic(id, isPublic);
    }



    @Override
    public int updateTask(com.naturalhub.system.domain.Mark3dTask task) {
        return mark3dTaskMapper.updateMark3dTask(task);
    }
}
