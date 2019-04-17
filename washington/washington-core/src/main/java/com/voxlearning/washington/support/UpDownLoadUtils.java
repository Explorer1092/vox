package com.voxlearning.washington.support;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.ObjectMetadata;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.RandomStringUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.storage.aliyunoss.module.config.AliyunOSSConfig;
import com.voxlearning.alps.storage.aliyunoss.module.config.AliyunossConfigManager;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 上传工具类
 *
 * @author Wenlong Meng
 * @since Jan 30, 2019
 */
@Slf4j
public class UpDownLoadUtils {

    private final static String DIR_PREFIX = "plat";
    private final static String OSS_ENDPOINT;
    private final static String OSS_ACCESS_ID;
    private final static String OSS_ACCESS_KEY;
    private final static String OSS_BUCKET;
    private final static String OSS_HOST;
    private static final Map<String, Boolean> folderCache = new ConcurrentHashMap<>();

    private static OSSClient client;

    static {
        AliyunossConfigManager configManager = AliyunossConfigManager.Companion.getInstance();
        AliyunOSSConfig configs = configManager.getAliyunOSSConfig("plat-doc-content");
        Objects.requireNonNull(configs);

        OSS_ENDPOINT = configs.getEndpoint();
        OSS_ACCESS_ID = configs.getAccessId();
        OSS_ACCESS_KEY = configs.getAccessKey();
        OSS_BUCKET = configs.getBucket();
        OSS_HOST = configs.getHost();
        client = new OSSClient(OSS_ENDPOINT, OSS_ACCESS_ID, OSS_ACCESS_KEY);
    }

    /**
     * 创建目录
     *
     * @param path 目录结构，这个有要求必须是 "xxx/xxx/" 以目录名开头，/结尾
     * @throws IOException
     */
    private static void createFolder(String path) throws IOException {
        if (folderCache.containsKey(path)) return;

        ObjectMetadata objectMeta = new ObjectMetadata();
        byte[] buffer = new byte[0];
        objectMeta.setContentLength(0);
        try (ByteArrayInputStream in = new ByteArrayInputStream(buffer)) {
            client.putObject(OSS_BUCKET, path, in, objectMeta);
        }

        folderCache.put(path, true);
    }

    private static void uploadFile(String key,InputStream inputStream,long fileLength) throws Exception {
        ObjectMetadata objectMeta = new ObjectMetadata();
        objectMeta.setContentLength(fileLength);
        client.putObject(OSS_BUCKET, key, inputStream, objectMeta);
    }

    /**
     * 上传文件到指定的目录
     *
     * @param bs 文件
     * @param ext 文件后缀
     */
    public static String upload(byte[] bs, String ext) {
        String result = "";
        String env = "prod";
        if (RuntimeMode.isDevelopment() || RuntimeMode.isTest()) {
            env = "test/";
        }

        try {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            String dir = DIR_PREFIX +"/" + env + ext + "/" + LocalDateTime.now().format(dateTimeFormatter) + "/";
            dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
            String fileName = LocalDateTime.now().format(dateTimeFormatter) + RandomStringUtils.randomNumeric(7) + "." + ext;
            createFolder(dir);
            uploadFile(dir + fileName, new ByteArrayInputStream(bs),bs.length);
            result = dir + fileName;
        } catch (Exception e) {
            log.error("upload({},{})", ext, e);
        }
        return result;
    }

    /**
     * get host
     *
     * @return
     */
    public static String host(){
        return OSS_HOST;
    }
}
