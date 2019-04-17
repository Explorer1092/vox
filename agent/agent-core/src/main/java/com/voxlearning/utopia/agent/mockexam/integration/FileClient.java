package com.voxlearning.utopia.agent.mockexam.integration;

import com.voxlearning.alps.lang.util.MapMessage;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.Serializable;

/**
 * 文件服务
 *
 * @Author: peng.zhang
 * @Date: 2018/8/15 15:46
 */
public interface FileClient {

    /**
     * 文件上传请求
     */
    @Data
    class UploadRequest implements Serializable {
        private MultipartFile inputFile;    //
        private String fileId;              // 文件ID
        private String fileName;            // 文件名
        private String contentType;         // 内容类型
        private InputStream inputStream;    // 文件流
    }

    /**
     * 文件下载请求
     */
    @Data
    class DownloadRequest implements Serializable {
        private String fileName;            // 文件名称
        private String contentType;         // 文件类型
        private byte[] fileBytes;                // 文件内容二进制
    }

    /**
     * 上传
     * @param request 请求
     * @return 上传结果
     */
    MapMessage upload(FileClient.UploadRequest request);

}
