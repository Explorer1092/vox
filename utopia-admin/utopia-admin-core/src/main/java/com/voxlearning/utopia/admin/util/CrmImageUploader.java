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

package com.voxlearning.utopia.admin.util;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.spi.storage.StorageClient;
import com.voxlearning.alps.spi.storage.StorageClientLocation;
import com.voxlearning.alps.spi.storage.StorageMetadata;
import com.voxlearning.alps.spi.storage.StorageSystem;
import com.voxlearning.utopia.api.constant.SupportedFileType;
import lombok.Cleanup;
import org.apache.commons.io.IOUtils;

import javax.inject.Named;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * CRM上传图片
 * Created by Shuai Huan on 2014/9/12.
 * 请用新的上传类  AdminOssManageUtils.java
 */
@Named
@Deprecated
public class CrmImageUploader extends SpringContainerSupport {

    @StorageClientLocation(system = StorageSystem.GFS, storage = "fs-default")
    private StorageClient fsDefault;

    //用GridFs存储
    public String upload(String prefix, String filename, InputStream inStream) {
        String ext = StringUtils.substringAfterLast(filename, ".");
        ext = StringUtils.defaultString(ext).trim().toLowerCase();

        SupportedFileType fileType;
        try {
            fileType = SupportedFileType.valueOf(ext);
        } catch (Exception ex) {
            logger.warn("Unsupported file type: {}", ext);
            throw new RuntimeException("不支持此格式文件");
        }

        try {
            byte[] content = IOUtils.toByteArray(inStream);
            String fileId = RandomUtils.nextObjectId();
            String fileName = prefix + "-" + fileId + "." + ext;
            String contentType = fileType.getContentType();

            @Cleanup ByteArrayInputStream bais = new ByteArrayInputStream(content);
            StorageMetadata metadata = new StorageMetadata();
            metadata.setContentType(contentType);
            fsDefault.uploadWithId(bais, fileId, fileName, null, metadata);
            return fileName;
        } catch (Exception ex) {
            logger.warn("Upload product image: failed writing into mongo gfs", ex.getMessage());
            throw new RuntimeException("上传文件失败");
        }
    }

    public void deletePhotoByFilename(String filename) {
        fsDefault.deleteByName(filename, null);
    }
}
