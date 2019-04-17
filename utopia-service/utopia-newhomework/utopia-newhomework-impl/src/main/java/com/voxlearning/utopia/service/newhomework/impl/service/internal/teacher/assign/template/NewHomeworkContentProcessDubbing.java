package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assign.template;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.context.AssignHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkApp;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkBookInfo;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestion;
import com.voxlearning.utopia.service.newhomework.api.mapper.BookInfoMapper;
import com.voxlearning.utopia.service.question.api.constant.NewHomeworkContentProcessTemp;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.Dubbing;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author guoqiang.li
 * @since 2017/10/31
 */
@Named
public class NewHomeworkContentProcessDubbing extends NewHomeworkContentProcessTemplate {
    @Override
    public NewHomeworkContentProcessTemp getNewHomeworkContentTemp() {
        return NewHomeworkContentProcessTemp.DUBBING;
    }

    @Override
    @SuppressWarnings("unchecked")
    public AssignHomeworkContext processHomeworkContent(AssignHomeworkContext context, Map<String, Object> practice, ObjectiveConfigType objectiveConfigType) {
        List<Map> apps = JsonUtils.fromJsonToList(JsonUtils.toJson(practice.get("apps")), Map.class);
        if (CollectionUtils.isNotEmpty(apps)) {
            if (NewHomeworkType.MothersDay == context.getNewHomeworkType() || NewHomeworkType.Activity == context.getNewHomeworkType()) {
                return processGroupHomeworkContent(context, apps, objectiveConfigType);
            }
            List<NewHomeworkApp> newHomeworkApps = new ArrayList<>();
            Map<BookInfoMapper, List<String>> bookDubbingIdsMap = new LinkedHashMap<>();
            for (Map app : apps) {
                NewHomeworkApp nha = new NewHomeworkApp();
                String dubbingId = SafeConverter.toString(app.get("dubbingId"));
                if (StringUtils.isBlank(dubbingId)) {
                    return contentError(context, objectiveConfigType);
                }

                Dubbing dubbing = dubbingLoaderClient.loadDubbingByIdIncludeDisabled(dubbingId);
                if (dubbing == null || CollectionUtils.isEmpty(dubbing.getPracticeQuestions())) {
                    return contentError(context, objectiveConfigType);
                }

                if (app.containsKey("book")) {
                    Map<String, Object> book = (Map<String, Object>) app.get("book");
                    if (book != null) {
                        BookInfoMapper bookInfoMapper = new BookInfoMapper();
                        bookInfoMapper.setBookId(SafeConverter.toString(book.get("bookId"), ""));
                        bookInfoMapper.setUnitId(SafeConverter.toString(book.get("unitId"), ""));
                        bookInfoMapper.setObjectiveId(SafeConverter.toString(app.get("objectiveId"), ""));
                        bookDubbingIdsMap.computeIfAbsent(bookInfoMapper, k -> new ArrayList<>()).add(dubbingId);
                    }
                }

                List<String> practiceQids = dubbing.getPracticeQuestions();
                List<NewHomeworkQuestion> newHomeworkQuestions = buildDubbingQuestions(practiceQids);
                if (CollectionUtils.isEmpty(newHomeworkQuestions)) {
                    return contentError(context, objectiveConfigType);
                }
                nha.setQuestions(newHomeworkQuestions);
                nha.setDubbingId(dubbingId);
                newHomeworkApps.add(nha);
            }
            if (CollectionUtils.isNotEmpty(newHomeworkApps)) {
                NewHomeworkPracticeContent nhpc = new NewHomeworkPracticeContent();
                nhpc.setType(objectiveConfigType);
                nhpc.setApps(newHomeworkApps);
                for (Long groupId : context.getGroupIds()) {
                    context.getGroupPractices().computeIfAbsent(groupId, k -> new ArrayList<>()).add(nhpc);
                }
            }

            List<NewHomeworkBookInfo> newHomeworkBookInfoList = bookDubbingIdsMap.entrySet()
                    .stream()
                    .map(entry -> {
                        NewHomeworkBookInfo newHomeworkBookInfo = new NewHomeworkBookInfo();
                        BookInfoMapper bookInfoMapper = entry.getKey();
                        newHomeworkBookInfo.setBookId(bookInfoMapper.getBookId());
                        newHomeworkBookInfo.setUnitId(bookInfoMapper.getUnitId());
                        newHomeworkBookInfo.setObjectiveId(bookInfoMapper.getObjectiveId());
                        newHomeworkBookInfo.setDubbingIds(entry.getValue());
                        return newHomeworkBookInfo;
                    })
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(newHomeworkBookInfoList)) {
                for (Long groupId : context.getGroupIds()) {
                    context.getGroupPracticesBooksMap().computeIfAbsent(groupId, k -> new LinkedHashMap<>()).put(objectiveConfigType, newHomeworkBookInfoList);
                }
            }
        }
        return context;
    }

    private AssignHomeworkContext processGroupHomeworkContent(AssignHomeworkContext context, List<Map> apps, ObjectiveConfigType objectiveConfigType) {
        Map<Long, List<NewHomeworkApp>> groupNewHomeworkApps = new LinkedHashMap<>();
        for (Map app : apps) {
            Long groupId = SafeConverter.toLong(app.get("groupId"));
            if (context.getGroupIds().contains(groupId)) {
                String dubbingId = SafeConverter.toString(app.get("dubbingId"));
                if (StringUtils.isBlank(dubbingId)) {
                    return contentError(context, objectiveConfigType);
                }
                Dubbing dubbing = dubbingLoaderClient.loadDubbingByIdIncludeDisabled(dubbingId);
                if (dubbing == null || CollectionUtils.isEmpty(dubbing.getPracticeQuestions())) {
                    return contentError(context, objectiveConfigType);
                }
                NewHomeworkApp nha = new NewHomeworkApp();
                List<String> practiceQids = dubbing.getPracticeQuestions();
                List<NewHomeworkQuestion> newHomeworkQuestions = buildDubbingQuestions(practiceQids);
                if (CollectionUtils.isEmpty(newHomeworkQuestions)) {
                    return contentError(context, objectiveConfigType);
                }
                nha.setQuestions(newHomeworkQuestions);
                nha.setDubbingId(dubbingId);
                groupNewHomeworkApps.computeIfAbsent(groupId, k -> new ArrayList<>()).add(nha);
            }
        }
        if (MapUtils.isNotEmpty(groupNewHomeworkApps)) {
            for (Long groupId : context.getGroupIds()) {
                List<NewHomeworkApp> newHomeworkApps = groupNewHomeworkApps.get(groupId);
                if (CollectionUtils.isNotEmpty(newHomeworkApps)) {
                    NewHomeworkPracticeContent nhpc = new NewHomeworkPracticeContent();
                    nhpc.setType(objectiveConfigType);
                    nhpc.setApps(newHomeworkApps);
                    List<NewHomeworkPracticeContent> newHomeworkPracticeContents = context.getGroupPractices().getOrDefault(groupId, new ArrayList<>());
                    newHomeworkPracticeContents.add(nhpc);
                    context.getGroupPractices().put(groupId, newHomeworkPracticeContents);
                }
            }
        }
        return context;
    }

    private List<NewHomeworkQuestion> buildDubbingQuestions(List<String> qids) {
        if (CollectionUtils.isEmpty(qids)) {
            return Collections.emptyList();
        }

        List<NewQuestion> questions = questionLoaderClient.loadQuestionsIncludeDisabledAsList(qids);
        if (CollectionUtils.isEmpty(questions)) {
            return Collections.emptyList();
        }

        List<NewHomeworkQuestion> result = new ArrayList<>();
        for (NewQuestion q : questions) {
            NewHomeworkQuestion nhq = new NewHomeworkQuestion();
            nhq.setQuestionId(q.getId());
            nhq.setQuestionVersion(q.getOlUpdatedAt() != null ? q.getOlUpdatedAt().getTime() : q.getVersion());
            // 配音题不打分，这个字段没用
            nhq.setScore(100D);
            nhq.setSeconds(q.getSeconds());
            nhq.setSubmitWay(q.getSubmitWays());
            result.add(nhq);
        }
        return result;
    }
}
