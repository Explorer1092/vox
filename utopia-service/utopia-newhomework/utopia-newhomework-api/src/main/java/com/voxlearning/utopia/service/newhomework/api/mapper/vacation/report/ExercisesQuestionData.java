package com.voxlearning.utopia.service.newhomework.api.mapper.vacation.report;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class ExercisesQuestionData implements Serializable {

    private static final long serialVersionUID = 8820811687362716396L;
    private String questionId; //题ID
    private String userAnswers; //用户答案
    private String standardAnswers;//标准答案
    private String difficultyName;//困难程度
    private String questionType;//题型
}