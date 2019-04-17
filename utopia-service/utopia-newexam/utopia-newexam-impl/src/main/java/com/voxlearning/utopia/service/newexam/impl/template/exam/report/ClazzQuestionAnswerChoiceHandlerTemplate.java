package com.voxlearning.utopia.service.newexam.impl.template.exam.report;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.service.newexam.api.constant.NewExamQuestionType;
import com.voxlearning.utopia.service.newexam.api.entity.NewExamProcessResult;
import com.voxlearning.utopia.service.newexam.api.mapper.report.clazz.NewExamClazzPrepareQuestionContext;
import com.voxlearning.utopia.service.newexam.api.mapper.report.clazz.NewExamClazzQuestionContext;
import com.voxlearning.utopia.service.newexam.api.mapper.report.clazz.NewExamDetailH5ToQuestion;
import com.voxlearning.utopia.service.question.api.entity.NewPaperQuestion;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.api.entity.NewQuestionOption;
import com.voxlearning.utopia.service.question.api.entity.NewQuestionsSubContents;

import javax.inject.Named;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.voxlearning.utopia.service.newexam.api.constant.NewExamQuestionType.Choice;

@Named
public class ClazzQuestionAnswerChoiceHandlerTemplate extends ClazzQuestionAnswerHandlerTemplate {
    @Override
    NewExamQuestionType getNewExamQuestionType() {
        return NewExamQuestionType.Choice;
    }

    @Override
    public void processSubQuestion(NewExamClazzQuestionContext newExamClazzQuestionContext) {
        NewExamProcessResult newExamProcessResult = newExamClazzQuestionContext.getNewExamProcessResult();
        int index = newExamClazzQuestionContext.getIndex();

        if (newExamProcessResult.getUserAnswers() == null ||
                newExamProcessResult.getUserAnswers().size() <= index) {
            return;
        }
        NewExamDetailH5ToQuestion.SubQuestion subQuestion = newExamClazzQuestionContext.getSubQuestion();
        NewQuestion newQuestion = newExamClazzQuestionContext.getNewQuestion();
        NewExamDetailH5ToQuestion.StudentAnswer studentAnswer = newExamClazzQuestionContext.getStudentAnswer();
        boolean b = newExamClazzQuestionContext.isGrasp();
        //是否掌握
        if (b) {
            subQuestion.setRightNum(1 + subQuestion.getRightNum());
        } else {
            subQuestion.setWrongNum(1 + subQuestion.getWrongNum());
        }
        //答案分析
        List<String> answerList = newExamProcessResult.getUserAnswers().get(index);
        NewQuestionsSubContents newQuestionsSubContents = newQuestion.getContent().getSubContents().get(index);
        String answer = pressAnswer(newQuestionsSubContents, answerList);
        studentAnswer.setAnswer(answer);
        Map<String, NewExamDetailH5ToQuestion.Answer> xuanZeAnswerMap = subQuestion.getXuanZeAnswerMap();
        if (xuanZeAnswerMap.containsKey(answer)) {
            NewExamDetailH5ToQuestion.Answer answer1 = xuanZeAnswerMap.get(answer);
            answer1.getStudents().add(studentAnswer);
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
        NewExamDetailH5ToQuestion.SubQuestion subQuestion = new NewExamDetailH5ToQuestion.SubQuestion();
        subQuestion.setAnalysis(newQuestionsSubContents.getAnalysis());
        subQuestion.setStandardScore(standardScore);
        subQuestion.setSubIndex(subIndex);
        newExamDetailToQuestion.getSubQuestions().add(subQuestion);
        subQuestion.setIndex(index);
        subQuestion.setQid(question.getId());
        subQuestion.setType(newExamQuestionType.getKey());
        subQuestion.setNewExamQuestionType(newExamQuestionType);


        //选择题需要把各种答案类型
        List<String> answerList = newQuestionsSubContents.getAnswerList();
        String standardAnswer = pressAnswer(newQuestionsSubContents, answerList);
        List<NewQuestionOption> options = newQuestionsSubContents.getOptions();
        if (CollectionUtils.isNotEmpty(options)) {
            int _index = 0;
            for (NewQuestionOption ignored : options) {
                String s1 = pressAnswer(newQuestionsSubContents, Collections.singletonList(_index + ""));
                NewExamDetailH5ToQuestion.Answer answer = new NewExamDetailH5ToQuestion.Answer();
                answer.setAnswer(s1);
                answer.setGrasp(Objects.equals(s1, standardAnswer));
                subQuestion.getXuanZeAnswer().add(answer);
                subQuestion.getXuanZeAnswerMap().put(s1, answer);
                _index++;
            }
        }
        subQuestion.setStandardAnswer(standardAnswer);
    }
}
