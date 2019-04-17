package com.voxlearning.utopia.service.newexam.impl.template.exam.report;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.newexam.api.constant.ExamReportAnswerStatType;
import com.voxlearning.utopia.service.newexam.api.entity.NewExamProcessResult;
import com.voxlearning.utopia.service.newexam.api.mapper.report.NewExamSingleSubQuestion;
import com.voxlearning.utopia.service.newexam.api.mapper.report.clazz.NewExamSinglePrepareQuestionContext;
import com.voxlearning.utopia.service.newexam.api.mapper.report.clazz.NewExamSingleQuestionContext;

import javax.inject.Named;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Named
public class SingleQuestionAnswerScoresHandlerTemplate extends SingleQuestionAnswerHandlerTemplate {
    @Override
    ExamReportAnswerStatType getExamReportAnswerStatType() {
        return ExamReportAnswerStatType.SCORES;
    }
    @Override
    public void statAnswerTypeDetail(NewExamSingleQuestionContext context) {
        NewExamProcessResult processResult = context.getNewExamProcessResult();
        int subIndex = context.getSubIndex();

        NewExamSingleSubQuestion singleSubQuestion = context.getSingleSubQuestion();
        List<NewExamSingleSubQuestion.StudentAnswer> answerStudentsList = singleSubQuestion.getAnswerStudentsList();
        Map<List<String>, NewExamSingleSubQuestion.StudentAnswer> studentAnswerMap = answerStudentsList
                .stream()
                .collect(Collectors.toMap(NewExamSingleSubQuestion.StudentAnswer::getViews, Function.identity()));

        NewExamSingleSubQuestion.StudentAnswer studentAnswer = null;
        NewExamSingleSubQuestion.Student student = new NewExamSingleSubQuestion.Student(context.getUserId(), context.getUserName());
        if (processResult == null || CollectionUtils.isEmpty(processResult.getUserAnswers()) || processResult.getUserAnswers().size() <= subIndex
                || processResult.getUserAnswers().get(subIndex).stream().allMatch(""::equals)
                || CollectionUtils.isEmpty(processResult.getSubGrasp()) || processResult.getSubGrasp().size() <= subIndex
                || CollectionUtils.isEmpty(processResult.processSubScore()) || processResult.processSubScore().size() <= subIndex) {
            studentAnswer = studentAnswerMap.get(Collections.singletonList("未作答"));
        } else {
            List<String> userAnswer = processResult.getUserAnswers().get(subIndex);
            List<Boolean> subMaster = processResult.getSubGrasp().get(subIndex);
            student.setAnswer(transformSubAnswer(userAnswer, context.getNewQuestionsSubContents()));
            boolean subMasterGrasp = subMaster.stream().allMatch(SafeConverter::toBoolean);
            student.setMaster(subMasterGrasp);
            student.setSubMaster(subMaster);

            if (processResult.processSubScore().get(subIndex).equals(singleSubQuestion.getStandardScore())) {
                studentAnswer = studentAnswerMap.get(Collections.singletonList("得满分"));
            } else if (processResult.processSubScore().get(subIndex) > 0) {
                studentAnswer = studentAnswerMap.get(Collections.singletonList("有扣分"));
            } else {
                studentAnswer = studentAnswerMap.get(Collections.singletonList("未得分"));
            }
        }
        studentAnswer.getStudents().add(student);
    }

    @Override
    public void prepareAnswerType(NewExamSinglePrepareQuestionContext context) {
        NewExamSingleSubQuestion singleSubQuestion = context.getSingleSubQuestion();
        singleSubQuestion.setAnswerStatType(context.getAnswerStatType().name());

        List<NewExamSingleSubQuestion.StudentAnswer> answerStudentsList = new LinkedList<>();
        NewExamSingleSubQuestion.StudentAnswer grasp = new NewExamSingleSubQuestion.StudentAnswer();
        grasp.setViews(Collections.singletonList("得满分"));
        grasp.setMaster("RIGHT");
        grasp.setStudentDetailShowAnswer(Boolean.TRUE);
        grasp.setSubMaster(Collections.singletonList(Boolean.TRUE));
        answerStudentsList.add(grasp);

        NewExamSingleSubQuestion.StudentAnswer deduct = new NewExamSingleSubQuestion.StudentAnswer();
        deduct.setViews(Collections.singletonList("有扣分"));
        deduct.setMaster("WRONG");
        deduct.setStudentDetailShowAnswer(Boolean.TRUE);
        deduct.setSubMaster(Collections.singletonList(Boolean.TRUE));
        answerStudentsList.add(deduct);

        NewExamSingleSubQuestion.StudentAnswer zero = new NewExamSingleSubQuestion.StudentAnswer();
        zero.setViews(Collections.singletonList("未得分"));
        zero.setMaster("WRONG");
        zero.setStudentDetailShowAnswer(Boolean.TRUE);
        zero.setSubMaster(Collections.singletonList(Boolean.FALSE));
        answerStudentsList.add(zero);

        NewExamSingleSubQuestion.StudentAnswer notDoAnswer = new NewExamSingleSubQuestion.StudentAnswer();
        notDoAnswer.setViews(Collections.singletonList("未作答"));
        notDoAnswer.setStudentDetailShowAnswer(Boolean.FALSE);
        notDoAnswer.setMaster("NOT_ANSWER");
        notDoAnswer.setSubMaster(Collections.singletonList(Boolean.TRUE));
        answerStudentsList.add(notDoAnswer);
        singleSubQuestion.setAnswerStudentsList(answerStudentsList);
    }
}
