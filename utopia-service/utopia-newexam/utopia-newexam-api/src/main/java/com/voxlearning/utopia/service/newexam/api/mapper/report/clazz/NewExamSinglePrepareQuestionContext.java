package com.voxlearning.utopia.service.newexam.api.mapper.report.clazz;

import com.voxlearning.utopia.service.newexam.api.constant.ExamReportAnswerStatType;
import com.voxlearning.utopia.service.newexam.api.entity.NewExamProcessResult;
import com.voxlearning.utopia.service.newexam.api.mapper.report.NewExamSingleSubQuestion;
import com.voxlearning.utopia.service.question.api.entity.NewExam;
import com.voxlearning.utopia.service.question.api.entity.NewQuestionsSubContents;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 用于单小题，处理数据前构建数据环境
 */
@Getter
@Setter
public class NewExamSinglePrepareQuestionContext implements Serializable {
    private static final long serialVersionUID = 3333016579253039444L;
    private ExamReportAnswerStatType answerStatType;
    private NewExamSingleSubQuestion singleSubQuestion;
    private NewQuestionsSubContents newQuestionsSubContents;
    private NewExam newExam;
    private Map<String, NewExamProcessResult> newExamProcessResultMap;
    private int subIndex;
    private List<List<String>> top3Answer;

    public NewExamSinglePrepareQuestionContext(ExamReportAnswerStatType answerStatType,
                                               NewExamSingleSubQuestion singleSubQuestion,
                                               NewQuestionsSubContents newQuestionsSubContents,
                                               NewExam newExam,
                                               Map<String, NewExamProcessResult> newExamProcessResultMap,
                                               int subIndex) {
        this.answerStatType = answerStatType;
        this.singleSubQuestion = singleSubQuestion;
        this.newQuestionsSubContents = newQuestionsSubContents;
        this.newExam = newExam;
        this.newExamProcessResultMap = newExamProcessResultMap;
        this.subIndex = subIndex;
    }
}
