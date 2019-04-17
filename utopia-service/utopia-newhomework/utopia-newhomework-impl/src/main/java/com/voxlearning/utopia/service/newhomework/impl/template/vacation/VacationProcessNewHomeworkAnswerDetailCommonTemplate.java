package com.voxlearning.utopia.service.newhomework.impl.template.vacation;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestion;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestionFile;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.vacationhomework.ReportRateContext;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkQuestionFileHelper;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.api.entity.NewQuestionsSubContents;
import com.voxlearning.utopia.service.question.api.entity.TestMethod;

import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Named
public class VacationProcessNewHomeworkAnswerDetailCommonTemplate extends VacationProcessNewHomeworkAnswerDetailTemplate {
    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.COMMON;
    }

    @Override
    public void processNewHomeworkAnswerDetailPersonal(ReportRateContext reportRateContext) {
        VacationHomework vacationHomework = reportRateContext.getNewHomework();
        VacationHomeworkResult vacationHomeworkResult = reportRateContext.getNewHomeworkResult();
        ObjectiveConfigType type = reportRateContext.getType();
        // 所有应该做的题
        List<String> qids = vacationHomework.findQuestionIds(type, true);
        // <已做的题,已做的题结果>
        if (!vacationHomeworkResult.getPractices().containsKey(type)) {
            return;
        }
        Map<String, String> processAnswers = vacationHomeworkResult.getPractices().get(type).processAnswers();
        if (MapUtils.isEmpty(processAnswers)) {
            return;
        }
        Map<String, VacationHomeworkProcessResult> processResultMap = processAnswers.values()
                .stream()
                .filter(reportRateContext.getNewHomeworkProcessResultMap()::containsKey)
                .collect(Collectors
                        .toMap(Function.identity(), reportRateContext.getNewHomeworkProcessResultMap()::get));

        Map<String, NewHomeworkQuestion> newHomeworkQuestionMap = new HashMap<>();
        Map<String, NewQuestion> newQuestionMap;
        if (ObjectiveConfigType.READ_RECITE.equals(reportRateContext.getType())) {
            List<NewHomeworkQuestion> newHomeworkQuestions = reportRateContext.getNewHomework().findNewHomeworkQuestions(reportRateContext.getType());
            newHomeworkQuestionMap = newHomeworkQuestions
                    .stream()
                    .collect(Collectors
                            .toMap(NewHomeworkQuestion::getQuestionId, Function.identity()));
            newQuestionMap = doHomeworkProcessor.initReadReciteDate(newHomeworkQuestions, reportRateContext.getType(), false);
        } else {
            newQuestionMap = qids
                    .stream()
                    .filter(reportRateContext.getAllNewQuestionMap()::containsKey)
                    .collect(Collectors
                            .toMap(Function.identity(), reportRateContext.getAllNewQuestionMap()::get));
        }
        Map<String, String> finalProcessAnswers = processAnswers;
        Map<String, NewQuestion> finalNewQuestionMap = newQuestionMap;
        Map<String, NewHomeworkQuestion> finalNewHomeworkQuestionMap = newHomeworkQuestionMap;
        Map<String, TestMethod> allTestMethodMap = new HashMap<>();
        Map<String, String> qidToTestMethodId = new HashMap<>();

        if (reportRateContext.getType() == ObjectiveConfigType.BASIC_KNOWLEDGE) {
            qidToTestMethodId = newQuestionMap.values()
                    .stream()
                    .filter(q -> CollectionUtils.isNotEmpty(q.testMethodList()))
                    .collect(Collectors.toMap(NewQuestion::getId, q -> q.testMethodList().get(0)));
            allTestMethodMap = testMethodLoaderClient.loadTestMethodIncludeDisabled(qidToTestMethodId.values());
        }

        Map<String, String> finalQidToTestMethodId = qidToTestMethodId;
        Map<String, TestMethod> finalAllTestMethodMap = allTestMethodMap;
        List<Map<String, Object>> questionAnswerList;
        questionAnswerList = qids.stream()
                .filter(processAnswers::containsKey)
                .map(qid -> {
                    String testMethodName = "";
                    if (finalQidToTestMethodId.containsKey(qid) && finalAllTestMethodMap.containsKey(finalQidToTestMethodId.get(qid))) {
                        TestMethod testMethod = finalAllTestMethodMap.get(finalQidToTestMethodId.get(qid));
                        testMethodName = testMethod != null ? SafeConverter.toString(testMethod.getName()) : "";
                    }
                    VacationHomeworkProcessResult tempResult = processResultMap.get(finalProcessAnswers.get(qid));
                    NewQuestion question = finalNewQuestionMap.getOrDefault(qid, null);
                    if (question == null || question.getContent() == null || question.getContent().getSubContents() == null) {
                        return null;
                    }
                    List<NewQuestionsSubContents> subContents = question.getContent().getSubContents();
                    List<List<String>> standardAnswers = subContents
                            .stream()
                            .map(o -> o.getAnswerList(reportRateContext.getNewHomework().getSubject()))
                            .collect(Collectors.toList());

                    return MapUtils.m("qid", qid,
                            "testMethodName", testMethodName,
                            "contentType", reportRateContext.getContentTypeMap().get(question.getContentTypeId()) != null ?
                                    reportRateContext.getContentTypeMap().get(question.getContentTypeId()).getName() :
                                    "无题型",
                            "difficulty", question.getDifficultyInt(),
                            "standardAnswers", tempResult != null ? NewHomeworkUtils.pressAnswer(subContents, standardAnswers) : "",
                            "userAnswers", tempResult != null ? NewHomeworkUtils.pressAnswer(subContents, tempResult.getUserAnswers()) : "",
                            "review", tempResult != null ? tempResult.getReview() : null,
                            "correction", tempResult != null ? tempResult.getCorrection() : null,
                            "correct_des", (tempResult != null && tempResult.getCorrection() != null) ? tempResult.getCorrection().getDescription() : "",
                            "fileType", (tempResult != null && CollectionUtils.isNotEmpty(tempResult.getFiles())) ?
                                    tempResult
                                            .getFiles()
                                            .stream()
                                            .flatMap(Collection::stream)
                                            .map(NewHomeworkQuestionFile::getFileType)
                                            .collect(Collectors.toSet()) :
                                    Collections.emptyList(),
                            "fileUrl", (tempResult != null && CollectionUtils.isNotEmpty(tempResult.getFiles())) ?
                                    tempResult
                                            .getFiles()
                                            .stream()
                                            .flatMap(Collection::stream)
                                            .map(NewHomeworkQuestionFileHelper::getFileUrl)
                                            .collect(Collectors.toList()) :
                                    Collections.emptyList(),
                            "articleName", finalNewQuestionMap.get(qid) != null ?
                                    finalNewQuestionMap.get(qid).getArticleName() :
                                    "",
                            "paragraphCName", finalNewQuestionMap.get(qid) != null ?
                                    finalNewQuestionMap.get(qid).getParagraph() :
                                    "",
                            "answerWay", finalNewHomeworkQuestionMap.get(qid) != null ?
                                    finalNewHomeworkQuestionMap.get(qid).processAnswerWay() :
                                    ""
                    );
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(questionAnswerList)) {
            reportRateContext.getResultMap().put(reportRateContext.getType(), questionAnswerList);
        }
    }
}
