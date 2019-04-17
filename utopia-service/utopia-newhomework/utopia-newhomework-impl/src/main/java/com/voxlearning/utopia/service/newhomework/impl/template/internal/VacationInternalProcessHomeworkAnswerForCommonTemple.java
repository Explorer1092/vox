package com.voxlearning.utopia.service.newhomework.impl.template.internal;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestion;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestionFile;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.newhomework.impl.service.NewHomeworkReportServiceImpl;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.student.work.DoHomeworkProcessor;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkQuestionFileHelper;
import com.voxlearning.utopia.service.newhomework.impl.template.VacationInternalProcessHomeworkAnswerTemple;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.NewContentType;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.api.entity.NewQuestionsSubContents;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Named
public class VacationInternalProcessHomeworkAnswerForCommonTemple extends VacationInternalProcessHomeworkAnswerTemple {
    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.COMMON;
    }

    @Inject
    private NewHomeworkReportServiceImpl newHomeworkReportService;

    @Override
    public void internalProcessHomeworkAnswer(Map<ObjectiveConfigType, Object> resultMap, Map<String, VacationHomeworkProcessResult> allProcessResultMap, Map<Integer, NewContentType> contentTypeMap, Map<String, NewQuestion> allQuestionMap, VacationHomework vacationHomework, VacationHomeworkResult vacationHomeworkResult, ObjectiveConfigType type) {
        List<Map<String, Object>> questionAnswerList = new LinkedList<>();
        // 所有应该做的题
        List<String> qids = vacationHomework.findQuestionIds(type, true);
        Map<String, NewQuestion> newQuestionMap;
        // <已做的题,已做的题结果>
        Map<String, String> answerMap = null;
        if (vacationHomeworkResult.getPractices().containsKey(type)) {
            answerMap = vacationHomeworkResult.getPractices().get(type).processAnswers();
        }
        if (MapUtils.isEmpty(answerMap)) {
            return;
        }
        Collection<String> processIds = answerMap.values();
        Map<String, VacationHomeworkProcessResult> processResultMap = processIds
                .stream()
                .filter(allProcessResultMap::containsKey)
                .collect(Collectors
                        .toMap(Function.identity(), allProcessResultMap::get));
        newQuestionMap = qids
                .stream()
                .filter(allQuestionMap::containsKey)
                .collect(Collectors
                        .toMap(Function.identity(), allQuestionMap::get));
        for (String qid : qids) {
            if (!answerMap.containsKey(qid))
                continue;
            String processId = answerMap.get(qid);
            if (!processResultMap.containsKey(processId))
                continue;
            if (!allQuestionMap.containsKey(qid))
                continue;
            NewQuestion question = allQuestionMap.get(qid);
            if (question == null || question.getContent() == null || question.getContent().getSubContents() == null)
                continue;
            VacationHomeworkProcessResult vacationHomeworkProcessResult = processResultMap.get(processId);
            List<NewQuestionsSubContents> subContents = question.getContent().getSubContents();
            List<List<String>> standardAnswers = subContents
                    .stream()
                    .map(o -> o.getAnswerList(vacationHomework.getSubject()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            Map<String, Object> m = MapUtils.m("qid", qid,
                    "contentType", contentTypeMap.get(question.getContentTypeId()) != null ?
                            contentTypeMap.get(question.getContentTypeId()).getName() :
                            "无题型",
                    "difficulty", question.getDifficultyInt(),
                    "standardAnswers", NewHomeworkUtils.pressAnswer(subContents, standardAnswers),
                    "userAnswers", NewHomeworkUtils.pressAnswer(subContents, vacationHomeworkProcessResult.getUserAnswers()),

                    "fileType", (CollectionUtils.isNotEmpty(vacationHomeworkProcessResult.getFiles())) ?
                            vacationHomeworkProcessResult.getFiles()
                                    .stream()
                                    .flatMap(Collection::stream)
                                    .map(NewHomeworkQuestionFile::getFileType)
                                    .collect(Collectors.toSet()) :
                            Collections.emptyList(),
                    "fileUrl", (CollectionUtils.isNotEmpty(vacationHomeworkProcessResult.getFiles())) ?
                            vacationHomeworkProcessResult.getFiles()
                                    .stream()
                                    .flatMap(Collection::stream)
                                    .map(NewHomeworkQuestionFileHelper::getFileUrl)
                                    .collect(Collectors.toList()) :
                            Collections.emptyList()
            );
            questionAnswerList.add(m);
        }
        if (CollectionUtils.isNotEmpty(questionAnswerList)) {
            resultMap.put(type, questionAnswerList);
        }
    }
}
