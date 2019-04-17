package com.voxlearning.washington.support;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.*;
import com.voxlearning.alps.config.manager.ConfigManager;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.RandomStringUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.time.FastDateFormat;
import com.voxlearning.alps.runtime.RuntimeMode;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author peng.zhang.a
 * @since 17-2-4
 */
@Slf4j
public class FileOssManageUtils {


    private final static String ACCESS_ID;
    private final static String ACCESS_KEY;

    // 带图片处理服务的访问地址
    private final static String OSS_IMAGE_HOST;
    // 文件服务的访问地址
    private final static String OSS_HOST;
    private final static String OSS_ENDPOINT;
    private final static String OSS_BUCKET = "17zy-homework";
    // 缓存
    private static final Map<String, Boolean> folderCache = new HashMap<>();
    private static OSSClient client;

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
            result = path + key;
        } catch (Exception e) {
            e.printStackTrace();
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
     * 上传文件到指定的目录
     *
     * @param inputStream   文件
     * @param ext 后缀
     * @throws Exception
     */
    public static String upload(InputStream inputStream,long fileLength,String fileName,String dir, String ext) throws Exception {
        String result = "";
        String env = "";
        if (RuntimeMode.isDevelopment() || RuntimeMode.isTest()) {
            env = "test";
        }

        try {
            String key = fileName + "." + ext;
            String path = env + dir + "/";

            createFolder(path);
            uploadFile(path + key, inputStream,fileLength);
            result = path + key;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 上传文件
     *
     * @param inputStream   文件
     * @param prefix 后缀
     * @throws Exception
     */
    public static String upload(InputStream inputStream,long fileLength,String fileName, String prefix) throws Exception {
        String result = "";
        String env = "";
        if (RuntimeMode.isDevelopment() || RuntimeMode.isTest()) {
            env = "test";
        }

        try {
            String key = FastDateFormat.getInstance("yyyyMMddHHmmssSSS").format(new Date()) + RandomStringUtils.randomNumeric(3) + "." + prefix;
//            String key = fileName + "." + prefix;
            String path = env + FastDateFormat.getInstance("yyyy/MM/dd").format(new Date()) + "/";

            createFolder(path);
            uploadFile(path + key, inputStream,fileLength);
            result = path + key;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private static void uploadFile(String key,InputStream inputStream,long fileLength) throws Exception {
        ObjectMetadata objectMeta = new ObjectMetadata();
        objectMeta.setContentLength(fileLength);
        client.putObject(OSS_BUCKET, key, inputStream, objectMeta);
    }

    public static InputStream downFile(String key){
        OSSObject ossObject =  client.getObject(OSS_BUCKET,key);
        return ossObject.getObjectContent();
    }

    public static List<String> getFileList(String dir){
        ListObjectsRequest listObjectsRequest = new ListObjectsRequest(OSS_BUCKET);
        listObjectsRequest.setPrefix(dir+File.separator);
        ObjectListing listing = client.listObjects(listObjectsRequest);
        List<String> filePaths = new LinkedList();
        for (OSSObjectSummary objectSummary : listing.getObjectSummaries()) {
            System.out.println(objectSummary.getKey());
            filePaths.add(objectSummary.getKey());
        }
        return filePaths;
    }

    public static void main(String[] args) {
        getFileList("testimage20181008191327574877");
    }

    public static void delFile(String key){
        client.deleteObject(OSS_BUCKET,key);
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


    /**
     * 上传文件到指定的目录
     *
     * @param bs 文件
     * @param ext 文件后缀
     */
    public static String upload(byte[] bs, String ext) {
        String result = "";
        String env = "";
        if (RuntimeMode.isDevelopment() || RuntimeMode.isTest()) {
            env = "test/";
        }

        try {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            String dir = env + ext + "/" + LocalDateTime.now().format(dateTimeFormatter) + "/";
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
