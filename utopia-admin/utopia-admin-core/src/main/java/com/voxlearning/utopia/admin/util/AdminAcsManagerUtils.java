package com.voxlearning.utopia.admin.util;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.mts.model.v20140618.QuerySnapshotJobListRequest;
import com.aliyuncs.mts.model.v20140618.QuerySnapshotJobListResponse;
import com.aliyuncs.mts.model.v20140618.SubmitSnapshotJobRequest;
import com.aliyuncs.mts.model.v20140618.SubmitSnapshotJobResponse;
import com.aliyuncs.profile.DefaultProfile;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.admin.data.SnapshotQueryResult;
import com.voxlearning.utopia.admin.data.SnapshotSubmitResult;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xinxin
 * @since 6/5/17.
 * 阿里云视频生成截图工具类
 */
@Slf4j
public class AdminAcsManagerUtils {
    private static final String accessKeyId = "LTAImevontKJPxh4";
    private static final String accessSecret = "sclbfzbZrriJt9e4n62ifQcFd3Guzr";
    private static final String regionId = "cn-beijing";
    private static final String endpointName = "cn-beijing";
    private static final String product = "Mts";
    private static final String domain = "mts.cn-beijing.aliyuncs.com";
    private static final String location = "oss-cn-beijing";
    private static final String bucket = "17zy-content-video";
    private static final String pipelineId = "8b3553cf7a8246de9dc03077a49a96ee";
    private static final int time = 10000;   //开始截图的毫秒数
    private static final int num = 6;    //截图数量
    private static final int interval = 10;    //截图间隔秒数

    private static final String FIELD_BUCKET = "Bucket";
    private static final String FIELD_LOCATION = "Location";
    private static final String FIELD_OBJECT = "Object";
    private static final String FIELD_OUTPUT_FILE = "OutputFile";
    private static final String FIELD_TIME = "Time";
    private static final String FIELD_NUM = "Num";
    private static final String FIELD_INTERVAL = "Interval";

    private static final DefaultProfile defaultProfile;
    private static final DefaultAcsClient defaultAcsClient;

    static {
        try {
            DefaultProfile.addEndpoint(endpointName, regionId, product, domain);
        } catch (ClientException ignore) {
        }
        defaultProfile = DefaultProfile.getProfile(regionId, accessKeyId, accessSecret);
        defaultAcsClient = new DefaultAcsClient(defaultProfile);
    }

    public static SnapshotSubmitResult submitSnapshotJob(String sourceFile) throws ClientException {
        SubmitSnapshotJobRequest request = new SubmitSnapshotJobRequest();
        request.setInput(JsonUtils.toJson(getInput(sourceFile)));
        request.setSnapshotConfig(JsonUtils.toJson(getSnapshotConfig(sourceFile)));
        request.setPipelineId(pipelineId);

        SubmitSnapshotJobResponse response = defaultAcsClient.getAcsResponse(request);
        if (log.isDebugEnabled()) {
            log.debug(JsonUtils.toJson(response));
        }

        SnapshotSubmitResult result = new SnapshotSubmitResult();
        result.setState(response.getSnapshotJob().getState());
        result.setSnapshots(getSnapshots(response.getSnapshotJob().getSnapshotConfig().getOutputFile().getObject()));

        return result;
    }


    public static SnapshotQueryResult querySnapshotJob(String jobId) throws ClientException {
        QuerySnapshotJobListRequest request = new QuerySnapshotJobListRequest();
        request.setSnapshotJobIds(jobId);

        QuerySnapshotJobListResponse response = defaultAcsClient.getAcsResponse(request);
        if (log.isDebugEnabled()) {
            log.debug(JsonUtils.toJson(response));
        }

        SnapshotQueryResult result = new SnapshotQueryResult();
        result.setState(response.getSnapshotJobList().get(0).getState());
        result.setSnapshots(getSnapshots(response.getSnapshotJobList().get(0).getSnapshotConfig().getOutputFile().getObject()));
        return result;
    }

    //生成截图文件路径
    private static List<String> getSnapshots(String template) {
        List<String> files = new ArrayList<>();
        for (int i = 1; i <= num; i++) {
            files.add(template.replace("{Count}", "0000" + i));
        }
        return files;
    }

    //生成input参数
    private static Map<String, String> getInput(String sourceFile) {
        Map<String, String> input = new HashMap<>();
        input.put(FIELD_BUCKET, bucket);
        input.put(FIELD_LOCATION, location);
        input.put(FIELD_OBJECT, sourceFile);
        return input;
    }

    //生成config参数
    private static Map<String, Object> getSnapshotConfig(String sourceFile) {
        Map<String, String> outputFile = new HashMap<>();
        outputFile.put(FIELD_BUCKET, bucket);
        outputFile.put(FIELD_LOCATION, location);
        outputFile.put(FIELD_OBJECT, getSnapshotFile(sourceFile));

        Map<String, Object> snapshotConfig = new HashMap<>();
        snapshotConfig.put(FIELD_OUTPUT_FILE, outputFile);
        snapshotConfig.put(FIELD_TIME, time);
        snapshotConfig.put(FIELD_NUM, num);
        snapshotConfig.put(FIELD_INTERVAL, interval);
        return snapshotConfig;
    }

    //生成截图路径配置
    private static String getSnapshotFile(String sourceFile) {
        String destFile = "snapshot/" + sourceFile;
        destFile = destFile.substring(0, destFile.lastIndexOf("."));
        destFile += "/{Count}.jpg";
        return destFile;
    }


}
