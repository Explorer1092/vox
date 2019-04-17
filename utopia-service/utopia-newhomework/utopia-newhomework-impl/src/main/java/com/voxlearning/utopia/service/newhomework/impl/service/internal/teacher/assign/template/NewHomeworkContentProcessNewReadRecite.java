/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assign.template;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.service.content.api.entity.ChineseSentence;
import com.voxlearning.utopia.service.newhomework.api.constant.QuestionBoxType;
import com.voxlearning.utopia.service.newhomework.api.context.AssignHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkApp;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkBookInfo;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestion;
import com.voxlearning.utopia.service.newhomework.api.mapper.BookInfoMapper;
import com.voxlearning.utopia.service.question.api.constant.NewHomeworkContentProcessTemp;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 小学语文读背练习，布置作业模版
 *
 * @author zhangbin
 * @since 2017/6/2 16:30
 */
@Named
@Deprecated
public class NewHomeworkContentProcessNewReadRecite extends NewHomeworkContentProcessTemplate {
    @Override
    public NewHomeworkContentProcessTemp getNewHomeworkContentTemp() {
        return NewHomeworkContentProcessTemp.NEW_READ_RECITE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public AssignHomeworkContext processHomeworkContent(AssignHomeworkContext context,
                                                        Map<String, Object> practice,
                                                        ObjectiveConfigType objectiveConfigType) {
        List<Map> apps = JsonUtils.fromJsonToList(JsonUtils.toJson(practice.get("apps")), Map.class);
        if (CollectionUtils.isNotEmpty(apps)) {
            List<NewHomeworkApp> newHomeworkAppList = new ArrayList<>();
            Set<String> newHomeworkAppKeySet = new HashSet<>();
            Map<BookInfoMapper, List<String>> bookQuestionBoxIdsMap = new LinkedHashMap<>();

            //先布置朗读后背诵
            apps = apps.stream()
                    .filter(e -> e.get("questionBoxType") != null)
                    .sorted(Comparator.comparing(e -> SafeConverter.toString(e.get("questionBoxType"))))
                    .collect(Collectors.toList());
            Set<Long> groupIds = context.getGroupIds();
            for (Map app : apps) {
                Map<String, Object> book = (Map) app.get("book");
                String lessonId = SafeConverter.toString(app.get("lessonId"));
                String questionBoxId = SafeConverter.toString(app.get("questionBoxId"));
                if (book != null) {
                    BookInfoMapper bookInfoMapper = new BookInfoMapper();
                    bookInfoMapper.setBookId(SafeConverter.toString(book.get("bookId"), ""));
                    bookInfoMapper.setUnitId(SafeConverter.toString(book.get("unitId"), ""));
                    bookInfoMapper.setLessonId(SafeConverter.toString(book.get("lessonId"), ""));
                    lessonId = bookInfoMapper.getLessonId();
                    bookInfoMapper.setSectionId(SafeConverter.toString(book.get("sectionId"), ""));
                    bookInfoMapper.setObjectiveId(SafeConverter.toString(app.get("objectiveId"), ""));
                    bookQuestionBoxIdsMap.computeIfAbsent(bookInfoMapper, k -> new ArrayList<>()).add(questionBoxId);
                }
                QuestionBoxType questionBoxType = QuestionBoxType.of(SafeConverter.toString(app.get("questionBoxType")));
                if (StringUtils.isBlank(questionBoxId) || questionBoxType == null) {
                    return contentError(context, objectiveConfigType);
                }

                NewHomeworkApp newHomeworkApp = new NewHomeworkApp();
                List<Map> questionsList = JsonUtils.fromJsonToList(JsonUtils.toJson(app.get("questions")), Map.class);
                Map<String, NewHomeworkQuestion> newHomeworkQuestionMap = new LinkedHashMap<>();
                List<String> questionIdList = new ArrayList<>();
                if (CollectionUtils.isNotEmpty(questionsList)) {
                    for (Map appQuestion : questionsList) {
                        NewHomeworkQuestion newHomeworkQuestion = new NewHomeworkQuestion();
                        String questionId = SafeConverter.toString(appQuestion.get("questionId"));
                        newHomeworkQuestion.setQuestionId(questionId);
                        if (appQuestion.containsKey("seconds")) {
                            newHomeworkQuestion.setSeconds(SafeConverter.toInt(appQuestion.get("seconds")));
                        }
                        if (appQuestion.containsKey("answerType")) {
                            newHomeworkQuestion.setAnswerType(SafeConverter.toInt(appQuestion.get("answerType")));
                        }
                        if (appQuestion.containsKey("submitWay")) {
                            newHomeworkQuestion.setSubmitWay((List<List<Integer>>) appQuestion.get("submitWay"));
                        }
                        context.setIncludeSubjective(true);
                        newHomeworkQuestionMap.put(questionId, newHomeworkQuestion);
                        questionIdList.add(questionId);
                    }
                }

                if (CollectionUtils.isEmpty(questionIdList)) {
                    continue;
                }

                //按照自然段标号升序布置
                List<NewQuestion> newQuestionList = questionLoaderClient.loadQuestionsIncludeDisabledAsList(questionIdList);

                Map<String, List<Long>> qidSentenceIdsMap = newQuestionList
                        .stream()
                        .collect(Collectors.toMap(NewQuestion::getId, NewQuestion::getSentenceIds));

                List<Long> chineseSentenceIds = newQuestionList
                        .stream()
                        .filter(question -> CollectionUtils.isNotEmpty(question.getSentenceIds()))
                        .map(NewQuestion::getSentenceIds)
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList());
                List<ChineseSentence> chineseSentences = chineseContentLoaderClient.loadChineseSentenceByIds(chineseSentenceIds);

                //句子所在的章节号
                Map<Long, Integer> sentenceIdSection = chineseSentences
                        .stream()
                        .collect(Collectors.toMap(ChineseSentence::getId, ChineseSentence::getParagraphContinuous));
                //句子所在章节的段落号
                Map<Long, Integer> sentenceIdParagraph = chineseSentences
                        .stream()
                        .collect(Collectors.toMap(ChineseSentence::getId, ChineseSentence::getParagraph));
                //题的章节号
                Map<String, Integer> qidSectionMap = new HashMap<>();
                //题的段落号
                Map<String, Integer> qidParagraphMap = new HashMap<>();

                List<Map<String, Object>> questionMapList = new ArrayList<>();
                if (MapUtils.isNotEmpty(qidSentenceIdsMap)) {
                    for (Map.Entry<String, List<Long>> entry : qidSentenceIdsMap.entrySet()) {
                        String questionId = entry.getKey();
                        List<Long> sentenceIds = entry.getValue();
                        Long sentenceId = 0L;
                        if (CollectionUtils.isNotEmpty(sentenceIds)) {
                            sentenceId = sentenceIds.iterator().next();
                        }
                        qidSectionMap.put(questionId, sentenceIdSection.get(sentenceId));
                        qidParagraphMap.put(questionId, sentenceIdParagraph.get(sentenceId));

                        Map<String, Object> question = new LinkedHashMap<>();
                        question.put("questionId", questionId);
                        question.put("sectionNumber", qidSectionMap.get(questionId));
                        question.put("paragraphNumber", qidParagraphMap.get(questionId));
                        questionMapList.add(question);
                    }
                }

                Comparator<Map<String, Object>> comparator = Comparator.comparingInt(a -> SafeConverter.toInt(a.get("sectionNumber")));
                comparator = comparator.thenComparing(Comparator.comparingInt(a -> SafeConverter.toInt(a.get("paragraphNumber"))));
                questionMapList = questionMapList.stream()
                        .filter(e -> e.get("sectionNumber") != null)
                        .filter(e -> e.get("paragraphNumber") != null)
                        .sorted(comparator)
                        .collect(Collectors.toList());

                Map<String, NewQuestion> newQuestionMap = questionLoaderClient.loadQuestionsIncludeDisabled(questionIdList);
                Map<String, Double> qscoreMap = questionLoaderClient.parseExamScoreByQuestions(new ArrayList<>(newQuestionMap.values()), 100.00);

                List<NewHomeworkQuestion> newHomeworkQuestionList = new ArrayList<>();
                for (Map<String, Object> entry : questionMapList) {
                    String qid = (String) entry.get("questionId");
                    if (qscoreMap.containsKey(qid)) {
                        NewHomeworkQuestion newHomeworkQuestion = newHomeworkQuestionMap.get(qid);
                        // 题目版本号
                        NewQuestion newQuestion = newQuestionMap.getOrDefault(qid, null);
                        //跟读类应用的标准分都是100
                        if (newHomeworkQuestion != null) {
                            newHomeworkQuestion.setScore(qscoreMap.get(qid));
                            if (newQuestion != null) {
                                newHomeworkQuestion.setQuestionVersion(
                                        newQuestion.getOlUpdatedAt() != null ?
                                                newQuestion.getOlUpdatedAt().getTime() :
                                                newQuestion.getVersion());
                            }
                        }
                        newHomeworkQuestionList.add(newHomeworkQuestion);
                    }
                }

                if (CollectionUtils.isEmpty(newHomeworkQuestionList)) {
                    continue;
                }

                newHomeworkApp.setLessonId(lessonId);
                newHomeworkApp.setQuestionBoxId(questionBoxId);
                newHomeworkApp.setQuestionBoxType(questionBoxType);
                newHomeworkApp.setQuestions(newHomeworkQuestionList);

                if (newHomeworkAppKeySet.contains(questionBoxId)) {
                    logger.warn("Found duplicate app, appKey : {}, homeworkSource : {}", questionBoxId, JsonUtils.toJson(context.getSource()));
                } else {
                    newHomeworkAppKeySet.add(questionBoxId);
                    newHomeworkAppList.add(newHomeworkApp);
                }
            }

            if (CollectionUtils.isNotEmpty(newHomeworkAppList)) {
                NewHomeworkPracticeContent newHomeworkPracticeContent = new NewHomeworkPracticeContent();
                newHomeworkPracticeContent.setApps(newHomeworkAppList);
                newHomeworkPracticeContent.setType(objectiveConfigType);

                Boolean includeSubjective = false;
                for (NewHomeworkApp newHomeworkApp : newHomeworkAppList) {
                    List<NewHomeworkQuestion> questions = newHomeworkApp.getQuestions();
                    if (CollectionUtils.isNotEmpty(questions)) {
                        for (NewHomeworkQuestion newHomeworkQuestion : questions) {
                            includeSubjective = newHomeworkQuestion.isSubjectiveQuestion();
                            if (SafeConverter.toBoolean(includeSubjective)) {
                                break;
                            }
                        }
                    }
                }
                newHomeworkPracticeContent.setIncludeSubjective(includeSubjective);

                if (CollectionUtils.isNotEmpty(groupIds)) {
                    for (Long groupId : groupIds) {
                        Map<Long, List<NewHomeworkPracticeContent>> groupPractices = context.getGroupPractices();
                        List<NewHomeworkPracticeContent> newHomeworkPracticeContents = groupPractices
                                .getOrDefault(groupId, new ArrayList<>());
                        newHomeworkPracticeContents.add(newHomeworkPracticeContent);
                        groupPractices.put(groupId, newHomeworkPracticeContents);
                    }
                }
            }

            if (MapUtils.isNotEmpty(bookQuestionBoxIdsMap)) {
                List<NewHomeworkBookInfo> newHomeworkBookInfoList = bookQuestionBoxIdsMap.entrySet()
                        .stream()
                        .map(entry -> {
                            NewHomeworkBookInfo newHomeworkBookInfo = new NewHomeworkBookInfo();
                            BookInfoMapper bookInfoMapper = entry.getKey();
                            newHomeworkBookInfo.setBookId(bookInfoMapper.getBookId());
                            newHomeworkBookInfo.setUnitId(bookInfoMapper.getUnitId());
                            newHomeworkBookInfo.setLessonId(bookInfoMapper.getLessonId());
                            newHomeworkBookInfo.setSectionId(bookInfoMapper.getSectionId());
                            newHomeworkBookInfo.setObjectiveId(bookInfoMapper.getObjectiveId());
                            newHomeworkBookInfo.setQuestionBoxIds(entry.getValue());
                            return newHomeworkBookInfo;
                        })
                        .collect(Collectors.toList());

                if (CollectionUtils.isNotEmpty(newHomeworkBookInfoList)) {
                    if (CollectionUtils.isNotEmpty(groupIds)) {
                        for (Long groupId : groupIds) {
                            LinkedHashMap<ObjectiveConfigType, List<NewHomeworkBookInfo>> practiceBooksMap = context
                                    .getGroupPracticesBooksMap()
                                    .getOrDefault(groupId, new LinkedHashMap<>());
                            practiceBooksMap.put(objectiveConfigType, newHomeworkBookInfoList);
                            context.getGroupPracticesBooksMap().put(groupId, practiceBooksMap);
                        }
                    }
                }
            }
        }
        return context;
    }
}
