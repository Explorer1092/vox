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

package com.voxlearning.utopia.mizar.utils;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.ObjectMetadata;
import com.voxlearning.alps.config.manager.ConfigManager;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.RandomStringUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.time.FastDateFormat;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.core.HttpClientType;
import lombok.Cleanup;
import org.slf4j.Logger;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Copy from OssManageUtils
 * Created by alex on 2016/5/27.
 */
public class MizarOssManageUtils {

    private static final Logger logger = LoggerFactory.getLogger(MizarOssManageUtils.class);

    private final static String ACCESS_ID;
    private final static String ACCESS_KEY;

    // 带图片处理服务的访问地址
    private final static String OSS_IMAGE_HOST;
    // 文件服务的访问地址
    private final static String OSS_HOST;
    private final static String OSS_ENDPOINT;
    private final static String OSS_BUCKET = "17zy-homework";
    private static OSSClient client;

    // 缓存
    private static final Map<String, Boolean> folderCache = new HashMap<>();
    private static final Map<String, String> suffixContentMap;
    public static final String invalidFile = "INVALID_FILE";

    static {
        Map<String, String> configs = ConfigManager.instance().getCommonConfig().getConfigs();

        ACCESS_ID = StringUtils.defaultString(configs.get("oss_aliyun_access_id"));
        ACCESS_KEY = StringUtils.defaultString(configs.get("oss_aliyun_access_key"));
        OSS_IMAGE_HOST = StringUtils.defaultString(configs.get("oss_homework_image_host"));
        OSS_HOST = StringUtils.defaultString(configs.get("oss_homework_host"));
        OSS_ENDPOINT = StringUtils.defaultString(configs.get("oss_homework_endpoint"));
        client = new OSSClient(OSS_ENDPOINT, ACCESS_ID, ACCESS_KEY);

        suffixContentMap = new HashMap<>();
        suffixContentMap.put("bmp", "image/bmp");
        suffixContentMap.put("gif", "image/gif");
        suffixContentMap.put("jpeg", "image/jpeg");
        suffixContentMap.put("jpg", "image/jpeg");
        suffixContentMap.put("png", "image/jpeg");
        suffixContentMap.put("html", "text/html");
        suffixContentMap.put("txt", "text/plain");
        suffixContentMap.put("vsd", "application/vnd.visio");
        suffixContentMap.put("pptx", "application/vnd.ms-powerpoint");
        suffixContentMap.put("ppt", "application/vnd.ms-powerpoint");
        suffixContentMap.put("docx", "application/msword");
        suffixContentMap.put("doc", "application/msword");
        suffixContentMap.put("xls", "application/vnd.ms-excel");
        suffixContentMap.put("xlsx", "application/vnd.ms-excel");
        suffixContentMap.put("xml", "text/xml");
        suffixContentMap.put("pdf", "application/pdf");
        suffixContentMap.put("mp3", "audio/mp3");
    }

    /**
     * 将远程url指定的文件上传到服务器
     *
     * @param url
     * @return
     */
    public static String upload(final String url) {
        AlpsHttpResponse httpResponse = HttpRequestExecutor.instance(HttpClientType.BASIC).get(url).socketTimeout(30000).execute();
        byte[] content = httpResponse.getOriginalResponse();
        String suffix = url.lastIndexOf('.') > 0 ? url.substring(url.lastIndexOf('.') + 1) : null;
        return upload(content, suffix);
    }

    /**
     * 将远程url指定的文件上传到服务器
     *
     * @param url
     * @return
     */
    public static String uploadBookStore(final String url) {
        AlpsHttpResponse httpResponse = HttpRequestExecutor.instance(HttpClientType.BASIC).get(url).socketTimeout(30000).execute();
        byte[] content = httpResponse.getOriginalResponse();
        String suffix = "jpg";
        String filePath = "bookStoreQrCode";
        return uploadByPath(content, suffix, filePath);
    }

    /**
     * @param content 文件内容
     * @param suffix  文件后缀
     * @return 远程url地址
     */
    public static String uploadByPath(final byte[] content, String suffix,String filePath) {
        String result = null;
        if (Objects.isNull(content) || content.length <= 0) {
            logger.warn("File upload failed,no content");
            return null;
        }
        if (suffix == null || !suffixContentMap.containsKey(suffix.toLowerCase())) {
            return invalidFile;
        }
        String defaultDir = "mizar/" + filePath + "/";
        if (RuntimeMode.isDevelopment() || RuntimeMode.isTest()) {
            defaultDir = "mizar/test/" + filePath + "/";
        }
        String key = FastDateFormat.getInstance("yyyyMMddHHmmssSSS").format(new Date()) + RandomStringUtils.randomNumeric(3) + "." + suffix;
        String path = defaultDir + FastDateFormat.getInstance("yyyy/MM/dd").format(new Date()) + "/";
        String fileName = path + key;
        try {
            createFolder(path);
            uploadFile(fileName, content);
            if (StringUtils.contains(contentType(suffix), "image")) {
                result = OSS_IMAGE_HOST + fileName;
            } else {
                result = OSS_HOST + fileName;
            }
        } catch (Exception e) {
            logger.warn("File upload failed", e);
        }
        return result;
    }


