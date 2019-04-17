package com.voxlearning.utopia.service.newexam.api.mapper.report.personal;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.service.newexam.api.constant.NewExamQuestionType;
import com.voxlearning.utopia.service.question.api.entity.NewPaperQuestion;
import com.voxlearning.utopia.service.question.api.entity.NewQuestionsSubContents;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 个人报告构建小题
 */
@Getter
@Setter
public class NewExamPersonalPrepareQuestionContext implements Serializable {
    private static final long serialVersionUID = -7577350077428690884L;
    private NewQuestionsSubContents newQuestionsSubContents;
    private NewExamPersonalQuestion newExamDetailToQuestion;
    private NewExamQuestionType newExamQuestionType;
    private NewPaperQuestion question;
    private int index;
    private double standardScore;
    private Subject subject;

    public NewExamPersonalPrepareQuestionContext(NewQuestionsSubContents newQuestionsSubContents,
                                                 NewExamPersonalQuestion newExamDetailToQuestion,
                                                 NewExamQuestionType newExamQuestionType,
                                                 NewPaperQuestion question,
                                                 int index,
                                                 double standardScore,
                                                 Subject subject) {
        this.newQuestionsSubContents = newQuestionsSubContents;
        this.newExamDetailToQuestion = newExamDetailToQuestion;
        this.newExamQuestionType = newExamQuestionType;
        this.question = question;
        this.index = index;
        this.standardScore = standardScore;
        this.subject = subject;
    }

}
