package com.voxlearning.washington.net.message.exam;

import com.voxlearning.utopia.service.newhomework.api.constant.CorrectType;
import com.voxlearning.utopia.service.newhomework.api.constant.Correction;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class CorrectQuestionRequest implements Serializable {

    private static final long serialVersionUID = 5944350548697578147L;

    private Long userId;

    private Boolean review;                                     // 是否已阅
    private CorrectType correctType;   // 批改类型
    private Correction correction;     // 批改信息
    private String teacherMark;                                 // 评语
}