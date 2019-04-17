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

package com.voxlearning.utopia.service.newhomework.impl.template.internal.content;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.utopia.service.content.api.entity.ChineseSentence;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.consumer.ChineseContentLoaderClient;
import com.voxlearning.utopia.service.newhomework.api.constant.QuestionBoxType;
import com.voxlearning.utopia.service.newhomework.api.mapper.readrecite.NewReadReciteBO;
import com.voxlearning.utopia.service.newhomework.api.mapper.readrecite.ReadReciteBO;
import com.voxlearning.utopia.service.newhomework.base.helper.NewHomeworkContentDecorator;
import com.voxlearning.utopia.service.newhomework.impl.template.NewHomeworkContentLoaderMapper;
import com.voxlearning.utopia.service.newhomework.impl.template.NewHomeworkContentLoaderTemplate;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.EmbedBook;
import com.voxlearning.utopia.service.question.api.entity.NewContentType;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.api.entity.ObjectiveConfig;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * 小学语文，课文读背练习推题接口
 *
 * @author zhangbin
 * @since 2017/5/31 17:13
 */
@Named
public class NewHomeworkNewReadReciteContentLoader extends NewHomeworkContentLoaderTemplate {
    private static final Logger logger = LoggerFactory.getLogger(NewHomeworkNewReadReciteContentLoader.class);

