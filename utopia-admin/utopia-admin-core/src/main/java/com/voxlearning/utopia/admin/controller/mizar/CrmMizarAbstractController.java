/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.admin.controller.mizar;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.admin.controller.AbstractAdminSystemController;
import com.voxlearning.utopia.admin.util.AdminOssManageUtils;
import com.voxlearning.utopia.service.mizar.consumer.loader.MizarLoaderClient;
import com.voxlearning.utopia.service.mizar.consumer.loader.MizarUserLoaderClient;
import com.voxlearning.utopia.service.mizar.consumer.service.MizarServiceClient;
import com.voxlearning.utopia.service.mizar.consumer.service.MizarUserServiceClient;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Yuechen.Wang
 * @since 2016-08-22
 */
public abstract class CrmMizarAbstractController extends AbstractAdminSystemController {

    @Inject protected MizarLoaderClient mizarLoaderClient;
    @Inject protected MizarUserLoaderClient mizarUserLoaderClient;

    @Inject protected MizarServiceClient mizarServiceClient;
    @Inject protected MizarUserServiceClient mizarUserServiceClient;

    protected String uploadPhoto(String file) {
        if (!(getRequest() instanceof MultipartHttpServletRequest)) {
            return null;
        }
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) getRequest();
        try {
            MultipartFile inputFile = multipartRequest.getFile(file);
            if (inputFile != null && !inputFile.isEmpty()) {
                return AdminOssManageUtils.upload(inputFile, "mizar");
            }
        } catch (Exception ex) {
            logger.error("上传失败,msg:{}", ex.getMessage(), ex);
        }
        return null;
    }

    // FIXME 删除暂时不太好使，学松老师表示9月4/5日左右有重构，到时候再处理
    protected void deletePhoto(String fileName) {
//        try {
//            if (StringUtils.isNotBlank(fileName)) {
//                AdminOssManageUtils.delete(fileName);
//            }
//        } catch (Exception ex) {
//            logger.error("删除失败,msg:{}", ex.getMessage(), ex);
//        }
    }

    protected List<String> splitString(String value) {
        if (StringUtils.isBlank(value)) {
            return Collections.emptyList();
        }
        return Arrays.stream(value.split(",")).filter(StringUtils::isNotBlank).collect(Collectors.toList());
    }
}
