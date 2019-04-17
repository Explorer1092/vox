package com.voxlearning.wechat.context;

import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * Created by xinxin on 2/2/2016.
 */
@Getter
@Setter
public class CorrectHomeworkContext implements Serializable {
    private static final long serialVersionUID = 7054340460964065859L;


    private String homeworkId;                          // 作业ID
    private ObjectiveConfigType type;                   // 作业形式
    private String questionId;                          // 试题ID
    private Boolean isBatch;                            // 是否为批量/一键批改
    private List<CorrectQuestionContext> corrections;   // 批改信息
}
