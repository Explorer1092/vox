package com.voxlearning.utopia.agent.mockexam.integration.support;

import com.voxlearning.alps.dao.mongo.gridfs.GridFSBucket;
import com.voxlearning.alps.dao.mongo.gridfs.GridFSBucketNamespace;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.storage.gridfs.factory.GridFSBucketFactory;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.agent.mockexam.integration.FileClient;
import lombok.Cleanup;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.io.InputStream;

/**
 * 文件上传实现类
 *
 * @Author: peng.zhang
 * @Date: 2018/8/15 15:52
 */
@Service
public class FileClientImpl implements FileClient {

    @Override
    public MapMessage upload(FileClient.UploadRequest request) {
        try {
            @Cleanup InputStream inStream = request.getInputFile().getInputStream();
            GridFSBucketNamespace namespace = new GridFSBucketNamespace("GFSDatabase");
            GridFSBucket bucket = GridFSBucketFactory.getInstance().newGridFSBucket("mongo-gfs", namespace);
            bucket.uploadFromStream(new ObjectId(request.getFileId()), request.getFileName(), request.getContentType(), inStream);
            String prePath = RuntimeMode.isUsingProductionData() ? "http://cdn-portrait.17zuoye.cn" : "http://cdn-portrait.test.17zuoye.net";
            return MapMessage.successMessage("上传成功").add("fileUrl", prePath + "/gridfs/" + request.getFileName());
        } catch (Exception e){
            return MapMessage.errorMessage().setInfo("上传失败");
        }
    }

}
