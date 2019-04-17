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

package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.work;

import com.alibaba.fastjson.JSON;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.content.api.constant.BookCatalogType;
import com.voxlearning.utopia.service.content.api.entity.ChineseSentence;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.api.entity.PracticeType;
import com.voxlearning.utopia.service.content.api.entity.WordStock;
import com.voxlearning.utopia.service.content.consumer.ChineseContentLoaderClient;
import com.voxlearning.utopia.service.content.consumer.WordStockLoaderClient;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkTag;
import com.voxlearning.utopia.service.newhomework.api.constant.ImmediateInterventionType;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.constant.WordTeachModuleType;
import com.voxlearning.utopia.service.newhomework.api.context.FinishHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.OralCommunicationQuestionResult;
import com.voxlearning.utopia.service.newhomework.api.entity.base.*;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.entity.wordspractice.ImageTextRhymeHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.*;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.newhomework.impl.dao.sub.SubHomeworkResultDao;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkProcessResultLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkResultLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.service.OralCommunicationServiceImpl;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.student.finish.FinishHomeworkProcessor;
import com.voxlearning.utopia.service.newhomework.impl.support.HomeworkTransform;
import com.voxlearning.utopia.service.newhomework.impl.support.ImageTextRhymeStarCalculator;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkSpringBean;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.content.QuestionConstants;
import com.voxlearning.utopia.service.question.api.entity.*;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Named
public class DoHomeworkProcessor extends NewHomeworkSpringBean {

    @Inject private RaikouSDK raikouSDK;

    @Inject
    private CommonConfigServiceClient commonConfigServiceClient;
    @Inject
    private ChineseContentLoaderClient chineseContentLoaderClient;
    @Inject
    private NewHomeworkLoaderImpl newHomeworkLoader;
    @Inject
    private NewHomeworkResultLoaderImpl newHomeworkResultLoader;
    @Inject
    private NewHomeworkProcessResultLoaderImpl newHomeworkProcessResultLoader;
    @Inject
    private FinishHomeworkProcessor finishHomeworkProcessor;
    @Inject
    private WordStockLoaderClient wordStockLoaderClient;
    @Inject
    private SubHomeworkResultDao subHomeworkResultDao;
    @Inject
    private OralCommunicationServiceImpl oralCommunicationClient;
    @Inject
    private ImageTextRhymeStarCalculator imageTextRhymeStarCalculator;

    // 需要链接到自学提升的
    private static List<ObjectiveConfigType> SelfStudyImproveTypeList = Arrays.asList(ObjectiveConfigType.EXAM, ObjectiveConfigType.INTELLIGENCE_EXAM, ObjectiveConfigType.MENTAL);
    private static List<Subject> SelfStudySubjectList = Arrays.asList(Subject.ENGLISH, Subject.MATH);

    public void handReadRecite(List<NewQuestion> newQuestionList, boolean needParagraph) {
        List<Long> chineseSentenceIds = newQuestionList.stream().filter(question -> CollectionUtils.isNotEmpty(question.getSentenceIds())).map(question -> question.getSentenceIds().get(0)).collect(Collectors.toList());
        List<ChineseSentence> chineseSentences = chineseContentLoaderClient.loadChineseSentenceByIds(chineseSentenceIds);
        Map<Long, ChineseSentence> mapChineseSentences = chineseSentences.stream().collect(Collectors.toMap(ChineseSentence::getId, o -> o));
        Set<String> lessonIds = new HashSet<>();
        newQuestionList.forEach(newQuestion -> {
            List<Long> sentenceIds = newQuestion.getSentenceIds();
            if (CollectionUtils.isNotEmpty(sentenceIds)) {
                ChineseSentence chineseSentence = mapChineseSentences.get(sentenceIds.get(0));
                if (CollectionUtils.isNotEmpty(newQuestion.getBooksNew())) {
                    lessonIds.add(newQuestion.getBooksNew().get(0).getLessonId());
                    newQuestion.setArticleName(newQuestion.getBooksNew().get(0).getLessonId());
                }
                if (needParagraph) {
                    newQuestion.setParagraph(chineseSentence.getParagraph().toString());
                } else {
                    newQuestion.setParagraph(transferToChineseName(chineseSentence.getParagraph().toString()));
                }

            }

        });
        Map<String, List<NewBookCatalog>> stringListMap = newContentLoaderClient.loadChildren(lessonIds, BookCatalogType.SECTION);
        newQuestionList.forEach(newQuestion -> {
            if (CollectionUtils.isNotEmpty(newQuestion.getBooksNew())) {
                List<NewBookCatalog> newBookCatalogs = stringListMap.get(newQuestion.getArticleName());
                if (CollectionUtils.isNotEmpty(newBookCatalogs)) {
                    String alias = newBookCatalogs.get(0).getAlias();
                    if (StringUtils.isBlank(alias)) {
                        newQuestion.setArticleName(newBookCatalogs.get(0).getName());
                    } else {
                        newQuestion.setArticleName(alias);
                    }
                }
            }
        });
    }

    /**
     * @param newQuestions newQuestions 可以是List<Map>/List<NewQuestion>两种类型
     * @return 返回类型根据输入的类型返回相同的类型
     */
    @SuppressWarnings("unchecked")
    public List sortHandle(List newQuestions) {
        if (CollectionUtils.isNotEmpty(newQuestions)) {
            final Map<Long, Object> map1;
            if (newQuestions.get(0) instanceof NewQuestion) {
                map1 = (Map<Long, Object>) newQuestions.stream().collect(Collectors.toMap(o -> ((NewQuestion) o).getSentenceIds().get(0), p -> p));
            } else if (newQuestions.get(0) instanceof Map) {
                map1 = (Map<Long, Object>) newQuestions.stream().collect(Collectors.toMap(o -> Long.parseLong(((List) ((Map) o).get("sentenceIds")).get(0).toString()), p -> p));
            } else {
                return Collections.EMPTY_LIST;
            }
            List<ChineseSentence> chineseSentences = chineseContentLoaderClient.loadChineseSentenceByIds(new LinkedList<>(map1.keySet()));
            Set<String> lessonIds = chineseSentences.stream().map(ChineseSentence::getBookCatalogId).collect(Collectors.toSet());
            Map<String, NewBookCatalog> lessonDatMap = newContentLoaderClient.loadBookCatalogByCatalogIds(lessonIds);
            Set<String> unitIds = lessonDatMap.values().stream().filter(o -> o.getAncestors() != null && o.getAncestors().size() > 0).map(o -> o.getAncestors().get(0).getId()).collect(Collectors.toSet());
            Map<String, NewBookCatalog> mapUnitDat = newContentLoaderClient.loadBookCatalogByCatalogIds(unitIds);
            List<Map<String, Object>> sortMapData = chineseSentences.stream().map(p -> {
                Map<String, Object> o;
                if (Objects.nonNull(p.getParagraph())) {
                    NewBookCatalog lessonDat = lessonDatMap.get(p.getBookCatalogId());
                    NewBookCatalog unitDat = lessonDat != null && lessonDat.getAncestors() != null && lessonDat.getAncestors().size() > 0 ? mapUnitDat.get(lessonDat.getAncestors().get(0).getId()) : null;
                    o = MiscUtils.m(
                            "unitRank", unitDat != null ? unitDat.getRank() : Integer.MAX_VALUE,
                            "sentenceId", p.getId(),
                            "lessonRank", lessonDat != null ? lessonDat.getRank() : Integer.MAX_VALUE,
                            "paragraph", p.getParagraph()
                    );
                } else {
                    o = MiscUtils.m(
                            "sentenceId", p.getId(),
                            "unitRank", Integer.MAX_VALUE
                    );
                }
                return o;
            }).collect(Collectors.toList());
            sortMapData.sort((o, p) -> {
                if (SafeConverter.toInt(o.get("unitRank")) != SafeConverter.toInt(p.get("unitRank"))) {
                    return Integer.compare(SafeConverter.toInt(o.get("unitRank")), SafeConverter.toInt(p.get("unitRank")));
                } else if (SafeConverter.toInt(o.get("lessonRank")) != SafeConverter.toInt(p.get("lessonRank"))) {
                    return Integer.compare(SafeConverter.toInt(o.get("lessonRank")), SafeConverter.toInt(p.get("lessonRank")));
                } else {
                    return Integer.compare(SafeConverter.toInt(o.get("paragraph")), SafeConverter.toInt(p.get("paragraph")));
                }
            });
            if (newQuestions.get(0) instanceof NewQuestion) {
                return sortMapData.stream().map(o -> (NewQuestion) map1.get(o.get("sentenceId"))).collect(Collectors.toList());
            } else if (newQuestions.get(0) instanceof Map) {
                return sortMapData.stream().map(o -> map1.get(o.get("sentenceId"))).collect(Collectors.toList());
            }
        }
        return Collections.EMPTY_LIST;
    }

    /**
     * 根据数字字符串转化为相应的第几自然段
     * 数字返回在1000内，如果超过一千内然后返回无内容字符串
     *
     * @param number 数字字符串
     * @return 根据数字字符串转化为相应的第几自然段
     */
    public String transferToChineseName(String number) {
        if (StringUtils.isBlank(number)) {
            return "";
        }
        String cNumber = NewHomeworkUtils.transferToChinese(number);
        return "第" + cNumber + "自然段";
    }

