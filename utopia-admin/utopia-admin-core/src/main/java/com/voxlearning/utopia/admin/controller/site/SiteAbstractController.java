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

package com.voxlearning.utopia.admin.controller.site;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.utopia.admin.controller.AbstractAdminSystemController;
import com.voxlearning.utopia.admin.dao.ActivityRechargeTeacherPersistence;
import com.voxlearning.utopia.admin.persist.AdminDictPersistence;
import com.voxlearning.utopia.admin.service.site.SiteService;
import com.voxlearning.utopia.admin.util.CrmImageUploader;
import com.voxlearning.utopia.service.business.consumer.MiscLoaderClient;
import com.voxlearning.utopia.service.business.consumer.MiscServiceClient;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;
import lombok.Cleanup;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 * @author Longlong Yu
 * @since 下午6:05,13-11-22.
 */
public abstract class SiteAbstractController extends AbstractAdminSystemController {

    /**
     * persistence
     */
    @Resource protected AdminDictPersistence adminDictPersistence;
    @Resource protected ActivityRechargeTeacherPersistence activityRechargeTeacherPersistence;

    /**
     * service
     */
    @Resource protected SiteService siteService;
    @Resource protected MiscServiceClient miscServiceClient;
    @Resource protected MiscLoaderClient miscLoaderClient;
    @Resource protected AppMessageServiceClient appMessageServiceClient;

    @Inject protected CrmImageUploader crmImageUploader;

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

}
