package com.voxlearning.utopia.service.newexam.impl.template.exam.report;

import com.voxlearning.utopia.service.newexam.api.constant.NewExamQuestionType;
import com.voxlearning.utopia.service.newexam.api.entity.NewExamProcessResult;
import com.voxlearning.utopia.service.newexam.api.mapper.report.personal.NewExamPersonalPrepareQuestionContext;
import com.voxlearning.utopia.service.newexam.api.mapper.report.personal.NewExamPersonalQuestion;
import com.voxlearning.utopia.service.newexam.api.mapper.report.personal.NewExamPersonalQuestionContext;
import com.voxlearning.utopia.service.question.api.entity.NewPaperQuestion;
import com.voxlearning.utopia.service.question.api.entity.NewQuestionsSubContents;

import javax.inject.Named;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 个人口语题处理
 */
@Named
public class PersonalQuestionAnswerOralHandlerTemplate extends PersonalQuestionAnswerHandlerTemplate {
    @Override
    NewExamQuestionType getNewExamQuestionType() {
        return NewExamQuestionType.Oral;
    }

    @Override
    public void processSubQuestion(NewExamPersonalQuestionContext newExamPersonalQuestionContext) {
        NewExamProcessResult p = newExamPersonalQuestionContext.getNewExamProcessResult();
        NewExamPersonalQuestion.NewExamPersonalSubQuestion subQuestion = newExamPersonalQuestionContext.getSubQuestion();
        int index = newExamPersonalQuestionContext.getIndex();
        if (p.getOralDetails().size() > index) {
            List<NewExamProcessResult.OralDetail> oralDetails = p.getOralDetails().get(index);
            if (oralDetails != null) {
                List<String> voiceUrlList = oralDetails.stream().map(NewExamProcessResult.OralDetail::getAudio).filter(Objects::nonNull).collect(Collectors.toList());
                subQuestion.setVoiceUrlList(voiceUrlList);
            }
        }
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

        //听力题需要参考答案
        if (newQuestionsSubContents.getOralDict() != null) {
            subQuestion.setReferenceAnswers(newQuestionsSubContents.getOralDict().getAnswers());
        }
    }
}