    @Inject
    private ChineseContentLoaderClient chineseContentLoaderClient;

    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.NEW_READ_RECITE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> loadContent(NewHomeworkContentLoaderMapper mapper) {
        List<Map<String, Object>> content = new ArrayList<>();
        if (Subject.CHINESE != mapper.getTeacher().getSubject()) {
            return content;
        }
        if (CollectionUtils.isEmpty(mapper.getSectionIds())) {
            return content;
        }
        try {
            List<Map<String, Object>> readReciteContent = new ArrayList<>();
            Set<String> relatedSectionIds = new HashSet<>();
            if (CollectionUtils.isNotEmpty(mapper.getObjectiveConfig().getContents())) {
                for (Map<String, Object> configContent : mapper.getObjectiveConfig().getContents()) {
                    int type = SafeConverter.toInt(configContent.get("type"));
                    String relatedCatalogId = SafeConverter.toString(configContent.get("related_catalog_id"));
                    // 新课文读背关联的sectionId不允许为空
                    if (StringUtils.isNotBlank(relatedCatalogId)
                            && mapper.getSectionIds().contains(relatedCatalogId)
                            && type == ObjectiveConfig.QUESTION_ID) {
                        relatedSectionIds.add(relatedCatalogId);
                        readReciteContent.add(configContent);
                    }
                }
            }
            Map<String, NewBookCatalog> relatedSectionMap = newContentLoaderClient.loadBookCatalogByCatalogIds(relatedSectionIds);
            Map<String, EmbedBook> questionBookMap = new LinkedHashMap<>();
            Set<String> allLessonIds = new HashSet<>();
            Map<String, String> boxIdLessonIdMap = new HashMap<>();
            Map<String, List<String>> boxIdQuestionIdsMap = new HashMap<>();
            if (CollectionUtils.isNotEmpty(readReciteContent)) {
                readReciteContent.forEach(e -> {
                    String sectionId = SafeConverter.toString(e.get("related_catalog_id"));
                    EmbedBook book = new EmbedBook();
                    book.setBookId(mapper.getBookId());
                    book.setUnitId(mapper.getUnitId());
                    book.setSectionId(sectionId);
                    NewBookCatalog section = relatedSectionMap.get(sectionId);
                    if (section != null && StringUtils.isNotBlank(section.getParentId())) {
                        book.setLessonId(section.getParentId());
                        allLessonIds.add(book.getLessonId());
                        boxIdLessonIdMap.put((String) e.get("id"), book.getLessonId());

                        List<String> questionIdList = conversionService.convert(e.get("question_ids"), List.class);
                        boxIdQuestionIdsMap.put((String) e.get("id"), questionIdList);

                        if (CollectionUtils.isNotEmpty(questionIdList)) {
                            questionIdList.forEach(qid -> {
                                if (!questionBookMap.containsKey(qid)) {
                                    questionBookMap.put(qid, book);
                                }
                            });
                        }
                    }
                });

                List<NewQuestion> newQuestionList = questionLoaderClient.loadQuestionByDocIds(questionBookMap.keySet());

                Map<String, List<Long>> qidSentenceIdsMap = newQuestionList
                        .stream()
                        .collect(Collectors.toMap(NewQuestion::getDocId, NewQuestion::getSentenceIds));

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

                if (MapUtils.isNotEmpty(qidSentenceIdsMap)) {
                    for (Map.Entry<String, List<Long>> entry : qidSentenceIdsMap.entrySet()) {
                        String questionDocId = entry.getKey();
                        List<Long> sentenceIds = entry.getValue();
                        Long sentenceId = 0L;
                        if (CollectionUtils.isNotEmpty(sentenceIds)) {
                            sentenceId = sentenceIds.iterator().next();
                        }
                        qidSectionMap.put(questionDocId, sentenceIdSection.get(sentenceId));
                        qidParagraphMap.put(questionDocId, sentenceIdParagraph.get(sentenceId));
                    }
                }

                Map<Long, ChineseSentence> mapChineseSentences = chineseSentences
                        .stream()
                        .collect(Collectors.toMap(ChineseSentence::getId, o -> o));

                //重点句子id
                Set<Long> keyPointSentenceIds = new HashSet<>();
                for (Map.Entry<Long, ChineseSentence> entry : mapChineseSentences.entrySet()) {
                    ChineseSentence chineseSentence = entry.getValue();
                    if (chineseSentence != null) {
                        if (SafeConverter.toBoolean(chineseSentence.getReciteParagraph())) {
                            keyPointSentenceIds.add(entry.getKey());
                        }
                    }
                }

                //计算重点段落
                Map<String, Boolean> qidKeyPointMap = new HashMap<>();
                if (MapUtils.isNotEmpty(qidSentenceIdsMap)) {
                    for (Map.Entry<String, List<Long>> entry : qidSentenceIdsMap.entrySet()) {
                        String qid = entry.getKey();
                        List<Long> sentenceIds = entry.getValue();
                        boolean tag = true;
                        if (CollectionUtils.isNotEmpty(sentenceIds)) {
                            for (Long sentenceId : sentenceIds) {
                                if (!keyPointSentenceIds.contains(sentenceId)) {
                                    tag = false;
                                    break;
                                }
                            }
                            qidKeyPointMap.put(qid, tag);
                        }
                    }
                }

                //计算题包类型
                Map<String, QuestionBoxType> questionBoxTypeMap = new HashMap<>();
                Map<String, NewQuestion> allDocIdQuestionMap = newQuestionList
                        .stream()
                        .collect(Collectors.toMap(NewQuestion::getDocId, Function.identity()));

                if (MapUtils.isNotEmpty(boxIdQuestionIdsMap)) {
                    for (Map.Entry<String, List<String>> entry : boxIdQuestionIdsMap.entrySet()) {
                        List<String> questionIdList = entry.getValue();
                        if (CollectionUtils.isNotEmpty(questionIdList)) {
                            String questionId = questionIdList.get(0);
                            NewQuestion newQuestion = null;
                            if (allDocIdQuestionMap.containsKey(questionId)) {
                                newQuestion = allDocIdQuestionMap.get(questionId);
                            }
                            if (newQuestion != null) {
                                if (SafeConverter.toInt(newQuestion.getContentTypeId()) == 1010014) {
                                    questionBoxTypeMap.put(entry.getKey(), QuestionBoxType.READ);
                                }
                                if (SafeConverter.toInt(newQuestion.getContentTypeId()) == 1010015) {
                                    questionBoxTypeMap.put(entry.getKey(), QuestionBoxType.RECITE);
                                }
                            }
                        }
                    }
                }

                //新题库-题型
                Map<Integer, NewContentType> contentTypeMap = questionContentTypeLoaderClient.loadQuestionContentTypeAsMap();
                Map<String, NewBookCatalog> lessonMap = newContentLoaderClient.loadBookCatalogByCatalogIds(allLessonIds);

                List<NewReadReciteBO> newReadReciteBOList = new ArrayList<>();
                if (CollectionUtils.isNotEmpty(allLessonIds)) {
                    for (String lessonId : allLessonIds) {
                        NewReadReciteBO newReadReciteBO = new NewReadReciteBO();
                        newReadReciteBO.setLessonId(lessonId);
                        String lessonName = "";
                        if (MapUtils.isNotEmpty(lessonMap)) {
                            lessonName = lessonMap.get(lessonId).getName();
                        }
                        newReadReciteBO.setLessonName(lessonName);

                        List<ReadReciteBO> readReciteBOList = new ArrayList<>();
                        Set<String> questionBoxIds = new HashSet<>();
                        for (Map.Entry<String, String> entry : boxIdLessonIdMap.entrySet()) {
                            if (lessonId.equals(entry.getValue())) {
                                questionBoxIds.add(entry.getKey());
                            }
                        }

                        if (CollectionUtils.isNotEmpty(questionBoxIds)) {
                            for (String questionBoxId : questionBoxIds) {
                                ReadReciteBO readReciteBO = new ReadReciteBO();
                                readReciteBO.setQuestionBoxId(questionBoxId);
                                if (questionBoxTypeMap.get(questionBoxId) != null) {
                                    readReciteBO.setQuestionBoxType(questionBoxTypeMap.get(questionBoxId));
                                    readReciteBO.setQuestionBoxName(questionBoxTypeMap.get(questionBoxId).getName());
                                }
                                readReciteBO.setLessonName(lessonName);
                                List<String> questionIds = boxIdQuestionIdsMap.get(questionBoxId);
                                readReciteBO.setQuestionIds(questionIds);

                                List<Map<String, Object>> questionMapList = new ArrayList<>();
                                for (String questionId : questionIds) {
                                    NewQuestion newQuestion = allDocIdQuestionMap.get(questionId);
                                    if (newQuestion != null) {
                                        Map<String, Object> question = NewHomeworkContentDecorator.decorateNewQuestion(newQuestion, contentTypeMap,
                                                Collections.emptyMap(), null, questionBookMap.get(questionId));
                                        question.put("sectionNumber", qidSectionMap.get(questionId));
                                        question.put("paragraphNumber", qidParagraphMap.get(questionId));
                                        question.put("paragraphImportant", qidKeyPointMap.get(questionId));
                                        questionMapList.add(question);
                                    }
                                }

                                Comparator<Map<String, Object>> comparator = Comparator.comparingInt(a -> SafeConverter.toInt(a.get("sectionNumber")));
                                comparator = comparator.thenComparingInt(a -> SafeConverter.toInt(a.get("paragraphNumber")));
                                questionMapList = questionMapList.stream()
                                        .filter(e -> e.get("sectionNumber") != null)
                                        .filter(e -> e.get("paragraphNumber") != null)
                                        .sorted(comparator)
                                        .collect(Collectors.toList());

                                readReciteBO.setQuestions(questionMapList);
                                readReciteBO.setQuestionNum(questionMapList.size());
                                readReciteBOList.add(readReciteBO);
                            }
                            //课文内按照题包类型排序：先朗读后背诵
                            readReciteBOList = readReciteBOList.stream()
                                    .filter(e -> e.getQuestionBoxName() != null)
                                    .sorted(Comparator.comparing(ReadReciteBO::getQuestionBoxName))
                                    .collect(Collectors.toList());
                        }
                        newReadReciteBO.setReadReciteList(readReciteBOList);

                        if (CollectionUtils.isNotEmpty(readReciteBOList)) {
                            newReadReciteBOList.add(newReadReciteBO);
                        }
                    }
                }

                //按照课文排序
                newReadReciteBOList = newReadReciteBOList.stream()
                        .filter(e -> e.getLessonId() != null)
                        .sorted(Comparator.comparing(NewReadReciteBO::getLessonId))
                        .collect(Collectors.toList());

                content.add(MapUtils.m(
                        "type", "package",
                        "packages", newReadReciteBOList
                ));
            }
        } catch (Exception ex) {
            logger.error("Load chinese read recite error:", ex);
        }
        return content;
    }

