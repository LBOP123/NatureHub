package com.naturalhub.common.utils.wiki;

import com.naturalhub.common.config.QwenConfig;
import com.naturalhub.common.config.WikiRagConfig;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Component
public class WikiRagChatUtil {

    @Autowired
    private WikiRagConfig wikiRagConfig;

    @Autowired
    private QwenConfig qwenConfig;

    public String search(String keyword) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(wikiRagConfig.getUrl()).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + wikiRagConfig.getKey());
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String graphql = "{ pages { search(query: \"" + keyword + "\") { results { id title } } } }";
            JSONObject requestBody = new JSONObject();
            requestBody.put("query", graphql);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(requestBody.toString().getBytes(StandardCharsets.UTF_8));
            }

            BufferedReader br = conn.getResponseCode() >= 400
                    ? new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))
                    : new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            br.close();
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getPageContent(String pageId) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(wikiRagConfig.getUrl()).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + wikiRagConfig.getKey());
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String graphql = "{ pages { single(id: " + pageId + ") { id title content } } }";
            JSONObject requestBody = new JSONObject();
            requestBody.put("query", graphql);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(requestBody.toString().getBytes(StandardCharsets.UTF_8));
            }

            BufferedReader br = conn.getResponseCode() >= 400
                    ? new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))
                    : new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            br.close();
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String qwen(String prompt) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(qwenConfig.getApi()).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + qwenConfig.getKey());
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            JSONObject body = new JSONObject();
            body.put("model", "qwen-plus");
            body.put("messages", new JSONArray().put(new JSONObject().put("role", "user").put("content", prompt)));
            body.put("temperature", 0.1);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.toString().getBytes(StandardCharsets.UTF_8));
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);

            JSONObject res = new JSONObject(sb.toString());
            return res.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
        } catch (Exception e) {
            return "AI调用失败：" + e.getMessage();
        }
    }

    public String chat(String question) {
        String wikiSearch = search(question);
        if (wikiSearch == null) return "知识库访问失败";

        try {
            JSONObject json = new JSONObject(wikiSearch);
            JSONArray results = json.getJSONObject("data")
                    .getJSONObject("pages")
                    .getJSONObject("search")
                    .getJSONArray("results");

            if (results.isEmpty()) return "未找到相关内容";

            StringBuilder context = new StringBuilder();
            for (int i = 0; i < results.length(); i++) {
                JSONObject item = results.getJSONObject(i);
                String pageId = item.getString("id");

                String pageJson = getPageContent(pageId);
                if (pageJson == null) continue;

                JSONObject pageObj = new JSONObject(pageJson);
                String content = pageObj.getJSONObject("data")
                        .getJSONObject("pages")
                        .getJSONObject("single")
                        .getString("content")
                        .replaceAll("<[^>]+>", "");

                context.append(content).append("\n\n");
            }

            String prompt = "你是生物知识库助手，请直接按照资料原文回答，不要做任何修改和精简。资料：" + context + " 问题：" + question;
            return qwen(prompt);
        } catch (Exception e) {
            return "解析知识库失败：" + e.getMessage();
        }
    }
}
