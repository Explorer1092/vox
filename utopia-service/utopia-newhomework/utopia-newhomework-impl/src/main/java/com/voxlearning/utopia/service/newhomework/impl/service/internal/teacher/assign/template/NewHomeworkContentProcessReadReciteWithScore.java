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
 * 小学语文课文读背，布置作业模版
 *
 * @author zhangbin
 * @since 2018/1/11
 */

@Named
public class NewHomeworkContentProcessReadReciteWithScore extends NewHomeworkContentProcessTemplate {
    @Override
    public NewHomeworkContentProcessTemp getNewHomeworkContentTemp() {
        return NewHomeworkContentProcessTemp.READ_RECITE_WITH_SCORE;
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
                        context.setIncludeSubjective(false);
                        newHomeworkQuestionMap.put(questionId, newHomeworkQuestion);
                        questionIdList.add(questionId);
                    }
                }

                if (CollectionUtils.isEmpty(questionIdList)) {
                    continue;
                }

                //按照自然段标号升序布置
                Map<String, NewQuestion> newQuestionMap = questionLoaderClient.loadQuestionsIncludeDisabled(questionIdList);
                Collection<NewQuestion> newQuestionList = newQuestionMap.values();

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
                Map<Long, Integer> sentenceIdSection = new HashMap<>();
                //句子所在章节的段落号
                Map<Long, Integer> sentenceIdParagraph = new HashMap<>();
                //句子所在的排行
                Map<Long, Integer> sentenceIdRank = new HashMap<>();
                for(ChineseSentence chineseSentence : chineseSentences){
                    sentenceIdSection.put(chineseSentence.getId(), chineseSentence.getParagraphContinuous());
                    sentenceIdParagraph.put(chineseSentence.getId(), chineseSentence.getParagraph());
                    sentenceIdRank.put(chineseSentence.getId(), chineseSentence.getRank());
                }

                List<Map<String, Object>> questionMapList = new ArrayList<>();
                if (MapUtils.isNotEmpty(qidSentenceIdsMap)) {
                    for (Map.Entry<String, List<Long>> entry : qidSentenceIdsMap.entrySet()) {
                        String questionId = entry.getKey();
                        List<Long> sentenceIds = entry.getValue();
                        Long sentenceId = 0L;
                        if (CollectionUtils.isNotEmpty(sentenceIds)) {
                            sentenceId = sentenceIds.iterator().next();
                        }
                        Map<String, Object> question = new LinkedHashMap<>();
                        question.put("questionId", questionId);
                        question.put("sectionNumber", sentenceIdSection.get(sentenceId));
                        question.put("paragraphNumber", sentenceIdParagraph.get(sentenceId));
                        question.put("rankNumber", sentenceIdRank.get(sentenceId));
                        questionMapList.add(question);
                    }
                }

                Comparator<Map<String, Object>> comparator = Comparator.comparingInt(a -> SafeConverter.toInt(a.get("sectionNumber")));
                comparator = comparator.thenComparing(a -> SafeConverter.toInt(a.get("rankNumber")));
                comparator = comparator.thenComparingInt(a -> SafeConverter.toInt(a.get("paragraphNumber")));
                questionMapList = questionMapList.stream()
                        .filter(e -> e.get("sectionNumber") != null)
                        .filter(e -> e.get("paragraphNumber") != null)
                        .filter(e -> e.get("rankNumber") != null)
                        .sorted(comparator)
                        .collect(Collectors.toList());

                List<NewHomeworkQuestion> newHomeworkQuestionList = new ArrayList<>();
                for (Map<String, Object> entry : questionMapList) {
                    String qid = (String) entry.get("questionId");
                    NewHomeworkQuestion newHomeworkQuestion = newHomeworkQuestionMap.get(qid);
                    // 题目版本号
                    NewQuestion newQuestion = newQuestionMap.getOrDefault(qid, null);
                    //跟读类应用的标准分都是100
                    if (newHomeworkQuestion != null) {
                        newHomeworkQuestion.setScore(100.00);
                        if (newQuestion != null) {
                            newHomeworkQuestion.setQuestionVersion(
                                    newQuestion.getOlUpdatedAt() != null ?
                                            newQuestion.getOlUpdatedAt().getTime() :
                                            newQuestion.getVersion());
                        }
                    }
                    newHomeworkQuestionList.add(newHomeworkQuestion);
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
                newHomeworkPracticeContent.setIncludeSubjective(false);

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
