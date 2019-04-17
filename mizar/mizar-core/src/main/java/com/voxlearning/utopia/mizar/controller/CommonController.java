package com.voxlearning.utopia.mizar.controller;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.mizar.utils.MizarOssManageUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

/**
 * Mizar welcome controller
 * Created by Alex on 16/8/13.
 */
@Controller
@RequestMapping("/common")
public class CommonController extends AbstractMizarController {

    @RequestMapping(value = "uploadphoto.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage uploadPhoto() {
        try {
            // 上传文件
            MapMessage fileMsg = $uploadFile("file", MAXIMUM_UPLOAD_PHOTO_SIZE);
            if (!fileMsg.isSuccess()) {
                return fileMsg;
            }
            String imgUrl = SafeConverter.toString(fileMsg.get("fileName"));
            if (StringUtils.isBlank(imgUrl)) {
                return MapMessage.errorMessage("图片上传失败");
            }
            if (MizarOssManageUtils.invalidFile.equals(imgUrl)) {
                return MapMessage.errorMessage("无效的文件类型！");
            }
            return MapMessage.successMessage().add("imgUrl", imgUrl);
        } catch (Exception ex) {
            logger.error("Upload Mizar photo failed.", ex);
            return MapMessage.errorMessage("图片上传失败");
        }
    }

    @RequestMapping(value = "uploadfile.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage uploadFile() {
        try {
            // 上传文件
            MapMessage fileMsg = $uploadFile("file", MAXIMUM_UPLOAD_FILE_SIZE);
            if (!fileMsg.isSuccess()) {
                return fileMsg;
            }
            String fileName = SafeConverter.toString(fileMsg.get("fileName"));
            if (StringUtils.isBlank(fileName)) {
                return MapMessage.errorMessage("文件上传失败");
            }
            if (MizarOssManageUtils.invalidFile.equals(fileName)) {
                return MapMessage.errorMessage("无效的文件类型！");
            }
            return MapMessage.successMessage().add("fileName", fileName);
        } catch (Exception ex) {
            logger.error("Upload Mizar file failed.", ex);
            return MapMessage.errorMessage("文件上传失败");
        }
    }

    @RequestMapping(value = "ueditorcontroller.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage ueditorController() {
        String action = getRequestString("action");
        switch (action) {
            case "config":
                return MapMessage.successMessage()
                        .add("imageActionName", "uploadimage")
                        .add("imageFieldName", "upfile")
                        .add("imageInsertAlign", "none")
                        .add("imageMaxSize", 2048000)
                        .add("imageUrlPrefix", "");
            case "uploadimage":
                MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) getRequest();
                MultipartFile imgFile = multipartRequest.getFile("upfile");
                if (imgFile.isEmpty()) {
                    return MapMessage.errorMessage("没有文件上传");
                }
                String originalFileName = imgFile.getOriginalFilename();
                try {
                    String url = $uploadFile("upfile");
                    return MapMessage.successMessage()
                            .add("url", url)
                            .add("title", imgFile.getName())
                            .add("state", "SUCCESS")
                            .add("original", originalFileName);
                } catch (Exception ex) {
                    logger.error("上传图片异常： " + ex.getMessage(), ex);
                    return MapMessage.errorMessage("上传图片异常： " + ex.getMessage());
                }
            default:
                return MapMessage.successMessage();
        }
    }
}
