package com.voxlearning.washington.net.message.exam;

import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class SaveCorrectQuestionRequest implements Serializable {

    private static final long serialVersionUID = -3001857150332861430L;

    // PS:一次作业的某一作业形式中的一道题，老师对不同学生的答题结果进行批改。

    private String homeworkId;                          // 作业ID
    private ObjectiveConfigType type;                   // 作业形式
    private String questionId;                          // 试题ID
    private Boolean isBatch;                            // 是否为批量/一键批改
    private List<CorrectQuestionRequest> corrections;   // 批改信息

}