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
import com.voxlearning.alps.core.util.ArrayUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.mongo.gridfs.GridFSBucket;
import com.voxlearning.alps.dao.mongo.gridfs.GridFSBucketNamespace;
import com.voxlearning.alps.lang.util.FileUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.storage.gridfs.factory.GridFSBucketFactory;
import lombok.Cleanup;
import org.bson.types.ObjectId;

import javax.inject.Named;
import java.io.ByteArrayInputStream;
import java.util.Date;

/**
 * @author RuiBao
 * @version 0.1
 * @since 4/24/2015
 */
@Named
public class MothersDayCardPhotoUploader extends SpringContainerSupport {

    public String upload(Long userId, String gfsId, String filedata) {
        if (userId == null || StringUtils.isBlank(filedata)) {
            logger.warn("Mdc upload image: user id is null or filedata is blank");
            return null;
        }
        byte[] image = FileUtils.parseByteArrayFromFiledata(filedata);
        if (ArrayUtils.isEmpty(image)) {
            logger.warn("Mdc upload image: no content");
            return null;
        }

        String filename = "mdcp-" + DateUtils.dateToString(new Date(), "yyyyMMdd") + "-" + userId + "-" + gfsId + ".jpg";

        try {
            @Cleanup ByteArrayInputStream inStream = new ByteArrayInputStream(image);
            // FIXME: =========================================================
            // FIXME: Use StorageClient instead
            // FIXME: =========================================================
            GridFSBucketNamespace namespace = new GridFSBucketNamespace("GFSDatabase");
            GridFSBucket bucket = GridFSBucketFactory.getInstance().newGridFSBucket("mongo-gfs", namespace);
            bucket.uploadFromStream(new ObjectId(gfsId), filename, "image/jpeg", inStream);
            return filename;
        } catch (Exception ex) {
            logger.warn("Upload monthers day card photo: failed writing into mongo gfs", ex.getMessage());
            return null;
        }
    }

    public void delete(String filename) {
        // FIXME: =============================================================
        // FIXME: Use StorageClient instead
        // FIXME: =============================================================
        GridFSBucketNamespace namespace = new GridFSBucketNamespace("GFSDatabase");
        GridFSBucket bucket = GridFSBucketFactory.getInstance().newGridFSBucket("mongo-gfs", namespace);
        bucket.safeDeleteByFilename(filename);
    }
}
