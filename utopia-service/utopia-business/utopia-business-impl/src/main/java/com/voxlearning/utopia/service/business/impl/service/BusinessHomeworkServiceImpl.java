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

package com.voxlearning.utopia.service.business.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.ObjectUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.PrimaryReadingLayout;
import com.voxlearning.utopia.api.constant.PrimaryReadingStatus;
import com.voxlearning.utopia.api.constant.PrimaryReadingSubPage;
import com.voxlearning.utopia.api.constant.PrimaryReadingWritingStyle;
import com.voxlearning.utopia.business.api.BusinessHomeworkService;
import com.voxlearning.utopia.entity.content.Reading;
import com.voxlearning.utopia.entity.content.ReadingParts;
import com.voxlearning.utopia.entity.content.ReadingQuestions;
import com.voxlearning.utopia.entity.content.UnitKnowledgePointRef;
import com.voxlearning.utopia.service.business.api.entity.ReadingDraft;
import com.voxlearning.utopia.service.business.impl.support.BusinessServiceSpringBean;
import com.voxlearning.utopia.service.content.api.entity.Book;
import com.voxlearning.utopia.service.content.api.entity.Unit;
import com.voxlearning.utopia.service.content.client.UnitKnowledgePointServiceClient;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import org.springframework.dao.DataAccessException;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;


@Named
@Service(interfaceClass = BusinessHomeworkService.class)
@ExposeService(interfaceClass = BusinessHomeworkService.class)
@Deprecated
public class BusinessHomeworkServiceImpl extends BusinessServiceSpringBean implements BusinessHomeworkService {

    @Inject private RaikouSystem raikouSystem;
    @Inject private UnitKnowledgePointServiceClient unitKnowledgePointServiceClient;

    // ========================================================================
    // DoHomeworkServiceImpl
    // ========================================================================

    @Override
    public List<Map<String, String>> getBookInfo(String type, String queryStr) {
        List<Map<String, String>> bookInfo = new ArrayList<>();
        try {
            if ("version".equals(type)) {
                Collection<String> presses = englishContentLoaderClient.getExtension().loadEnglishAvailableBookPresses();
                int i = 0;
                for (String press : presses) {
                    Map<String, String> bookVersionMap = new HashMap<>(0);
                    bookVersionMap.put(String.valueOf(i), press);
                    bookInfo.add(bookVersionMap);
                    i++;
                }
            }

            if ("book".equals(type)) {
                List<Book> books = englishContentLoaderClient.loadEnglishBooks()
                        .enabled()
                        .online()
                        .filter(p -> ObjectUtils.compare(p.getPress(), queryStr) == 0)
                        .toList();

                for (Book book : books) {
                    Map<String, String> bookMap = new HashMap<>(0);
                    bookMap.put("id", String.valueOf(book.getId()));
                    bookMap.put("name", book.getCname());
                    bookInfo.add(bookMap);
                }
            }

            if ("unit".equals(type)) {
                List<Unit> units = englishContentLoaderClient.loadEnglishBookUnits(ConversionUtils.toLong(queryStr));
                for (Unit unit : units) {
                    Map<String, String> unitMap = new HashMap<>(0);
                    unitMap.put("id", String.valueOf(unit.getId()));
                    unitMap.put("name", unit.getCname());
                    bookInfo.add(unitMap);
                }
            }

        } catch (DataAccessException e) {
            logger.error("取学生掌握知识点失败", e);
            return null;
        }
        return bookInfo;
    }

