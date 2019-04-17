package com.voxlearning.washington.support;


import com.voxlearning.alps.logger.LoggerFactory;
import org.slf4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;

public class TobyAvatar {
    private static Logger log = LoggerFactory.getLogger(TobyAvatar.class);
    private static String PATH_PRE = TobyAvatar.class.getResource("/toby_avatar_bg").getPath() + "/";
//    private static String PATH_PRE = "D:\\gitlab\\vox-17zuoye\\washington\\washington-webapp\\src\\main\\resources\\toby_avatar_bg" + "/";

    public static void main(String[] args) throws Exception {
        BufferedImage image = pressImage(getBGImg(),  "https://v.17zuoye.cn/Prize/chanpin/2018xuedouleyuan/tuobi/hat4.png", 160, 150);
        image = pressImage(image,  "https://v.17zuoye.cn/Prize/chanpin/2018xuedouleyuan/tuobi/suit7.png", 0, 0);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", byteArrayOutputStream);
        FileOutputStream fs = new FileOutputStream(new File("C:/Users/17ZY-HPYKFD2/Desktop/test.png"));
        fs.write(byteArrayOutputStream.toByteArray());
    }

    public static byte[] getDefaultImgBytes() throws IOException {
        FileInputStream fin = new FileInputStream(new File(PATH_PRE + "toby_default.jpg"));
        byte[] bytes = new byte[fin.available()];
        fin.read(bytes);
        return bytes;
    }

    public static BufferedImage getDefaultImg() throws IOException {
        return ImageIO.read(new File(PATH_PRE + "toby_default.jpg"));
    }

    public static BufferedImage getBGImg() throws IOException {
        return ImageIO.read(new File(PATH_PRE + "toby_bg.jpg"));
    }


    public static BufferedImage pressImage(final Image image, String waterPath, int x, int y) throws Exception {
        return pressImage(image, waterPath, x, y, 1.0f);
    }

    /**
     * 添加图片水印
     *
     * @param image     目标图片
     * @param waterPath 水印图片路径
     * @param x         水印图片距离目标图片左侧的偏移量，如果x<0, 则在正中间
     * @param y         水印图片距离目标图片上侧的偏移量，如果y<0, 则在正中间
     * @param alpha     透明度(0.0 -- 1.0, 0.0为完全透明，1.0为完全不透明)
     */
    private static BufferedImage pressImage(final Image image, String waterPath, int x, int y, float alpha) throws Exception {
        final Image waterImage = toImage(waterPath);
        int width = image.getWidth(null);
        int height = image.getHeight(null);
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bufferedImage.createGraphics();
        g.drawImage(image, 0, 0, width, height, null);

        int width_1 = waterImage.getWidth(null);
        int height_1 = waterImage.getHeight(null);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));

        int widthDiff = width - width_1;
        int heightDiff = height - height_1;
        if (x < 0) {
            x = widthDiff / 2;
        } else if (x > widthDiff) {
            x = widthDiff;
        }
        if (y < 0) {
            y = heightDiff / 2;
        } else if (y > heightDiff) {
            y = heightDiff;
        }
        g.drawImage(waterImage, x, y, width_1, height_1, null); // 水印文件结束
        g.dispose();
        return bufferedImage;
    }

    private static Image toImage(String imgPath) throws Exception {
//        URLConnection conn = url.openConnection();
//        IOUtils.readFully(conn.getInputStream(), -1, true);
        return ImageIO.read(new URL(imgPath).openStream());
    }
}
