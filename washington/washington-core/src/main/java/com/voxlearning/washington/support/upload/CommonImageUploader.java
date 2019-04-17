package com.voxlearning.washington.support.upload;

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

/**
 * Created by Summer Yang on 2016/7/21.
 */
@Named
public class CommonImageUploader extends SpringContainerSupport {

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
            String fileName = prefix + "-" + fileId + ".jpg";
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
            logger.warn("Upload image: failed writing into mongo gfs", ex.getMessage());
            throw new RuntimeException("上传文件失败");
        }
    }
}
