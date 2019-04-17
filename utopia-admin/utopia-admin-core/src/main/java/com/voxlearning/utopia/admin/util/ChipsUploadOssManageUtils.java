package com.voxlearning.utopia.admin.util;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.PolicyConditions;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.repackaged.org.apache.commons.collections4.map.HashedMap;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.RandomStringUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.time.FastDateFormat;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.storage.aliyunoss.module.config.AliyunOSSConfig;
import com.voxlearning.alps.storage.aliyunoss.module.config.AliyunossConfigManager;
import com.voxlearning.utopia.admin.constant.UploadFileType;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

public class ChipsUploadOssManageUtils {

    private static final Logger logger = LoggerFactory.getLogger(ChipsUploadOssManageUtils.class);

    private final static String DIR_PREFIX = "ai-teacher";
    private final static String OSS_ENDPOINT;
    private final static String OSS_ACCESS_ID;
    private final static String OSS_ACCESS_KEY;
    private final static String OSS_BUCKET;
    private final static String OSS_HOST;
    private final static String OSS_VIDEO_SNAPSHOT_HOST;

    private static String AGENT_BASE_URL;

    private static OSSClient client;

    static {
        AliyunossConfigManager configManager = AliyunossConfigManager.Companion.getInstance();
        AliyunOSSConfig configs = configManager.getAliyunOSSConfig("chips-content");
        Objects.requireNonNull(configs);

        OSS_ENDPOINT = configs.getEndpoint();
        OSS_ACCESS_ID = configs.getAccessId();
        OSS_ACCESS_KEY = configs.getAccessKey();
        OSS_BUCKET = configs.getBucket();
        OSS_HOST = configs.getHost();
        OSS_VIDEO_SNAPSHOT_HOST = "v.17xueba.com/";

        client = new OSSClient(OSS_ENDPOINT, OSS_ACCESS_ID, OSS_ACCESS_KEY);

    }

    // platform ：  agent, admin 等
    public static MapMessage getSignature(UploadFileType uploadFileType, String platform, HttpServletResponse response) {

        boolean isTest = RuntimeMode.current().lt(Mode.STAGING);
//        if (uploadFileType != UploadFileType.doc) {
//            return MapMessage.errorMessage("文件类型有误");
//        }

        String dir = DIR_PREFIX + "/" + platform + "/" + (isTest ? "test/" : "pro/")
                + (uploadFileType.equals(UploadFileType.unsupported) ? "file" : uploadFileType.name())
                + "/" + FastDateFormat.getInstance("yyyy/MM").format(new Date()) + "/";

        String filename = FastDateFormat.getInstance("yyyyMMddHHmmssS").format(new Date()) + RandomStringUtils.randomNumeric(3);

        long expireTime = 7200;
        long expireEndTime = System.currentTimeMillis() + expireTime * 1000;
        java.sql.Date expiration = new java.sql.Date(expireEndTime);

        PolicyConditions policyConds = new PolicyConditions();
        policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, 1024 * 1024 * 4096L);   // 文件大小 0 - 4G
        policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, dir);   // 文件必须上传到指定目录

        String postPolicy = client.generatePostPolicy(expiration, policyConds);

        try {
            byte[] binaryData = postPolicy.getBytes("utf-8");
            String encodedPolicy = BinaryUtil.toBase64String(binaryData);

            String postSignature = client.calculatePostSignature(postPolicy);

            MapMessage respMap = new MapMessage();
            respMap.put("accessid", OSS_ACCESS_ID);
            respMap.put("policy", encodedPolicy);
            respMap.put("signature", postSignature);
            respMap.put("dir", dir);
            respMap.put("filename", filename);
            respMap.put("host", "https://" + OSS_BUCKET + "." + OSS_ENDPOINT);
            respMap.put("videoHost", OSS_HOST);
            respMap.put("videoSnapshotHost", OSS_VIDEO_SNAPSHOT_HOST);
            respMap.put("expire", String.valueOf(expireEndTime / 1000));
            respMap.put("callback", getCallback(platform));

            respMap.put("accessKeySecret", OSS_ACCESS_KEY);
            respMap.put("endpoint", OSS_ENDPOINT);
            respMap.put("bucket", OSS_BUCKET);

            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Methods", "GET, POST");
            return respMap;
        } catch (Exception ex) {
            logger.error("GetPostBody failed from OssCallback:{}", ex.getMessage());
            return MapMessage.errorMessage().set("error", ex.getMessage());
        }
    }

    private static String getCallback(String plat) {
        String callbackUrl = "/mobile/file/osscallback.vpage";
        if (StringUtils.equals("agent", plat)) {
            callbackUrl = AGENT_BASE_URL + "/mobile/file/osscallback.vpage";
        }
        Map<String, Object> callback = new HashedMap<>();
        callback.put("callbackUrl", callbackUrl);
        callback.put("callbackBody", "bucket=${bucket}&object=${object}&etag=${etag}&size=${size}&mineType=${mineType}");
        System.out.println(JsonUtils.toJson(callback));
        return BinaryUtil.toBase64String(JsonUtils.toJson(callback).getBytes());
    }

    /**
     * 下载方法
     *
     * @param url oss文件地址
     * @Param localFilePath 用户本地保存地址 e:/1.mp4
     */
    public static OSSObject download(String url, String localFilePath) {
        // 下载OSS文件到本地文件。如果指定的本地文件存在会覆盖，不存在则新建。
        String objectName = url.substring(("https//" + OSS_HOST).length() + 1);
//        client.getObject(new GetObjectRequest(OSS_BUCKET, objectName), new File(localFilePath));
        // ossObject包含文件所在的存储空间名称、文件名称、文件元信息以及一个输入流。
        return client.getObject(OSS_BUCKET, objectName);
    }
}
