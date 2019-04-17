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

package com.voxlearning.utopia.admin.controller.opmanager;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.admin.controller.AbstractAdminSystemController;
import com.voxlearning.utopia.admin.service.site.SiteService;
import com.voxlearning.utopia.admin.util.CrmImageUploader;
import com.voxlearning.utopia.api.constant.SupportedFileType;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.business.consumer.MiscLoaderClient;
import com.voxlearning.utopia.service.business.consumer.MiscServiceClient;
import lombok.Cleanup;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.annotation.Resource;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 * @author Longlong Yu
 * @since 下午6:05,13-11-22.
 */
public abstract class OpManagerAbstractController extends AbstractAdminSystemController {

    @Inject protected CrmImageUploader crmImageUploader;

    /**
     * service
     */
    @Resource protected MiscServiceClient miscServiceClient;
    @Resource protected MiscLoaderClient miscLoaderClient;
    @Resource protected SiteService siteService;

    protected String getAdminBaseUrl() {
        switch (RuntimeMode.current()) {
            case PRODUCTION:
                return "http://admin.17zuoye.net";
            case STAGING:
                return "http://admin.staging.17zuoye.net";
            case TEST:
                return "http://admin.test.17zuoye.net";
            default:
                return "http://admin.test.17zuoye.net";
        }
    }

    /**
     * 统一记录操作的日志的格式，便于之后查询
     */
    void saveOperationLog(String operation, Long targetId, String targetStr, String comment) {
        if (RuntimeMode.isDevelopment()) {
            // FIXME 暂时不想让开发调试污染日志
            return;
        }
        addAdminLog(operation, targetId, targetStr, comment, null);
    }

    // 获取prePath
    protected String getPrePath() {
        return getMainHostBaseUrl() + "/gridfs/";
    }

    protected String uploadFile(MultipartFile file) {
        try {
            if (!file.isEmpty()) {
                String prefix = "vendorimg-" + DateUtils.dateToString(new Date(), "yyyyMMdd");
                String originalFileName = file.getOriginalFilename();
                @Cleanup InputStream inStream = file.getInputStream();
                return crmImageUploader.upload(prefix, originalFileName, inStream);
            }
        } catch (IOException ex) {
            logger.error("上传文件失败： " + ex.getMessage());
        }
        return null;
    }

    protected XSSFWorkbook getRequestWorkbook() {
        if (!(getRequest() instanceof MultipartHttpServletRequest)) {
            logger.error("getRequestWorkbook - Not MultipartHttpServletRequest");
            return null;
        }
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) getRequest();
        try {
            MultipartFile file = multipartRequest.getFile("import");
            if (file == null || file.isEmpty()) {
                logger.error("getRequestWorkbook - Empty MultipartFile with name['import']");
                return null;
            }
            String fileName = file.getOriginalFilename();
            String fileExt = StringUtils.substringAfterLast(fileName, ".");
            fileExt = StringUtils.defaultString(fileExt).trim().toLowerCase();
            SupportedFileType fileType = SupportedFileType.valueOf(fileExt);
            if (SupportedFileType.xls != fileType && SupportedFileType.xlsx != fileType) {
                logger.error("getRequestWorkbook - Not a SupportedFileType with fileName = {}", fileName);
                return null;
            }
            @Cleanup InputStream in = file.getInputStream();
            return new XSSFWorkbook(in);
        } catch (Exception e) {
            logger.error("getRequestWorkbook - Excp : {}", e);
            return null;
        }
    }

}
