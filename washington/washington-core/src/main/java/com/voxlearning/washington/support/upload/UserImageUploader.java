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

import com.voxlearning.alps.config.manager.ConfigManager;
import com.voxlearning.alps.core.util.ArrayUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.FileUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.RandomStringUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.time.FastDateFormat;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.storage.StorageClient;
import com.voxlearning.alps.spi.storage.StorageClientLocation;
import com.voxlearning.alps.spi.storage.StorageMetadata;
import com.voxlearning.alps.spi.storage.StorageSystem;
import lombok.Cleanup;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Named;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Date;

@Named
public class UserImageUploader extends SpringContainerSupport {

    private static final String IMAGE_CONTENT_TYPE = "image/jpeg";

    @StorageClientLocation(storage = "user")
    private StorageClient storageClient;

    @StorageClientLocation(system = StorageSystem.GFS, storage = "fs-default")
    private StorageClient fsDefault;

    /**
     * Parse jpeg image from passed in filedata, and store it into mongo gridfs.
     * The new created gfs 'id' will be returned. Return null in case of any
     * error occurs.
     */
    public String uploadImageFromFiledata(Long userId, String gfsId, String filedata) {
        if (userId == null || StringUtils.isBlank(filedata)) {
            logger.warn("Upload image: user id is null or filedata is blank");
            return null;
        }
        byte[] image = FileUtils.parseByteArrayFromFiledata(filedata);
        if (ArrayUtils.isEmpty(image)) {
            logger.warn("Upload image: no content");
            return null;
        }

        String filename = "avatar-" + userId + "-" + gfsId + ".jpg";

        try {
            @Cleanup ByteArrayInputStream inStream = new ByteArrayInputStream(image);

            // try to upload to oss first
            StorageMetadata storageMetadata = new StorageMetadata();
            storageMetadata.setContentLength((long) image.length);
            String ossId = storageClient.upload(inStream, filename, "gridfs", storageMetadata);

            if (ossId == null) {
                storageMetadata = new StorageMetadata();
                storageMetadata.setContentType(IMAGE_CONTENT_TYPE);
                fsDefault.uploadWithId(inStream, gfsId, filename, null, storageMetadata);
            }
            return filename;
        } catch (Exception ex) {
            logger.warn("Upload image: failed writing into mongo gfs", ex);
            return null;
        }
    }

    public String uploadAvatarFromMultipartFile(Long userId, String gfsId,MultipartFile inputFile) throws IOException {
        if (userId == null || inputFile == null) {
            return null;
        }

        try {
            String filename = "avatar-" + userId + "-" + gfsId + ".jpg";

            StorageMetadata storageMetadata = new StorageMetadata();
            storageMetadata.setContentLength(inputFile.getSize());
            String ossId = storageClient.upload(inputFile.getInputStream(), filename, "gridfs", storageMetadata);
            if (ossId == null) {
                storageMetadata = new StorageMetadata();
                storageMetadata.setContentType(IMAGE_CONTENT_TYPE);
                fsDefault.uploadWithId(inputFile.getInputStream(), gfsId, filename, null, storageMetadata);
            }

            return filename;
        } catch (Exception e) {
            logger.warn("Upload image: failed upload user avatar data, user id {}", userId, e);
            return null;
        }
    }

    public String uploadAvatarFromMultipartFile(Long userId, MultipartFile inputFile) throws IOException {
        if (userId == null || inputFile == null) {
            return null;
        }

        try {
            String gfsId = RandomUtils.nextObjectId();
            String filename = "avatar-" + userId + "-" + gfsId + ".jpg";

            StorageMetadata storageMetadata = new StorageMetadata();
            storageMetadata.setContentLength(inputFile.getSize());
            String ossId = storageClient.upload(inputFile.getInputStream(), filename, "gridfs", storageMetadata);
            if (ossId == null) {
                storageMetadata = new StorageMetadata();
                storageMetadata.setContentType(IMAGE_CONTENT_TYPE);
                fsDefault.uploadWithId(inputFile.getInputStream(), gfsId, filename, null, storageMetadata);
            }

            return filename;
        } catch (Exception e) {
            logger.warn("Upload image: failed upload user avatar data, user id {}", userId, e);
            return null;
        }
    }

    public String uploadAvatar(Long userId, String gfsId, byte [] bytes) throws IOException {
        if (userId == null || bytes == null) {
            return null;
        }

        try {
            String filename = "avatar-" + userId + "-" + gfsId + ".jpg";

            StorageMetadata storageMetadata = new StorageMetadata();
            storageMetadata.setContentLength((long) bytes.length);
            String ossId = storageClient.upload(new ByteArrayInputStream(bytes), filename, "gridfs", storageMetadata);
            if (ossId == null) {
                storageMetadata = new StorageMetadata();
                storageMetadata.setContentType(IMAGE_CONTENT_TYPE);
                fsDefault.uploadWithId(new ByteArrayInputStream(bytes), gfsId, filename, null, storageMetadata);
            }

            return filename;
        } catch (Exception e) {
            logger.warn("Upload image: failed upload user avatar data, user id {}", userId, e);
            return null;
        }
    }

}
