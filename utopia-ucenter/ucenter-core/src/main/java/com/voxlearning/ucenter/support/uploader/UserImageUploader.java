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

package com.voxlearning.ucenter.support.uploader;

import com.voxlearning.alps.core.util.ArrayUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.FileUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.storage.StorageClient;
import com.voxlearning.alps.spi.storage.StorageClientLocation;
import com.voxlearning.alps.spi.storage.StorageMetadata;
import com.voxlearning.alps.spi.storage.StorageSystem;
import lombok.Cleanup;

import javax.inject.Named;
import java.io.ByteArrayInputStream;

@Named
public class UserImageUploader extends SpringContainerSupport {

    private static final String IMAGE_CONTENT_TYPE = "image/jpeg";

    @StorageClientLocation(storage = "user")
    private StorageClient ossStorageClient;

    @StorageClientLocation(system = StorageSystem.GFS, storage = "fs-default")
    private StorageClient gfsStorageClient;

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
            String ossId = ossStorageClient.upload(inStream, filename, "gridfs", storageMetadata);

            if (ossId == null) {
                storageMetadata = new StorageMetadata();
                storageMetadata.setContentType(IMAGE_CONTENT_TYPE);
                gfsStorageClient.uploadWithId(inStream, gfsId, filename, null, storageMetadata);
            }
            return filename;
        } catch (Exception ex) {
            logger.warn("Upload image: failed writing into mongo gfs", ex);
            return null;
        }
    }


}
