package com.naturalhub.common.utils.wiki;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class WikiRagChatUtil {

    private static final String WIKI_URL = "http://81.68.236.229:3000/graphql";
    private static final String WIKI_KEY = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJhcGkiOjEsImdycCI6MSwiaWF0IjoxNzcyNzI1ODMyLCJleHAiOjE4MDQyODM0MzIsImF1ZCI6InVybjp3aWtpLmpzIiwiaXNzIjoidXJuOndpa2kuanMifQ.M1DTkryXnFrgjSQ2fUZ55sFOCOdrzO8CTcORa8y6rRc-AxU5yHvUovsCXJD3lzvvdc4-gADwdfaZ6pT18GrTq1vI7WciAx45J6zJd16oB-wa9ObVpvPYBJLt1qm71RAt-xGhxOQgTHQXll1wtixGJTC0Ywfs5HuyhSxiVsCuPlnyPDaQrDDc-gGXxEuSI5XbKizCHrmlH4QqxP8F-HvPxCXnd4VCFzQ3DDyBTkXX67dwM8em4NBe_0RKdOjAR9GaNsKGJGoCVKAe53tiRQbaxFlzbM_ZuGvAJlZpSjx7V6KzM-rnypZQN9nScr0ZHJsWwJORr2D8I2Ts7BvUDVgzQw";
    private static final String QWEN_KEY = "sk-64fbaf4273d349eeaebddde942edd65f";
    private static final String QWEN_API = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";

    public static String search(String keyword) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(WIKI_URL).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + WIKI_KEY);
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

    public static String getPageContent(String pageId) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(WIKI_URL).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + WIKI_KEY);
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

    public static String qwen(String prompt) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(QWEN_API).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + QWEN_KEY);
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

    public static String chat(String question) {
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
                String title = item.getString("title");

                String pageJson = getPageContent(pageId);
                if (pageJson == null) continue;

                JSONObject pageObj = new JSONObject(pageJson);
                String content = pageObj.getJSONObject("data")
                        .getJSONObject("pages")
                        .getJSONObject("single")
                        .getString("content")
                        .replaceAll("<[^>]+>", "");

//                context.append("【").append(title).append("】\n");
                context.append(content).append("\n\n");
            }

            String prompt = "你是生物知识库助手，请直接按照资料原文回答，不要做任何修改和精简。资料：" + context + " 问题：" + question;
            return qwen(prompt);
        } catch (Exception e) {
            return "解析知识库失败：" + e.getMessage();
        }
    }
}
