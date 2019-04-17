package com.voxlearning.utopia.service.newexam.api.mapper.report.clazz;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.service.newexam.api.constant.NewExamQuestionType;
import com.voxlearning.utopia.service.question.api.entity.NewPaperQuestion;
import com.voxlearning.utopia.service.question.api.entity.NewQuestionsSubContents;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 班级处理数据
 */
@Setter
@Getter
public class NewExamClazzPrepareQuestionContext implements Serializable {
    private static final long serialVersionUID = 2842754932831931072L;
    private NewQuestionsSubContents newQuestionsSubContents;
    private double standardScore;
    private int subIndex;
    private int index;
    private NewPaperQuestion question;
    private NewExamQuestionType newExamQuestionType;
    private NewExamDetailH5ToQuestion newExamDetailToQuestion;
    private Subject subject;


    public NewExamClazzPrepareQuestionContext(NewQuestionsSubContents newQuestionsSubContents,
                                              double standardScore,
                                              int subIndex,
                                              int index,
                                              NewPaperQuestion question,
                                              NewExamQuestionType newExamQuestionType,
                                              NewExamDetailH5ToQuestion newExamDetailToQuestion,
                                              Subject subject) {
        this.newQuestionsSubContents = newQuestionsSubContents;
        this.standardScore = standardScore;
        this.subIndex = subIndex;
        this.index = index;
        this.question = question;
        this.newExamQuestionType = newExamQuestionType;
        this.newExamDetailToQuestion = newExamDetailToQuestion;
        this.subject = subject;
    }
}
