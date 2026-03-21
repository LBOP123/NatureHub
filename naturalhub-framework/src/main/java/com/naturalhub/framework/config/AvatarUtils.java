package com.naturalhub.framework.config;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.Random;

public class AvatarUtils {

    private static final Random random = new Random();
    private static final int SIZE = 100;
    private static final int FONT_SIZE = 40;

    // 预定义柔和背景色组（自然主题）
    private static final int[][] SOFT_COLORS = {
        {67, 160, 107},  // 自然绿
        {41, 128, 185},  // 天空蓝
        {142, 107, 191}, // 薰衣草紫
        {192, 100, 10},  // 琥珀橙
        {39, 174, 96},   // 翠绿
        {52, 152, 219},  // 海洋蓝
        {155, 89, 182},  // 紫罗兰
        {230, 126, 34},  // 橙黄
        {26, 188, 156},  // 青绿
        {231, 76, 60},   // 珊瑚红
    };

    /**
     * 生成用户名头像字节流（可直接上传到七牛云）
     */
    public static InputStream generateAvatarStream(String username) {
        try {
            byte[] bytes = generateAvatarBytes(username);
            return new ByteArrayInputStream(bytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 生成用户名头像字节数组
     */
    public static byte[] generateAvatarBytes(String username) {
        try {
            BufferedImage image = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();

            // 抗锯齿
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // 随机背景色（从预定义色板取，按用户名哈希保证同一用户颜色固定）
            int colorIdx = Math.abs(username.hashCode()) % SOFT_COLORS.length;
            int[] rgb = SOFT_COLORS[colorIdx];
            g.setColor(new Color(rgb[0], rgb[1], rgb[2]));
            g.fillRect(0, 0, SIZE, SIZE);

            // 取首字符（中文取第一个字，英文取首字母大写）
            String firstChar = username.substring(0, 1).toUpperCase();

            // 绘制白色文字居中
            g.setColor(Color.WHITE);
            g.setFont(new Font("Microsoft YaHei", Font.BOLD, FONT_SIZE));
            FontMetrics metrics = g.getFontMetrics();
            int x = (SIZE - metrics.stringWidth(firstChar)) / 2;
            int y = ((SIZE - metrics.getHeight()) / 2) + metrics.getAscent();
            g.drawString(firstChar, x, y);
            g.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            javax.imageio.ImageIO.write(image, "png", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    /**
     * 生成 Base64 头像字符串（兼容旧接口）
     */
    public static String generateAvatarBase64(String username) {
        try {
            byte[] bytes = generateAvatarBytes(username);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
