package com.voxlearning.wechat.context;

import com.voxlearning.utopia.service.newhomework.api.constant.CorrectType;
import com.voxlearning.utopia.service.newhomework.api.constant.Correction;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by xinxin on 2/2/2016.
 */
@Getter
@Setter
public class CorrectQuestionContext implements Serializable {
    private static final long serialVersionUID = 3239479083288315163L;

    private Long userId;

    private Boolean review;                                     // 是否已阅
    private CorrectType correctType;   // 批改类型
    private Correction correction;     // 批改信息
    private String teacherMark;                                 // 评语
}
