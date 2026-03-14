package com.naturalhub.common.utils.cogVideo;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class CogVideoDemo {
    // 换成你的智谱API_KEY
    private static final String API_KEY = "0f331daca169454c878b857e1e7c5bcd.CjzVA75ltZ5kxvae";
    private static final String BASE_URL = "https://open.bigmodel.cn/api/paas/v4";

    public static void main(String[] args) {
        try {
            System.out.println("===== 提交猫咪科普视频生成任务 =====");
            String taskId = submitCatVideoTask();
            System.out.println("任务ID：" + taskId);

            System.out.println("\n===== 开始轮询查询结果 =====");
            queryTaskResultLoop(taskId);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 提交猫咪科普视频任务（核心在这里！）
    private static String submitCatVideoTask() throws UnirestException {
        String prompt = """
                猫咪科普视频：猫咪的常见习性。
                猫咪性格温顺、爱干净、昼伏夜出。
                画面展示猫咪舔毛清洁身体、白天睡觉、晚上玩耍跑跳。
                猫咪蹭人标记领地、发出呼噜声表示放松。
                画面温馨清晰，1080P，可爱宠物风格，流畅动画
                """;

        String body = """
                {
                  "model": "cogvideox-3",
                  "prompt": "%s",
                  "quality": "quality",
                  "with_audio": true,
                  "size": "1920x1080",
                  "fps": 30,
                  "duration": 10
                }
                """.formatted(prompt.replace("\n", " "));

        HttpResponse<JsonNode> response = Unirest.post(BASE_URL + "/videos/generations")
                .header("Authorization", "Bearer " + API_KEY)
                .header("Content-Type", "application/json")
                .body(body)
                .asJson();

        return response.getBody().getObject().getString("id");
    }

    // 轮询查询任务
    private static void queryTaskResultLoop(String taskId) throws Exception {
        while (true) {
            HttpResponse<JsonNode> res = Unirest.get(BASE_URL + "/async-result/" + taskId)
                    .header("Authorization", "Bearer " + API_KEY)
                    .asJson();

            JsonNode json = res.getBody();
            String status = json.getObject().optString("task_status", "PROCESSING");

            System.out.println("当前状态：" + status);

            if (status.equals("SUCCESS")) {
                String url = json.getObject().getJSONArray("video_result")
                        .getJSONObject(0).getString("url");
                System.out.println("\n✅ 视频生成成功！");
                System.out.println("视频地址：" + url);
                break;
            }

            if (status.equals("FAIL")) {
                System.err.println("❌ 视频生成失败");
                break;
            }

            Thread.sleep(3000);
        }
    }
}