    /**
     * @param content 文件内容
     * @param suffix  文件后缀
     * @return 远程url地址
     */
    public static String upload(final byte[] content, String suffix) {
        String result = null;
        if (Objects.isNull(content) || content.length <= 0) {
            logger.warn("File upload failed,no content");
            return null;
        }
        if (suffix == null || !suffixContentMap.containsKey(suffix.toLowerCase())) {
            return invalidFile;
        }
        String defaultDir = "mizar/";
        if (RuntimeMode.isDevelopment() || RuntimeMode.isTest()) {
            defaultDir = "mizar/test/";
        }
        String key = FastDateFormat.getInstance("yyyyMMddHHmmssSSS").format(new Date()) + RandomStringUtils.randomNumeric(3) + "." + suffix;
        String path = defaultDir + FastDateFormat.getInstance("yyyy/MM/dd").format(new Date()) + "/";
        String fileName = path + key;
        try {
            createFolder(path);
            uploadFile(fileName, content);
            if (StringUtils.contains(contentType(suffix), "image")) {
                result = OSS_IMAGE_HOST + fileName;
            } else {
                result = OSS_HOST + fileName;
            }
        } catch (Exception e) {
            logger.warn("File upload failed", e);
        }
        return result;
    }

    public static String upload(MultipartFile fileData, String folder) {
        String result = null;
        String env = "mizar/" + folder + "/";
        if (RuntimeMode.isDevelopment() || RuntimeMode.isTest()) {
            env = "mizar/test/" + folder + "/";
        }
        try {
            String suffix = StringUtils.substringAfterLast(fileData.getOriginalFilename(), ".");
            if (StringUtils.isBlank(suffix)) {
                suffix = "jpg";
            }
            String key = FastDateFormat.getInstance("yyyyMMddHHmmssSSS").format(new Date()) + RandomStringUtils.randomNumeric(3) + "." + suffix;
            String path = env + FastDateFormat.getInstance("yyyy/MM/dd").format(new Date()) + "/";
            String fileName = path + key;
            createFolder(path);
            uploadFile(fileName, fileData);

            if (StringUtils.contains(contentType(suffix), "image")) {
                result = OSS_IMAGE_HOST + fileName;
            } else {
                result = OSS_HOST + fileName;
            }
        } catch (Exception e) {
            logger.warn("File upload failed", e);
        }
        return result;
    }

    /**
     * 上传文件
     *
     * @param fileData 文件
     */
    public static String upload(MultipartFile fileData) {
        String result = null;
        String env = "mizar/";
        if (RuntimeMode.isDevelopment() || RuntimeMode.isTest()) {
            env = "mizar/test/";
        }
        try {
            String suffix = StringUtils.substringAfterLast(fileData.getOriginalFilename(), ".");
            if (suffix == null || !suffixContentMap.containsKey(suffix.toLowerCase())) {
                return invalidFile;
            }
            String key = FastDateFormat.getInstance("yyyyMMddHHmmssSSS").format(new Date()) + RandomStringUtils.randomNumeric(3) + "." + suffix;
            String path = env + FastDateFormat.getInstance("yyyy/MM/dd").format(new Date()) + "/";
            String fileName = path + key;
            createFolder(path);
            uploadFile(fileName, fileData);

            if (StringUtils.contains(contentType(suffix), "image")) {
                result = OSS_IMAGE_HOST + fileName;
            } else {
                result = OSS_HOST + fileName;
            }
        } catch (Exception e) {
            logger.warn("File upload failed", e);
        }
        return result;
    }

    /**
     * 删除文件
     *
     * @param fileName 文件
     */
    public static void delete(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return;
        }
        try {
            client.deleteObject(OSS_BUCKET, fileName);
        } catch (Exception e) {
            logger.warn("File delete failed, fileName={}", fileName, e);
        }
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

    /**
     * 文件上传
     *
     * @param key      文件名
     * @param fileData 文件
     * @throws Exception
     */
    private static void uploadFile(String key, MultipartFile fileData) throws Exception {
        ObjectMetadata objectMeta = new ObjectMetadata();
        objectMeta.setContentLength(fileData.getSize());
        @Cleanup InputStream input = fileData.getInputStream();
        client.putObject(OSS_BUCKET, key, input, objectMeta);
    }

    /**
     * @param key
     * @param content 文件内容
     * @throws Exception
     */
    private static void uploadFile(String key, byte[] content) throws Exception {
        ObjectMetadata objectMeta = new ObjectMetadata();
        objectMeta.setContentLength(content.length);
        @Cleanup InputStream input = new ByteArrayInputStream(content);
        client.putObject(OSS_BUCKET, key, input, objectMeta);
    }

    /**
     * 判断OSS服务文件上传时文件的contentType
     *
     * @param suffix 后缀
     * @return 文件类型
     */
    private static String contentType(String suffix) {
        suffix = suffix.toLowerCase();
        String contentType = suffixContentMap.get(suffix);
        return contentType == null ? "text/html" : contentType;
    }
}
