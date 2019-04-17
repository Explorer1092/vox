package com.voxlearning.utopia.mizar.controller.bookstore;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.mizar.controller.AbstractMizarController;
import com.voxlearning.utopia.mizar.utils.MizarOssManageUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

@Controller
@RequestMapping("/bookstore/manager/tool")
public class BookStoreToolKitController extends AbstractMizarController {

    // 上传图片至阿里云
    @RequestMapping(value = "uploadphoto.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage uploadPhoto() {
        Integer width = getRequestInt("width", 0);
        Integer height = getRequestInt("height", 0);

        String path = getRequestString("path");
        if (StringUtils.isEmpty(path)) return MapMessage.errorMessage("请填写上传路径");
        if (!(getRequest() instanceof MultipartHttpServletRequest)) return MapMessage.errorMessage("上传失败");

        try {
            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) getRequest();
            MultipartFile inputFile = multipartRequest.getFile("file");

            MapMessage mapMessage = validateImg(inputFile, width, height);
            if (!mapMessage.isSuccess()) {
                return mapMessage;
            }

            if (inputFile != null && !inputFile.isEmpty()) {
                String fileName = MizarOssManageUtils.upload(inputFile, path);
                return MapMessage.successMessage(fileName);
            }
        } catch (Exception ignored) {
        }
        return MapMessage.errorMessage("上传失败");
    }


    private MapMessage validateImg(MultipartFile file, int width, int height) {
        if (width == 0 || height == 0) {
            return MapMessage.successMessage();
        }
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            int h = image.getHeight();
            int w = image.getWidth();
            if (w != width || h != height) {
                return MapMessage.errorMessage("图片大小不匹配，请重新上传大小为" + width + "*" + height + "的图片！");
            }
            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error("Failed validate Img, ex={}", ex);
            return MapMessage.errorMessage("图片校验异常！");
        }
    }
}
