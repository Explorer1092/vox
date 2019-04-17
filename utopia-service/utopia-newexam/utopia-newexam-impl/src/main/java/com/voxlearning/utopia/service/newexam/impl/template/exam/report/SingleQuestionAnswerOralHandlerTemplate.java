package com.voxlearning.utopia.service.newexam.impl.template.exam.report;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.service.newexam.api.constant.ExamReportAnswerStatType;
import com.voxlearning.utopia.service.newexam.api.entity.NewExamProcessResult;
import com.voxlearning.utopia.service.newexam.api.mapper.report.NewExamSingleSubQuestion;
import com.voxlearning.utopia.service.newexam.api.mapper.report.clazz.NewExamSinglePrepareQuestionContext;
import com.voxlearning.utopia.service.newexam.api.mapper.report.clazz.NewExamSingleQuestionContext;
import com.voxlearning.utopia.service.question.api.entity.NewQuestionsSubContents;

import javax.inject.Named;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Named
public class SingleQuestionAnswerOralHandlerTemplate extends SingleQuestionAnswerHandlerTemplate {
    @Override
    ExamReportAnswerStatType getExamReportAnswerStatType() {
        return ExamReportAnswerStatType.ORAL;
    }

    @Override
    public void statAnswerTypeDetail(NewExamSingleQuestionContext context) {
        NewExamProcessResult processResult = context.getNewExamProcessResult();
        int subIndex = context.getSubIndex();
        if (processResult == null || CollectionUtils.isEmpty(processResult.getOralDetails()) || processResult.getOralDetails().size() < subIndex) {
            return;
        }
        List<NewExamProcessResult.OralDetail> oralDetails = processResult.getOralDetails().get(subIndex);
        NewExamSingleSubQuestion.OralStudent student = new NewExamSingleSubQuestion.OralStudent();
        student.setUserId(context.getUserId());
        student.setUserName(context.getUserName());
        student.setScore(context.getScore());
        NewExamSingleSubQuestion singleSubQuestion = context.getSingleSubQuestion();
        if (oralDetails != null) {
            List<String> voiceUrlList = oralDetails.stream()
                    .map(NewExamProcessResult.OralDetail::getAudio)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            student.setVoiceUrls(voiceUrlList);
        }
        singleSubQuestion.getStudents().add(student);
    }

    @Override
    public void prepareAnswerType(NewExamSinglePrepareQuestionContext context) {
        NewExamSingleSubQuestion singleSubQuestion = context.getSingleSubQuestion();
        NewQuestionsSubContents newQuestionsSubContents = context.getNewQuestionsSubContents();
        singleSubQuestion.setAnswerStatType(context.getAnswerStatType().name());

        //口语题参考答案
        if (newQuestionsSubContents.getOralDict() != null) {
            singleSubQuestion.setKouYuReferenceAnswers(newQuestionsSubContents.getOralDict().getAnswers());
        }

    }
}
