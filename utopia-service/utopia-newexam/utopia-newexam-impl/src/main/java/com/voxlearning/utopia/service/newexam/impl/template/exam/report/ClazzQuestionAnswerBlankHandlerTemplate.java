package com.voxlearning.utopia.service.newexam.impl.template.exam.report;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.newexam.api.constant.NewExamQuestionType;
import com.voxlearning.utopia.service.newexam.api.entity.NewExamProcessResult;
import com.voxlearning.utopia.service.newexam.api.mapper.report.clazz.NewExamClazzPrepareQuestionContext;
import com.voxlearning.utopia.service.newexam.api.mapper.report.clazz.NewExamClazzQuestionContext;
import com.voxlearning.utopia.service.newexam.api.mapper.report.clazz.NewExamDetailH5ToQuestion;
import com.voxlearning.utopia.service.question.api.entity.NewPaperQuestion;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.api.entity.NewQuestionsSubContents;

import javax.inject.Named;
import java.util.List;

/**
 * 班级维度，对每小题：填空题统计的处理
 */
@Named
public class ClazzQuestionAnswerBlankHandlerTemplate extends ClazzQuestionAnswerHandlerTemplate {
    @Override
    NewExamQuestionType getNewExamQuestionType() {
        return NewExamQuestionType.Blank;
    }

    @Override
    public void processSubQuestion(NewExamClazzQuestionContext newExamClazzQuestionContext) {
        int index = newExamClazzQuestionContext.getIndex();
        NewExamProcessResult newExamProcessResult = newExamClazzQuestionContext.getNewExamProcessResult();
        if (newExamProcessResult.getUserAnswers() == null || newExamProcessResult.getUserAnswers().size() <= index) {
            return;
        }
        NewExamDetailH5ToQuestion.SubQuestion subQuestion = newExamClazzQuestionContext.getSubQuestion();
        NewExamDetailH5ToQuestion.StudentAnswer studentAnswer = newExamClazzQuestionContext.getStudentAnswer();
        boolean b = newExamClazzQuestionContext.isGrasp();
        List<Boolean> baleen = newExamClazzQuestionContext.getBaleen();


        //是否正确
        if (b) {
            subQuestion.setRightNum(1 + subQuestion.getRightNum());
            subQuestion.getTianKongRightStudents().add(studentAnswer);
        } else {
            subQuestion.getTianKongWrongStudents().add(studentAnswer);
            subQuestion.setWrongNum(1 + subQuestion.getWrongNum());
        }

        //答案分析
        List<String> answerList = newExamProcessResult.getUserAnswers().get(index);
        NewQuestion newQuestion = newExamClazzQuestionContext.getNewQuestion();
        NewQuestionsSubContents newQuestionsSubContents = newQuestion.getContent().getSubContents().get(index);
        String answer = pressAnswer(newQuestionsSubContents, answerList);
        studentAnswer.setAnswer(answer);


        //每个空的答案分析
        int i = 0;
        for (boolean kl : baleen) {
            if (i < baleen.size() && i < answerList.size()) {
                String s = answerList.get(i);
                if (StringUtils.isBlank(s))
                    s = "未作答";
                studentAnswer.getAnswerList().add(MapUtils.m(
                        "answer", s,
                        "grasp", kl
                ));
            }
            i++;
        }
    }

    @Override
    public void prepareSubQuestion(NewExamClazzPrepareQuestionContext newExamClazzPrepareQuestionContext) {
        NewQuestionsSubContents newQuestionsSubContents = newExamClazzPrepareQuestionContext.getNewQuestionsSubContents();
        double standardScore = newExamClazzPrepareQuestionContext.getStandardScore();
        int subIndex = newExamClazzPrepareQuestionContext.getSubIndex();
        int index = newExamClazzPrepareQuestionContext.getIndex();
        NewPaperQuestion question = newExamClazzPrepareQuestionContext.getQuestion();
        NewExamDetailH5ToQuestion newExamDetailToQuestion = newExamClazzPrepareQuestionContext.getNewExamDetailToQuestion();
        NewExamQuestionType newExamQuestionType = newExamClazzPrepareQuestionContext.getNewExamQuestionType();
        Subject subject = newExamClazzPrepareQuestionContext.getSubject();
        NewExamDetailH5ToQuestion.SubQuestion subQuestion = new NewExamDetailH5ToQuestion.SubQuestion();
        subQuestion.setAnalysis(newQuestionsSubContents.getAnalysis());
        subQuestion.setStandardScore(standardScore);
        subQuestion.setSubIndex(subIndex);
        newExamDetailToQuestion.getSubQuestions().add(subQuestion);
        subQuestion.setIndex(index);
        subQuestion.setQid(question.getId());
        subQuestion.setType(newExamQuestionType.getKey());
        subQuestion.setNewExamQuestionType(newExamQuestionType);


        //填空题，对于多答案的支持
        List<String> answerList = newQuestionsSubContents.getAnswerList(subject);
        String standardAnswer = pressAnswer(newQuestionsSubContents, answerList);
        subQuestion.setStandardAnswer(standardAnswer);
    }
}
