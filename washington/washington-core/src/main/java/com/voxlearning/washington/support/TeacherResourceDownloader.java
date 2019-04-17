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

package com.voxlearning.washington.support;

import com.mongodb.MongoGridFSException;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.mongo.gridfs.GridFSBucket;
import com.voxlearning.alps.dao.mongo.gridfs.GridFSBucketNamespace;
import com.voxlearning.alps.dao.mongo.gridfs.GridFSUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.storage.gridfs.factory.GridFSBucketFactory;
import com.voxlearning.utopia.data.DownloadContent;
import com.voxlearning.utopia.service.business.api.entity.GridFileInfo;
import com.voxlearning.utopia.storage.api.client.StorageLoaderClient;
import lombok.Cleanup;
import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.ByteArrayOutputStream;

@Named
public class TeacherResourceDownloader extends SpringContainerSupport {

    @Inject private StorageLoaderClient storageLoaderClient;

    /**
     * Download teacher resource of specified id.
     * Return null in case of no resource found or any error occurs.
     */
    public DownloadContent downloadTeacherResource(String resourceId) {
        if (!ObjectId.isValid(resourceId)) {
            return null;
        }

        // FIXME: =============================================================
        // FIXME: Use StorageClient instead
        // FIXME: =============================================================
        GridFSBucketNamespace namespace = new GridFSBucketNamespace("GFSDatabase");
        GridFSBucket bucket = GridFSBucketFactory.getInstance().newGridFSBucket("mongo-gfs", namespace);

        GridFSDownloadStream downloadStream;
        try {
            downloadStream = bucket.openDownloadStream(new ObjectId(resourceId));
        } catch (MongoGridFSException ex) {
            downloadStream = null;
        }
        if (downloadStream == null) {
            logger.warn("Teacher resource file '{}' not found", resourceId);
            return null;
        }

        try {
            GridFSFile fsFile = downloadStream.getGridFSFile();
            DownloadContent downloadContent = new DownloadContent();
            downloadContent.setContentType(GridFSUtils.getContentType(fsFile, "application/octet-stream"));

            GridFileInfo info = storageLoaderClient.getStorageLoader()
                    .loadGridFileInfoByGfsId(resourceId)
                    .getUninterruptibly();
            if (info == null) {
                downloadContent.setFilename(resourceId);
            } else {
                downloadContent.setFilename(StringUtils.defaultString(info.getFileName(), resourceId));
            }

            byte[] content = IOUtils.toByteArray(downloadStream);
            @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            outStream.write(content);
            outStream.flush();
            downloadContent.setContent(outStream.toByteArray());
            return downloadContent;
        } catch (Exception ex) {
            logger.warn("Error occurs when downloading teacher resource '{}'", resourceId, ex);
            return null;
        } finally {
            IOUtils.closeQuietly(downloadStream);
        }
    }
}
