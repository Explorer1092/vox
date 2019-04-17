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

import com.voxlearning.alps.core.util.ArrayUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.mongo.gridfs.GridFSBucket;
import com.voxlearning.alps.dao.mongo.gridfs.GridFSBucketNamespace;
import com.voxlearning.alps.lang.util.FileUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.storage.gridfs.factory.GridFSBucketFactory;
import com.voxlearning.utopia.api.constant.GridFileStatus;
import com.voxlearning.utopia.api.constant.GridFileType;
import com.voxlearning.utopia.api.constant.ReadingUgcSupportedFileType;
import com.voxlearning.utopia.api.constant.SupportedFileType;
import com.voxlearning.utopia.core.config.CommonConfiguration;
import com.voxlearning.utopia.service.business.api.entity.GridFileInfo;
import com.voxlearning.utopia.storage.api.client.StorageServiceClient;
import lombok.Cleanup;
import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Named
public class TeacherResourceUploader extends SpringContainerSupport {

    @Inject private StorageServiceClient storageServiceClient;

    private static final String readingDbName = "vox-reading-ugc";
    protected CommonConfiguration commonConfiguration = CommonConfiguration.getInstance();

    public String uploadTeacherResource(Long userId, String filename, GridFileType type, InputStream inStream) {
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
            String fileName = "teacher-res-" + fileId + "." + ext;
            String contentType = fileType.getContentType();

            @Cleanup ByteArrayInputStream bais = new ByteArrayInputStream(content);

            // FIXME: =========================================================
            // FIXME: Use StorageClient instead
            // FIXME: =========================================================
            GridFSBucketNamespace namespace = new GridFSBucketNamespace("GFSDatabase");
            GridFSBucket bucket = GridFSBucketFactory.getInstance().newGridFSBucket("mongo-gfs", namespace);

            bucket.uploadFromStream(new ObjectId(fileId), fileName, contentType, bais);

            GridFileInfo fileInfo = new GridFileInfo();
            fileInfo.setGfsId(fileId);
            fileInfo.setFileName(filename);
            fileInfo.setUserId(userId);
            fileInfo.setFileType(type);
            fileInfo.setFileStatus(GridFileStatus.未审核);

            storageServiceClient.getStorageService().insertGridFileInfo(fileInfo).awaitUninterruptibly();

            return fileId;
        } catch (Exception ex) {
            logger.warn("Failed uploading teacher resource: {}", ex.getMessage());
            throw new RuntimeException("上传文件失败");
        }
    }

    public String uploadTeacherReadingUgc(String filename, InputStream inStream) {
        String ext = StringUtils.substringAfterLast(filename, ".");
        ext = StringUtils.defaultString(ext).trim().toLowerCase();

        ReadingUgcSupportedFileType fileType;
        try {
            fileType = ReadingUgcSupportedFileType.valueOf(ext);
            if (fileType.equals(ReadingUgcSupportedFileType.mp3)) {
                ext = "sy3";
            }
        } catch (Exception ex) {
            logger.warn("Unsupported file type: {}", ext);
            throw new RuntimeException("不支持此格式文件");
        }

        try {
            byte[] content = IOUtils.toByteArray(inStream);
            String fileId = RandomUtils.nextObjectId();
            String fileName = "teacher-reading-" + fileId + "." + ext;
            String contentType = fileType.getContentType();

            @Cleanup ByteArrayInputStream bais = new ByteArrayInputStream(content);
            // FIXME: =========================================================
            // FIXME: Use StorageClient instead
            // FIXME: =========================================================
            GridFSBucketNamespace namespace = new GridFSBucketNamespace(readingDbName);
            GridFSBucket bucket = GridFSBucketFactory.getInstance().newGridFSBucket("mongo-gfs", namespace);
            bucket.uploadFromStream(new ObjectId(fileId), fileName, contentType, bais);

            return "fs-reading-ugc/" + fileName;
        } catch (Exception ex) {
            logger.warn("Failed uploading teacher reading UGC: {}", ex.getMessage());
            throw new RuntimeException("上传文件失败");
        }
    }

    public String uploadReadingCoverFromFiledata(String filedata) {
        if (StringUtils.isBlank(filedata)) {
            logger.warn("Upload readingCover: user id is null or filedata is blank");
            return null;
        }
        byte[] image = FileUtils.parseByteArrayFromFiledata(filedata);
        if (ArrayUtils.isEmpty(image)) {
            logger.warn("Upload readingCover: no content");
            return null;
        }

        String fileId = RandomUtils.nextObjectId();
        String fileName = "teacher-reading-" + fileId + ".jpg";

        try {
            @Cleanup ByteArrayInputStream inStream = new ByteArrayInputStream(image);
            // FIXME: =========================================================
            // FIXME: Use StorageClient instead
            // FIXME: =========================================================
            GridFSBucketNamespace namespace = new GridFSBucketNamespace(readingDbName);
            GridFSBucket bucket = GridFSBucketFactory.getInstance().newGridFSBucket("mongo-gfs", namespace);
            bucket.uploadFromStream(new ObjectId(fileId), fileName, "image/jpeg", inStream);
            return "fs-reading-ugc/" + fileName;
        } catch (Exception ex) {
            logger.warn("Upload readingCover: failed writing into mongo gfs", ex);
            return null;
        }
    }

}
