package com.voxlearning.utopia.service.newexam.impl.template.exam.report;

import com.voxlearning.utopia.service.newexam.api.constant.NewExamQuestionType;
import com.voxlearning.utopia.service.newexam.api.entity.NewExamProcessResult;
import com.voxlearning.utopia.service.newexam.api.mapper.report.personal.NewExamPersonalPrepareQuestionContext;
import com.voxlearning.utopia.service.newexam.api.mapper.report.personal.NewExamPersonalQuestion;
import com.voxlearning.utopia.service.newexam.api.mapper.report.personal.NewExamPersonalQuestionContext;
import com.voxlearning.utopia.service.question.api.entity.NewPaperQuestion;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.api.entity.NewQuestionsSubContents;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * 个人选择题
 */
@Named
public class PersonalQuestionChoiceAnswerHandlerTemplate extends PersonalQuestionAnswerHandlerTemplate {
    @Override
    NewExamQuestionType getNewExamQuestionType() {
        return NewExamQuestionType.Choice;
    }

    @Inject
    private ClazzQuestionAnswerChoiceHandlerTemplate clazzQuestionAnswerChoiceHandlerTemplate;

    @Override
    public void processSubQuestion(NewExamPersonalQuestionContext newExamPersonalQuestionContext) {
        NewExamProcessResult p = newExamPersonalQuestionContext.getNewExamProcessResult();
        int index = newExamPersonalQuestionContext.getIndex();

        if (p.getUserAnswers() == null || p.getUserAnswers().size() <= index) {
            return;
        }
        NewExamPersonalQuestion.NewExamPersonalSubQuestion subQuestion = newExamPersonalQuestionContext.getSubQuestion();
        NewQuestion newQuestion = newExamPersonalQuestionContext.getNewQuestion();
        boolean b = newExamPersonalQuestionContext.isGrasp();
        List<String> answerList = p.getUserAnswers().get(index);
        NewQuestionsSubContents newQuestionsSubContents = newQuestion.getContent().getSubContents().get(index);
        //转义答案
        String answer = clazzQuestionAnswerChoiceHandlerTemplate.pressAnswer(newQuestionsSubContents, answerList);
        subQuestion.setPersonalAnswer(answer);
        subQuestion.setPersonalGrasp(b);
    }

    @Override
    public void prepareSubQuestion(NewExamPersonalPrepareQuestionContext newExamPersonalPrepareQuestionContext) {
        NewQuestionsSubContents newQuestionsSubContents = newExamPersonalPrepareQuestionContext.getNewQuestionsSubContents();
        NewExamPersonalQuestion newExamDetailToQuestion = newExamPersonalPrepareQuestionContext.getNewExamDetailToQuestion();
        NewPaperQuestion question = newExamPersonalPrepareQuestionContext.getQuestion();
        int index = newExamPersonalPrepareQuestionContext.getIndex();
        double standardScore = newExamPersonalPrepareQuestionContext.getStandardScore();
        NewExamQuestionType newExamQuestionType = newExamPersonalPrepareQuestionContext.getNewExamQuestionType();

        NewExamPersonalQuestion.NewExamPersonalSubQuestion subQuestion = new NewExamPersonalQuestion.NewExamPersonalSubQuestion();
        subQuestion.setAnalysis(newQuestionsSubContents.getAnalysis());
        newExamDetailToQuestion.getSubQuestions().add(subQuestion);
        subQuestion.setStandardScore(standardScore);
        subQuestion.setIndex(index);
        subQuestion.setQid(question.getId());
        subQuestion.setNewExamQuestionType(newExamQuestionType);
        subQuestion.setType(newExamQuestionType.getKey());

        //选择题需要设置标准答案
        List<String> answerList = newQuestionsSubContents.getAnswerList();
        String standardAnswer = clazzQuestionAnswerChoiceHandlerTemplate.pressAnswer(newQuestionsSubContents, answerList);
        subQuestion.setStandardAnswer(standardAnswer);


    }
}
