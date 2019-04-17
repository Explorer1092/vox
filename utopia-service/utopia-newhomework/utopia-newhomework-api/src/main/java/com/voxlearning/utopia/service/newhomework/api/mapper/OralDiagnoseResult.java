package com.voxlearning.utopia.service.newhomework.api.mapper;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 口语诊断结果
 */
@Getter
@Setter
public class OralDiagnoseResult implements Serializable {
    private static final long serialVersionUID = 2516125610375119829L;

    private String diagnoseTag;     // 诊断标签 单因素:d 错音对:d->t
    private String diagnoseType;    // 诊断类型 单音素:SINGLE 错音对:PAIRS
    private Double score;           // 诊断得分 单因素得分 或者 错音对的易混音得分
    private String sample;          // 评测对象 单因素传lines里面的sample，错音对传confused里面的sample
    private String userText;        // 识别文本 单因素传lines里面的usertext，错音对传confused里面的usertext
}
