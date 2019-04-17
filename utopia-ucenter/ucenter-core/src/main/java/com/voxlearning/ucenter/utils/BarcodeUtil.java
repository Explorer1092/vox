package com.voxlearning.ucenter.utils;


import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.voxlearning.alps.logger.LoggerFactory;
import org.slf4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;

/**
 * @Author:XiaochaoWei
 * @Description: 生成条形码
 * @CreateTime: 2017/6/12
 */
public class BarcodeUtil {

    private static final Logger logger = LoggerFactory.getLogger(BarcodeUtil.class);
    private static final int BLACK = -16777216;
    private static final int WHITE = -1;

    public static byte[] generateCode(String content) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            int width = 111;
            int height = 16;
            HashMap<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            BitMatrix matrix = new MultiFormatWriter().encode(
                    new String(content.getBytes("UTF-8"), "ISO-8859-1"),
                    BarcodeFormat.CODE_128,
                    width,
                    height,
                    hints);

            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    image.setRGB(x, y, matrix.get(x, y) ? BLACK : WHITE);
                }
            }
            ImageIO.write(image, "png", out);
        } catch (Exception e) {
            logger.error("generate barcode error,", e);
        }
        return out.toByteArray();

    }

//    public static void main(String[] args) {
//        BufferedImage image = null;
//        Result result = null;
//        try {
//            image = ImageIO.read(new File("D:\\test2.png"));
//            if (image == null) {
//                System.out.println("the decode image may be not exit.");
//            }
//            LuminanceSource source = new BufferedImageLuminanceSource(image);
//            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
//
//            result = new MultiFormatReader().decode(bitmap, null);
//            System.out.println("=========" + result);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

}
