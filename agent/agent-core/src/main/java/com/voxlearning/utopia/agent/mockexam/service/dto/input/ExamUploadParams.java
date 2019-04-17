package com.voxlearning.utopia.agent.mockexam.service.dto.input;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传参数
 *
 * @Author: peng.zhang
 * @Date: 2018/8/15 16:27
 */
@Data
public class ExamUploadParams {

    private MultipartFile inputFile;        // 文件

}
