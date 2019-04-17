package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assign.template;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.service.newhomework.api.context.AssignHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkApp;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkBookInfo;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestion;
import com.voxlearning.utopia.service.newhomework.api.mapper.BookInfoMapper;
import com.voxlearning.utopia.service.question.api.constant.NewHomeworkContentProcessTemp;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.api.entity.PictureBook;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 绘本模板
 *
 * @author xuesong.zhang
 * @since 2016-07-13
 */
@Named
@Deprecated
public class NewHomeworkContentProcessReading extends NewHomeworkContentProcessTemplate {

    @Override
    public NewHomeworkContentProcessTemp getNewHomeworkContentTemp() {
        return NewHomeworkContentProcessTemp.READING;
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

                PictureBook pictureBook = pictureBookLoaderClient.loadPictureBooksIncludeDisabled(Collections.singletonList(pictureBookId)).getOrDefault(pictureBookId, null);
                if (pictureBook == null || CollectionUtils.isEmpty(pictureBook.getPracticeQuestions())) {
                    return contentError(context, objectiveConfigType);
                }

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

                // 跟读题部分
                if (CollectionUtils.isNotEmpty(pictureBook.getOralQuestions())) {
                    List<String> oralQids = pictureBook.getOralQuestions();
                    // 组装跟读题
                    nha.setOralQuestions(buildReadingQuestions(oralQids, true));
                }

                // 应试题部分，应试题不能为空
                List<String> practiceQids = pictureBook.getPracticeQuestions();
                nha.setQuestions(buildReadingQuestions(practiceQids, false));
                nha.setPictureBookId(pictureBookId);
                newHomeworkApps.add(nha);
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

    private List<NewHomeworkQuestion> buildReadingQuestions(List<String> qids, boolean isOral) {
        if (CollectionUtils.isEmpty(qids)) {
            return Collections.emptyList();
        }

        List<NewQuestion> questions = questionLoaderClient.loadQuestionsIncludeDisabledAsList(qids);
        if (CollectionUtils.isEmpty(questions)) {
            return Collections.emptyList();
        }

        List<NewHomeworkQuestion> result = new ArrayList<>();
        if (Objects.equals(isOral, Boolean.TRUE)) {
            for (NewQuestion q : questions) {
                NewHomeworkQuestion nhq = new NewHomeworkQuestion();
                nhq.setQuestionId(q.getId());
                // 题目版本号
                nhq.setQuestionVersion(q.getOlUpdatedAt() != null ? q.getOlUpdatedAt().getTime() : q.getVersion());
                // 这个属性对于口语类的题没什么作用，暂时预留吧
                nhq.setScore(100D);
                nhq.setSeconds(q.getSeconds());
                nhq.setSubmitWay(q.getSubmitWays());
                result.add(nhq);
            }
        } else {
            Map<String, Double> scoreMap = questionLoaderClient.parseExamScoreByQuestions(questions, 100.00);
            for (NewQuestion q : questions) {
                NewHomeworkQuestion nhq = new NewHomeworkQuestion();
                nhq.setQuestionId(q.getId());
                // 题目版本号
                nhq.setQuestionVersion(q.getOlUpdatedAt() != null ? q.getOlUpdatedAt().getTime() : q.getVersion());
                // 这个属性对于口语类的题没什么作用，暂时预留吧
                nhq.setScore(scoreMap.get(q.getId()));
                nhq.setSeconds(q.getSeconds());
                nhq.setSubmitWay(q.getSubmitWays());
                result.add(nhq);
            }
        }
        return result;
    }
}