    /**
     * 根据作业Id和作业类型取题
     * categoryId和lessonId为BasicApp专用属性
     * videoId 重难点视频专属，趣味配音也复用这个字段
     * questionBoxId 题包id，新课文读背专属
     * stoneDataId 字词讲练
     * wordTeachModuleType 字词讲练模块类型
     */
    public Map<String, Object> loadHomeworkQuestions(HomeworkQuestionAnswerRequest request) {

        String homeworkId = request.getHomeworkId();
        ObjectiveConfigType objectiveConfigType = request.getObjectiveConfigType();
        Integer categoryId = request.getCategoryId();
        String lessonId = request.getLessonId();
        String videoId = request.getVideoId();
        String questionBoxId = request.getQuestionBoxId();
        String stoneDataId = request.getStoneDataId();
        WordTeachModuleType wordTeachModuleType = request.getWordTeachModuleType();

        if (StringUtils.isBlank(homeworkId) || objectiveConfigType == null) {
            return Collections.emptyMap();
        }
        if (((StringUtils.equalsIgnoreCase(objectiveConfigType.name(), ObjectiveConfigType.BASIC_APP.name())
                || StringUtils.equalsIgnoreCase(objectiveConfigType.name(), ObjectiveConfigType.LS_KNOWLEDGE_REVIEW.name())
                || StringUtils.equalsIgnoreCase(objectiveConfigType.name(), ObjectiveConfigType.NATURAL_SPELLING.name()))
                && (categoryId == null || categoryId == 0 || StringUtils.isBlank(lessonId)))
                || (ObjectiveConfigType.KEY_POINTS.equals(objectiveConfigType) && StringUtils.isBlank(videoId))
                || ((ObjectiveConfigType.NEW_READ_RECITE.equals(objectiveConfigType)
                || ObjectiveConfigType.READ_RECITE_WITH_SCORE.equals(objectiveConfigType)
                || ObjectiveConfigType.WORD_RECOGNITION_AND_READING.equals(objectiveConfigType))
                && StringUtils.isBlank(questionBoxId))
                || (ObjectiveConfigType.WORD_TEACH_AND_PRACTICE.equals(objectiveConfigType) && StringUtils.isBlank(stoneDataId) && wordTeachModuleType != null)) {
            return Collections.emptyMap();
        }

        NewHomework newHomework = newHomeworkLoader.load(homeworkId);
        if (null == newHomework) {
            return Collections.emptyMap();
        }

        List<NewHomeworkQuestion> newHomeworkQuestions;
        if (StringUtils.equalsIgnoreCase(objectiveConfigType.name(), ObjectiveConfigType.BASIC_APP.name())
                || StringUtils.equalsIgnoreCase(objectiveConfigType.name(), ObjectiveConfigType.LS_KNOWLEDGE_REVIEW.name())
                || StringUtils.equalsIgnoreCase(objectiveConfigType.name(), ObjectiveConfigType.NATURAL_SPELLING.name())) {
            newHomeworkQuestions = newHomework.findNewHomeworkQuestions(objectiveConfigType, lessonId, categoryId);
        } else if (ObjectiveConfigType.KEY_POINTS.equals(objectiveConfigType)) {
            newHomeworkQuestions = newHomework.findNewHomeworkKeyPointQuestions(objectiveConfigType, videoId);
        } else if (ObjectiveConfigType.NEW_READ_RECITE.equals(objectiveConfigType)
                || ObjectiveConfigType.READ_RECITE_WITH_SCORE.equals(objectiveConfigType)
                || ObjectiveConfigType.WORD_RECOGNITION_AND_READING.equals(objectiveConfigType)) {
            newHomeworkQuestions = newHomework.findNewHomeworkReadReciteQuestions(objectiveConfigType, questionBoxId);
        } else if (ObjectiveConfigType.DUBBING.equals(objectiveConfigType) || ObjectiveConfigType.DUBBING_WITH_SCORE.equals(objectiveConfigType)) {
            newHomeworkQuestions = newHomework.findNewHomeworkDubbingQuestions(objectiveConfigType, videoId);
        } else if (ObjectiveConfigType.LEVEL_READINGS.equals(objectiveConfigType)) {
            newHomeworkQuestions = new ArrayList<>(newHomework.findNewHomeworkQuestions(objectiveConfigType, videoId));
            newHomeworkQuestions.addAll(newHomework.findNewHomeworkOralQuestions(objectiveConfigType, videoId));
        } else if (ObjectiveConfigType.WORD_TEACH_AND_PRACTICE.equals(objectiveConfigType)) {
            newHomeworkQuestions = newHomework.findNewHomeworkWordTeachQuestions(objectiveConfigType, stoneDataId, wordTeachModuleType);
        } else {
            newHomeworkQuestions = newHomework.findNewHomeworkQuestions(objectiveConfigType);
        }

        if (CollectionUtils.isEmpty(newHomeworkQuestions)) {
            return Collections.emptyMap();
        }

        Map<String, Object> questionMap = new HashMap<>();
        Map<String, NewHomeworkBookInfo> bookInfoMap = new HashMap<>();
        Map<String, Object> extraInfo = new HashMap<>();
        NewHomeworkBook newHomeworkBook = newHomeworkLoader.loadNewHomeworkBook(homeworkId);
        if (newHomeworkBook != null && MapUtils.isNotEmpty(newHomeworkBook.getPractices())) {
            List<NewHomeworkBookInfo> newHomeworkBookInfos = newHomeworkBook.getPractices().get(objectiveConfigType);
            if (newHomeworkBookInfos != null) {
                for (NewHomeworkBookInfo info : newHomeworkBookInfos) {
                    if (CollectionUtils.isNotEmpty(info.getQuestions())) {
                        for (String qid : info.getQuestions()) {
                            bookInfoMap.put(qid, info);
                        }
                    }
                    if (CollectionUtils.isNotEmpty(info.getPapers())) {
                        for (String pid : info.getPapers()) {
                            bookInfoMap.put(pid, info);
                        }
                    }
                    if (CollectionUtils.isNotEmpty(info.getVideos())) {
                        for (String vid : info.getVideos()) {
                            bookInfoMap.put(vid, info);
                        }
                    }
                    if (CollectionUtils.isNotEmpty(info.getQuestionBoxIds())) {
                        for (String qbid : info.getQuestionBoxIds()) {
                            bookInfoMap.put(qbid, info);
                        }
                    }
                    if (CollectionUtils.isNotEmpty(info.getStoneIds())) {
                        for (String stoneId : info.getStoneIds()) {
                            bookInfoMap.put(stoneId, info);
                        }
                    }
                }
            }
        }

        List<String> allQuestionIds = new ArrayList<>();
        newHomeworkQuestions.forEach(question -> allQuestionIds.add(question.getQuestionId()));
        Set<String> eids = new LinkedHashSet<>();
        Map<String, Map<String, Object>> examUnitMap = new LinkedHashMap<>();
        Map<String, NewQuestion> mapReadReciteDate = new HashMap<>();
        // 重点段落
        Map<String, Boolean> keyPointParagraphMap = new HashMap<>();
        // 重点字词及拼音对应的句子
        Map<String, List<ChineseSentence>> qidKeyPointSentencesMap = new HashMap<>();
        // 重点字词id对应的句子
        Map<Long, ChineseSentence> wordIdSentenceMap = new HashMap<>();
        if (objectiveConfigType == ObjectiveConfigType.READ_RECITE
                || objectiveConfigType == ObjectiveConfigType.NEW_READ_RECITE
                || objectiveConfigType == ObjectiveConfigType.READ_RECITE_WITH_SCORE) {
            mapReadReciteDate = initReadReciteDate(
                    newHomeworkQuestions,
                    objectiveConfigType,
                    objectiveConfigType == ObjectiveConfigType.NEW_READ_RECITE || objectiveConfigType == ObjectiveConfigType.READ_RECITE_WITH_SCORE
            );

            keyPointParagraphMap = initReadReciteKeyPointParagraph(allQuestionIds);

            initReadReciteKeyPointWord(
                    newHomeworkQuestions,
                    objectiveConfigType,
                    mapReadReciteDate,
                    qidKeyPointSentencesMap,
                    wordIdSentenceMap
            );
        }

        int normalTime = 0;
        Map<String, List<List<Integer>>> extra = new HashMap<>();
        for (NewHomeworkQuestion question : newHomeworkQuestions) {
            String qid = question.getQuestionId();
            normalTime += question.getSeconds();

            NewHomeworkBookInfo bookInfo = bookInfoMap.get(question.getQuestionId());
            if (objectiveConfigType == ObjectiveConfigType.KEY_POINTS) {
                bookInfo = bookInfoMap.get(videoId);
            }
            if (objectiveConfigType == ObjectiveConfigType.NEW_READ_RECITE
                    || objectiveConfigType == ObjectiveConfigType.READ_RECITE_WITH_SCORE) {
                bookInfo = bookInfoMap.get(questionBoxId);
            }
            if (objectiveConfigType == ObjectiveConfigType.WORD_TEACH_AND_PRACTICE) {
                bookInfo = bookInfoMap.get(stoneDataId);
            }
            if (bookInfo != null) {
                if (objectiveConfigType == ObjectiveConfigType.READ_RECITE
                        || objectiveConfigType == ObjectiveConfigType.NEW_READ_RECITE) {
                    extra.put(question.getQuestionId(), question.getAnswerWay());
                    NewQuestion q = mapReadReciteDate.get(question.getQuestionId());
                    if (q != null) {
                        extraInfo.put(question.getQuestionId(), MiscUtils.m(
                                "articleName", q.getArticleName(),
                                "paragraphCName", q.getParagraph()
                        ));
                    }
                } else if (objectiveConfigType == ObjectiveConfigType.READ_RECITE_WITH_SCORE) {
                    extra.put(question.getQuestionId(), question.getAnswerWay());
                    NewQuestion newQuestion = mapReadReciteDate.get(question.getQuestionId());
                    if (newQuestion != null) {
                        List<Map<String, Object>> keyPointWords = new ArrayList<>();
                        List<ChineseSentence> keyWordSentences = qidKeyPointSentencesMap.get(question.getQuestionId());
                        keyWordSentences.forEach(e -> keyPointWords.add(
                                MapUtils.m(
                                        "sentence", wordIdSentenceMap.get(e.getId()) == null ? "" : wordIdSentenceMap.get(e.getId()).getContent(),
                                        "word", e.getContent(),
                                        "phonetic", e.getContentPinyinMark())));

                        extraInfo.put(question.getQuestionId(), MiscUtils.m(
                                "articleName", newQuestion.getArticleName(),
                                "paragraph", newQuestion.getParagraph(),
                                "keyPointParagraph", keyPointParagraphMap.getOrDefault(newQuestion.getId(), false),
                                "keyPointWords", keyPointWords
                        ));
                    }
                }
                examUnitMap.put(qid, MiscUtils.m("bookId", bookInfo.getBookId(),
                        "unitId", bookInfo.getUnitId(),
                        "lessonId", bookInfo.getLessonId(),
                        "unitGroupId", bookInfo.getUnitGroupId(),
                        "sectionId", bookInfo.getSectionId()
                ));
            }

            eids.add(qid);
        }

        if (objectiveConfigType == ObjectiveConfigType.WORD_TEACH_AND_PRACTICE && wordTeachModuleType.equals(WordTeachModuleType.IMAGETEXTRHYME)) {
            List<NewHomeworkApp> newHomeworkApps = newHomework.findNewHomeworkApps(ObjectiveConfigType.WORD_TEACH_AND_PRACTICE);
            LinkedList<LinkedHashMap> extraInfoList = new LinkedList<>();
            for (NewHomeworkApp newHomeworkApp : newHomeworkApps) {
                if (!stoneDataId.equals(newHomeworkApp.getStoneDataId())) {
                    continue;
                }
                List<ImageTextRhymeHomework> imageTextRhymeQuestions = newHomeworkApp.getImageTextRhymeQuestions();
                if (CollectionUtils.isEmpty(imageTextRhymeQuestions)) {
                    continue;
                }
                for (ImageTextRhymeHomework imageTextRhymeQuestion : imageTextRhymeQuestions) {
                    LinkedHashMap<String, Object> imageTextRhymeQuestionMap = new LinkedHashMap<>();
                    imageTextRhymeQuestionMap.put("chapterId", imageTextRhymeQuestion.getChapterId());
                    imageTextRhymeQuestionMap.put("questionIds", imageTextRhymeQuestion.getChapterQuestions().stream().map(NewHomeworkQuestion::getQuestionId).collect(Collectors.toList()));
                    extraInfoList.add(imageTextRhymeQuestionMap);
                }
            }
            questionMap.put("extraInfo", extraInfoList);
        }

        if (objectiveConfigType == ObjectiveConfigType.READ_RECITE
                || objectiveConfigType == ObjectiveConfigType.NEW_READ_RECITE
                || objectiveConfigType == ObjectiveConfigType.READ_RECITE_WITH_SCORE) {
            questionMap.put("extra", extra);
            questionMap.put("extraInfo", extraInfo);
        }

        if (objectiveConfigType == ObjectiveConfigType.DUBBING || objectiveConfigType == ObjectiveConfigType.DUBBING_WITH_SCORE) {
            Dubbing dubbing = dubbingLoaderClient.loadDubbingByIdIncludeDisabled(videoId);
            List<String> questionIds = newHomeworkQuestions.stream().map(NewHomeworkQuestion::getQuestionId).collect(Collectors.toList());
            Map<String, NewQuestion> dubbingQuestionMap = questionLoaderClient.loadQuestionsIncludeDisabled(questionIds);
            if (dubbing != null && MapUtils.isNotEmpty(dubbingQuestionMap)) {
                List<Long> wordStockIds = dubbing.getKeyWords()
                        .stream()
                        .filter(dubbingKeyWord -> dubbingKeyWord.getWordStockId() != null)
                        .map(Dubbing.DubbingKeyWord::getWordStockId)
                        .collect(Collectors.toList());
                Map<Long, WordStock> wordStockMap = wordStockLoaderClient.loadWordStocks(wordStockIds);
                Map<String, Dubbing.DubbingKeyWord> dubbingKeyWordMap = dubbing.getKeyWords()
                        .stream()
                        .filter(dubbingKeyWord -> dubbingKeyWord.getEnglishWord() != null)
                        .collect(Collectors.toMap(Dubbing.DubbingKeyWord::getEnglishWord, Function.identity(), (k1, k2) -> k2));
                Set<String> englishWords = new HashSet<>(dubbingKeyWordMap.keySet());
                List<Map<String, Object>> sentenceList = new ArrayList<>();
                dubbingQuestionMap.values()
                        .forEach(question -> {
                            NewQuestionOralDictOptions options = null;
                            NewQuestionsSubContents subContents = question.getContent().getSubContents().get(0);
                            if (subContents != null && subContents.getOralDict() != null && CollectionUtils.isNotEmpty(subContents.getOralDict().getOptions())) {
                                options = subContents.getOralDict().getOptions().get(0);
                            }
                            if (options != null) {
                                Map<String, Object> map = new HashMap<>();
                                List<String> englishWordList = new ArrayList<>();

                                Iterator<String> iterator = englishWords.iterator();
                                while (iterator.hasNext()) {
                                    String word = iterator.next();
                                    boolean isInclude = ContainsStr(options.getText(), word);
                                    if (isInclude) {
                                        englishWordList.add(word);
                                        iterator.remove();
                                    }
                                }
                                map.put("sentenceChineseContent", options.getCnText());
                                map.put("sentenceEnglishContent", options.getText());
                                map.put("sentenceVideoStart", options.getVoiceStart());
                                map.put("sentenceVideoEnd", options.getVoiceEnd());
                                map.put("questionId", question.getId());
                                List<Map<String, Object>> keyWordList = new ArrayList<>();
                                if (CollectionUtils.isNotEmpty(englishWordList)) {
                                    englishWordList.forEach(e -> {
                                        Dubbing.DubbingKeyWord keyWord = dubbingKeyWordMap.get(e);
                                        if (keyWord != null) {
                                            Map<String, Object> wordMap = new HashMap<>();
                                            wordMap.put("dubbing_key_word_chinese", keyWord.getChineseWord());
                                            wordMap.put("dubbing_key_word_english", keyWord.getEnglishWord());
                                            String audioUrl = null;
                                            if (MapUtils.isNotEmpty(wordStockMap)) {
                                                WordStock wordStock = wordStockMap.get(keyWord.getWordStockId());
                                                if (wordStock != null) {
                                                    if (SafeConverter.toBoolean(keyWord.getAudioIsUs(), true)) {
                                                        audioUrl = wordStock.getAudioUS();
                                                    } else {
                                                        audioUrl = wordStock.getAudioUK();
                                                    }
                                                }
                                            }
                                            if (audioUrl != null) {
                                                wordMap.put("dubbing_key_word_audio_url", audioUrl);
                                                keyWordList.add(wordMap);
                                            }
                                        }
                                    });
                                }
                                map.put("keyWordList", keyWordList);
                                sentenceList.add(map);
                            }
                        });
                //判断该配音是否是欢快歌曲类型   欢快歌曲类型ID:DC_10300000140166
                DubbingCategory dubbingCategory = dubbingLoaderClient.loadDubbingCategoriesByIds(Collections.singleton(dubbing.getCategoryId())).get(dubbing.getCategoryId());
                questionMap.put("isSong", dubbingCategory != null && Objects.equals("DC_10300000140166", dubbingCategory.getParentId()));
                questionMap.put("dubbingId", videoId);
                questionMap.put("dubbingName", dubbing.getVideoName());
                questionMap.put("videoUrl", dubbing.getVideoUrl());
                questionMap.put("backgroundMusicUrl", dubbing.getBackgroundMusic());
                questionMap.put("coverImgUrl", dubbing.getCoverUrl());
                questionMap.put("sentenceList", sentenceList);
            }
        }

        if (objectiveConfigType == ObjectiveConfigType.LEVEL_READINGS) {
            List<NewHomeworkQuestion> examQuestions = newHomework.findNewHomeworkQuestions(objectiveConfigType, videoId);
            List<NewHomeworkQuestion> oralQuestions = newHomework.findNewHomeworkOralQuestions(objectiveConfigType, videoId);
            questionMap.put("examQuestionIds", examQuestions.stream().map(NewHomeworkQuestion::getQuestionId).collect(Collectors.toList()));
            questionMap.put("oralQuestionIds", oralQuestions.stream().map(NewHomeworkQuestion::getQuestionId).collect(Collectors.toList()));
        } else {
            questionMap.put("examUnitMap", examUnitMap);
            questionMap.put("normalTime", normalTime);
            questionMap.put("eids", eids);
        }
        return questionMap;
    }

