package com.voxlearning.utopia.service.newexam.api.context;

import com.voxlearning.utopia.service.newexam.api.entity.NewExamProcessResult;
import com.voxlearning.utopia.service.newexam.api.entity.NewExamRegistration;
import com.voxlearning.utopia.service.newexam.api.entity.NewExamResult;
import com.voxlearning.utopia.service.question.api.entity.NewExam;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * Created by tanguohong on 2016/3/23.
 */
@Getter
@Setter
public class CorrectNewExamContext extends AbstractContext<CorrectNewExamContext> {

    private static final long serialVersionUID = -1105912379620090003L;

    // in
    private String newExamId;
    private String questionId;
    private Map<Long, Double> userScoreMap;
    private Integer subId;

    // m
    private NewExam newExam;
    private NewQuestion newQuestion;
    private Boolean isNewOral;
    private Double standardScore; // 这道题目的标准分
    private Double subStandardScore; // 子题标准分
    private List<String> newExamResultIds;
    private List<String> newExamProcessResultIds;
    private Map<String, NewExamResult> newExamResultMap;
    private Map<String, NewExamRegistration> newExamRegistrationMap;
    private Map<Long, NewExamProcessResult> newExamProcessResultMap;
    private List<Map<String, Object>> correctSuccessUsers; //返回批改成功信息
    private List<Map<String, Object>> correctErrorUsers; //返回批改失败信息

}
