package com.voxlearning.utopia.agent.utils;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.PolicyConditions;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.config.NCS;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.repackaged.org.apache.commons.collections4.map.HashedMap;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.RandomStringUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.time.FastDateFormat;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.storage.aliyunoss.module.config.AliyunOSSConfig;
import com.voxlearning.alps.storage.aliyunoss.module.config.AliyunossConfigManager;
import com.voxlearning.utopia.agent.constants.UploadFileType;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

/**
 * @author song.wang
 * @date 2018/7/10
 */
public class FlatVideoOssManageUtils {

    private static final Logger logger = LoggerFactory.getLogger(FlatVideoOssManageUtils.class);

    private final static String DIR_PREFIX = "plat";
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
        AliyunOSSConfig configs = configManager.getAliyunOSSConfig("plat-video-content");
        Objects.requireNonNull(configs);

        OSS_ENDPOINT = configs.getEndpoint();
        OSS_ACCESS_ID = configs.getAccessId();
        OSS_ACCESS_KEY = configs.getAccessKey();
        OSS_BUCKET = configs.getBucket();
        OSS_HOST = configs.getHost();
        OSS_VIDEO_SNAPSHOT_HOST = "ss.17zuoye.cn/";

        client = new OSSClient(OSS_ENDPOINT, OSS_ACCESS_ID, OSS_ACCESS_KEY);

        if (RuntimeMode.current() == Mode.DEVELOPMENT) {
            AGENT_BASE_URL = NCS.getProperty("product.config.agent_base_url", "http://127.0.0.1:8083");
        } else if (RuntimeMode.current() == Mode.TEST) {
            AGENT_BASE_URL = NCS.getProperty("product.config.agent_base_url", "http://marketing.test.17zuoye.net");
        } else if (RuntimeMode.current() == Mode.STAGING) {
            AGENT_BASE_URL = NCS.getProperty("product.config.agent_base_url", "http://marketing.staging.17zuoye.net");
        } else if (RuntimeMode.current() == Mode.PRODUCTION) {
            AGENT_BASE_URL = NCS.getProperty("product.config.agent_base_url", "http://marketing.oaloft.com/");
        }
    }

    // platform ：  agent, admin 等
    public static MapMessage getSignature(UploadFileType uploadFileType, String platform, HttpServletResponse response) {
        boolean isTest = RuntimeMode.current().lt(Mode.STAGING);
        if (uploadFileType != UploadFileType.video) {
            return MapMessage.errorMessage("文件类型有误");
        }

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
            respMap.put("host", "http://" + OSS_BUCKET + "." + OSS_ENDPOINT);
            respMap.put("videoHost", OSS_HOST);
            respMap.put("videoSnapshotHost", OSS_VIDEO_SNAPSHOT_HOST);
            respMap.put("expire", String.valueOf(expireEndTime / 1000));
            respMap.put("callback", getCallback(platform));
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Methods", "GET, POST");
            return respMap;
        } catch (Exception ex) {
            return null;
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


    public static void handleOssCallback(HttpServletRequest request, HttpServletResponse response) {
        try {
            String ossCallbackBody = getPostBody(request.getInputStream(), Integer.parseInt(request.getHeader("content-length")));
            boolean ret = verifyOSSCallbackRequest(request, ossCallbackBody);
            if (ret)
                response(request, response, "{\"Status\":\"OK\"}", HttpServletResponse.SC_OK);
            else
                response(request, response, "{\"Status\":\"verdify not ok\"}", HttpServletResponse.SC_BAD_REQUEST);
        } catch (IOException e) {
            logger.warn("GetPostBody failed from OssCallback:{}", e.getMessage());
        }
    }

    private static boolean verifyOSSCallbackRequest(HttpServletRequest request, String ossCallbackBody) throws NumberFormatException, IOException {
        boolean ret;
        String pubKeyInput = request.getHeader("x-oss-pub-key-url");
        byte[] authorization = BinaryUtil.fromBase64String(request.getHeader("Authorization"));
        byte[] pubKey = BinaryUtil.fromBase64String(pubKeyInput);
        if (pubKey == null)
            return false;
        String pubKeyAddr = new String(pubKey);
        if (!pubKeyAddr.startsWith("http://gosspublic.alicdn.com/") && !pubKeyAddr.startsWith("https://gosspublic.alicdn.com/"))
            return false;
        String retString = HttpRequestExecutor.defaultInstance().get(pubKeyAddr).execute().getResponseString();
        retString = retString.replace("-----BEGIN PUBLIC KEY-----", "");
        retString = retString.replace("-----END PUBLIC KEY-----", "");
        String queryString = request.getQueryString();
        String uri = request.getRequestURI();
        String authStr = java.net.URLDecoder.decode(uri, "UTF-8");
        if (queryString != null && !queryString.equals("")) {
            authStr += "?" + queryString;
        }
        authStr += "\n" + ossCallbackBody;
        ret = doCheck(authStr, authorization, retString);
        return ret;
    }

    private static boolean doCheck(String content, byte[] sign, String publicKey) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            byte[] encodedKey = BinaryUtil.fromBase64String(publicKey);
            PublicKey pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));
            java.security.Signature signature = java.security.Signature.getInstance("MD5withRSA");
            signature.initVerify(pubKey);
            signature.update(content.getBytes());
            return signature.verify(sign);
        } catch (Exception e) {
            logger.warn("doCheck failed from OssCallback:{}", e.getMessage());
            return false;
        }
    }

    private static String getPostBody(InputStream is, int contentLen) {
        if (contentLen > 0) {
            int readLen = 0;
            int readLengthThisTime;
            byte[] message = new byte[contentLen];
            try {
                while (readLen != contentLen) {
                    readLengthThisTime = is.read(message, readLen, contentLen - readLen);
                    if (readLengthThisTime == -1) {
                        break;
                    }
                    readLen += readLengthThisTime;
                }
                return new String(message);
            } catch (IOException e) {
                logger.warn("getPostBody failed:{}", e.getMessage());
            }
        }
        return "";
    }

    private static void response(HttpServletRequest request, HttpServletResponse response, String results, int status) throws IOException {
        String callbackFunName = request.getParameter("callback");
        response.addHeader("Content-Length", String.valueOf(results.length()));
        if (callbackFunName == null || callbackFunName.equalsIgnoreCase(""))
            response.getWriter().println(results);
        else
            response.getWriter().println(callbackFunName + "( " + results + " )");
        response.setStatus(status);
        response.flushBuffer();
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
