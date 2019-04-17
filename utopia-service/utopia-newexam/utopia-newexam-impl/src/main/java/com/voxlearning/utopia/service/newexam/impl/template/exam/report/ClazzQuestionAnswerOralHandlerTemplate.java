package com.voxlearning.utopia.service.newexam.impl.template.exam.report;

import com.voxlearning.utopia.service.newexam.api.constant.NewExamQuestionType;
import com.voxlearning.utopia.service.newexam.api.entity.NewExamProcessResult;
import com.voxlearning.utopia.service.newexam.api.mapper.report.clazz.NewExamClazzPrepareQuestionContext;
import com.voxlearning.utopia.service.newexam.api.mapper.report.clazz.NewExamClazzQuestionContext;
import com.voxlearning.utopia.service.newexam.api.mapper.report.clazz.NewExamDetailH5ToQuestion;
import com.voxlearning.utopia.service.question.api.entity.NewPaperQuestion;
import com.voxlearning.utopia.service.question.api.entity.NewQuestionsSubContents;

import javax.inject.Named;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.newexam.api.constant.NewExamQuestionType.Oral;

@Named
public class ClazzQuestionAnswerOralHandlerTemplate extends ClazzQuestionAnswerHandlerTemplate {
    @Override
    NewExamQuestionType getNewExamQuestionType() {
        return NewExamQuestionType.Oral;
    }

    @Override
    public void processSubQuestion(NewExamClazzQuestionContext newExamClazzQuestionContext) {
        int index = newExamClazzQuestionContext.getIndex();
        NewExamDetailH5ToQuestion.StudentAnswer studentAnswer = newExamClazzQuestionContext.getStudentAnswer();
        NewExamProcessResult newExamProcessResult = newExamClazzQuestionContext.getNewExamProcessResult();
        NewExamDetailH5ToQuestion.SubQuestion subQuestion = newExamClazzQuestionContext.getSubQuestion();
        //口语地址
        if (newExamProcessResult.getOralDetails().size() > index) {
            List<NewExamProcessResult.OralDetail> oralDetails = newExamProcessResult.getOralDetails().get(index);
            if (oralDetails != null) {
                List<String> voiceUrlList = oralDetails.stream().map(NewExamProcessResult.OralDetail::getAudio).filter(Objects::nonNull).collect(Collectors.toList());
                studentAnswer.setVoiceUrls(voiceUrlList);
            }
        }
        subQuestion.getKouYuAnswer().add(studentAnswer);
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


        //参考答案
        if (newQuestionsSubContents.getOralDict() != null) {
            subQuestion.setKouYuReferenceAnswers(newQuestionsSubContents.getOralDict().getAnswers());
        }

    }

}
