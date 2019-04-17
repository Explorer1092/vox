package com.voxlearning.utopia.service.newexam.api.mapper.report.clazz;

import com.voxlearning.utopia.service.newexam.api.entity.NewExamProcessResult;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 班级各题报告处理环境数据
 */
@Getter
@Setter
public class NewExamClazzQuestionContext implements Serializable {
    private static final long serialVersionUID = -5576017072003068987L;
    private int index;//小题号
    private NewExamDetailH5ToQuestion.StudentAnswer studentAnswer;//学生答案
    private NewExamDetailH5ToQuestion.SubQuestion subQuestion;//小题
    private NewExamProcessResult newExamProcessResult;//处理数据
    private boolean grasp;//是否掌握
    private List<Boolean> baleen;
    private NewQuestion newQuestion;

    public NewExamClazzQuestionContext(int index,
                                       NewExamDetailH5ToQuestion.StudentAnswer studentAnswer,
                                       NewExamDetailH5ToQuestion.SubQuestion subQuestion,
                                       NewExamProcessResult newExamProcessResult,
                                       boolean grasp,
                                       List<Boolean> baleen,
                                       NewQuestion newQuestion) {
        this.index = index;
        this.studentAnswer = studentAnswer;
        this.subQuestion = subQuestion;
        this.newExamProcessResult = newExamProcessResult;
        this.grasp = grasp;
        this.baleen = baleen;
        this.newQuestion = newQuestion;
    }
}