    @Override
    public MapMessage getReadingDraftByReadingId(Long readingId) {
        String key = "ReadingDraft-ReadingId:" + readingId;
        CacheObject<ReadingDraft> cacheObject = businessCacheSystem.CBS.flushable.get(key);
        if (cacheObject == null) {
            logger.error("缓存中通过readingId获取ReadingDraft失败:{}", readingId);
            return MapMessage.errorMessage();
        }
        ReadingDraft readingDraft = cacheObject.getValue();
        try {
            if (readingDraft == null) {
                Reading reading = readingPersistence.load(readingId);
                if (reading == null) {
                    return MapMessage.errorMessage("readingID不存在" + readingId);
                }

                Map<Long, List<ReadingParts>> preloadedReadingParts = readingPartsPersistence
                        .findByReadingIds(Collections.singleton(readingId));

                readingDraft = readingToDraft(reading, preloadedReadingParts);
                TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(readingDraft.getUgcAuthor());

                ExRegion region = raikouSystem.loadRegion(teacherDetail.getRegionCode());
                if (region != null) {
                    String name = StringUtils.isNotBlank(teacherDetail.getProfile().getRealname()) ? teacherDetail.getProfile().getRealname().substring(0, 1) + "老师" : "老师";
                    if (region.isMunicipalitiy()) {
                        readingDraft.getContent().put("authorInfo", "作者: " + region.getProvinceName() + " " + name);
                    } else {
                        readingDraft.getContent().put("authorInfo", "作者: " + region.getProvinceName() + " " + region.getCityName() + " " + name);
                    }
                }

                businessCacheSystem.CBS.flushable.set(key, DateUtils.getCurrentToDayEndSecond(), readingDraft);
            }
        } catch (Exception ex) {
            logger.error("Failed to getReadingDraftByReadingId (readingId={})", readingId, ex);
            return MapMessage.errorMessage();
        }
        return MapMessage.successMessage().add("readingDraft", readingDraft);

    }

