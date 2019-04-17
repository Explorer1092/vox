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
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.storage.gridfs.factory.GridFSBucketFactory;
import lombok.Cleanup;
import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Named;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 * @author RuiBao
 * @version 0.1
 * @since 7/7/2015
 */
@Named
public class MissionPictureUploader extends SpringContainerSupport {

    public String upload(Long missionId, MultipartFile filedata) {
        if (missionId == null || filedata.isEmpty()) return null;

        String ext = StringUtils.substringAfterLast(filedata.getOriginalFilename(), ".");
        ext = StringUtils.isBlank(ext) ? "jpg" : StringUtils.defaultString(ext).trim().toLowerCase();

        String gfsId = RandomUtils.nextObjectId();
        String filename = "sprmp-" + DateUtils.dateToString(new Date(), "yyyyMMdd") + "-" + missionId + "-" + gfsId + "." + ext;
        try {
            @Cleanup InputStream inStream = filedata.getInputStream();
            byte[] content = IOUtils.toByteArray(inStream);
            @Cleanup ByteArrayInputStream bais = new ByteArrayInputStream(content);

            // FIXME: =========================================================
            // FIXME: Use StorageClient instead
            // FIXME: =========================================================
            GridFSBucketNamespace namespace = new GridFSBucketNamespace("GFSDatabase");
            GridFSBucket bucket = GridFSBucketFactory.getInstance().newGridFSBucket("mongo-gfs", namespace);

            bucket.uploadFromStream(new ObjectId(gfsId), filename, "image/jpeg", bais);
            return filename;
        } catch (Exception ex) {
            logger.warn("Upload spr mission picture: failed writing into mongo gfs", ex.getMessage());
            return null;
        }
    }

    public String upload(Long missionId, String filedata) throws IOException {
        if (missionId == null || StringUtils.isBlank(filedata)) return null;
        byte[] image = FileUtils.parseByteArrayFromFiledata(filedata);
        if (ArrayUtils.isEmpty(image)) return null;

        String gfsId = RandomUtils.nextObjectId();
        String filename = "sprmp-" + DateUtils.dateToString(new Date(), "yyyyMMdd") + "-" + missionId + "-" + gfsId + ".jpg";
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
            logger.warn("Upload spr mission picture: failed writing into mongo gfs", ex.getMessage());
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