    /**
     * 当前句子是否包含某个单词
     */
    public static boolean ContainsStr(String s1, String s2) {
        String s1Lower = s1.toLowerCase();
        String s2Lower = s2.toLowerCase();
        if (s1Lower.indexOf(s2Lower) >= 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 初始化课文读背题的数据
     */
    public Map<String, NewQuestion> initReadReciteDate(List<NewHomeworkQuestion> newHomeworkQuestions, ObjectiveConfigType objectiveConfigType, Boolean needParagraph) {
        Map<String, NewQuestion> mapNewQuestions = new HashMap<>();
        if ((objectiveConfigType == ObjectiveConfigType.READ_RECITE
                || objectiveConfigType == ObjectiveConfigType.NEW_READ_RECITE
                || objectiveConfigType == ObjectiveConfigType.READ_RECITE_WITH_SCORE)
                && CollectionUtils.isNotEmpty(newHomeworkQuestions)) {
            List<String> newQuestionsIds = newHomeworkQuestions.stream().map(NewHomeworkQuestion::getQuestionId).collect(Collectors.toList());
            List<NewQuestion> newQuestionList = new LinkedList<>(questionLoaderClient.loadQuestionsIncludeDisabled(newQuestionsIds).values());
            handReadRecite(newQuestionList, needParagraph);
            mapNewQuestions = newQuestionList.stream().collect(Collectors.toMap(NewQuestion::getId, o -> o));
        }
        return mapNewQuestions;
    }

    /**
     * 课文读背重点段落
     */
    public Map<String, Boolean> initReadReciteKeyPointParagraph(Collection<String> questionIds) {
        Map<String, NewQuestion> newQuestionMap = questionLoaderClient.loadQuestionsIncludeDisabled(questionIds);
        Map<String, Boolean> result = new LinkedHashMap<>();
        List<Long> cids = newQuestionMap.values()
                .stream()
                .filter(o -> CollectionUtils.isNotEmpty(o.getSentenceIds()))
                .map(o -> o.getSentenceIds().get(0))
                .collect(Collectors.toList());
        List<ChineseSentence> chineseSentences = chineseContentLoaderClient.loadChineseSentenceByIds(cids);
        Map<Long, ChineseSentence> mapChineseSentences = chineseSentences
                .stream()
                .collect(Collectors.toMap(ChineseSentence::getId, o -> o));
        for (NewQuestion newQuestion : newQuestionMap.values()) {
            if (CollectionUtils.isEmpty(newQuestion.getSentenceIds()))
                continue;
            Long cid = newQuestion.getSentenceIds().get(0);
            if (!mapChineseSentences.containsKey(cid))
                continue;
            ChineseSentence chineseSentence = mapChineseSentences.get(cid);
            result.put(newQuestion.getId(), SafeConverter.toBoolean(chineseSentence.getReciteParagraph()));
        }
        return result;
    }

    /**
     * 课文读背重点字词
     */
    public void initReadReciteKeyPointWord(List<NewHomeworkQuestion> newHomeworkQuestions,
                                           ObjectiveConfigType objectiveConfigType,
                                           Map<String, NewQuestion> newQuestionMap,
                                           Map<String, List<ChineseSentence>> qidKeyPointSentencesMap,
                                           Map<Long, ChineseSentence> wordIdSentenceMap) {
        // 每道题的句子ids
        Map<String, List<Long>> qidSentenceIdsMap = new LinkedHashMap<>();

        if (objectiveConfigType == ObjectiveConfigType.READ_RECITE_WITH_SCORE) {
            for (NewHomeworkQuestion question : newHomeworkQuestions) {
                NewQuestion newQuestion = newQuestionMap.get(question.getQuestionId());
                List<Long> sentenceIds = newQuestion.getSentenceIds();
                qidSentenceIdsMap.put(question.getQuestionId(), sentenceIds);
            }

            // 所有的句子
            List<Long> allSentenceIds = qidSentenceIdsMap
                    .values()
                    .stream()
                    .filter(Objects::nonNull)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
            List<ChineseSentence> chineseSentences = chineseContentLoaderClient.loadChineseSentenceByIds(allSentenceIds);
            Map<Long, ChineseSentence> allSentenceMap = new HashMap<>();
            if (CollectionUtils.isNotEmpty(chineseSentences)) {
                allSentenceMap = chineseSentences
                        .stream()
                        .collect(Collectors.toMap(ChineseSentence::getId, Function.identity()));
            }

            // 每个句子的重点字词ids
            Map<Long, List<String>> sentenceIdWordIdsMap = new LinkedHashMap<>();
            if (CollectionUtils.isNotEmpty(chineseSentences)) {
                for (ChineseSentence chineseSentence : chineseSentences) {
                    Long sentenceId = chineseSentence.getId();
                    String wordIdStr = chineseSentence.getNewWordIds();
                    if (StringUtils.isNotBlank(wordIdStr)) {
                        List<String> wordIds = Arrays.asList(wordIdStr.split(","));
                        sentenceIdWordIdsMap.put(sentenceId, wordIds);
                        if (CollectionUtils.isNotEmpty(wordIds)) {
                            Map<Long, ChineseSentence> finalAllSentenceMap = allSentenceMap;
                            wordIds.stream()
                                    .filter(StringUtils::isNotBlank)
                                    .forEach(wordId -> wordIdSentenceMap.put(SafeConverter.toLong(wordId), finalAllSentenceMap.get(sentenceId)));
                        }
                    }
                }
            }
            List<Long> allWordIds = new ArrayList<>();
            if (MapUtils.isNotEmpty(sentenceIdWordIdsMap)) {
                allWordIds = sentenceIdWordIdsMap
                        .values()
                        .stream()
                        .flatMap(Collection::stream)
                        .map(SafeConverter::toLong)
                        .collect(Collectors.toList());
            }

            // 包含重点字词的句子
            Map<Long, ChineseSentence> chineseWordSentenceMap = new HashMap<>();
            if (CollectionUtils.isNotEmpty(allWordIds)) {
                List<ChineseSentence> chineseWordSentences = chineseContentLoaderClient.loadChineseSentenceByIds(allWordIds);
                if (CollectionUtils.isNotEmpty(chineseWordSentences)) {
                    chineseWordSentenceMap = chineseWordSentences
                            .stream()
                            .collect(Collectors.toMap(ChineseSentence::getId, Function.identity()));
                }
            }

            for (Map.Entry<String, List<Long>> entry : qidSentenceIdsMap.entrySet()) {
                List<ChineseSentence> keyPointWords = new ArrayList<>();
                String questionId = entry.getKey();
                List<Long> sentenceIds = entry.getValue();
                if (CollectionUtils.isNotEmpty(sentenceIds)) {
                    for (Long sentenceId : sentenceIds) {
                        List<String> sentenceIdWordIds = sentenceIdWordIdsMap.get(sentenceId);
                        if (CollectionUtils.isNotEmpty(sentenceIdWordIds)) {
                            for (String wordId : sentenceIdWordIds) {
                                keyPointWords.add(chineseWordSentenceMap.get(SafeConverter.toLong(wordId)));
                            }
                        }
                    }
                }
                qidKeyPointSentencesMap.put(questionId, keyPointWords);
            }
        }
    }

    /**
     * 取题答案
     */
    public Map<String, Object> questionAnswer(HomeworkQuestionAnswerRequest request) {

        String homeworkId = request.getHomeworkId();
        ObjectiveConfigType objectiveConfigType = request.getObjectiveConfigType();
        Long studentId = request.getStudentId();
        Integer categoryId = request.getCategoryId();
        String lessonId = request.getLessonId();
        String videoId = request.getVideoId();
        String questionBoxId = request.getQuestionBoxId();
        String stoneDataId = request.getStoneDataId();
        WordTeachModuleType wordTeachModuleType = request.getWordTeachModuleType();

        if (StringUtils.isBlank(homeworkId) || objectiveConfigType == null) {
            return Collections.emptyMap();
        }
        if ((StringUtils.equalsIgnoreCase(objectiveConfigType.name(), ObjectiveConfigType.BASIC_APP.name())
                || StringUtils.equalsIgnoreCase(objectiveConfigType.name(), ObjectiveConfigType.LS_KNOWLEDGE_REVIEW.name())
                || StringUtils.equalsIgnoreCase(objectiveConfigType.name(), ObjectiveConfigType.NATURAL_SPELLING.name()))
                && (categoryId == null || categoryId == 0 || StringUtils.isBlank(lessonId))
                || ((ObjectiveConfigType.NEW_READ_RECITE.equals(objectiveConfigType)
                || ObjectiveConfigType.READ_RECITE_WITH_SCORE.equals(objectiveConfigType)
                || ObjectiveConfigType.WORD_RECOGNITION_AND_READING.equals(objectiveConfigType))
                && StringUtils.isBlank(questionBoxId))
                || (ObjectiveConfigType.WORD_TEACH_AND_PRACTICE.equals(objectiveConfigType) && StringUtils.isBlank(stoneDataId) && wordTeachModuleType == null)) {
            return Collections.emptyMap();
        }

        Map<String, Object> questionAnswerMap = new HashMap<>();
        if (StringUtils.isNotBlank(homeworkId) && studentId != null) {
            NewHomework newHomework = newHomeworkLoader.load(homeworkId);
            if (newHomework == null) {
                return Collections.emptyMap();
            }
            NewHomeworkResult newHomeworkResult = newHomeworkResultLoader.loadNewHomeworkResult(newHomework.toLocation(), studentId, true);
            if (newHomeworkResult == null) {
                return Collections.emptyMap();
            }

            // 趣味配音这个接口不用查process，直接返回配音的相关信息
            if (ObjectiveConfigType.DUBBING == objectiveConfigType || ObjectiveConfigType.DUBBING_WITH_SCORE == objectiveConfigType) {
                Map<ObjectiveConfigType, NewHomeworkResultAnswer> newHomeworkResultAnswerMap = newHomeworkResult.getPractices();
                if (MapUtils.isEmpty(newHomeworkResultAnswerMap)
                        || (!newHomeworkResultAnswerMap.containsKey(ObjectiveConfigType.DUBBING)
                        && !newHomeworkResultAnswerMap.containsKey(ObjectiveConfigType.DUBBING_WITH_SCORE))) {
                    return Collections.emptyMap();
                }

                NewHomeworkResultAnswer answer;
                if (ObjectiveConfigType.DUBBING == objectiveConfigType) {
                    answer = newHomeworkResultAnswerMap.get(ObjectiveConfigType.DUBBING);
                } else {
                    answer = newHomeworkResultAnswerMap.get(ObjectiveConfigType.DUBBING_WITH_SCORE);
                }
                if (MapUtils.isNotEmpty(answer.getAppAnswers()) && answer.getAppAnswers().containsKey(videoId)) {
                    NewHomeworkResultAppAnswer appAnswer = answer.getAppAnswers().get(videoId);
                    Dubbing dubbing = dubbingLoaderClient.loadDubbingByIdIncludeDisabled(videoId);
                    if (dubbing != null && MapUtils.isNotEmpty(appAnswer.getAnswers())) {
                        Collection<String> resultIds = appAnswer.getAnswers().values();
                        Map<String, NewHomeworkProcessResult> newHomeworkProcessResults = newHomeworkProcessResultLoader.loads(homeworkId, resultIds);
                        Map<String, Double> questionScoreMap = new HashMap<>();
                        newHomeworkProcessResults.values().forEach(q -> questionScoreMap.put(q.getQuestionId(), q.getActualScore()));

                        List<NewHomeworkQuestion> newHomeworkQuestions = newHomework.findNewHomeworkDubbingQuestions(objectiveConfigType, videoId);
                        List<String> questionIds = newHomeworkQuestions.stream().map(NewHomeworkQuestion::getQuestionId).collect(Collectors.toList());
                        Map<String, NewQuestion> dubbingQuestionMap = questionLoaderClient.loadQuestionsIncludeDisabled(questionIds);
                        List<Map<String, Object>> sentenceList = new ArrayList<>();
                        if (MapUtils.isNotEmpty(dubbingQuestionMap)) {
                            dubbingQuestionMap.values()
                                    .forEach(question -> {
                                        NewQuestionOralDictOptions options = null;
                                        NewQuestionsSubContents subContents = question.getContent().getSubContents().get(0);
                                        if (subContents != null && subContents.getOralDict() != null && CollectionUtils.isNotEmpty(subContents.getOralDict().getOptions())) {
                                            options = subContents.getOralDict().getOptions().get(0);
                                        }
                                        if (options != null) {
                                            sentenceList.add(MapUtils.m(
                                                    "sentence_chinese_content", options.getCnText(),
                                                    "sentence_english_content", options.getText(),
                                                    "sentence_video_start", options.getVoiceStart(),
                                                    "sentence_video_end", options.getVoiceEnd(),
                                                    "question_id", question.getId(),
                                                    "question_score", questionScoreMap.get(question.getId()) != null ? questionScoreMap.get(question.getId()) : 0
                                            ));
                                        }
                                    });
                        }

                        return MapUtils.m(
                                "dubbingId", videoId,
                                "dubbingName", dubbing.getVideoName(),
                                "coverImgUrl", dubbing.getCoverUrl(),
                                "dubbingVideoUrl", appAnswer.getVideoUrl(),
                                "sentenceCount", appAnswer.getAnswers().size(),
                                "sentenceList", sentenceList,
                                "skipUploadVideo", appAnswer.getSkipUploadVideo()
                        );
                    }
                }
                return Collections.emptyMap();
            }
            if (ObjectiveConfigType.ORAL_COMMUNICATION == objectiveConfigType) {
                Map<ObjectiveConfigType, NewHomeworkResultAnswer> newHomeworkResultAnswerMap = newHomeworkResult.getPractices();
                if (MapUtils.isEmpty(newHomeworkResultAnswerMap)
                        || (!newHomeworkResultAnswerMap.containsKey(ObjectiveConfigType.ORAL_COMMUNICATION)
                )) {
                    return Collections.emptyMap();
                }
                OralCommunicationQuestionResult result = oralCommunicationClient.getHomeworkStoneAnswerInfo(newHomework, newHomeworkResult, studentId, stoneDataId);
                return MapUtils.m("result", result);
            }
            Collection<String> resultIds;
            if (StringUtils.equalsIgnoreCase(objectiveConfigType.name(), ObjectiveConfigType.BASIC_APP.name())
                    || StringUtils.equalsIgnoreCase(objectiveConfigType.name(), ObjectiveConfigType.LS_KNOWLEDGE_REVIEW.name())
                    || StringUtils.equalsIgnoreCase(objectiveConfigType.name(), ObjectiveConfigType.NATURAL_SPELLING.name())) {
                resultIds = newHomeworkResult.findHomeworkProcessIdsForBaseAppByCategoryIdAndLessonId(String.valueOf(categoryId), lessonId, objectiveConfigType);
            } else if ((StringUtils.equalsIgnoreCase(objectiveConfigType.name(), ObjectiveConfigType.KEY_POINTS.name()))) {
                resultIds = newHomeworkResult.findHomeworkProcessIdsForKeyPointsByVideoId(videoId);
            } else if (StringUtils.equalsIgnoreCase(objectiveConfigType.name(), ObjectiveConfigType.NEW_READ_RECITE.name())) {
                resultIds = newHomeworkResult.findHomeworkProcessIdsForReadReciteByQuestionBoxId(questionBoxId);
            } else if (StringUtils.equalsIgnoreCase(objectiveConfigType.name(), ObjectiveConfigType.READ_RECITE_WITH_SCORE.name())
                    || StringUtils.equalsIgnoreCase(objectiveConfigType.name(), ObjectiveConfigType.WORD_RECOGNITION_AND_READING.name())) {
                resultIds = newHomeworkResult.findHomeworkProcessIdsForReadReciteWithScoreByQuestionBoxId(questionBoxId, objectiveConfigType);
            } else if (StringUtils.equalsIgnoreCase(objectiveConfigType.name(), ObjectiveConfigType.WORD_TEACH_AND_PRACTICE.name())) {
                resultIds = newHomeworkResult.findHomeworkProcessIdsForWordTeachByStoneDataId(stoneDataId, objectiveConfigType, wordTeachModuleType);
            } else if (StringUtils.equalsIgnoreCase(objectiveConfigType.name(), ObjectiveConfigType.READING.name())
                    || StringUtils.equalsIgnoreCase(objectiveConfigType.name(), ObjectiveConfigType.LEVEL_READINGS.name())) {
                // 根据configType类型，复用videoId
                resultIds = newHomeworkResult.findHomeworkProcessIdsForReading(videoId, objectiveConfigType);
            } else {
                resultIds = newHomeworkResult.findHomeworkProcessIdsByObjectiveConfigType(objectiveConfigType);
            }

            Map<String, NewHomeworkProcessResult> newHomeworkProcessResults = newHomeworkProcessResultLoader.loads(homeworkId, resultIds);
            if (newHomeworkProcessResults != null) {
                Collection<NewHomeworkProcessResult> processResults = newHomeworkProcessResults.values();

                // 2016-11-18发生了一件事情，移动端的连线题提交数据倒序了，在这里做一下兼容处理
                Set<String> questionSet = processResults.stream().filter(o -> StringUtils.isNotBlank(o.getQuestionId())).map(NewHomeworkProcessResult::getQuestionId).collect(Collectors.toSet());
                Map<String, NewQuestion> questionMap = questionLoaderClient.loadQuestionsIncludeDisabled(questionSet);

                for (NewHomeworkProcessResult processResult : processResults) {
                    String key = processResult.getQuestionId();
                    NewQuestion q = questionMap.get(key);
                    if (q == null) continue;
                    Map<String, Object> value;
                    if (Objects.equals(q.getContent().getSubContents().get(0).getSubContentTypeId(), QuestionConstants.LianXianTi)) {
                        // 连线题的一个特殊处理
                        processResult.reverseForLianXianTi();
                    }

                    List<List<String>> oralAudios = new ArrayList<>();
                    List<List<List<NaturalSpellingSentence>>> sentences = new ArrayList<>();
                    if (CollectionUtils.isNotEmpty(processResult.getOralDetails())) {
                        List<List<BaseHomeworkProcessResult.OralDetail>> oralDetailList = processResult.getOralDetails();
                        for (List<BaseHomeworkProcessResult.OralDetail> list1 : oralDetailList) {
                            List<String> audios = new ArrayList<>();
                            List<List<NaturalSpellingSentence>> sentenceList = new ArrayList<>();
                            if (CollectionUtils.isNotEmpty(list1)) {
                                for (BaseHomeworkProcessResult.OralDetail oralDetail : list1) {
                                    audios.add(oralDetail.getAudio());
                                    sentenceList.add(oralDetail.getSentences());
                                }
                            }
                            if (CollectionUtils.isNotEmpty(audios)) {
                                oralAudios.add(audios);
                            }
                            if (CollectionUtils.isNotEmpty(sentenceList)) {
                                sentences.add(sentenceList);
                            }
                        }
                    }

                    List<String> imgList = new ArrayList<>();
                    List<String> correctList = new ArrayList<>();
                    if (CollectionUtils.isNotEmpty(processResult.getFiles())) {
                        for (List<NewHomeworkQuestionFile> fileList : processResult.getFiles()) {
                            if (CollectionUtils.isNotEmpty(fileList)) {
                                for (NewHomeworkQuestionFile file : fileList) {
                                    imgList.add("https://oss-image.17zuoye.com/" + file.getRelativeUrl());
                                }
                            }
                        }
                    }

                    Boolean intervention = processResult.isInterventionExcludeCompositeQuestion();
                    String hintDescription = null;
                    List<List> interventionAnswer = null;
                    if (intervention) {
                        ImmediateInterventionType immediateInterventionType = processResult.getImmediateInterventionType();
                        if (immediateInterventionType != null) {
                            hintDescription = immediateInterventionType.getDescription();
                        }
                        interventionAnswer = JSON.parseArray(processResult.getInterventionStringAnswer(), List.class);
                    }
                    value = MapUtils.m(
                            "oralAudios", oralAudios,
                            "files", processResult.getFiles(),
                            "subMaster", processResult.getSubGrasp(),
                            "master", processResult.getGrasp(),
                            "userAnswers", processResult.getUserAnswers(),
                            "fullScore", processResult.getStandardScore(),
                            "score", processResult.getScore(),
                            "oralScoreLevel", processResult.getAppOralScoreLevel(),
                            "intervention", intervention,//是否命中干预
                            "hintDescription", hintDescription,
                            "interventionAnswer", interventionAnswer,
                            "imgList", imgList,
                            "correctList", correctList
                    );
                    if (ObjectiveConfigType.READ_RECITE_WITH_SCORE.equals(objectiveConfigType)) {
                        List<List<NaturalSpellingSentence.Word>> wordList = new ArrayList<>();
                        List<NaturalSpellingSentence> spellingSentences = sentences.get(0).get(0);
                        if (CollectionUtils.isNotEmpty(spellingSentences)) {
                            for (NaturalSpellingSentence naturalSpellingSentence : spellingSentences) {
                                List<NaturalSpellingSentence.Word> words = naturalSpellingSentence.getWords();
                                if (CollectionUtils.isNotEmpty(words)) {
                                    wordList.add(words);
                                }
                            }
                        }
                        value.putAll(MapUtils.m(
                                "standardScore", processResult.getActualScore(),
                                "words", wordList
                        ));
                    }
                    Map<String, String> additions = processResult.getAdditions();
                    if (additions != null && additions.containsKey("hwTrajectory")) {
                        value.put("hwTrajectory", JSON.parseArray(additions.get("hwTrajectory"), List.class));
                    }
                    questionAnswerMap.put(key, value);
                }
            }
        }
        return questionAnswerMap;
    }

    /**
     * 获取字词讲练作业应试试题信息
     * 字词讲练-字词训练模块PC报告专用
     */
    public Map<String, Object> loadWordTeachQuestionsAnswer(HomeworkQuestionAnswerRequest request) {
        String homeworkId = request.getHomeworkId();
        ObjectiveConfigType objectiveConfigType = request.getObjectiveConfigType();
        Long studentId = request.getStudentId();

        if (StringUtils.isBlank(homeworkId) || objectiveConfigType == null || !ObjectiveConfigType.WORD_TEACH_AND_PRACTICE.equals(objectiveConfigType)) {
            return Collections.emptyMap();
        }

        Map<String, Object> stoneQuestionAnswerMap = new LinkedHashMap<>();
        if (StringUtils.isNotBlank(homeworkId) && studentId != null) {
            NewHomework newHomework = newHomeworkLoader.load(homeworkId);
            if (newHomework == null) {
                return Collections.emptyMap();
            }
            NewHomeworkResult newHomeworkResult = newHomeworkResultLoader.loadNewHomeworkResult(newHomework.toLocation(), studentId, true);
            if (newHomeworkResult == null) {
                return Collections.emptyMap();
            }
            Map<String, List> stoneResultIds = new LinkedHashMap<>();
            if (StringUtils.equalsIgnoreCase(objectiveConfigType.name(), ObjectiveConfigType.WORD_TEACH_AND_PRACTICE.name())) {
                stoneResultIds = newHomeworkResult.findHomeworkProcessIdsForWordTeachByModuleType(objectiveConfigType, WordTeachModuleType.WORDEXERCISE);
            }

            for (Map.Entry<String, List> entry : stoneResultIds.entrySet()) {
                String stoneDataId = entry.getKey();
                Map<String, NewHomeworkProcessResult> newHomeworkProcessResults = newHomeworkProcessResultLoader.loads(homeworkId, entry.getValue());
                if (newHomeworkProcessResults != null) {
                    Collection<NewHomeworkProcessResult> processResults = newHomeworkProcessResults.values();
                    Set<String> questionSet = processResults.stream().filter(o -> StringUtils.isNotBlank(o.getQuestionId())).map(NewHomeworkProcessResult::getQuestionId).collect(Collectors.toSet());
                    Map<String, NewQuestion> questionMap = questionLoaderClient.loadQuestionsIncludeDisabled(questionSet);

                    Map<String, Object> questionAnswerMap = new HashMap<>();
                    for (NewHomeworkProcessResult processResult : processResults) {
                        String key = processResult.getQuestionId();
                        NewQuestion q = questionMap.get(key);
                        if (q == null) {
                            continue;
                        }
                        Map<String, Object> value;
                        if (Objects.equals(q.getContent().getSubContents().get(0).getSubContentTypeId(), QuestionConstants.LianXianTi)) {
                            // 连线题的一个特殊处理
                            processResult.reverseForLianXianTi();
                        }

                        value = MapUtils.m(
                                "oralAudios", new ArrayList<>(),
                                "files", processResult.getFiles(),
                                "subMaster", processResult.getSubGrasp(),
                                "master", processResult.getGrasp(),
                                "userAnswers", processResult.getUserAnswers(),
                                "fullScore", processResult.getStandardScore(),
                                "score", processResult.getScore(),
                                "oralScoreLevel", processResult.getAppOralScoreLevel(),
                                "intervention", processResult.isIntervention(),//是否命中干预
                                "hintDescription", "",
                                "interventionAnswer", processResult.getUserAnswers(),
                                "imgList", new ArrayList<>(),
                                "correctList", new ArrayList<>()
                        );
                        questionAnswerMap.put(key, value);
                    }
                    stoneQuestionAnswerMap.put(stoneDataId, questionAnswerMap);
                }
            }
        }
        return stoneQuestionAnswerMap;
    }

    /**
     * 取需要订正的所有的题，做错的部分才需要，所以有学生id
     * 目前只有数学EXAM有错题订正的功能
     *
     * @param homeworkId          作业id
     * @param objectiveConfigType 作业类型
     * @param studentId           学生id
     * @return Map
     */
    public Map<String, Object> loadHomeworkCorrectQuestions(String homeworkId, ObjectiveConfigType objectiveConfigType, Long studentId) {

        if (StringUtils.isBlank(homeworkId) || objectiveConfigType == null) {
            return Collections.emptyMap();
        }
        NewHomework newHomework = newHomeworkLoader.load(homeworkId);
        if (null == newHomework) {
            return Collections.emptyMap();
        }
        Map<String, NewHomeworkQuestion> newHomeworkQuestionMap = newHomework.findNewHomeworkQuestions(objectiveConfigType)
                .stream()
                .collect(Collectors.toMap(NewHomeworkQuestion::getQuestionId, Function.identity()));
        if (MapUtils.isEmpty(newHomeworkQuestionMap)) {
            return Collections.emptyMap();
        }
        Map<String, Object> questionMap = new HashMap<>();

        // 所有的qid|similarQid为key的带有教材信息的map
        Map<String, NewHomeworkBookInfo> bookInfoMap = new HashMap<>();
        NewHomeworkBook newHomeworkBook = newHomeworkLoader.loadNewHomeworkBook(homeworkId);
        if (newHomeworkBook != null) {
            List<NewHomeworkBookInfo> newHomeworkBookInfos = newHomeworkBook.getPractices().get(objectiveConfigType);
            if (newHomeworkBookInfos != null) {
                newHomeworkBookInfos.stream()
                        .filter(info -> CollectionUtils.isNotEmpty(info.getQuestions()))
                        .forEach(info -> {
                            for (String qid : info.getQuestions()) {
                                NewHomeworkQuestion homeworkQuestion = newHomeworkQuestionMap.getOrDefault(qid, null);
                                if (homeworkQuestion != null) {
                                    String similarQid = homeworkQuestion.getSimilarQuestionId();
                                    bookInfoMap.put(qid + "|" + similarQid, info);
                                }
                            }
                        });
            }
        }
        // end

        // 当前学生的所有错题
        NewHomeworkResult newHomeworkResult = newHomeworkResultLoader.loadNewHomeworkResult(newHomework.toLocation(), studentId, true);
        List<String> processIds = newHomeworkResult.findHomeworkProcessIdsByObjectiveConfigType(objectiveConfigType);
        List<String> wrongQids = newHomeworkProcessResultLoader.loads(newHomework.getId(), processIds)
                .values()
                .stream()
                .filter(o -> !Objects.equals(o.getGrasp(), Boolean.TRUE))
                .map(NewHomeworkProcessResult::getQuestionId)
                .collect(Collectors.toList());

        // 拼装带有教材信息的所需数据
        Map<String, Map<String, Object>> examUnitMap = new LinkedHashMap<>();
        int normalTime = 0;
        for (String tempQid : wrongQids) {
            for (NewHomeworkQuestion question : newHomeworkQuestionMap.values()) {
                if (StringUtils.equals(question.getQuestionId(), tempQid)) {
                    String qid = question.getQuestionId();
                    normalTime += question.getSeconds();
                    String key = qid + "|" + question.getSimilarQuestionId();
                    NewHomeworkBookInfo bookInfo = bookInfoMap.get(key);
                    if (bookInfo != null) {
                        examUnitMap.put(
                                key, MiscUtils.m(
                                        "bookId", bookInfo.getBookId(),
                                        "unitId", bookInfo.getUnitId(),
                                        "lessonId", bookInfo.getLessonId(),
                                        "unitGroupId", bookInfo.getUnitGroupId(),
                                        "sectionId", bookInfo.getSectionId(),
                                        "sourceQuestionId", qid,
                                        "similarQuestionId", question.getSimilarQuestionId()
                                )
                        );
                    } else {
                        examUnitMap.put(
                                key, MiscUtils.m(
                                        "sourceQuestionId", qid,
                                        "similarQuestionId", question.getSimilarQuestionId()
                                )
                        );
                    }
                }
            }
        }

        questionMap.put("examUnitMap", examUnitMap);
        questionMap.put("normalTime", normalTime);
        questionMap.put("eids", wrongQids);

        Map<String, String> wrongReasonMap = new LinkedHashMap<>();
        for (QuestionWrongReason o : QuestionWrongReason.values()) {
            wrongReasonMap.put(o.name(), o.getDesc());
        }

        questionMap.put("wrongReason", wrongReasonMap);
        questionMap.put("homeworkTag", HomeworkTag.Correct);
        return questionMap;
    }

    /**
     * 当前学生已经订正过的答案
     * 目前只有数学EXAM有错题订正的功能
     *
     * @param homeworkId          作业id
     * @param objectiveConfigType 作业形式
     * @param studentId           学生id
     * @return Map
     */
    public Map<String, Object> loadCorrectQuestionAnswer(String homeworkId, ObjectiveConfigType objectiveConfigType, Long studentId) {
        if (StringUtils.isBlank(homeworkId) || objectiveConfigType == null) {
            return Collections.emptyMap();
        }
        Map<String, Object> questionAnswerMap = new HashMap<>();
        if (StringUtils.isNotBlank(homeworkId) && studentId != null) {
            NewHomework newHomework = newHomeworkLoader.load(homeworkId);
            if (newHomework == null) {
                return Collections.emptyMap();
            }
            // source, similar
//            Map<String, String> sourceSimilarMap = newHomework.findSimilarQuestionIds(objectiveConfigType);

            // 取错题
            NewHomeworkResult newHomeworkResult = newHomeworkResultLoader.loadNewHomeworkResult(newHomework.toLocation(), studentId, true);
            if (newHomeworkResult == null || newHomeworkResult.getPractices() == null) {
                return Collections.emptyMap();
            }
            NewHomeworkResultAnswer newHomeworkResultAnswer = newHomeworkResult.getPractices().get(objectiveConfigType);
            if (newHomeworkResultAnswer == null) {
                return Collections.emptyMap();
            }
            Collection<String> wrongResultId = newHomeworkResultAnswer.getAnswers().values();
            Map<String, NewHomeworkProcessResult> wrongProcessResult = newHomeworkProcessResultLoader.loads(homeworkId, wrongResultId);
            if (wrongProcessResult != null) {
                for (NewHomeworkProcessResult processResult : wrongProcessResult.values()) {
                    Map<String, Object> questionAnswer = new HashMap<>();
                    questionAnswer.put("subMaster", processResult.getSubGrasp());
                    questionAnswer.put("master", processResult.getGrasp());
                    questionAnswer.put("userAnswers", processResult.getUserAnswers());
                    questionAnswer.put("fullScore", processResult.getStandardScore());
                    questionAnswer.put("score", processResult.getScore());
                    questionAnswer.put("sourceQuestionId", processResult.getSourceQuestionId());
                    questionAnswer.put("similarQuestionId", processResult.getQuestionId());

                    questionAnswerMap.put(processResult.getQuestionId(), questionAnswer);
                }
            }
        }
        return questionAnswerMap;
    }

    public void finishHomework(NewHomework newHomework, NewHomeworkResult newHomeworkResult, Long studentId) {
        if (newHomeworkResult != null && !newHomeworkResult.getPractices().isEmpty()) {
            LogCollector.info("backend-general", MiscUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", studentId,
                    "mod1", newHomework.getId(),
                    "op", "repair newhomework result"
            ));
            AlpsThreadPool.getInstance().submit(() -> {
                // 按分组读取班级信息
                Long groupId = newHomework.getClazzGroupId();
                GroupMapper group = raikouSDK.getClazzClient()
                        .getGroupLoaderClient()
                        .loadGroupDetail(groupId, false)
                        .firstOrNull();
                ObjectiveConfigType objectiveConfigType = null;
                for (ObjectiveConfigType type : newHomeworkResult.getPractices().keySet()) {
                    NewHomeworkResultAnswer ra = newHomeworkResult.getPractices().get(type);
                    if (!ra.isFinished()) {
                        objectiveConfigType = type;
                        break;
                    }
                }
                // 所有类型都已完成，用最后一个类型来修复数据
                if (objectiveConfigType == null) {
                    for (ObjectiveConfigType type : newHomeworkResult.getPractices().keySet()) {
                        objectiveConfigType = type;
                    }
                }
                if (objectiveConfigType != null) {
                    NewHomeworkResultAnswer newHomeworkResultAnswer = newHomeworkResult.getPractices().get(objectiveConfigType);
                    if (ObjectiveConfigType.BASIC_APP.equals(objectiveConfigType)
                            || ObjectiveConfigType.LS_KNOWLEDGE_REVIEW.equals(objectiveConfigType)
                            || ObjectiveConfigType.NATURAL_SPELLING.equals(objectiveConfigType)) {
                        if (newHomeworkResultAnswer != null && newHomeworkResultAnswer.getAppAnswers() != null) {
                            for (NewHomeworkResultAppAnswer appAnswer : newHomeworkResultAnswer.getAppAnswers().values()) {
                                if (appAnswer.getFinishAt() == null && appAnswer.getAnswers() != null) {
                                    Collection<String> processIds = appAnswer.getAnswers().values();
                                    Integer categoryId = appAnswer.getCategoryId() != null ? appAnswer.getCategoryId() : 0;
                                    String lessonId = appAnswer.getLessonId();
                                    String key = StringUtils.join(Arrays.asList(categoryId, lessonId), "-");
                                    Double score = 0d;
                                    Long duration = 0L;
                                    if (CollectionUtils.isNotEmpty(processIds)) {
                                        // 布置的题目和做过的题一致，将剩下的属性补全
                                        Map<String, NewHomeworkProcessResult> processResultMap = newHomeworkProcessResultLoader.loads(newHomework.getId(), processIds);
                                        for (NewHomeworkProcessResult npr : processResultMap.values()) {
                                            score += npr.getScore();
                                            duration += npr.getDuration();
                                        }
                                        Double avgScore = score;
                                        Long practiceId = appAnswer.getPracticeId();
                                        if (practiceId == null && MapUtils.isNotEmpty(processResultMap)) {
                                            // 处理新结构丢数据的情况
                                            NewHomeworkProcessResult newHomeworkProcessResult = processResultMap.values().iterator().next();
                                            practiceId = newHomeworkProcessResult.getPracticeId();
                                            lessonId = newHomeworkProcessResult.getLessonId();
                                            categoryId = newHomeworkProcessResult.getCategoryId();
                                            key = StringUtils.join(Arrays.asList(categoryId, lessonId), "-");
                                            NewHomeworkResultAppAnswer newHomeworkResultAppAnswer = new NewHomeworkResultAppAnswer();
                                            newHomeworkResultAppAnswer.setPracticeId(practiceId);
                                            newHomeworkResultAppAnswer.setLessonId(lessonId);
                                            newHomeworkResultAppAnswer.setCategoryId(categoryId);
                                            newHomeworkResultAppAnswer.setAnswers(new LinkedHashMap<>());
                                            newHomeworkResultAppAnswer.setOralAnswers(new LinkedHashMap<>());
                                            newHomeworkResultService.doHomeworkBasicAppPractice(newHomework.toLocation(), studentId, objectiveConfigType, key, newHomeworkResultAppAnswer);
                                        }
                                        PracticeType practiceType = practiceLoaderClient.loadPractice(practiceId);
                                        //跟读题打分是根据引擎分数来的，每句话分数都是100制，所以需要求个平均分
                                        if (practiceType.getNeedRecord()) {
                                            avgScore = new BigDecimal(score).divide(new BigDecimal(processResultMap.size()), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
                                        }
                                        newHomeworkResultService.finishHomeworkBasicAppPractice(newHomework, studentId, objectiveConfigType, key, avgScore, duration);
                                    } else {
                                        score = 100D;
                                        duration = NewHomeworkUtils.processDuration(0L);
                                        newHomeworkResultService.finishHomeworkBasicAppPractice(newHomework, studentId, objectiveConfigType, key, score, duration);
                                    }
                                }
                            }
                        }
                    } else if (ObjectiveConfigType.READING.equals(objectiveConfigType)) {
                        if (newHomeworkResultAnswer != null && newHomeworkResultAnswer.getAppAnswers() != null) {
                            for (NewHomeworkResultAppAnswer appAnswer : newHomeworkResultAnswer.getAppAnswers().values()) {
                                if (appAnswer.getFinishAt() == null && MapUtils.isNotEmpty(appAnswer.getAnswers())) {
                                    String pictureBookId = appAnswer.getPictureBookId();
                                    Set<String> processIds = new HashSet<>(appAnswer.getAnswers().values());
                                    Set<String> oralProcessIds = new HashSet<>();
                                    if (MapUtils.isNotEmpty(appAnswer.getOralAnswers())) {
                                        processIds.addAll(appAnswer.getOralAnswers().values());
                                        oralProcessIds.addAll(appAnswer.getOralAnswers().values());
                                    }
                                    if (pictureBookId == null) {
                                        Double score = 0d;
                                        Long duration = 0L;
                                        Map<String, NewHomeworkProcessResult> processResultMap = newHomeworkProcessResultLoader.loads(newHomework.getId(), processIds);
                                        for (NewHomeworkProcessResult npr : processResultMap.values()) {
                                            if (!oralProcessIds.contains(npr.getId())) {
                                                score += npr.getScore();
                                            }
                                            duration += npr.getDuration();
                                        }
                                        NewHomeworkProcessResult newHomeworkProcessResult = processResultMap.values().iterator().next();
                                        pictureBookId = newHomeworkProcessResult.getPictureBookId();

                                        NewHomeworkResultAppAnswer newHomeworkResultAppAnswer = new NewHomeworkResultAppAnswer();
                                        newHomeworkResultAppAnswer.setScore(score);
                                        newHomeworkResultAppAnswer.setDuration(duration);
                                        newHomeworkResultAppAnswer.setPictureBookId(pictureBookId);
                                        newHomeworkResultAppAnswer.setConsumeTime(duration);
                                        newHomeworkResultAppAnswer.setAnswers(new LinkedHashMap<>());
                                        newHomeworkResultAppAnswer.setOralAnswers(new LinkedHashMap<>());
                                        newHomeworkResultAppAnswer.setFinishAt(new Date());
                                        newHomeworkResultService.doHomeworkBasicAppPractice(newHomework.toLocation(), studentId, objectiveConfigType, pictureBookId, newHomeworkResultAppAnswer);
                                    }
                                }
                            }
                        }
                    } else if (ObjectiveConfigType.KEY_POINTS.equals(objectiveConfigType)) {
                        if (newHomeworkResultAnswer != null && newHomeworkResultAnswer.getAppAnswers() != null) {
                            for (NewHomeworkResultAppAnswer appAnswer : newHomeworkResultAnswer.getAppAnswers().values()) {
                                String videoId = appAnswer.getVideoId();
                                List<String> processIds = newHomeworkResult.findHomeworkProcessIdsForKeyPointsByVideoId(videoId);
                                Map<String, NewHomeworkProcessResult> processResultMap = newHomeworkProcessResultLoader.loads(newHomework.getId(), processIds);
                                Double score = 0d;
                                Long duration = 0L;
                                boolean allQuestionsRight = true;
                                for (NewHomeworkProcessResult npr : processResultMap.values()) {
                                    score += npr.getScore();
                                    duration += npr.getDuration();
                                    if (!SafeConverter.toBoolean(npr.getGrasp())) {
                                        allQuestionsRight = false;
                                    }
                                }
                                //当题目全部正确时，但是总分计算结果不是100分就把总分设置为100分
                                if (allQuestionsRight && score != null && score < 100D) {
                                    score = 100D;
                                }
                                newHomeworkResultService.finishHomeworkKeyPoint(newHomework.toLocation(), studentId, objectiveConfigType, videoId, score, duration);
                            }
                        }
                    } else if (ObjectiveConfigType.NEW_READ_RECITE.equals(objectiveConfigType)) {
                        if (newHomeworkResultAnswer != null && newHomeworkResultAnswer.getAppAnswers() != null) {
                            for (NewHomeworkResultAppAnswer appAnswer : newHomeworkResultAnswer.getAppAnswers().values()) {
                                String questionBoxId = appAnswer.getQuestionBoxId();
                                List<String> processIds = newHomeworkResult.findHomeworkProcessIdsForReadReciteByQuestionBoxId(questionBoxId);
                                Map<String, NewHomeworkProcessResult> processResultMap = newHomeworkProcessResultLoader.loads(newHomework.getId(), processIds);
                                Long duration = 0L;
                                for (NewHomeworkProcessResult npr : processResultMap.values()) {
                                    duration += npr.getDuration();
                                }
                                newHomeworkResultService.finishHomeworkKeyPoint(newHomework.toLocation(), studentId, objectiveConfigType, questionBoxId, null, duration);
                            }
                        }
                    } else if (ObjectiveConfigType.READ_RECITE_WITH_SCORE.equals(objectiveConfigType)) {
                        if (newHomeworkResultAnswer != null && newHomeworkResultAnswer.getAppAnswers() != null) {
                            for (NewHomeworkResultAppAnswer appAnswer : newHomeworkResultAnswer.getAppAnswers().values()) {
                                String questionBoxId = appAnswer.getQuestionBoxId();
                                List<String> processIds = newHomeworkResult.findHomeworkProcessIdsForReadReciteWithScoreByQuestionBoxId(questionBoxId, objectiveConfigType);
                                Map<String, NewHomeworkProcessResult> processResultMap = newHomeworkProcessResultLoader.loads(newHomework.getId(), processIds);
                                Long duration = 0L;
                                Double score = 0D;
                                Integer standardNum = 0;
                                for (NewHomeworkProcessResult npr : processResultMap.values()) {
                                    duration += npr.getDuration();
                                    score += npr.getScore();
                                    if (npr.getGrasp() != null && Boolean.TRUE.equals(npr.getGrasp())) {
                                        ++standardNum;
                                    }
                                }
                                Integer appQuestionNum = 0;
                                List<NewHomeworkQuestion> newHomeworkQuestions = newHomework.findNewHomeworkReadReciteQuestions(
                                        ObjectiveConfigType.READ_RECITE_WITH_SCORE,
                                        questionBoxId);
                                if (CollectionUtils.isNotEmpty(newHomeworkQuestions)) {
                                    appQuestionNum = newHomeworkQuestions.size();
                                }
                                newHomeworkResultService.finishHomeworkReadReciteWithScore(
                                        newHomework.toLocation(),
                                        studentId,
                                        objectiveConfigType,
                                        questionBoxId,
                                        score,
                                        duration,
                                        standardNum,
                                        appQuestionNum);
                            }
                        }
                    } else if (ObjectiveConfigType.WORD_RECOGNITION_AND_READING.equals(objectiveConfigType)) {
                        if (newHomeworkResultAnswer != null && newHomeworkResultAnswer.getAppAnswers() != null) {
                            for (NewHomeworkResultAppAnswer appAnswer : newHomeworkResultAnswer.getAppAnswers().values()) {
                                String questionBoxId = appAnswer.getQuestionBoxId();
                                List<String> processIds = newHomeworkResult.findHomeworkProcessIdsForReadReciteWithScoreByQuestionBoxId(questionBoxId, objectiveConfigType);
                                Map<String, NewHomeworkProcessResult> processResultMap = newHomeworkProcessResultLoader.loads(newHomework.getId(), processIds);
                                Double score = 0d;
                                Long duration = 0L;
                                boolean allQuestionsRight = true;
                                for (NewHomeworkProcessResult npr : processResultMap.values()) {
                                    score += npr.getScore();
                                    duration += npr.getDuration();
                                    if (!SafeConverter.toBoolean(npr.getGrasp())) {
                                        allQuestionsRight = false;
                                    }
                                }
                                //当题目全部正确时，但是总分计算结果不是100分就把总分设置为100分
                                if (allQuestionsRight && score != null && score < 100D) {
                                    score = 100D;
                                }
                                newHomeworkResultService.finishHomeworkKeyPoint(newHomework.toLocation(), studentId, objectiveConfigType, questionBoxId, score, duration);
                            }
                        }
                    } else if (ObjectiveConfigType.WORD_TEACH_AND_PRACTICE.equals(objectiveConfigType)) {
                        if (newHomeworkResultAnswer != null && newHomeworkResultAnswer.getAppAnswers() != null) {
                            for (NewHomeworkResultAppAnswer appAnswer : newHomeworkResultAnswer.getAppAnswers().values()) {
                                String stoneId = appAnswer.getStoneId();

                                List<NewHomeworkApp> newHomeworkApps = newHomework.findNewHomeworkApps(objectiveConfigType);
                                NewHomeworkApp newHomeworkApp = new NewHomeworkApp();
                                for (NewHomeworkApp app : newHomeworkApps) {
                                    if (app.getStoneDataId().equals(stoneId)) {
                                        newHomeworkApp = app;
                                    }
                                }
                                boolean containsWordExercise = CollectionUtils.isNotEmpty(newHomeworkApp.getWordExerciseQuestions());
                                boolean containsImageTextRhyme = CollectionUtils.isNotEmpty(newHomeworkApp.getImageTextRhymeQuestions());
                                boolean containsChineseCharacterCulture = CollectionUtils.isNotEmpty(newHomeworkApp.getChineseCharacterCultureCourseIds());
                                // 模块数量
                                int moduleNum = 0;
                                if (containsWordExercise) {
                                    moduleNum++;
                                }
                                if (containsImageTextRhyme) {
                                    moduleNum++;
                                }
                                if (containsChineseCharacterCulture) {
                                    moduleNum++;
                                }
                                List<String> processIds = newHomeworkResult.findHomeworkProcessIdsForWordTeachByStoneDataId(stoneId, objectiveConfigType, null);
                                Map<String, NewHomeworkProcessResult> processResultMap = newHomeworkProcessResultLoader.loads(newHomework.getId(), processIds);

                                Map<String, Integer> chapterScoreMap = new HashMap<>();
                                Map<String, Double> imageTextRhymeQuestionScoreMap = new HashMap<>();
                                Long duration = 0L;
                                Double totalScore;
                                Double wordExerciseScore = 0D;
                                Double imageTextRhymeQuestionScore = 0D;
                                Double finalImageTextRhymeScore = 0D;

                                // 只有汉字文化模块
                                if (moduleNum == 1 && containsChineseCharacterCulture) {
                                    if (newHomeworkApp.getChineseCharacterCultureCourseIds().size() == processIds.size()) {
                                        for (NewHomeworkProcessResult npr : processResultMap.values()) {
                                            duration += npr.getDuration();
                                        }
                                        totalScore = 100.00;
                                        newHomeworkResultService.finishHomeworkWordTeachAndPractice(
                                                newHomework.toLocation(),
                                                studentId,
                                                objectiveConfigType,
                                                stoneId,
                                                totalScore,
                                                duration,
                                                wordExerciseScore,
                                                finalImageTextRhymeScore);
                                    }
                                } else {
                                    // 计算分数&答题时长
                                    for (NewHomeworkProcessResult npr : processResultMap.values()) {
                                        duration += npr.getDuration();
                                        if (npr.getWordTeachModuleType().equals(WordTeachModuleType.WORDEXERCISE)) {
                                            wordExerciseScore += SafeConverter.toDouble(npr.getScore());
                                        }
                                        if (npr.getWordTeachModuleType().equals(WordTeachModuleType.IMAGETEXTRHYME)) {
                                            imageTextRhymeQuestionScoreMap.put(npr.getQuestionId(), npr.getActualScore());
                                        }
                                    }
                                    // 图文入韵部分分数计算特殊处理(图文入韵模块基准分60)
                                    if (containsImageTextRhyme) {
                                        for (ImageTextRhymeHomework imageTextRhymeHomework : newHomeworkApp.getImageTextRhymeQuestions()) {
                                            for (NewHomeworkQuestion question : imageTextRhymeHomework.getChapterQuestions()) {
                                                if (imageTextRhymeQuestionScoreMap.get(question.getQuestionId()) != null) {
                                                    imageTextRhymeQuestionScore += imageTextRhymeQuestionScoreMap.get(question.getQuestionId());
                                                }
                                            }
                                            Integer chapterScore = new BigDecimal(imageTextRhymeQuestionScore).divide(new BigDecimal(imageTextRhymeHomework.getChapterQuestions().size()), BigDecimal.ROUND_HALF_UP).intValue();
                                            Integer star = imageTextRhymeStarCalculator.calculateImageTextRhymeStar(chapterScore);
                                            Integer score = imageTextRhymeStarCalculator.calculateImageTextRhymeScore(star);    //百分制分数
                                            chapterScoreMap.put(imageTextRhymeHomework.getChapterId(), score != 0 ? score : 60);
                                        }
                                        Integer totalChapterScore = chapterScoreMap.values().stream().mapToInt(Integer::intValue).sum();
                                        finalImageTextRhymeScore = new BigDecimal(totalChapterScore).divide(new BigDecimal(newHomeworkApp.getImageTextRhymeQuestions().size()), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
                                    }
                                    // 没有汉字文化模块
                                    if (!containsChineseCharacterCulture && validatePracticeFinished(newHomework, appAnswer, stoneId)) {
                                        totalScore = new BigDecimal(wordExerciseScore + finalImageTextRhymeScore).divide(new BigDecimal(moduleNum), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
                                        newHomeworkResultService.finishHomeworkWordTeachAndPractice(
                                                newHomework.toLocation(),
                                                studentId,
                                                objectiveConfigType,
                                                stoneId,
                                                totalScore,
                                                duration,
                                                wordExerciseScore,
                                                finalImageTextRhymeScore);
                                    } else if (containsChineseCharacterCulture && validatePracticeFinished(newHomework, appAnswer, stoneId)) {
                                        List<String> chineseCourseProcessIds = newHomeworkResult.findHomeworkProcessIdsForWordTeachByStoneDataId(stoneId, objectiveConfigType, WordTeachModuleType.CHINESECHARACTERCULTURE);
                                        if (newHomeworkApp.getChineseCharacterCultureCourseIds().size() == chineseCourseProcessIds.size()) {
                                            totalScore = new BigDecimal(wordExerciseScore + finalImageTextRhymeScore + 100D).divide(new BigDecimal(moduleNum), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
                                            newHomeworkResultService.finishHomeworkWordTeachAndPractice(
                                                    newHomework.toLocation(),
                                                    studentId,
                                                    objectiveConfigType,
                                                    stoneId,
                                                    totalScore,
                                                    duration,
                                                    wordExerciseScore,
                                                    finalImageTextRhymeScore);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                FinishHomeworkContext ctx = new FinishHomeworkContext();
                ctx.setUserId(studentId);
                ctx.setUser(studentLoaderClient.loadStudent(studentId));
                ctx.setClazzGroupId(groupId);
                ctx.setClazzGroup(group);
                ctx.setHomeworkId(newHomework.getId());
                ctx.setHomework(newHomework);
                ctx.setNewHomeworkType(newHomework.getNewHomeworkType());
                ctx.setObjectiveConfigType(objectiveConfigType);
                ctx.setClientType("pc");
                ctx.setClientName("pc");
                ctx.setSupplementaryData(true);
                finishHomeworkProcessor.process(ctx);
            });
        }
    }

    /**
     * 校验作业中某个练习的题目和已做的是否一致
     */
    private boolean validateWordTeachPracticeFinished(NewHomework newHomework,
                                                      NewHomeworkResultAppAnswer appAnswer,
                                                      String stoneId) {
        boolean result = false;
        List<NewHomeworkQuestion> questionList = newHomework.findNewHomeworkWordTeachQuestions(ObjectiveConfigType.WORD_TEACH_AND_PRACTICE, stoneId, null);
        if (CollectionUtils.isNotEmpty(questionList)) {
            Set<String> homeworkQids = questionList.stream()
                    .filter(o -> StringUtils.isNotBlank(o.getQuestionId()))
                    .map(NewHomeworkQuestion::getQuestionId)
                    .collect(Collectors.toSet());
            Set<String> resultQids = new HashSet<>();
            if (MapUtils.isNotEmpty(appAnswer.getAnswers())) {
                resultQids.addAll(appAnswer.getAnswers().keySet());
            }
            if (MapUtils.isNotEmpty(appAnswer.getImageTextRhymeAnswers())) {
                resultQids.addAll(appAnswer.getImageTextRhymeAnswers().keySet());
            }

            result = CollectionUtils.isEqualCollection(homeworkQids, resultQids);
        }
        return result;
    }

    public void fixWordHomeworkResultData(NewHomework newHomework, NewHomeworkResult newHomeworkResult, Long studentId) {
        if (newHomeworkResult != null && !newHomeworkResult.getPractices().isEmpty()) {
            LogCollector.info("backend-general", MiscUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", studentId,
                    "mod1", newHomework.getId(),
                    "op", "repair newhomework result"
            ));
            ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.WORD_RECOGNITION_AND_READING;
            AlpsThreadPool.getInstance().submit(() -> {
                NewHomeworkResultAnswer newHomeworkResultAnswer = newHomeworkResult.getPractices().get(objectiveConfigType);
                if (newHomeworkResultAnswer != null && MapUtils.isNotEmpty(newHomeworkResultAnswer.getAppAnswers())) {
                    LinkedHashMap<String, NewHomeworkResultAppAnswer> appAnswerMap = newHomeworkResultAnswer.getAppAnswers();
                    for (Map.Entry<String, NewHomeworkResultAppAnswer> appAnswerEntry : appAnswerMap.entrySet()) {
                        NewHomeworkResultAppAnswer appAnswer = appAnswerEntry.getValue();
                        if (appAnswer != null && (appAnswer.getAppQuestionNum() == null || appAnswer.getStandardNum() == null)) {
                            String questionBoxId = appAnswer.getQuestionBoxId();
                            if (questionBoxId == null) {
                                appAnswerMap.remove(appAnswerEntry.getKey());
                                continue;
                            }
                            List<String> processIds = newHomeworkResult.findHomeworkProcessIdsForReadReciteWithScoreByQuestionBoxId(questionBoxId, objectiveConfigType);
                            if (CollectionUtils.isNotEmpty(processIds)) {
                                Map<String, NewHomeworkProcessResult> processResultMap = newHomeworkProcessResultLoader.loads(newHomework.getId(), processIds);
                                if (validatePracticeFinished(newHomework, appAnswer, questionBoxId)) {
                                    int standardNum = 0;
                                    long duration = 0;
                                    for (NewHomeworkProcessResult npr : processResultMap.values()) {
                                        if (SafeConverter.toBoolean(npr.getGrasp())) {
                                            ++standardNum;
                                        }
                                        duration += npr.getDuration();
                                    }
                                    int appQuestionNum = 0;
                                    List<NewHomeworkQuestion> newHomeworkQuestions = newHomework.findNewHomeworkReadReciteQuestions(objectiveConfigType, questionBoxId);
                                    if (CollectionUtils.isNotEmpty(newHomeworkQuestions)) {
                                        appQuestionNum = newHomeworkQuestions.size();
                                    }
                                    double score = new BigDecimal(standardNum).divide(new BigDecimal(appQuestionNum), 2, BigDecimal.ROUND_HALF_UP).doubleValue() * 100;
                                    appAnswer.setScore(score);
                                    appAnswer.setDuration(duration);
                                    appAnswer.setFinishAt(new Date());
                                    appAnswer.setAppQuestionNum(appQuestionNum);
                                    appAnswer.setStandardNum(standardNum);
                                    appAnswerMap.put(questionBoxId, appAnswer);
                                }
                            } else {
                                appAnswerMap.remove(questionBoxId);
                            }
                        }
                    }

                    newHomeworkResultAnswer.setAppAnswers(appAnswerMap);
                    newHomeworkResult.getPractices().values().forEach(a -> {
                        a.setAnswers(null);
                        if (MapUtils.isNotEmpty(a.getAppAnswers())) {
                            a.getAppAnswers().values().forEach(n -> {
                                n.setAnswers(null);
                            });
                        }
                    });
                    SubHomeworkResult subHomeworkResult = HomeworkTransform.NewHomeworkResultToSub(newHomeworkResult);
                    subHomeworkResultDao.upsert(subHomeworkResult);
                }
            });
        }
    }

    /**
     * 校验作业中某个练习的题目和已做的是否一致
     */
    private boolean validatePracticeFinished(NewHomework newHomework,
                                             NewHomeworkResultAppAnswer appAnswer,
                                             String questionBoxId) {
        boolean result = false;
        List<NewHomeworkQuestion> questionList = newHomework.findNewHomeworkReadReciteQuestions(ObjectiveConfigType.WORD_RECOGNITION_AND_READING, questionBoxId);
        if (CollectionUtils.isNotEmpty(questionList)) {
            Set<String> homeworkQids = questionList.stream()
                    .filter(o -> StringUtils.isNotBlank(o.getQuestionId()))
                    .map(NewHomeworkQuestion::getQuestionId)
                    .collect(Collectors.toSet());
            Set<String> resultQids = appAnswer.getAnswers().keySet();
            result = CollectionUtils.isEqualCollection(homeworkQids, resultQids);
        }
        return result;
    }

    //完成作业活动通过配置翻倍重新计算系数学豆数量
    public int generateFinishHomeworkActivityIntegral(Integer integral, NewHomework newHomework, Integer regionCode) {
        //读取页面内容的配置信息
        String config = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_STUDENT.name(), "hw_finished_integral_reward");
        List<Map> configMaps = JsonUtils.fromJsonToList(config, Map.class);

        Date currentDate = new Date();
        for (Map map : configMaps) {
            Object sd = map.get("startDate");
            Object ed = map.get("endDate");
            Object integralCoefficient = map.get("integralCoefficient");
            Object integralIncrement = map.get("integralIncrement");
            Object hts = map.get("homeworkType");
            Object htss = map.get("HomeworkTag");
            Object sbs = map.get("subject");
            Object rcs = map.get("regionCode");
            if (sd != null) {
                if (currentDate.before(DateUtils.stringToDate(SafeConverter.toString(sd)))) {
                    continue;
                }
            }

            if (ed != null) {
                if (currentDate.after(DateUtils.stringToDate(SafeConverter.toString(ed)))) {
                    continue;
                }
            }

            NewHomeworkType homeworkType = newHomework.getType();
            if (hts != null && homeworkType != null) {
                List<String> homeworkTypes = (List<String>) hts;
                if (!homeworkTypes.contains(homeworkType.name())) {
                    continue;
                }
            }

            HomeworkTag homeworkTag = newHomework.getHomeworkTag();
            if (homeworkTag == null) continue;
            if (htss != null && homeworkTag != null) {
                List<String> HomeworkTags = (List<String>) htss;
                if (!HomeworkTags.contains(homeworkTag.name())) {
                    continue;
                }
            }

            Subject subject = newHomework.getSubject();
            if (sbs != null && subject != null) {
                List<String> subjects = (List<String>) sbs;
                if (!subjects.contains(subject.name())) {
                    continue;
                }
            }

            if (rcs != null && regionCode != null) {
                List<Integer> regionCodes = (List<Integer>) rcs;
                if (!regionCodes.contains(regionCode)) {
                    continue;
                }
            }

            //如果是系数
            if (integralCoefficient != null) {
                integral = new BigDecimal(integral * SafeConverter.toDouble(integralCoefficient)).setScale(0, BigDecimal.ROUND_UP).intValue();
            }

            //如果是增加个数
            if (integralIncrement != null) {
                integral = integral + SafeConverter.toInt(integralIncrement);
            }
        }

        return integral;
    }
}
