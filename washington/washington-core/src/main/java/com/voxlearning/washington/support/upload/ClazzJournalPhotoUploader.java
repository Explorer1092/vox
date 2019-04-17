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

package com.voxlearning.washington.support.upload;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.mongo.gridfs.GridFSBucket;
import com.voxlearning.alps.dao.mongo.gridfs.GridFSBucketNamespace;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.storage.gridfs.factory.GridFSBucketFactory;
import com.voxlearning.utopia.api.constant.SupportedFileType;
import lombok.Cleanup;
import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;

import javax.inject.Named;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Date;

/**
 * Created by XiaoPeng.Yang on 14-6-12.
 */

@Named
public class ClazzJournalPhotoUploader extends SpringContainerSupport {

    public String uploadClazzJournalImageFromFileData(Long userId, String filename, InputStream inStream) {
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
            String fileName = "cjp-" + DateUtils.dateToString(new Date(), "yyyyMMdd") + "-" + userId + "-" + fileId + ".jpg";
            String contentType = fileType.getContentType();

            @Cleanup ByteArrayInputStream bais = new ByteArrayInputStream(content);

            // FIXME: =========================================================
            // FIXME: Use StorageClient instead
            // FIXME: =========================================================
            GridFSBucketNamespace namespace = new GridFSBucketNamespace("GFSDatabase");
            GridFSBucket bucket = GridFSBucketFactory.getInstance().newGridFSBucket("mongo-gfs", namespace);
            bucket.uploadFromStream(new ObjectId(fileId), fileName, contentType, bais);
            return fileName;
        } catch (Exception ex) {
            logger.warn("Upload clazz journal photo: failed writing into mongo gfs", ex.getMessage());
            throw new RuntimeException("上传文件失败");
        }
    }

    public void deletePhotoByFilename(String filename) {
        // FIXME: =============================================================
        // FIXME: Use StorageClient instead
        // FIXME: =============================================================
        GridFSBucketNamespace namespace = new GridFSBucketNamespace("GFSDatabase");
        GridFSBucket bucket = GridFSBucketFactory.getInstance().newGridFSBucket("mongo-gfs", namespace);
        bucket.safeDeleteByFilename(filename);
    }
}
