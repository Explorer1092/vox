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

package com.voxlearning.utopia.agent.utils;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.ObjectMetadata;
import com.voxlearning.alps.config.manager.ConfigManager;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.RandomStringUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.time.FastDateFormat;
import com.voxlearning.alps.runtime.RuntimeMode;
import lombok.Cleanup;
import org.slf4j.Logger;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Copy from OssManageUtils
 * Created by alex on 2016/5/27.
 */
public class AgentOssManageUtils {

    private static final Logger logger = LoggerFactory.getLogger(AgentOssManageUtils.class);

    private final static String ACCESS_ID;
    private final static String ACCESS_KEY;
    //private final static String BUCKET_NAME = "17-homework";

    // 带图片处理服务的访问地址
    private final static String OSS_IMAGE_HOST;
    // 文件服务的访问地址
    private final static String OSS_HOST;
    private final static String OSS_ENDPOINT;
    private final static String OSS_BUCKET = "17zy-homework";
    private static OSSClient client;

    // 缓存
    private static final Map<String, Boolean> folderCache = new HashMap<>();

    static {
        Map<String, String> configs = ConfigManager.instance().getCommonConfig().getConfigs();

        ACCESS_ID = StringUtils.defaultString(configs.get("oss_aliyun_access_id"));
        ACCESS_KEY = StringUtils.defaultString(configs.get("oss_aliyun_access_key"));
        OSS_IMAGE_HOST = StringUtils.defaultString(configs.get("oss_homework_image_host"));
        OSS_HOST = StringUtils.defaultString(configs.get("oss_homework_host"));
        OSS_ENDPOINT = StringUtils.defaultString(configs.get("oss_homework_endpoint"));

        client = new OSSClient(OSS_ENDPOINT, ACCESS_ID, ACCESS_KEY);
    }

    /**
     * @param filedata 文件
     * @param rootPath 文件根目录，可根据服务自行定义
     * @return 文件地址
     */
    public static String upload(MultipartFile filedata, String rootPath) {

        String result = "";
        String env = "";
        if (RuntimeMode.isDevelopment() || RuntimeMode.isTest()) {
            env = "test";
        }

        try {
            String prefix = StringUtils.substringAfterLast(filedata.getOriginalFilename(), ".");
            String key = FastDateFormat.getInstance("yyyyMMddHHmmssSSS").format(new Date()) + RandomStringUtils.randomNumeric(3) + "." + prefix;
            String path = rootPath + "/" + env + FastDateFormat.getInstance("yyyy/MM/dd").format(new Date()) + "/";

            createFolder(path);
            uploadFile(path + key, filedata);

            if (StringUtils.contains(contentType(prefix), "image")) {
                result = OSS_IMAGE_HOST + path + key;
            } else {
                result = OSS_HOST + path + key;
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.warn("File upload failed", e);
        }

        return result;
    }

    /**
     * 上传文件
     *
     * @param filedata 文件
     */
    public static String upload(MultipartFile filedata) {

        String result = "";
        String env = "marketing/";
        if (RuntimeMode.isDevelopment() || RuntimeMode.isTest()) {
            env = "marketing/test/";
        }

        try {
            String prefix = StringUtils.substringAfterLast(filedata.getOriginalFilename(), ".");
            if (StringUtils.isBlank(prefix)) {
                prefix = "jpg";
            }
            String key = FastDateFormat.getInstance("yyyyMMddHHmmssSSS").format(new Date()) + RandomStringUtils.randomNumeric(3) + "." + prefix;
            String path = env + FastDateFormat.getInstance("yyyy/MM/dd").format(new Date()) + "/";

            createFolder(path);
            uploadFile(path + key, filedata);

            if (StringUtils.contains(contentType(prefix), "image")) {
                result = OSS_IMAGE_HOST + path + key;
            } else {
                result = OSS_HOST + path + key;
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.warn("File upload failed", e);
        }

        return result;
    }

    /**
     * 上传文件
     *
     * @param file   文件
     * @param prefix 后缀
     * @throws Exception
     */
    public static String upload(File file, String prefix) throws Exception {
        String result = "";
        String env = "";
        if (RuntimeMode.isDevelopment() || RuntimeMode.isTest()) {
            env = "test";
        }

        try {
            String key = FastDateFormat.getInstance("yyyyMMddHHmmssSSS").format(new Date()) + RandomStringUtils.randomNumeric(3) + "." + prefix;
            String path = env + FastDateFormat.getInstance("yyyy/MM/dd").format(new Date()) + "/";

            createFolder(path);
            uploadFile(path + key, file);
            if (StringUtils.contains(contentType(prefix), "image")) {
                result = OSS_IMAGE_HOST + path + key;
            } else {
                result = OSS_HOST + path + key;
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.warn("File upload failed", e);
        }

        return result;
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
     * @param filedata 文件
     * @throws Exception
     */
    private static void uploadFile(String key, MultipartFile filedata) throws Exception {
        ObjectMetadata objectMeta = new ObjectMetadata();
        objectMeta.setContentLength(filedata.getSize());
        @Cleanup InputStream input = filedata.getInputStream();
        client.putObject(OSS_BUCKET, key, input, objectMeta);
    }

    /**
     * 文件上传
     *
     * @param key  文件名
     * @param file 文件
     * @throws Exception
     */
    private static void uploadFile(String key, File file) throws Exception {
        ObjectMetadata objectMeta = new ObjectMetadata();
        objectMeta.setContentLength(file.length());
        @Cleanup InputStream input = new FileInputStream(file);
        client.putObject(OSS_BUCKET, key, input, objectMeta);
    }

    /**
     * 判断OSS服务文件上传时文件的contentType
     *
     * @param prefix 后缀
     * @return 文件类型
     */
    private static String contentType(String prefix) {
        if (StringUtils.equalsIgnoreCase(prefix, "bmp")) {
            return "image/bmp";
        }
        if (StringUtils.equalsIgnoreCase(prefix, "gif")) {
            return "image/gif";
        }
        if (StringUtils.equalsIgnoreCase(prefix, "jpeg") ||
                StringUtils.equalsIgnoreCase(prefix, "jpg") ||
                StringUtils.equalsIgnoreCase(prefix, "png")) {
            return "image/jpeg";
        }
        if (StringUtils.equalsIgnoreCase(prefix, "html")) {
            return "text/html";
        }
        if (StringUtils.equalsIgnoreCase(prefix, "txt")) {
            return "text/plain";
        }
        if (StringUtils.equalsIgnoreCase(prefix, "vsd")) {
            return "application/vnd.visio";
        }
        if (StringUtils.equalsIgnoreCase(prefix, "ppts") ||
                StringUtils.equalsIgnoreCase(prefix, "ppt")) {
            return "application/vnd.ms-powerpoint";
        }
        if (StringUtils.equalsIgnoreCase(prefix, "docx") ||
                StringUtils.equalsIgnoreCase(prefix, "doc")) {
            return "application/msword";
        }
        if (StringUtils.equalsIgnoreCase(prefix, "xml")) {
            return "text/xml";
        }
        return "text/html";
    }

}
