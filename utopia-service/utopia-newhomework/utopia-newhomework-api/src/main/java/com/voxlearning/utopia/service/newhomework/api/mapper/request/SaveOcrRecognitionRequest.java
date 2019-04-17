package com.voxlearning.utopia.service.newhomework.api.mapper.request;

import com.voxlearning.utopia.service.newhomework.api.mapper.OcrMentalImageDetail;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * 提交ocr识别结果请求参数
 * @author majianxin
 */
@Getter
@Setter
public class SaveOcrRecognitionRequest implements Serializable {

    private static final long serialVersionUID = -8937996431952961956L;

    private String clientType;  // 客户端类型:ios, android
    private String clientName;  // 客户端名称:17Student
    private Long studentId;     // 学生ID(家长通必传)
    private List<OcrMentalImageDetail> ocrMentalImageDetails; // 图片识别详情
    private String version;
}