    @Override
    public Map<String, Object> previewContent(Teacher teacher, String bookId, List<String> contentIdList) {
        List<Map<String, Object>> contentList = Collections.emptyList();
        //contentIdList格式："lessonId|questionBoxId|questionBoxType|questionId:1:1,questionId:2:0","..."
        Map<String, List<String>> boxIdQuestionIdsMap = new LinkedHashMap<>();
        Map<String, String> boxIdLessonIdMap = new HashMap<>();
        Map<String, String> questionIdParagraphMap = new HashMap<>();
        Set<String> readBoxIdSet = new HashSet<>();
        Set<String> reciteBoxIdSet = new HashSet<>();
        try {
            if (CollectionUtils.isNotEmpty(contentIdList)) {
                contentIdList.forEach(contentId -> {
                    if (StringUtils.isNotBlank(contentId)) {
                        String splitContentIds[] = StringUtils.split(contentId, "|");
                        if (splitContentIds.length == 4) {
                            String lessonId = splitContentIds[0];
                            String questionBoxId = splitContentIds[1];
                            String questionBoxType = splitContentIds[2];
                            String[] questions = splitContentIds[3].split(",");
                            List<String> questionIdList = new ArrayList<>();
                            if (questions.length > 0) {
                                for (String s : questions) {
                                    if (StringUtils.isNotBlank(s)) {
                                        String[] questionInfo = s.split(":");
                                        if (questionInfo.length == 4) {
                                            String qid = questionInfo[0];
                                            String sectionNum = questionInfo[1];
                                            String paragraphNum = questionInfo[2];
                                            String paragraphImportant = questionInfo[3];
                                            questionIdList.add(qid);
                                            questionIdParagraphMap.put(qid, sectionNum + "-" + paragraphNum + "-" + paragraphImportant);
                                        }
                                    }
                                }
                            }
                            boxIdQuestionIdsMap.put(questionBoxId, questionIdList);
                            boxIdLessonIdMap.put(questionBoxId, lessonId);
                            if (QuestionBoxType.READ.equals(QuestionBoxType.of(questionBoxType))) {
                                readBoxIdSet.add(questionBoxId);
                            } else if (QuestionBoxType.RECITE.equals(QuestionBoxType.of(questionBoxType))) {
                                reciteBoxIdSet.add(questionBoxId);
                            }
                        }
                    }
                });
            }

            if (CollectionUtils.isNotEmpty(readBoxIdSet) || CollectionUtils.isNotEmpty(reciteBoxIdSet)) {
                Map<String, NewBookCatalog> allLessonMap = newContentLoaderClient.loadBookCatalogByCatalogIds(boxIdLessonIdMap.values());
                Map<String, String> lessonIdNameMap = allLessonMap.values()
                        .stream()
                        .collect(Collectors.toMap(NewBookCatalog::getId, NewBookCatalog::getName));

                contentList = processPreview(readBoxIdSet, QuestionBoxType.READ, boxIdLessonIdMap, lessonIdNameMap, boxIdQuestionIdsMap,
                        questionIdParagraphMap);
                contentList.addAll(
                        processPreview(reciteBoxIdSet, QuestionBoxType.RECITE, boxIdLessonIdMap, lessonIdNameMap, boxIdQuestionIdsMap,
                                questionIdParagraphMap));
            }
        } catch (Exception ex) {
            logger.error("previewContent NEW_READ_RECITE error:", ex);
        }

        return MapUtils.m(
                "type", getObjectiveConfigType(),
                "typeName", getObjectiveConfigType().getValue(),
                "contents", contentList);
    }

