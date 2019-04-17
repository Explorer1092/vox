package com.voxlearning.utopia.service.newexam.impl.template.exam.report;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.service.newexam.api.constant.ExamReportAnswerStatType;
import com.voxlearning.utopia.service.newexam.api.entity.NewExamProcessResult;
import com.voxlearning.utopia.service.newexam.api.mapper.report.NewExamSingleSubQuestion;
import com.voxlearning.utopia.service.newexam.api.mapper.report.clazz.NewExamSinglePrepareQuestionContext;
import com.voxlearning.utopia.service.newexam.api.mapper.report.clazz.NewExamSingleQuestionContext;
import com.voxlearning.utopia.service.newexam.impl.loader.NewExamReportLoaderImpl;
import com.voxlearning.utopia.service.question.api.entity.NewQuestionsSubContents;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Named
public class SingleQuestionAnswerTop3HandlerTemplate extends SingleQuestionAnswerHandlerTemplate {
    @Override
    ExamReportAnswerStatType getExamReportAnswerStatType() {
        return ExamReportAnswerStatType.TOP_3;
    }

    @Override
    public void statAnswerTypeDetail(NewExamSingleQuestionContext context) {
        NewExamProcessResult processResult = context.getNewExamProcessResult();
        int subIndex = context.getSubIndex();

        NewExamSingleSubQuestion singleSubQuestion = context.getSingleSubQuestion();
        List<NewExamSingleSubQuestion.StudentAnswer> answerStudentsList = singleSubQuestion.getAnswerStudentsList();
        Map<List<String>, NewExamSingleSubQuestion.StudentAnswer> studentAnswerMap = answerStudentsList
                .stream()
                .collect(Collectors.toMap(NewExamSingleSubQuestion.StudentAnswer::getViews, Function.identity(), (o1, o2) -> o2));

        List<List<String>> top3Answer = context.getTop3Answer();
        NewExamSingleSubQuestion.StudentAnswer studentAnswer = null;
        NewExamSingleSubQuestion.Student student = new NewExamSingleSubQuestion.Student(context.getUserId(), context.getUserName());
        if (processResult == null || CollectionUtils.isEmpty(processResult.getUserAnswers()) || processResult.getUserAnswers().size() <= subIndex
                || processResult.getUserAnswers().get(subIndex).stream().allMatch(""::equals)
                || CollectionUtils.isEmpty(processResult.getSubGrasp()) || processResult.getSubGrasp().size() <= subIndex) {
            studentAnswer = studentAnswerMap.get(Collections.singletonList("未作答"));
        } else {
            List<String> userAnswer = processResult.getUserAnswers().get(subIndex);
            List<Boolean> subMaster = processResult.getSubGrasp().get(subIndex);
            NewQuestionsSubContents newQuestionsSubContents = context.getNewQuestionsSubContents();
            boolean subMasterGrasp = newExamReportLoader.isSubQuestionGrasp(newQuestionsSubContents, subMaster);
            if (top3Answer.contains(userAnswer)) {
                studentAnswer = studentAnswerMap.get(transformSubAnswer(userAnswer, newQuestionsSubContents));
                studentAnswer.setMaster(subMasterGrasp ? "RIGHT" : "WRONG");
                studentAnswer.setSubMaster(subMaster);
            } else {
                //其他分组
                studentAnswer = studentAnswerMap.get(Collections.singletonList("其他"));
                student.setAnswer(transformSubAnswer(userAnswer, newQuestionsSubContents));
                student.setMaster(subMasterGrasp);
                student.setSubMaster(subMaster);
            }
        }
        studentAnswer.getStudents().add(student);
    }

    @Override
    public void prepareAnswerType(NewExamSinglePrepareQuestionContext context) {
        NewExamSingleSubQuestion singleSubQuestion = context.getSingleSubQuestion();
        singleSubQuestion.setAnswerStatType(context.getAnswerStatType().name());

        Map<String, NewExamProcessResult> newExamProcessResultMap = context.getNewExamProcessResultMap();
        List<List<String>> answersGroup = newExamProcessResultMap.values()
                .stream()
                .filter(p -> CollectionUtils.isNotEmpty(p.getUserAnswers()) && p.getUserAnswers().size() >= context.getSubIndex()
                        && !p.getUserAnswers().get(context.getSubIndex()).stream().allMatch(""::equals))
                .collect(Collectors.groupingBy(p -> p.getUserAnswers().get(context.getSubIndex()), Collectors.counting()))
                .entrySet()
                .stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        List<List<String>> top3Answer = answersGroup.size() > 3 ? answersGroup.subList(0, 3) : answersGroup;
        context.setTop3Answer(top3Answer);
        List<NewExamSingleSubQuestion.StudentAnswer> answerStudentsList = new LinkedList<>();
        if (CollectionUtils.isNotEmpty(top3Answer)) {
            for (List<String> showAnswer : top3Answer) {
                NewExamSingleSubQuestion.StudentAnswer studentAnswer = new NewExamSingleSubQuestion.StudentAnswer();
                studentAnswer.setViews(transformSubAnswer(showAnswer, context.getNewQuestionsSubContents()));
                studentAnswer.setStudentDetailShowAnswer(Boolean.FALSE);
                answerStudentsList.add(studentAnswer);
            }
        }
        NewExamSingleSubQuestion.StudentAnswer others = new NewExamSingleSubQuestion.StudentAnswer();
        others.setViews(Collections.singletonList("其他"));
        others.setMaster("OTHER"); //前端标注为红色, 并不一定是未掌握
        others.setStudentDetailShowAnswer(Boolean.TRUE);
        others.setSubMaster(Collections.singletonList(Boolean.TRUE));
        answerStudentsList.add(others);

        NewExamSingleSubQuestion.StudentAnswer notDoAnswer = new NewExamSingleSubQuestion.StudentAnswer();
        notDoAnswer.setViews(Collections.singletonList("未作答"));
        notDoAnswer.setStudentDetailShowAnswer(Boolean.FALSE);
        notDoAnswer.setMaster("NOT_ANSWER");
        notDoAnswer.setSubMaster(Collections.singletonList(Boolean.TRUE));
        answerStudentsList.add(notDoAnswer);
        singleSubQuestion.setAnswerStudentsList(answerStudentsList);
    }

    @Inject private NewExamReportLoaderImpl newExamReportLoader;
}
