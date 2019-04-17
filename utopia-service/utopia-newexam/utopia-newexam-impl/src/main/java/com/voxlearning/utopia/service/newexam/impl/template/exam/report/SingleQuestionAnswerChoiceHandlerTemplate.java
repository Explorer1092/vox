package com.voxlearning.utopia.service.newexam.impl.template.exam.report;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.service.newexam.api.constant.ExamReportAnswerStatType;
import com.voxlearning.utopia.service.newexam.api.entity.NewExamProcessResult;
import com.voxlearning.utopia.service.newexam.api.mapper.report.NewExamSingleSubQuestion;
import com.voxlearning.utopia.service.newexam.api.mapper.report.clazz.NewExamSinglePrepareQuestionContext;
import com.voxlearning.utopia.service.newexam.api.mapper.report.clazz.NewExamSingleQuestionContext;
import com.voxlearning.utopia.service.question.api.entity.NewQuestionOption;
import com.voxlearning.utopia.service.question.api.entity.NewQuestionsSubContents;

import javax.inject.Named;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Named
public class SingleQuestionAnswerChoiceHandlerTemplate extends SingleQuestionAnswerHandlerTemplate {
    @Override
    ExamReportAnswerStatType getExamReportAnswerStatType() {
        return ExamReportAnswerStatType.CHOICE;
    }


    @Override
    public void statAnswerTypeDetail(NewExamSingleQuestionContext context) {
        int subIndex = context.getSubIndex();
        NewExamProcessResult processResult = context.getNewExamProcessResult();
        NewExamSingleSubQuestion singleSubQuestion = context.getSingleSubQuestion();
        List<NewExamSingleSubQuestion.StudentAnswer> answerStudentsList = singleSubQuestion.getAnswerStudentsList();
        Map<List<String>, NewExamSingleSubQuestion.StudentAnswer> studentAnswerMap = answerStudentsList
                .stream()
                .collect(Collectors.toMap(NewExamSingleSubQuestion.StudentAnswer::getViews, Function.identity()));

        NewExamSingleSubQuestion.StudentAnswer studentAnswer = null;
        if (processResult == null || CollectionUtils.isEmpty(processResult.getUserAnswers()) || processResult.getUserAnswers().size() <= subIndex
                || processResult.getUserAnswers().get(subIndex).stream().allMatch(""::equals)
                || CollectionUtils.isEmpty(processResult.getSubGrasp()) || processResult.getSubGrasp().size() <= subIndex) {
            studentAnswer = studentAnswerMap.get(Collections.singletonList("未作答"));
        } else {
            //答案转成ABC形式
            List<String> userAnswer = processResult.getUserAnswers().get(subIndex);
            studentAnswer = studentAnswerMap.get(transformSubAnswer(userAnswer, context.getNewQuestionsSubContents()));
            studentAnswer.setSubMaster(processResult.getSubGrasp().get(subIndex));
        }
        studentAnswer.getStudents().add(new NewExamSingleSubQuestion.Student(context.getUserId(), context.getUserName()));
    }

    @Override
    public void prepareAnswerType(NewExamSinglePrepareQuestionContext context) {
        NewQuestionsSubContents newQuestionsSubContents = context.getNewQuestionsSubContents();
        NewExamSingleSubQuestion singleSubQuestion = context.getSingleSubQuestion();
        singleSubQuestion.setAnswerStatType(context.getAnswerStatType().name());
        List<String> standAnswer = newQuestionsSubContents.getAnswerList();
        //选择题需要各个答案类型
        List<NewQuestionOption> options = newQuestionsSubContents.getOptions();
        List<NewExamSingleSubQuestion.StudentAnswer> answerStudentsList = new LinkedList<>();
        if (CollectionUtils.isNotEmpty(options)) {
            for (int i = 0; i < options.size(); i++) {
                NewExamSingleSubQuestion.StudentAnswer studentAnswer = new NewExamSingleSubQuestion.StudentAnswer();
                studentAnswer.setViews(transformSubAnswer(Collections.singletonList(String.valueOf(i)), newQuestionsSubContents));
                studentAnswer.setMaster(standAnswer.get(0).equals(String.valueOf(i)) ? "RIGHT" : "WRONG");
                studentAnswer.setStudentDetailShowAnswer(Boolean.FALSE);
                answerStudentsList.add(studentAnswer);
            }
        }
        NewExamSingleSubQuestion.StudentAnswer studentAnswer = new NewExamSingleSubQuestion.StudentAnswer();
        studentAnswer.setViews(Collections.singletonList("未作答"));
        studentAnswer.setStudentDetailShowAnswer(Boolean.FALSE);
        answerStudentsList.add(studentAnswer);
        studentAnswer.setMaster("NOT_ANSWER");
        studentAnswer.setSubMaster(Collections.singletonList(Boolean.TRUE));//未作答不标红
        singleSubQuestion.setAnswerStudentsList(answerStudentsList);
    }
}
