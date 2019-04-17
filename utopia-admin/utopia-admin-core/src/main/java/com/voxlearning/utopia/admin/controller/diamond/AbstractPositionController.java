package com.voxlearning.utopia.admin.controller.diamond;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.admin.controller.AbstractAdminSystemController;
import com.voxlearning.utopia.admin.util.AdminOssManageUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.File;

/**
 * @author feng.guo
 * @since 2019-03-30
 */
public abstract class AbstractPositionController extends AbstractAdminSystemController {

    private static final String FILE_ROOT_PATH = "wonderland";

    /**
     * 校验请求参数是否为空
     */
    protected void validateParamNotNull(String paramKey, String paramName) {
        String paramValue = getRequest().getParameter(paramKey);
        if (StringUtils.isEmpty(paramValue)) {
            throw new IllegalArgumentException(paramName + "不能为空");
        }
    }

    /**
     * 默认读取fileKey的图片，若没有图片则读取fileKey+"Url"的参数
     */
    public String uploadImage(String fileKey) {
        return uploadFile(fileKey);
    }

    /**
     * 默认读取fileKey的图片，若没有图片则读取fileKey+"Url"的参数
     */
    public String uploadFile(String fileKey) {

        if ((getRequest() instanceof MultipartHttpServletRequest)) {
            try {
                MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) getRequest();
                MultipartFile inputFile = multipartRequest.getFile(fileKey);
                return uploadFile(inputFile);
            } catch (Exception ignored) {
                logger.error("upload img error");
            }
        }
        return getRequestString(fileKey + "Url");
    }

    /**
     * 默认读取fileKey的图片，若没有图片则读取fileKey+"Url"的参数
     */
    public String uploadFile(File inputFile) {

        try {
            if (inputFile != null) {
                return AdminOssManageUtils.upload(inputFile, FILE_ROOT_PATH);
            }
        } catch (Exception ignored) {
            logger.error("upload img error");
        }
        return null;
    }

    /**
     * 默认读取fileKey的图片，若没有图片则读取fileKey+"Url"的参数
     */
    public String uploadFile(MultipartFile inputFile) {

        try {
            if (inputFile != null && !inputFile.isEmpty() && inputFile.getSize() != 0) {
                return AdminOssManageUtils.upload(inputFile, FILE_ROOT_PATH);
            }
        } catch (Exception ignored) {
            logger.error("upload img error");
        }
        return null;
    }
}
