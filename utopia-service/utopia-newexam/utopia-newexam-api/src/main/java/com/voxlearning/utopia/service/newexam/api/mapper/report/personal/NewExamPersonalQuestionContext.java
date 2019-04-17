package com.voxlearning.utopia.service.newexam.api.mapper.report.personal;

import com.voxlearning.utopia.service.newexam.api.entity.NewExamProcessResult;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 个人报告处理数据环境
 */
@Setter
@Getter
public class NewExamPersonalQuestionContext implements Serializable {
    private static final long serialVersionUID = -231872158894394926L;
    private NewExamProcessResult newExamProcessResult;
    private int index;
    private List<Boolean> baleen;
    private NewExamPersonalQuestion.NewExamPersonalSubQuestion subQuestion;
    private boolean grasp;
    private NewQuestion newQuestion;

    public NewExamPersonalQuestionContext(NewExamProcessResult newExamProcessResult,
                                          int index,
                                          List<Boolean> baleen,
                                          NewExamPersonalQuestion.NewExamPersonalSubQuestion subQuestion,
                                          boolean grasp,
                                          NewQuestion newQuestion) {
        this.newExamProcessResult = newExamProcessResult;
        this.index = index;
        this.baleen = baleen;
        this.subQuestion = subQuestion;
        this.grasp = grasp;
        this.newQuestion = newQuestion;
    }

}
