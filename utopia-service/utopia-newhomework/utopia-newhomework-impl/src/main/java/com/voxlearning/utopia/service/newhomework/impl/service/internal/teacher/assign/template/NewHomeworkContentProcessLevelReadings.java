package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assign.template;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.prometheus.service.data.api.client.PictureBookPlusServiceClient;
import com.voxlearning.utopia.service.newhomework.api.constant.PictureBookPracticeType;
import com.voxlearning.utopia.service.newhomework.api.context.AssignHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkApp;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkBookInfo;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestion;
import com.voxlearning.utopia.service.newhomework.api.mapper.BookInfoMapper;
import com.voxlearning.utopia.service.question.api.constant.NewHomeworkContentProcessTemp;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.api.entity.PictureBookPlus;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Named
public class NewHomeworkContentProcessLevelReadings extends NewHomeworkContentProcessTemplate {

    @Inject private PictureBookPlusServiceClient pictureBookPlusServiceClient;

    @Override
    public NewHomeworkContentProcessTemp getNewHomeworkContentTemp() {
        return NewHomeworkContentProcessTemp.LEVEL_READINGS;
    }

    @Override
    @SuppressWarnings("unchecked")
    public AssignHomeworkContext processHomeworkContent(AssignHomeworkContext context, Map<String, Object> practice, ObjectiveConfigType objectiveConfigType) {
        List<Map> apps = JsonUtils.fromJsonToList(JsonUtils.toJson(practice.get("apps")), Map.class);
        if (CollectionUtils.isNotEmpty(apps)) {
            List<NewHomeworkApp> newHomeworkApps = new ArrayList<>();
            Map<BookInfoMapper, List<String>> bookReadingIdsMap = new LinkedHashMap<>();
            for (Map app : apps) {
                NewHomeworkApp nha = new NewHomeworkApp();
                String pictureBookId = SafeConverter.toString(app.get("pictureBookId"));
                if (StringUtils.isBlank(pictureBookId)) {
                    return contentError(context, objectiveConfigType);
                }

                PictureBookPlus pictureBookPlus = pictureBookPlusServiceClient.loadByIds(Collections.singleton(pictureBookId)).get(pictureBookId);
                if (pictureBookPlus == null) {
                    return contentError(context, objectiveConfigType);
                }

                List<String> practiceTypes = (List<String>) app.get("practiceTypes");
                if (CollectionUtils.isEmpty(practiceTypes)) {
                    return contentError(context, objectiveConfigType);
                }

                Set<PictureBookPracticeType> practiceTypesSet = new HashSet<>();
                for (String type : practiceTypes) {
                    PictureBookPracticeType practiceType = PictureBookPracticeType.of(type);
                    if (practiceType != null) {
                        practiceTypesSet.add(practiceType);
                    }
                }
                if (CollectionUtils.isEmpty(practiceTypesSet)) {
                    return contentError(context, objectiveConfigType);
                }
                // 习题和跟读必须有一个，不然会没法打分
                if (!practiceTypesSet.contains(PictureBookPracticeType.ORAL) && !practiceTypesSet.contains(PictureBookPracticeType.EXAM)) {
                    return contentError(context, objectiveConfigType);
                }

                Set<String> questionIds = new HashSet<>();

                // 习题
                boolean containExam = practiceTypesSet.contains(PictureBookPracticeType.EXAM) && CollectionUtils.isNotEmpty(pictureBookPlus.getPracticeQuestions());
                if (containExam) {
                    questionIds.addAll(pictureBookPlus.getPracticeQuestions());
                }

                // 跟读
                boolean containOral = practiceTypesSet.contains(PictureBookPracticeType.ORAL) && CollectionUtils.isNotEmpty(pictureBookPlus.getOralQuestions());
                if (containOral) {
                    questionIds.addAll(pictureBookPlus.getOralQuestions());
                }

                // 配音
                nha.setContainsDubbing(practiceTypesSet.contains(PictureBookPracticeType.DUBBING));

                Map<String, NewQuestion> questionMap = questionLoaderClient.loadQuestionsIncludeDisabled(questionIds);
                if (MapUtils.isEmpty(questionMap)) {
                    return contentError(context, objectiveConfigType);
                }

                // 计算每题的标准分
                Map<String, Double> scoreMap = questionLoaderClient.parseExamScoreByQuestions(new ArrayList<>(questionMap.values()), 40.00);

                if (containExam) {
                    List<NewHomeworkQuestion> newHomeworkQuestions = buildQuestions(pictureBookPlus.getPracticeQuestions(), questionMap, scoreMap);
                    if (CollectionUtils.isNotEmpty(newHomeworkQuestions)) {
                        nha.setQuestions(newHomeworkQuestions);
                    }
                }

                if (containOral) {
                    List<NewHomeworkQuestion> newHomeworkQuestions = buildQuestions(pictureBookPlus.getOralQuestions(), questionMap, scoreMap);
                    if (CollectionUtils.isNotEmpty(newHomeworkQuestions)) {
                        nha.setOralQuestions(newHomeworkQuestions);
                    }
                }

                nha.setPictureBookId(pictureBookId);
                newHomeworkApps.add(nha);

                if (app.containsKey("book")) {
                    Map<String, Object> book = (Map) app.get("book");
                    if (book != null) {
                        BookInfoMapper bookInfoMapper = new BookInfoMapper();
                        bookInfoMapper.setBookId(SafeConverter.toString(book.get("bookId"), ""));
                        bookInfoMapper.setUnitId(SafeConverter.toString(book.get("unitId"), ""));
                        bookInfoMapper.setObjectiveId(SafeConverter.toString(app.get("objectiveId"), ""));
                        bookReadingIdsMap.computeIfAbsent(bookInfoMapper, k -> new ArrayList<>()).add(pictureBookId);
                    }
                }
            }

            if (CollectionUtils.isNotEmpty(newHomeworkApps)) {
                NewHomeworkPracticeContent nhpc = new NewHomeworkPracticeContent();
                nhpc.setType(objectiveConfigType);
                nhpc.setApps(newHomeworkApps);
                for (Long groupId : context.getGroupIds()) {
                    List<NewHomeworkPracticeContent> newHomeworkPracticeContents = context.getGroupPractices().getOrDefault(groupId, new ArrayList<>());
                    newHomeworkPracticeContents.add(nhpc);
                    context.getGroupPractices().put(groupId, newHomeworkPracticeContents);
                }
            }

            List<NewHomeworkBookInfo> newHomeworkBookInfoList = bookReadingIdsMap.entrySet()
                    .stream()
                    .map(entry -> {
                        NewHomeworkBookInfo newHomeworkBookInfo = new NewHomeworkBookInfo();
                        BookInfoMapper bookInfoMapper = entry.getKey();
                        newHomeworkBookInfo.setBookId(bookInfoMapper.getBookId());
                        newHomeworkBookInfo.setUnitId(bookInfoMapper.getUnitId());
                        newHomeworkBookInfo.setObjectiveId(bookInfoMapper.getObjectiveId());
                        newHomeworkBookInfo.setPictureBooks(entry.getValue());
                        return newHomeworkBookInfo;
                    })
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(newHomeworkBookInfoList)) {
                for (Long groupId : context.getGroupIds()) {
                    LinkedHashMap<ObjectiveConfigType, List<NewHomeworkBookInfo>> practiceBooksMap = context.getGroupPracticesBooksMap()
                            .getOrDefault(groupId, new LinkedHashMap<>());
                    practiceBooksMap.put(objectiveConfigType, newHomeworkBookInfoList);
                    context.getGroupPracticesBooksMap().put(groupId, practiceBooksMap);
                }
            }
        }
        return context;
    }

    private List<NewHomeworkQuestion> buildQuestions(List<String> qids, Map<String, NewQuestion> questionMap, Map<String, Double> scoreMap) {
        if (CollectionUtils.isEmpty(qids)) {
            return Collections.emptyList();
        }

        List<NewHomeworkQuestion> result = new ArrayList<>();
        for (String qid : qids) {
            NewQuestion newQuestion = questionMap.get(qid);
            if (newQuestion != null) {
                NewHomeworkQuestion nhq = new NewHomeworkQuestion();
                nhq.setQuestionId(newQuestion.getId());
                // 题目版本号
                nhq.setQuestionVersion(newQuestion.getOlUpdatedAt() != null ? newQuestion.getOlUpdatedAt().getTime() : newQuestion.getVersion());
                nhq.setScore(scoreMap.get(newQuestion.getId()));
                nhq.setSeconds(newQuestion.getSeconds());
                nhq.setSubmitWay(newQuestion.getSubmitWays());
                result.add(nhq);
            }
        }
        return result;
    }
}
