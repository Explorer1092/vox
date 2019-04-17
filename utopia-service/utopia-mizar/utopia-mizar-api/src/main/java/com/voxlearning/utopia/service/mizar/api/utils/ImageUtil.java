package com.voxlearning.utopia.service.mizar.api.utils;

import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Created by xiang.lv on 2016/10/28.
 *  图片大小验证
 * @author xiang.lv
 */
final public class ImageUtil {

    /**
     * 判断指定文大小是否相等
     * @param width
     * @param height
     * @param file
     * @return
     */
    public static boolean checkImageSize(final Integer width,final Integer height,final MultipartFile file) {
        BufferedImage image = null;
        try {
            image = ImageIO.read(file.getInputStream());
            int iwidth = image.getWidth();
            int iheight = image.getHeight();
            // 弹窗的类型仅仅校验图片的最大尺寸
            return width == width && height == iheight;
        } catch (IOException e) {
            return false;
        }
    }

}
