package com.voxlearning.utopia.admin.util;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.PutObjectResult;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.mts.model.v20140618.QuerySnapshotJobListResponse;
import com.aliyuncs.mts.model.v20140618.SubmitSnapshotJobResponse;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.admin.data.SnapshotQueryResult;
import com.voxlearning.utopia.admin.data.SnapshotSubmitResult;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;

/**
 * @author xinxin
 * @since 6/6/17.
 */
@RunWith(AlpsTestRunner.class)
public class TestAdminAcsManagerUtils {
    @Test
    public void testSubmitSnapshotJob() throws ClientException {
        String url = "https://v.17zuoye.cn/class/2017/03/16/20170316175318086503.mp4";
        SnapshotSubmitResult response = AdminAcsManagerUtils.submitSnapshotJob("class/2017/03/16/20170316175318086503.mp4");
        System.out.println(JsonUtils.toJson(response));
    }

    @Test
    public void testQuerySnapshotJob() throws ClientException {
        SnapshotQueryResult response = AdminAcsManagerUtils.querySnapshotJob("d81f266a02394365beb1ee133e9f93d0");
        System.out.println(JsonUtils.toJson(response));
    }

    @Test
    public void testCreateOssDirectory() {
        OSSClient ossClient = new OSSClient("oss-cn-beijing.aliyuncs.com", "LTAImevontKJPxh4", "sclbfzbZrriJt9e4n62ifQcFd3Guzr");
        PutObjectResult result = ossClient.putObject("17zy-content-video", "class/snapshot", new ByteArrayInputStream("snapshot test".getBytes()));
        System.out.println(JsonUtils.toJson(result));
        ossClient.shutdown();
    }
}