    private ReadingDraft readingToDraft(Reading reading, Map<Long, List<ReadingParts>> preloadedReadingParts) {
        String cname = reading.getCname();
        String ename = reading.getEname();
        Integer templateId = reading.getTemplateId();
        Integer colorId = reading.fetchColorId();
        String color = reading.getColor();
        String coverUri = reading.getCoverUri();
        String coverUri1 = reading.getCoverThumbnail1Uri();
        String coverUri2 = reading.getCoverThumbnail2Uri();
        String coverUri3 = reading.getCoverThumbnail2GrayUri();
        Integer difficultyLevel = reading.getDifficultyLevel();
        String status = StringUtils.isBlank(reading.getStatus()) ? "published" : reading.getStatus();
        PrimaryReadingWritingStyle primaryReadingWritingStyle = PrimaryReadingWritingStyle.getById(reading.getStyle());
        String style = primaryReadingWritingStyle != null ? primaryReadingWritingStyle.name() : null;
        Long ugcAuthor = reading.getUgcAuthor();
        Integer wordCount = reading.getWordsCount();
        Integer recommendTime = reading.getRecommendTime();
        ReadingDraft readingDraft = new ReadingDraft();
        Long readingId = reading.getId();
        List<ReadingParts> readingPartsList = preloadedReadingParts.get(readingId);
        if (readingPartsList == null) {
            readingPartsList = Collections.emptyList();
        }
        Map<String, List<ReadingParts>> readingPageMap = new LinkedHashMap<>();
        //按页码归类
        for (ReadingParts readingParts : readingPartsList) {
            String key = readingParts.getPageNum() + "|" + readingParts.getPageLayout();
            if (readingPageMap.get(key) != null) {
                readingPageMap.get(key).add(readingParts);
            } else {
                List<ReadingParts> readingPartses = new ArrayList<>();
                readingPartses.add(readingParts);
                readingPageMap.put(key, readingPartses);
            }
        }
        //
        List<Map<String, Object>> readingPages = new ArrayList<>();
        for (String key : readingPageMap.keySet()) {
            List<ReadingParts> readingPartses = readingPageMap.get(key);
            Collections.sort(readingPartses, new Comparator<ReadingParts>() {
                @Override
                public int compare(ReadingParts o1, ReadingParts o2) {
                    int rank1 = SafeConverter.toInt(o1.getRank());
                    int rank2 = SafeConverter.toInt(o2.getRank());
                    return rank1 - rank2;
                }
            });
            String layout = StringUtils.substringAfter(key, "|");
            Integer pageNum = conversionService.convert(StringUtils.substringBefore(key, "|"), Integer.class);
            Map<String, Object> pageMap = new LinkedHashMap<>();
            pageMap.put("pageNum", pageNum);
            pageMap.put("pageLayout", layout);
            if (PrimaryReadingLayout.ptpt.name().equals(layout) || PrimaryReadingLayout.tt.name().equals(layout)) {
                Map<String, Object> firstHalfPageMap = new LinkedHashMap<>();
                Map<String, Object> afterHalfPageMap = new LinkedHashMap<>();
                for (ReadingParts readingParts : readingPartses) {
                    List<Map<String, Object>> keyWordMaps = mapToList(readingParts.getKeyWords());
                    List<Map<String, Object>> keySentenceMaps = mapToList(readingParts.getKeySentences());
                    String keySentencesAnalysis = readingParts.getKeySentencesAnalysis();
                    String picUri = readingParts.getPicUri();
                    String dialogRole = readingParts.getDialogRole();
                    Integer paragraph = readingParts.getParagraph();
                    String entext = readingParts.getEntext();
                    String cntext = readingParts.getCntext();
                    Integer rank = readingParts.getRank();
                    String audioUri = readingParts.getAudioUri();
                    Map<String, Object> sentenceMap = new LinkedHashMap<>();
                    sentenceMap.put("dialogRole", dialogRole);
                    sentenceMap.put("paragraph", paragraph);
                    sentenceMap.put("entext", entext);
                    sentenceMap.put("cntext", cntext);
                    sentenceMap.put("rank", rank);
                    sentenceMap.put("audioUri", audioUri);

                    if (PrimaryReadingSubPage.firstHalfPage.getIndex() == readingParts.getSubPageNum()) {
                        if (StringUtils.isNotBlank(keySentencesAnalysis)) {
                            firstHalfPageMap.put("keySentencesAnalysis", keySentencesAnalysis);
                        }
                        firstHalfPageMap.put("picUri", picUri);

                        if (keySentenceMaps != null) {
                            Object keySentences = firstHalfPageMap.get("keySentences");
                            if (keySentences != null) {
                                List<Map<String, Object>> keySentenceList = (List<Map<String, Object>>) keySentences;
                                keySentenceList.addAll(keySentenceMaps);
                                firstHalfPageMap.put("keySentences", keySentenceList);
                            } else {
                                List<Map<String, Object>> keySentenceList = new ArrayList<>();
                                keySentenceList.addAll(keySentenceMaps);
                                firstHalfPageMap.put("keySentences", keySentenceList);
                            }
                        }

                        if (keyWordMaps != null) {
                            Object keyWords = firstHalfPageMap.get("keyWords");
                            if (keyWords != null) {
                                List<Map<String, Object>> keyWordList = (List<Map<String, Object>>) keyWords;
                                keyWordList.addAll(keyWordMaps);
                                firstHalfPageMap.put("keyWords", keyWordList);
                            } else {
                                List<Map<String, Object>> keyWordList = new ArrayList<>();
                                keyWordList.addAll(keyWordMaps);
                                firstHalfPageMap.put("keyWords", keyWordList);
                            }
                        }

                        Object sentences = firstHalfPageMap.get("readingSentences");
                        if (sentences != null) {
                            List<Map<String, Object>> sentenceList = (List<Map<String, Object>>) sentences;
                            sentenceList.add(sentenceMap);
                            firstHalfPageMap.put("readingSentences", sentenceList);
                        } else {
                            List<Map<String, Object>> sentenceList = new ArrayList<>();
                            sentenceList.add(sentenceMap);
                            firstHalfPageMap.put("readingSentences", sentenceList);
                        }

                    } else {
                        if (StringUtils.isNotBlank(keySentencesAnalysis)) {
                            firstHalfPageMap.put("keySentencesAnalysis", keySentencesAnalysis);
                        }
                        afterHalfPageMap.put("picUri", picUri);

                        if (keySentenceMaps != null) {
                            Object keySentences = afterHalfPageMap.get("keySentences");
                            if (keySentences != null) {
                                List<Map<String, Object>> keySentenceList = (List<Map<String, Object>>) keySentences;
                                keySentenceList.addAll(keySentenceMaps);
                                afterHalfPageMap.put("keySentences", keySentenceList);
                            } else {
                                List<Map<String, Object>> keySentenceList = new ArrayList<>();
                                keySentenceList.addAll(keySentenceMaps);
                                afterHalfPageMap.put("keySentences", keySentenceList);
                            }
                        }

                        if (keyWordMaps != null) {
                            Object keyWords = afterHalfPageMap.get("keyWords");
                            if (keyWords != null) {
                                List<Map<String, Object>> keyWordList = (List<Map<String, Object>>) keyWords;
                                keyWordList.addAll(keyWordMaps);
                                afterHalfPageMap.put("keyWords", keyWordList);
                            } else {
                                List<Map<String, Object>> keyWordList = new ArrayList<>();
                                keyWordList.addAll(keyWordMaps);
                                afterHalfPageMap.put("keyWords", keyWordList);
                            }
                        }


                        Object sentences = afterHalfPageMap.get("readingSentences");
                        if (sentences != null) {
                            List<Map<String, Object>> sentenceList = (List<Map<String, Object>>) sentences;
                            sentenceList.add(sentenceMap);
                            afterHalfPageMap.put("readingSentences", sentenceList);
                        } else {
                            List<Map<String, Object>> sentenceList = new ArrayList<>();
                            sentenceList.add(sentenceMap);
                            afterHalfPageMap.put("readingSentences", sentenceList);
                        }
                    }
                }
                pageMap.put("firstHalfPage", firstHalfPageMap);
                pageMap.put("afterHalfPage", afterHalfPageMap);

            } else {
                for (ReadingParts readingParts : readingPartses) {
                    List<Map<String, Object>> keyWordMaps = mapToList(readingParts.getKeyWords());
                    List<Map<String, Object>> keySentenceMaps = mapToList(readingParts.getKeySentences());
                    String keySentencesAnalysis = readingParts.getKeySentencesAnalysis();
                    String picUri = readingParts.getPicUri();
                    pageMap.put("keySentencesAnalysis", keySentencesAnalysis);
                    pageMap.put("picUri", picUri);
                    String dialogRole = readingParts.getDialogRole();
                    Integer paragraph = readingParts.getParagraph();
                    String entext = readingParts.getEntext();
                    String cntext = readingParts.getCntext();
                    Integer rank = readingParts.getRank();
                    String audioUri = readingParts.getAudioUri();
                    Map<String, Object> sentenceMap = new LinkedHashMap<>();
                    sentenceMap.put("dialogRole", dialogRole);
                    sentenceMap.put("paragraph", paragraph);
                    sentenceMap.put("entext", entext);
                    sentenceMap.put("cntext", cntext);
                    sentenceMap.put("rank", rank);
                    sentenceMap.put("audioUri", audioUri);

                    if (keySentenceMaps != null) {
                        Object keySentences = pageMap.get("keySentences");
                        if (keySentences != null) {
                            List<Map<String, Object>> keySentenceList = (List<Map<String, Object>>) keySentences;
                            keySentenceList.addAll(keySentenceMaps);
                            pageMap.put("keySentences", keySentenceList);
                        } else {
                            List<Map<String, Object>> keySentenceList = new ArrayList<>();
                            keySentenceList.addAll(keySentenceMaps);
                            pageMap.put("keySentences", keySentenceList);
                        }
                    }

                    if (keyWordMaps != null) {
                        Object keyWords = pageMap.get("keyWords");
                        if (keyWords != null) {
                            List<Map<String, Object>> keyWordList = (List<Map<String, Object>>) keyWords;
                            keyWordList.addAll(keyWordMaps);
                            pageMap.put("keyWords", keyWordList);
                        } else {
                            List<Map<String, Object>> keyWordList = new ArrayList<>();
                            keyWordList.addAll(keyWordMaps);
                            pageMap.put("keyWords", keyWordList);
                        }
                    }


                    Object sentences = pageMap.get("readingSentences");
                    if (sentences != null) {
                        List<Map<String, Object>> sentenceList = (List<Map<String, Object>>) sentences;
                        sentenceList.add(sentenceMap);
                        pageMap.put("readingSentences", sentenceList);
                    } else {
                        List<Map<String, Object>> sentenceList = new ArrayList<>();
                        sentenceList.add(sentenceMap);
                        pageMap.put("readingSentences", sentenceList);
                    }
                }


            }
            readingPages.add(pageMap);
        }
        List<ReadingQuestions> readingQuestionsList = readingQuestionsPersistence.findByReadingId(readingId);
        List<Map<String, Object>> readingQuestions = new ArrayList<>();
        for (ReadingQuestions readingQuestion : readingQuestionsList) {
            Map<String, Object> questionMap = new LinkedHashMap<>();
            questionMap.put("type", readingQuestion.getType());
            questionMap.put("content", readingQuestion.getContent());
            questionMap.put("contentPic", readingQuestion.getContentPic());
            questionMap.put("answerOptions", readingQuestion.getAnswerOptions() == null ? null : JsonUtils.fromJsonToList(readingQuestion.getAnswerOptions(), String.class));
            questionMap.put("rightAnswer", readingQuestion.getRightAnswer() == null ? null : JsonUtils.fromJsonToList(readingQuestion.getRightAnswer(), Object.class));
            questionMap.put("questionComment", readingQuestion.getQuestionComment());
            questionMap.put("rank", readingQuestion.getRank());
            questionMap.put("id", readingQuestion.getId());
            readingQuestions.add(questionMap);
        }

        //阅读理解和知识点的关联关系存
        // List<UnitKnowledgePointRef> unitKnowledgePointRefList = unitKnowledgePointRefPersistence.findByBookIdAndUnitId(readingId, -1L);
        List<UnitKnowledgePointRef> unitKnowledgePointRefList = unitKnowledgePointServiceClient.getUnitKnowledgePointService()
                .findUnitKnowledgePointRefsByBookId(readingId)
                .getUninterruptibly()
                .stream()
                .filter(e -> Objects.equals(e.getUnitId(), -1L))
                .collect(Collectors.toList());
        List<Long> points = new ArrayList<>();
        for (UnitKnowledgePointRef unitKnowledgePointRef : unitKnowledgePointRefList) {
            points.add(unitKnowledgePointRef.getPointId());
        }
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("cname", cname);
        content.put("ename", ename);
        content.put("style", style);
        content.put("difficultyLevel", difficultyLevel);
        content.put("templateId", templateId);
        content.put("points", points);
        content.put("colorId", colorId);
        content.put("color", color);
        content.put("coverUri", coverUri);
        content.put("coverUri1", coverUri1);
        content.put("coverUri2", coverUri2);
        content.put("coverUri3", coverUri3);
        content.put("ugcAuthor", ugcAuthor);
        content.put("status", status);
        content.put("readingPages", readingPages);
        content.put("readingQuestions", readingQuestions);

        readingDraft.setCname(cname);
        readingDraft.setEname(ename);
        readingDraft.setPoints(points);
        readingDraft.setDifficultyLevel(difficultyLevel);
        readingDraft.setStatus(PrimaryReadingStatus.verifyed.name());
        readingDraft.setStyle(style);
        readingDraft.setUgcAuthor(ugcAuthor);
        readingDraft.setWordsCount(wordCount);
        readingDraft.setContent(content);
        readingDraft.setRecommendTime(recommendTime);
        readingDraft.setCreateDatetime(reading.getCreateDatetime());
        readingDraft.setUpdateDatetime(new Date());
        return readingDraft;
    }

    private List<Map<String, Object>> mapToList(String content) {
        if (StringUtils.isBlank(content)) {
            return null;
        }
        Map<String, Object> contentMap = JsonUtils.fromJson(content);
        List<Map<String, Object>> contentList = new ArrayList<>();
        if (contentMap != null) {
            for (String key : contentMap.keySet()) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("entext", key);
                map.put("cntext", contentMap.get(key));
                contentList.add(map);
            }
            return contentList;
        }
        return null;
    }

    private String listToMap(String content) {
        if (StringUtils.isBlank(content)) {
            return null;
        }
        String result = null;
        List<Map> contentList = JsonUtils.fromJsonToList(content, Map.class);
        if (contentList != null && contentList.size() > 0) {
            Map<Object, Object> contentMap = new LinkedHashMap<>();
            for (Map map : contentList) {
                if (StringUtils.isNotBlank(conversionService.convert(map.get("entext"), String.class))) {
                    contentMap.put(map.get("entext"), map.get("cntext"));
                }
            }
            if (!contentMap.isEmpty()) {
                result = JsonUtils.toJson(contentMap);
            }
        }
        return result;

    }


}