    private List<Map<String, Object>> processPreview(Set<String> questionBoxIds,
                                                     QuestionBoxType questionBoxType,
                                                     Map<String, String> boxIdLessonIdMap,
                                                     Map<String, String> lessonIdNameMap,
                                                     Map<String, List<String>> boxIdQuestionIdMap,
                                                     Map<String, String> questionIdParagraphMap) {
        List<Map<String, Object>> contentList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(questionBoxIds)) {
            Map<String, Object> contentMap = new LinkedHashMap<>();
            contentMap.put("type", questionBoxType);
            contentMap.put("typeName", questionBoxType.getName());
            List<Map<String, Object>> lessonMapList = new ArrayList<>();
            for (String boxId : questionBoxIds) {
                String lessonId = boxIdLessonIdMap.get(boxId);
                String lessonName = lessonIdNameMap.get(lessonId);
                Map<String, Object> lessonMap = new HashMap<>();
                lessonMap.put("lessonId", lessonId);
                lessonMap.put("lessonName", lessonName);
                lessonMap.put("questionBoxId", boxId);
                List<String> questionIds = boxIdQuestionIdMap.get(boxId);
                List<Map<String, Object>> questionMapList = new ArrayList<>();
                for (String qid : questionIds) {
                    Map<String, Object> questionMap = new HashMap<>();
                    questionMap.put("id", qid);
                    String[] paragraphInfo = questionIdParagraphMap.get(qid).split("-");
                    if (paragraphInfo.length == 3) {
                        questionMap.put("sectionNumber", paragraphInfo[0]);
                        questionMap.put("paragraphNumber", paragraphInfo[1]);
                        questionMap.put("paragraphImportant", paragraphInfo[2].equals("1"));
                    }
                    questionMapList.add(questionMap);
                }

                //课文内按自然段排序
                Comparator<Map<String, Object>> comparator = Comparator.comparingInt(a -> SafeConverter.toInt(a.get("sectionNumber")));
                comparator = comparator.thenComparingInt(a -> SafeConverter.toInt(a.get("paragraphNumber")));
                questionMapList = questionMapList.stream()
                        .filter(e -> e.get("sectionNumber") != null)
                        .filter(e -> e.get("paragraphNumber") != null)
                        .sorted(comparator)
                        .collect(Collectors.toList());

                lessonMap.put("questions", questionMapList);
                lessonMapList.add(lessonMap);

                //相同类型下按课文排序
                lessonMapList = lessonMapList.stream()
                        .filter(e -> e.get("lessonId") != null)
                        .sorted((o1, o2) -> {
                            String s1 = (String) o1.get("lessonId");
                            String s2 = (String) o2.get("lessonId");
                            if (s1.compareTo(s2) > 0) {
                                return 1;
                            } else {
                                return -1;
                            }
                        })
                        .collect(Collectors.toList());
                contentMap.put("lessons", lessonMapList);
            }
            contentList.add(contentMap);
        }
        return contentList;
    }
}