package com.naturalhub.framework.config;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Base64;
import java.io.ByteArrayOutputStream;
import java.util.Random;

public class AvatarUtils {

    private static final Random random = new Random();
    // 头像尺寸 100x100
    private static final int SIZE = 100;
    // 字体大小
    private static final int FONT_SIZE = 40;

    /**
     * 生成用户名头像（首字母 + 随机背景）
     * @return Base64 图片字符串（可直接存入数据库 avatar 字段）
     */
    public static String generateAvatarBase64(String username) {
        try {
            // 1. 创建图片
            BufferedImage image = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();

            // 2. 随机柔和背景色（避免深色看不清文字）
            g.setColor(getRandomSoftColor());
            g.fillRect(0, 0, SIZE, SIZE);

            // 3. 获取首字母（大写）
            String firstLetter = username.substring(0, 1).toUpperCase();

            // 4. 绘制文字（白色、居中）
            g.setColor(Color.WHITE);
            g.setFont(new Font("Microsoft YaHei", Font.BOLD, FONT_SIZE));
            FontMetrics metrics = g.getFontMetrics();
            int x = (SIZE - metrics.stringWidth(firstLetter)) / 2;
            int y = ((SIZE - metrics.getHeight()) / 2) + metrics.getAscent();
            g.drawString(firstLetter, x, y);

            g.dispose();

            // 5. 转 Base64
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            javax.imageio.ImageIO.write(image, "png", baos);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 生成柔和随机色
     */
    private static Color getRandomSoftColor() {
        int r = 100 + random.nextInt(155);
        int g = 100 + random.nextInt(155);
        int b = 100 + random.nextInt(155);
        return new Color(r, g, b);
    }
}