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

package com.voxlearning.utopia.service.newhomework.impl.service.helper;

import com.voxlearning.alps.annotation.meta.Ktwelve;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.utopia.api.constant.Constants;
import com.voxlearning.utopia.service.content.api.constant.BookCatalogType;
import com.voxlearning.utopia.service.content.api.entity.*;
import com.voxlearning.utopia.service.content.api.mapper.PracticeLesson;
import com.voxlearning.utopia.service.content.api.mapper.QAndA;
import com.voxlearning.utopia.service.content.api.mapper.SentenceMapper;
import com.voxlearning.utopia.service.content.api.util.EnglishWordStockUtils;
import com.voxlearning.utopia.service.content.consumer.EnglishContentLoaderClient;
import com.voxlearning.utopia.service.content.consumer.MathContentLoaderClient;
import com.voxlearning.utopia.service.content.consumer.WordStockLoaderClient;
import com.voxlearning.utopia.service.newhomework.api.constant.NatureSpellingType;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkApp;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkBookInfo;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkQuestion;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkPackage;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkBook;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkLivecastLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkSpringBean;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.api.entity.NewQuestionOralDictOptions;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.client.GrayFunctionManagerClient;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants.USTALK_MOVE_DATE;

/**
 * Flash game loader helper implementation.
 *
 * @author Xiaohai Zhang
 * @since Nov 28, 2014
 */
@Named
public class FlashGameLoaderHelper extends NewHomeworkSpringBean {
    private static final Logger logger = LoggerFactory.getLogger(FlashGameLoaderHelper.class);

    @Inject private EnglishContentLoaderClient englishContentLoaderClient;
    @Inject private MathContentLoaderClient mathContentLoaderClient;
    @Inject private WordStockLoaderClient wordStockLoaderClient;
    @Inject private EnglishFlashGameComboDataLoader englishFlashGameComboDataLoader;
    @Inject private GrayFunctionManagerClient grayFunctionManagerClient;
    @Inject private NewHomeworkLivecastLoaderImpl newHomeworkLivecastLoader;

    public QAndA assembleWords(Sentence sentence) {
        sentence = Objects.requireNonNull(sentence).clone();
        sentence = sentence.fixInvalidFields();
        String enText = sentence.getEnText();
        String cnText = sentence.getCnText();
        List<WordStock> wordStocks = wordStockLoaderClient.loadWordStocksByEntext(enText);
        WordStock wordStock = null;
        for (WordStock ws : wordStocks) {
            if (sentence.hasMultiMeaning()) {
                if (Objects.equals(sentence.getMultiMeaning(), ws.getMultiMeaning())) {
                    wordStock = ws;
                    break;
                }
            } else {
                if (StringUtils.equals(sentence.getEnText(), ws.getEnText())) {
                    wordStock = ws;
                    break;
                }
            }
        }
        String tag = wordStock == null ? "" : StringUtils.defaultString(wordStock.getTag());
        String[] wordList = wordStockLoaderClient.randomFourWords(cnText, enText, tag);
        QAndA qa = new QAndA();
        for (int i = 0; i < wordList.length; i++) {
            String st1 = wordList[i];
            switch (i) {
                case 0:
                    qa.setWord1(st1);
                    break;
                case 1:
                    qa.setWord2(st1);
                    break;
                case 2:
                    qa.setWord3(st1);
                    break;
                case 3:
                    qa.setWord4(st1);
                    break;
            }
        }
        int a = RandomUtils.nextInt(4);
        switch (a) {
            case 0:
                qa.setWord1(EnglishWordStockUtils.stringChange(cnText));
                break;
            case 1:
                qa.setWord2(EnglishWordStockUtils.stringChange(cnText));
                break;
            case 2:
                qa.setWord3(EnglishWordStockUtils.stringChange(cnText));
                break;
            case 3:
                qa.setWord4(EnglishWordStockUtils.stringChange(cnText));
                break;
        }
        qa.setAnswer(Integer.toString(a));
        return qa;
    }

    public QAndA assembleWords(SentenceMapper sentence) {
        return assembleWords(Objects.requireNonNull(sentence).toSentence());
    }

    /**
     * 以lessonId和practiceId为键值，提供1个小时的缓存
     */
    public Map<String, Object> loadVocabularyListenData(Long lessonId, PracticeType englishPractice) {
        Objects.requireNonNull(lessonId);
        Objects.requireNonNull(englishPractice);

        PracticeLesson practiceLesson = new PracticeLesson(englishPractice.getId().intValue(), lessonId);
        EnglishFlashGameComboData combo = englishFlashGameComboDataLoader.load(practiceLesson);

        Lesson lesson = combo.getLesson();
        List<Sentence> sentences = combo.getSentences();
        List<SentenceMapper> sentenceMappers = new ArrayList<>(sentences.size());
        for (Sentence sentence : sentences) {
            SentenceMapper sentenceMapper = new SentenceMapper();
            sentenceMapper.setId(sentence.getId());
            sentenceMapper.setCnText(sentence.getCnText());
            sentenceMapper.setDialogRole(sentence.getDialogRole());
            sentenceMapper.setDisabled(sentence.getDisabled());
            sentenceMapper.setEnText(sentence.getEnText());
            sentenceMapper.setLanguageXml(sentence.getLanguageXml());
            sentenceMapper.setLesson(lesson);
            sentenceMapper.setMetadata(sentence.getMetadata());
            sentenceMapper.setPracticeType(sentence.getPracticeType());
            sentenceMapper.setSentenceNum(sentence.getSentenceNum());
            sentenceMapper.setSlowWavePlayTime(sentence.getSlowWavePlayTime());
            sentenceMapper.setSlowWaveUri(sentence.getSlowWaveUri());
            sentenceMapper.setSyllableNum(sentence.getSyllableNum());
            sentenceMapper.setType(sentence.getType());
            sentenceMapper.setWavePlayTime(sentence.getWavePlayTime());
            sentenceMapper.setWaveUri(sentence.getWaveUri());
            sentenceMapper.setMultiMeaning(sentence.getMultiMeaning());
            sentenceMappers.add(sentenceMapper);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("gameTime", combo.fetchGameTime());
        data.put("lesson", lesson);
        data.put("sentences", sentenceMappers);
        for (SentenceMapper sentence : sentenceMappers) {
            QAndA qa = assembleWords(sentence);
            sentence.setQa(qa);
        }
        return data;
    }

    /**
     * 以lessonId和practiceId为键值，提供1个小时的缓存
     */
    public Map<String, Object> loadVocabularySpeakData(Long lessonId, PracticeType englishPractice) {
        Objects.requireNonNull(lessonId);
        Objects.requireNonNull(englishPractice);

        PracticeLesson practiceLesson = new PracticeLesson(englishPractice.getId().intValue(), lessonId);
        EnglishFlashGameComboData combo = englishFlashGameComboDataLoader.load(practiceLesson);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("gameTime", combo.fetchGameTime());
        data.put("lesson", combo.getLesson());
        data.put("sentences", combo.getSentences());
        data.put("classLevel", combo.fetchClazzLevel());
        return data;
    }

    /**
     * 以lessonId和practiceId为键值，提供1个小时的缓存
     */
    public Map<String, Object> loadGrammarData(Long lessonId, PracticeType englishPractice) {
        Objects.requireNonNull(lessonId);
        Objects.requireNonNull(englishPractice);

        PracticeLesson practiceLesson = new PracticeLesson(englishPractice.getId().intValue(), lessonId);
        EnglishFlashGameComboData combo = englishFlashGameComboDataLoader.load(practiceLesson);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("gameTime", combo.fetchGameTime());
        data.put("lesson", combo.getLesson());
        data.put("sentences", combo.getSentences());
        data.put("timeXiShu", 2000);
        return data;
    }

    /**
     * 以lessonId和practiceId为键值，提供1个小时的缓存
     */
    public Map<String, Object> loadListenAndSpeakData(Long lessonId, PracticeType englishPractice) {
        Objects.requireNonNull(lessonId);
        Objects.requireNonNull(englishPractice);

        PracticeLesson practiceLesson = new PracticeLesson(englishPractice.getId().intValue(), lessonId);
        EnglishFlashGameComboData combo = englishFlashGameComboDataLoader.load(practiceLesson);

        Lesson lesson = combo.getLesson();
        List<Sentence> sentences = combo.getSentences();
        List<SentenceMapper> sentenceMappers = new ArrayList<>();
        for (Sentence st : sentences) {
            SentenceMapper sentenceMapper = new SentenceMapper();
            sentenceMapper.setId(st.getId());
            sentenceMapper.setCnText(st.getCnText());
            sentenceMapper.setDialogRole(st.getDialogRole());
            sentenceMapper.setDisabled(st.getDisabled());
            sentenceMapper.setEnText(st.getEnText());
            sentenceMapper.setLanguageXml(st.getLanguageXml());
            sentenceMapper.setLesson(lesson);
            sentenceMapper.setMetadata(st.getMetadata());
            sentenceMapper.setPracticeType(st.getPracticeType());
            sentenceMapper.setSentenceNum(st.getSentenceNum());
            sentenceMapper.setSlowWavePlayTime(st.getSlowWavePlayTime());
            sentenceMapper.setSlowWaveUri(st.getSlowWaveUri());
            sentenceMapper.setSyllableNum(st.getSyllableNum());
            sentenceMapper.setVoiceText(st.getVoiceText());
            if (st.getType() == null) {
                sentenceMapper.setType(0);
            } else {
                sentenceMapper.setType(st.getType());
            }
            sentenceMapper.setWavePlayTime(st.getWavePlayTime());
            sentenceMapper.setWaveUri(st.getWaveUri());
            // 循环跟读的句子组，查看本句是否包含重点单词
            // 默认为“0”，即本句没有重点单词
            String keyString = "0";
            sentenceMapper.setKeyWord(keyString);
            sentenceMappers.add(sentenceMapper);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("gameTime", combo.fetchGameTime());
        data.put("lesson", lesson);
        data.put("sentences", sentenceMappers);
        data.put("classLevel", combo.fetchClazzLevel());
        return data;
    }

    /**
     * 以lessonId和practiceId为键值，提供1个小时的缓存
     */
    public Map<String, Object> loadVocabularyAndPictureData(Long lessonId, PracticeType englishPractice) {
        Objects.requireNonNull(lessonId);
        Objects.requireNonNull(englishPractice);

        PracticeLesson practiceLesson = new PracticeLesson(englishPractice.getId().intValue(), lessonId);
        EnglishFlashGameComboData combo = englishFlashGameComboDataLoader.load(practiceLesson);

        Lesson lesson = combo.getLesson();
        List<Sentence> sentences = combo.getSentences();
        List<SentenceMapper> sentenceMappers = new ArrayList<>();
        for (Sentence st : sentences) {
            SentenceMapper sentenceMapper = new SentenceMapper();
            sentenceMapper.setId(st.getId());
            sentenceMapper.setCnText(st.getCnText());
            sentenceMapper.setEnText(st.getEnText());
            sentenceMapper.setSlowWaveUri(st.getSlowWaveUri());
            sentenceMapper.setWaveUri(st.getWaveUri());
            List<WordStock> wordStocks = wordStockLoaderClient.loadWordStocksByEntext(st.getEnText());
            for (WordStock wordStock : wordStocks) {
                // FIXME: 因为现在只有小学有部分游戏取xml数据，所以此处写死Ktwelve.PRIMARY_SCHOOL
                if (Ktwelve.PRIMARY_SCHOOL == wordStock.getKtwelve()) {
                    if (st.hasMultiMeaning()) {
                        if (st.getMultiMeaning().equals(wordStock.getMultiMeaning())) {
                            if (wordStock.hasPicture()) {
                                sentenceMapper.setSentenceImg(wordStock.getPictureUrl());
                            }
                            break;
                        }
                    } else {
                        if (st.getEnText().equals(wordStock.getEnText())) {
                            if (wordStock.hasPicture()) {
                                sentenceMapper.setSentenceImg(wordStock.getPictureUrl());
                            }
                            break;
                        }
                    }
                }
            }
            sentenceMappers.add(sentenceMapper);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("gameTime", combo.fetchGameTime());
        data.put("lesson", lesson);
        data.put("sentences", sentenceMappers);
        data.put("classLevel", combo.fetchClazzLevel());
        return data;
    }

    public Map<String, Object> loadVocabularyAndParaphraseData(Long lessonId, PracticeType englishPractice) {
        Objects.requireNonNull(lessonId);
        Objects.requireNonNull(englishPractice);

        PracticeLesson practiceLesson = new PracticeLesson(englishPractice.getId().intValue(), lessonId);
        EnglishFlashGameComboData combo = englishFlashGameComboDataLoader.load(practiceLesson);

        Lesson lesson = combo.getLesson();
        List<Sentence> sentences = combo.getSentences();
        List<SentenceMapper> sentenceMappers = new ArrayList<>();
        for (Sentence st : sentences) {
            SentenceMapper sentenceMapper = new SentenceMapper();
            sentenceMapper.setId(st.getId());
            sentenceMapper.setCnText(st.getCnText());
            sentenceMapper.setEnText(st.getEnText());
            sentenceMapper.setSlowWaveUri(st.getSlowWaveUri());
            sentenceMapper.setWaveUri(st.getWaveUri());
            List<WordStock> wordStocks = wordStockLoaderClient.loadWordStocksByEntext(st.getEnText());
            for (WordStock wordStock : wordStocks) {
                // FIXME: 因为现在只有小学有部分游戏取xml数据，所以此处写死Ktwelve.PRIMARY_SCHOOL
                if (Ktwelve.PRIMARY_SCHOOL == wordStock.getKtwelve()) {
                    if (st.hasMultiMeaning()) {
                        if (st.getMultiMeaning().equals(wordStock.getMultiMeaning())) {
                            if (StringUtils.isNotBlank(wordStock.getParaphrase1()))
                                sentenceMapper.getSentenceParaphrases().add(wordStock.getParaphrase1());
                            if (StringUtils.isNotBlank(wordStock.getParaphrase2()))
                                sentenceMapper.getSentenceParaphrases().add(wordStock.getParaphrase2());
                            if (StringUtils.isNotBlank(wordStock.getParaphrase3()))
                                sentenceMapper.getSentenceParaphrases().add(wordStock.getParaphrase3());
                            if (StringUtils.isNotBlank(wordStock.getParaphrase4()))
                                sentenceMapper.getSentenceParaphrases().add(wordStock.getParaphrase4());
                            break;
                        }

                    } else {
                        if (StringUtils.isNotBlank(wordStock.getParaphrase1()))
                            sentenceMapper.getSentenceParaphrases().add(wordStock.getParaphrase1());
                        if (StringUtils.isNotBlank(wordStock.getParaphrase2()))
                            sentenceMapper.getSentenceParaphrases().add(wordStock.getParaphrase2());
                        if (StringUtils.isNotBlank(wordStock.getParaphrase3()))
                            sentenceMapper.getSentenceParaphrases().add(wordStock.getParaphrase3());
                        if (StringUtils.isNotBlank(wordStock.getParaphrase4()))
                            sentenceMapper.getSentenceParaphrases().add(wordStock.getParaphrase4());
                        break;
                    }
                }
            }
            sentenceMappers.add(sentenceMapper);
        }


        Map<String, Object> data = new LinkedHashMap<>();
        data.put("gameTime", combo.fetchGameTime());
        data.put("lesson", lesson);
        data.put("sentences", sentenceMappers);
        return data;
    }

    /**
     * Flash&Html5游戏新数据接口
     * Flash&Html5英语游戏内容数据结构：
     * version          String        （数据版本 ）
     * gradeLevel       String        （年级）
     * gameTime         String        （游戏时间 分钟）
     * lessonId         String        （课程id）
     * lessonName       String        （课程名称）
     * numberOfRoles    String        （角色数量）
     * sentence                       （数据区，可循环多个）
     * sentenceId       String        （句子id）
     * dialogRole       String        （对话角色）
     * data                           （基础字段）
     * foreignText      String        （外文数据）
     * nativeText       String        （中文数据）
     * voiceText        String        （语音数据）
     * audioUrl         String        （音频地址）
     * audioTime        String        （音频时长）
     * pictureUrl       String        （图片地址）
     * extraData                      （扩展字段）
     * text0            String        （数据0）
     * text1            String        （数据1）
     * text2            String        （数据2）
     * text3            String        （数据3）
     * answer           String        （答案）
     * …..                            （扩展字段）
     */
    public MapMessage loadData(Long userId, String cdnUrl, Long lessonId, PracticeType englishPractice, Ktwelve k12, Boolean isNew) {
        // 这里的isNew永远都是false
        Objects.requireNonNull(lessonId);
        Objects.requireNonNull(englishPractice);
        Objects.requireNonNull(k12);

        PracticeLesson practiceLesson = new PracticeLesson(englishPractice.getId().intValue(), lessonId);
        EnglishFlashGameComboData combo = englishFlashGameComboDataLoader.load(practiceLesson);
        if (combo.getLesson() == null) {
            return MapMessage.errorMessage("lesson不存在").add("lessonId", lessonId);
        }

        Map<String, Object> lessonMap = new LinkedHashMap<>();
        //不同游戏类型扩展属性
        if (StringUtils.equals(englishPractice.getDataType(), Constants.GameDataTemplat_sentenceFillingData)) {
            lessonMap.putAll(fillSentenceFillingDataMapData(combo));
        }

        List<Map<String, Object>> sentenceMappers = loadEnglishSentenceMappers(userId, combo.getSentences(), englishPractice, k12, null, false);
        lessonMap.put("version", "1.0.0.1");
        lessonMap.put("gradeLevel", combo.fetchClazzLevel() == null ? 0 : combo.fetchClazzLevel());
        lessonMap.put("gameTime", combo.fetchGameTime() == null ? 0 : combo.fetchGameTime());
        lessonMap.put("lessonId", lessonId);
        lessonMap.put("lessonName", combo.getLesson().getCname());
        lessonMap.put("numberOfRoles", combo.getLesson().getRoleCount());
        lessonMap.put("sentence", sentenceMappers);
        lessonMap.put("cdnUrl", cdnUrl);
        lessonMap.put("hasDialog", combo.getLesson().hasDialog());
        lessonMap.put("categoryName", englishPractice.getCategoryName());   //练习形式
        lessonMap.put("actionType", englishPractice.getActionType());       //练习方式

        return MapMessage.successMessage().add("gameData", lessonMap);
    }

    /**
     * 目前只有通天塔用到。由于通天塔不关注lesson相关信息（推题逻辑也导致无法提供lesson信息），故相关字段全都填的假值
     */
    public MapMessage loadDataFromSentenceList(Long userId,
                                               String cdnUrl,
                                               List<Sentence> sentences,
                                               PracticeType englishPractice,
                                               Ktwelve k12,
                                               Map<String, String> algovMap,
                                               Boolean isNew) {
        Objects.requireNonNull(sentences);
        Objects.requireNonNull(englishPractice);
        Objects.requireNonNull(k12);

        Map<String, Object> lessonMap = new LinkedHashMap<>();
        List<Map<String, Object>> sentenceMappers = loadEnglishSentenceMappers(userId, sentences, englishPractice, k12, algovMap, false);
        // 以下字段中 0，""则为通天塔中不适用，但flash必须填写的字段。填入假值
        lessonMap.put("version", "1.0.0.1");
        lessonMap.put("gradeLevel", 0);
        lessonMap.put("gameTime", 0);
        lessonMap.put("lessonId", 0);
        lessonMap.put("lessonName", "");
        lessonMap.put("numberOfRoles", 0);
        lessonMap.put("sentence", sentenceMappers);
        lessonMap.put("cdnUrl", cdnUrl);
        lessonMap.put("hasDialog", false);
        lessonMap.put("categoryName", englishPractice.getCategoryName());   // 练习形式
        lessonMap.put("actionType", englishPractice.getActionType());       // 练习方式
        return MapMessage.successMessage().add("gameData", lessonMap);
    }

    public Map<String, Object> loadMentalArithmeticDataWithDscp(Long pointId, Integer amount, PracticeType mathPractice, String dataType, String baseDscp) {
        //TODO MATH：根据pointId，获取base和baseChoice，并取amount的数量进行解析
        MathPoint mathPoint = mathContentLoaderClient.loadMathPoint(pointId);

        Map<String, Object> data = new LinkedHashMap<>();
        if (mathPoint == null) {
            return data;
        }
        data.put("pointId", pointId);
        data.put("pointName", mathPoint.getPointName());
        List<MathBase> mathBases = mathContentLoaderClient.loadMathPointBases(pointId);
        mathBases = new LinkedList<>(mathBases);

        Collections.shuffle(mathBases);

        List<Map<String, Object>> mathBaseMaps = new ArrayList<>();
        List<Map<String, Object>> backupMaps = new ArrayList<>();
        if (mathBases.size() > 0) {
            List<MathBase> candidates = new ArrayList<>();
            Set<Long> candidateIds = new HashSet<>();
            for (MathBase mathBase : mathBases) {
                if (1 != mathBase.getStatus()) {
                    continue;
                }
                // 根据BaseType进行过滤
                if (!isMathBaseDataMatch(mathBase, dataType)) {
                    continue;
                }
                candidateIds.add(mathBase.getId());
                candidates.add(mathBase);
            }
            Map<Long, List<MathBaseChoice>> baseChoices = mathContentLoaderClient.loadMathBaseBaseChoices(candidateIds);

            for (MathBase mathBase : candidates) {
                Map<String, Object> mathBaseMap = new LinkedHashMap<>();
                Long baseId = mathBase.getId();
                mathBaseMap.put("baseId", baseId);
                mathBaseMap.put("baseTitle", mathBase.getBaseTitle());
                mathBaseMap.put("baseDscp", mathBase.getBaseDscp());
                mathBaseMap.put("baseContent", JsonUtils.fromJson(mathBase.getBaseContent()));
                mathBaseMap.put("baseType", mathBase.getBaseType());
                List<Long> answerList = new ArrayList<>();
                List<MathBaseChoice> mathBaseChoices = baseChoices.get(baseId);
                if (mathBaseChoices == null) {
                    mathBaseChoices = new ArrayList<>();
                }
                List<Map<Long, Object>> mathBaseChoiceMaps = new ArrayList<>();
                for (MathBaseChoice mathBaseChoice : mathBaseChoices) {
                    if (1 == mathBaseChoice.getStatus() && !mathBaseChoice.getDisabled()) {
                        Map<Long, Object> mathBaseChoiceMap = new HashMap<>();
                        if (mathBaseChoice.getRightAnswer()) {
                            mathBaseMap.put("answer", mathBaseChoice.getId());
                            answerList.add(mathBaseChoice.getId());
                        }
                        mathBaseChoiceMap.put(mathBaseChoice.getId(), mathBaseChoice.getChoiceContent());
                        mathBaseChoiceMaps.add(mathBaseChoiceMap);
                    }
                }

                // FIXME 原来的计算知识点要求最少有三个选项，新加的竖式脱式只需要一个选项就可以了
                if (mathPoint.isCalculateMathPoint() && mathPoint.getId() <= 195 && mathBaseChoiceMaps.size() < 3) {
                    continue;
                }

                if (answerList.size() > 0) {
                    mathBaseMap.put("answerList", answerList);
                }

                mathBaseMap.put("choice", mathBaseChoiceMaps);
                if (StringUtils.isNotEmpty(baseDscp)) {
                    if (mathBase.getBaseDscp().equals(baseDscp)) {
                        mathBaseMaps.add(mathBaseMap);
                    } else {
                        backupMaps.add(mathBaseMap);
                    }
                } else {
                    mathBaseMaps.add(mathBaseMap);
                }

                // 判断题量是否足够
                if (mathBaseMaps.size() >= amount) {
                    break;
                }
            }
        }
        if (StringUtils.isNotEmpty(baseDscp)) {
            for (Map<String, Object> bc : backupMaps) {
                if (mathBaseMaps.size() >= amount) {
                    break;
                }
                mathBaseMaps.add(bc);
            }
        }
        data.put("base", mathBaseMaps);
        return data;
    }

    public Map<String, Object> loadMentalArithmeticData(Long pointId, Integer amount, PracticeType mathPractice, String dataType) {
        return loadMentalArithmeticDataWithDscp(pointId, amount, mathPractice, dataType, "");
    }

    public List<Map<String, Object>> loadEnglishSentenceMappers(List<Sentence> sentences, Ktwelve ktwelve) {
        if (CollectionUtils.isEmpty(sentences)) return Collections.emptyList();

        List<Map<String, Object>> result = new ArrayList<>();
        for (Sentence sentence : sentences) {
            String ent = StringUtils.trim(StringUtils.defaultString(sentence.getEnText()));
            String cnt = StringUtils.trim(StringUtils.defaultString(sentence.getCnText()));
            String vt = StringUtils.trim(StringUtils.defaultString(sentence.getVoiceText()));

            Map<String, Object> map = new HashMap<>();
            map.put("sentenceId", sentence.getId());
            map.put("nativeText", cnt);
            map.put("foreignText", ent);
            map.put("voiceText", StringUtils.isEmpty(sentence.getVoiceText()) ? ent : vt);
            map.put("audioUrl", sentence.getWaveUri());
            map.put("audioTime", sentence.getWavePlayTimeValue());
            map.put("ektag", "word:" + ent);
            map.put("pictureUrl", fetchPictureUrlFromSentence(sentence, ktwelve));
            result.add(map);
        }

        return result;
    }

    /**
     * @param cdnUrl          cdn地址
     * @param newLessonId     新教材结构中的lessonId，字符串
     * @param englishPractice PracticeType实体
     * @param k12             K12？
     * @param homeworkId      作业id
     * @param categoryId      PracticeType中的类型id
     * @return MapMessage
     */
    public MapMessage loadNewData(Long userId, String cdnUrl, String newLessonId, PracticeType englishPractice, Ktwelve k12,
                                  String homeworkId, Integer categoryId, Boolean isNew, NewHomeworkType newHomeworkType, String objectiveConfigType) {
        Objects.requireNonNull(englishPractice);
        Objects.requireNonNull(k12);

        NewBookCatalog lesson = newContentLoaderClient.loadBookCatalogByCatalogId(newLessonId);
        if (lesson == null) {
            return MapMessage.errorMessage("lesson不存在").add("lessonId", newLessonId);
        }

        Integer clazzLevel = 0;
        NewBookCatalogAncestor ancestor = lesson.getAncestors().stream().filter(o -> StringUtils.equalsIgnoreCase(o.getNodeType(), BookCatalogType.BOOK.name())).findFirst().orElse(null);
        if (ancestor != null) {
            NewBookProfile bookProfile = newContentLoaderClient.loadBookProfilesIncludeDisabled(Collections.singleton(ancestor.getId())).getOrDefault(ancestor.getId(), null);
            if (bookProfile != null) {
                clazzLevel = bookProfile.getClazzLevel();
            }
        }

        Map<String, Object> lessonMap = new LinkedHashMap<>();
        List<NewQuestion> questionList;
        if (NewHomeworkType.WinterVacation.equals(newHomeworkType) || NewHomeworkType.SummerVacation.equals(newHomeworkType)) {
            questionList = loadVacationHomeworkEnglishQuestionMappers(userId, homeworkId, newLessonId, englishPractice, k12, categoryId, isNew);
        } else if (NewHomeworkConstants.AllowUserTokenTypes.contains(newHomeworkType)) {
            questionList = loadEnglishQuestionMappersForYIQIXUE(userId, homeworkId, newLessonId, englishPractice, k12, categoryId, isNew, objectiveConfigType);
        } else {
            questionList = loadEnglishQuestionMappers(userId, homeworkId, newLessonId, englishPractice, k12, categoryId, isNew, objectiveConfigType);
        }
        Integer gameTime = questionList.stream().mapToInt(NewQuestion::getSeconds).sum();

        lessonMap.put("version", "1.0.0.1");
        lessonMap.put("questions", questionList);
        lessonMap.put("cdnUrl", cdnUrl);
        lessonMap.put("gradeLevel", clazzLevel);
        lessonMap.put("gameTime", gameTime);
        lessonMap.put("lessonId", newLessonId);
        lessonMap.put("lessonName", lesson.getName());
        lessonMap.put("categoryName", englishPractice.getCategoryName());   //练习形式
        lessonMap.put("actionType", englishPractice.getActionType());       //练习方式

        // 自然拼读字母学习类型字体
        if (Objects.equals(NatureSpellingType.ALPHABETIC_PRACTICE.categoryId, categoryId)) {
            String phonicsFont = "handwritten";
            String bookId = "";
            // 假期作业获取bookId
            if (NewHomeworkType.WinterVacation.equals(newHomeworkType) || NewHomeworkType.SummerVacation.equals(newHomeworkType)) {
                VacationHomework vacationHomework = vacationHomeworkDao.load(homeworkId);
                if (vacationHomework != null) {
                    VacationHomeworkPackage vacationHomeworkPackage = vacationHomeworkPackageDao.load(vacationHomework.getPackageId());
                    if (vacationHomeworkPackage != null) {
                        bookId = vacationHomeworkPackage.getBookId();
                    }
                }
            } else {
                NewHomeworkBook newHomeworkBook = newHomeworkLoader.loadNewHomeworkBook(homeworkId);
                if (newHomeworkBook != null && MapUtils.isNotEmpty(newHomeworkBook.getPractices())) {
                    List<NewHomeworkBookInfo> newHomeworkBookInfoList = newHomeworkBook.getPractices().values().iterator().next();
                    if (CollectionUtils.isNotEmpty(newHomeworkBookInfoList)) {
                        bookId = newHomeworkBookInfoList.get(0).getBookId();
                    }
                }
            }
            if (StringUtils.isNotBlank(bookId)) {
                NewBookProfile newBookProfile = newContentLoaderClient.loadBookProfilesIncludeDisabled(Collections.singleton(bookId)).get(bookId);
                if (newBookProfile != null && MapUtils.isNotEmpty(newBookProfile.getExtras())) {
                    phonicsFont = SafeConverter.toString(newBookProfile.getExtras().get("phonics_font"), "handwritten");
                }
            }
            lessonMap.put("phonicsFont", phonicsFont);
        }

        return MapMessage.successMessage().add("gameData", lessonMap);
    }

    /**
     * 用于flash预览
     *
     * @param cdnUrl          cdn地址
     * @param newLessonId     新教材结构中的lessonId，字符串
     * @param englishPractice PracticeType实体
     * @param k12             K12？
     * @param questionIds     题ids
     * @return MapMessage
     */
    public MapMessage loadPreviewNewDate(Long userId, String cdnUrl, String newLessonId, PracticeType englishPractice, Ktwelve k12, List<String> questionIds, Boolean isNew, String bookId) {
        Objects.requireNonNull(englishPractice);
        Objects.requireNonNull(k12);

        NewBookCatalog lesson = newContentLoaderClient.loadBookCatalogByCatalogId(newLessonId);
        if (lesson == null) {
            return MapMessage.errorMessage("lesson不存在").add("lessonId", newLessonId);
        }

        Integer clazzLevel = 0;
        NewBookProfile bookProfile;
        NewBookCatalogAncestor ancestor = lesson.getAncestors().stream().filter(o -> StringUtils.equalsIgnoreCase(o.getNodeType(), BookCatalogType.BOOK.name())).findFirst().orElse(null);
        if (ancestor != null) {
            bookProfile = newContentLoaderClient.loadBookProfilesIncludeDisabled(Collections.singleton(ancestor.getId())).getOrDefault(ancestor.getId(), null);
            if (bookProfile != null) {
                clazzLevel = bookProfile.getClazzLevel();
            }
        }

        Map<String, Object> lessonMap = new LinkedHashMap<>();
        List<NewQuestion> questionList = buildSentenceContents(userId, questionIds, englishPractice, k12, isNew);
        Integer gameTime = questionList.stream().mapToInt(NewQuestion::getSeconds).sum();

        lessonMap.put("version", "1.0.0.1");
        lessonMap.put("gradeLevel", clazzLevel);
        lessonMap.put("gameTime", gameTime);
        lessonMap.put("lessonId", newLessonId);
        lessonMap.put("lessonName", lesson.getName());
        lessonMap.put("questions", questionList);
        lessonMap.put("cdnUrl", cdnUrl);
        lessonMap.put("categoryName", englishPractice.getCategoryName());   //练习形式
        lessonMap.put("actionType", englishPractice.getActionType());       //练习方式

        // 自然拼读字母学习类型字体
        if (Objects.equals(NatureSpellingType.ALPHABETIC_PRACTICE.categoryId, englishPractice.getCategoryId())) {
            String phonicsFont = "handwritten";
            bookProfile = newContentLoaderClient.loadBookProfilesIncludeDisabled(Collections.singleton(bookId)).get(bookId);
            if (bookProfile != null && MapUtils.isNotEmpty(bookProfile.getExtras())) {
                phonicsFont = SafeConverter.toString(bookProfile.getExtras().get("phonics_font"), "handwritten");
            }
            lessonMap.put("phonicsFont", phonicsFont);
        }

        return MapMessage.successMessage().add("gameData", lessonMap);
    }

    // ========================================================================
    // PRIVATE METHODS
    // ========================================================================

    private String fetchPictureUrlFromSentence(Sentence sentence, Ktwelve ktwelve) {
        if (sentence == null) return "";

        String ent = StringUtils.trim(StringUtils.defaultString(sentence.getEnText()));
        List<WordStock> words = wordStockLoaderClient.loadWordStocksByEntext(ent);

        for (WordStock word : words) {
            if (word.getKtwelve() != ktwelve) continue;

            if (sentence.getMultiMeaning() != null && sentence.getMultiMeaning() > 0) {
                if (sentence.getMultiMeaning().equals(word.getMultiMeaning()))
                    return word.getThumbPictureUrl();
            } else {
                if (StringUtils.equals(ent, word.getEnText()))
                    return word.getThumbPictureUrl();
            }
        }
        return "";
    }

    private List<NewQuestion> loadEnglishQuestionMappers(Long userId, String hid, String lessonId, PracticeType englishPractice,
                                                         Ktwelve k12, Integer categoryId, Boolean isNew, String objectiveConfigTypeStr) {
        NewHomework newHomework = newHomeworkLoader.load(hid);
        if (newHomework == null) return Collections.emptyList();
        ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.of(objectiveConfigTypeStr);
        List<NewHomeworkApp> appList = newHomework.getPractices().stream()
                .filter(o -> CollectionUtils.isNotEmpty(o.getApps()))
                .filter(o -> objectiveConfigType == null || o.getType() == objectiveConfigType)
                .map(NewHomeworkPracticeContent::getApps)
                .flatMap(Collection::stream)
                .filter(o -> StringUtils.equals(lessonId, o.getLessonId()))
                .filter(o -> (Objects.equals(categoryId, o.getCategoryId())))
                .collect(Collectors.toList());

        List<String> qidList = appList.stream()
                .map(NewHomeworkApp::getQuestions)
                .flatMap(Collection::stream)
                .map(NewHomeworkQuestion::getQuestionId)
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(appList)) {
            return buildSentenceContents(userId, qidList, englishPractice, k12, isNew);
        } else {
            return Collections.emptyList();
        }
    }

    private List<NewQuestion> loadEnglishQuestionMappersForYIQIXUE(Long userId, String hid, String lessonId, PracticeType englishPractice,
                                                                   Ktwelve k12, Integer categoryId, Boolean isNew, String objectiveConfigTypeStr) {
        LiveCastHomework homework = newHomeworkLivecastLoader.loadLiveCastHomeworkIncludeDisabled(hid);
        if (homework == null) return Collections.emptyList();
        ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.of(objectiveConfigTypeStr);
        List<NewHomeworkApp> appList = homework.getPractices().stream()
                .filter(o -> CollectionUtils.isNotEmpty(o.getApps()))
                .filter(o -> objectiveConfigType == null || o.getType() == objectiveConfigType)
                .map(NewHomeworkPracticeContent::getApps)
                .flatMap(Collection::stream)
                .filter(o -> StringUtils.equals(lessonId, o.getLessonId()))
                .filter(o -> (Objects.equals(categoryId, o.getCategoryId())))
                .collect(Collectors.toList());

        List<String> qidList = appList.stream()
                .map(NewHomeworkApp::getQuestions)
                .flatMap(Collection::stream)
                .map(NewHomeworkQuestion::getQuestionId)
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(appList)) {
            return buildSentenceContents(userId, qidList, englishPractice, k12, isNew);
        } else {
            return Collections.emptyList();
        }
    }

    private List<NewQuestion> loadVacationHomeworkEnglishQuestionMappers(Long userId, String hid, String lessonId, PracticeType englishPractice, Ktwelve k12, Integer categoryId, Boolean isNew) {
        VacationHomework vacationHomework = vacationHomeworkDao.load(hid);
        if (vacationHomework == null) return Collections.emptyList();
        List<NewHomeworkApp> appList = vacationHomework.getPractices().stream()
                .filter(o -> CollectionUtils.isNotEmpty(o.getApps()))
                .map(NewHomeworkPracticeContent::getApps)
                .flatMap(Collection::stream)
                .filter(o -> StringUtils.equals(lessonId, o.getLessonId()))
                .filter(o -> (Objects.equals(categoryId, o.getCategoryId())))
                .collect(Collectors.toList());

        List<String> qidList = appList.stream()
                .map(NewHomeworkApp::getQuestions)
                .flatMap(Collection::stream)
                .map(NewHomeworkQuestion::getQuestionId)
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(appList)) {
            return buildSentenceContents(userId, qidList, englishPractice, k12, isNew);
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * question中添加QuestionContents属性
     *
     * @param questionIds 试题ids
     * @return List<NewQuestion>
     */
    private List<NewQuestion> buildSentenceContents(Long userId, List<String> questionIds, PracticeType englishPractice, Ktwelve k12, Boolean isNew) {
        List<NewQuestion> tempQuestionList = new ArrayList<>(questionLoaderClient.loadQuestionsIncludeDisabled(questionIds).values());
        List<NewQuestion> questionList = new ArrayList<>();
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(userId);
        Boolean voiceEngineReadOnly = false;
        if (studentDetail != null && grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "VoiceEngine", "ReadOnly")) {
            voiceEngineReadOnly = true;
        }
        if (CollectionUtils.isNotEmpty(tempQuestionList)) {
            Set<Long> sentenceIds = tempQuestionList.stream()
                    .filter(o -> CollectionUtils.isNotEmpty(o.getSentenceIds()))
                    .map(NewQuestion::getSentenceIds)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet());

            // 六种特殊练习类型这里直接处理返回。wiki:30328209
            if (NewHomeworkConstants.SpecialPracticeTypeList.contains(englishPractice.getPracticeType())) {
                return tempQuestionList;
            }

            Map<Long, Sentence> sentenceMap = englishContentLoaderClient.loadEnglishSentencesIncludeDisabled(sentenceIds);
            Boolean finalVoiceEngineReadOnly = voiceEngineReadOnly;
            tempQuestionList.forEach(o -> {
                        if (CollectionUtils.isNotEmpty(o.getSentenceIds())) {
                            // 应用类型的题，此数组长度为1，或者只取第一个，有问题问俊杰
                            Long sentenceId = o.getSentenceIds().get(0);
                            if (sentenceMap.get(sentenceId) != null) {
                                Sentence sentence = sentenceMap.get(sentenceId);
                                Map<String, Object> sentenceData = buildSentenceData(finalVoiceEngineReadOnly, o, sentence, englishPractice, k12, null, isNew);
                                o.setSentenceContents(Collections.singletonList(sentenceData));

                                // 处理兼容 begin 将正确位置的listenUrl刷到非空的o.getContent().getSubContents().get(0).getListenUrl()上面（在subContent下的listenUrl中）
                                List<NewQuestionOralDictOptions> options = o.getContent().getSubContents().get(0).getOralDict().getOptions();
                                String listenUrl = o.getContent().getSubContents().get(0).getListenUrl();
                                if (StringUtils.isBlank(listenUrl) && CollectionUtils.isNotEmpty(options)) {
                                    // 说明是非句子听力类的题型
                                    listenUrl = options.get(0).getListenUrl();
                                    if (StringUtils.isNotBlank(listenUrl)) {
                                        o.getContent().getSubContents().get(0).setListenUrl(listenUrl);
                                    }
                                }
                                if (StringUtils.isBlank(o.getListenUrl()) && CollectionUtils.isNotEmpty(options)) {
                                    listenUrl = options.get(0).getListenUrl();
                                    if (StringUtils.isNotBlank(listenUrl)) {
                                        o.setListenUrl(listenUrl);
                                    }
                                }
                                // 处理兼容 end

                                // 处理 图片数据兼容，只对看图识词处理 begin
                                if (o.getContentType2Id() == 1032005 || o.getContentType2Id() == 5032005) {
                                    String pictureUrl = processQuestionContentImage(o);
                                    if (StringUtils.isNotBlank(pictureUrl) && StringUtils.isBlank(o.getContent().getSubContents().get(0).getPictureUrl())) {
                                        o.getContent().getSubContents().get(0).setPictureUrl(pictureUrl);
                                    }
                                    if (StringUtils.isNotBlank(pictureUrl) && StringUtils.isBlank(o.getPictureUrl())) {
                                        o.setPictureUrl(pictureUrl);
                                    }
                                }

                            } else {
                                logger.warn("sentence error, sentenceId:{}, questionId:{} ", o.getSentenceIds(), o.getId());
                            }
                        }
                        questionList.add(o);
                    }
            );
        }
        return questionList;
    }

    // 取出看图识词中正确的图片地址
    private String processQuestionContentImage(NewQuestion question) {
        String imgUrl = "";
        if (question != null) {
            String content = question.getContent().getSubContents().get(0).getContent();
            String imagePatternStr = "<img[^>]*src=\"([^\"]+)\"";
            Pattern imagePattern = Pattern.compile(imagePatternStr, Pattern.CASE_INSENSITIVE);
            Matcher matcher = imagePattern.matcher(content);
            while (matcher.find()) {
                try {
                    imgUrl = matcher.group(1);
                } catch (Exception ignored) {
                }
            }
        }
        return imgUrl;
    }

    private List<Map<String, Object>> loadEnglishSentenceMappers(Long userId, List<Sentence> sentences,
                                                                 PracticeType practice,
                                                                 Ktwelve k12,
                                                                 Map<String, String> algovMap,
                                                                 Boolean isNew) {

        if (CollectionUtils.isEmpty(sentences) || practice == null || k12 == null) {
            logger.warn("Empty sentences or null practice or null K12 specified");
            return Collections.emptyList();
        }
        User user = userLoaderClient.loadUser(userId);
        Boolean voiceEngineReadOnly = false;
        if (user != null && UserType.STUDENT.getType() == user.getUserType()) {
            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(userId);
            if (studentDetail != null && grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "VoiceEngine", "ReadOnly")) {
                voiceEngineReadOnly = true;
            }
        }
        List<Map<String, Object>> mappers = new ArrayList<>();
        for (Sentence st : sentences) {
            mappers.add(buildSentenceData(voiceEngineReadOnly, null, st, practice, k12, algovMap, false));
        }
        return mappers;
    }

    private Map<String, Object> buildSentenceData(Boolean voiceEngineReadOnly,
                                                  NewQuestion question,
                                                  Sentence st,
                                                  PracticeType practice,
                                                  Ktwelve k12,
                                                  Map<String, String> algovMap,
                                                  Boolean isNew) {
        st = st.clone();
        if (st.getCnText() == null) {
            st.setCnText("");
        }
        if (st.getEnText() == null) {
            st.setEnText("");
        }
        Map<String, Object> mapper = new LinkedHashMap<>();
        mapper.put("sentenceId", st.getId());
        mapper.put("dialogRole", st.getDialogRole());

        Map<String, Object> dataMap = new LinkedHashMap<>();
        dataMap.put("foreignText", StringUtils.trim(st.getEnText()));
        dataMap.put("nativeText", st.getCnText());
        if (StringUtils.isEmpty(st.getVoiceText())) {
            dataMap.put("voiceText", st.getEnText());
        } else {
            dataMap.put("voiceText", st.getVoiceText());
        }

        // 兼容代码，将正确位置的listenUrl刷到为空的audioUrl上
        if (Objects.equals(Boolean.TRUE, isNew) && question != null && StringUtils.isBlank(st.getWaveUri())) {
            List<NewQuestionOralDictOptions> options = question.getContent().getSubContents().get(0).getOralDict().getOptions();
            // 或者如果能把音频统一刷到这个位置，取得时候会方便一些
            String listenUrl = question.getContent().getSubContents().get(0).getListenUrl();
            if (StringUtils.isNotBlank(listenUrl)) {
                dataMap.put("audioUrl", listenUrl);
            } else {
                if (CollectionUtils.isNotEmpty(options)) {
                    listenUrl = options.get(0).getListenUrl();
                    if (StringUtils.isNotBlank(listenUrl)) {
                        dataMap.put("audioUrl", listenUrl);
                    }
                }
            }
        } else {
            dataMap.put("audioUrl", st.getWaveUri());
        }

        dataMap.put("audioTime", st.getWavePlayTimeValue());
        dataMap.put("type", st.getType());
        if (practice.getNeedRecord() && voiceEngineReadOnly) {
            dataMap.put("type", 10);
        }
        // add algov
        String algovKey = "word#" + st.getEnText();
        if (algovMap != null && algovMap.containsKey(algovKey)) {
            dataMap.put("algov", algovMap.get(algovKey));
        }

        List<WordStock> wordStocks = wordStockLoaderClient.loadWordStocksByEntext(st.getEnText());
        for (WordStock wordStock : wordStocks) {
            if (k12 != wordStock.getKtwelve()) {
                continue;
            }
            if (st.getMultiMeaning() != null && st.getMultiMeaning() > 0) {
                if (st.getMultiMeaning().equals(wordStock.getMultiMeaning())) {
                    if (wordStock.hasPicture()) {
                        dataMap.put("pictureUrl", wordStock.getPictureUrl());
                    }
                    break;
                }
            } else {
                if (st.getEnText().equals(wordStock.getEnText())) {
                    if (wordStock.hasPicture()) {
                        dataMap.put("pictureUrl", wordStock.getPictureUrl());
                    }
                    break;
                }
            }
        }

        if (question != null && (question.getContentType2Id() == 1032005 || question.getContentType2Id() == 5032005)) {
            String pictureUrl = processQuestionContentImage(question);
            if (StringUtils.isNotBlank(pictureUrl) && dataMap.getOrDefault("pictureUrl", null) == null) {
                dataMap.put("pictureUrl", pictureUrl);
            }
        }

        // 只有摩天楼的时候才需要视频地址，这是哪辈子的东西，估计已经没了？
        if (90L == practice.getId()) {
            for (WordStock wordStock : wordStocks) {
                if (StringUtils.isNotBlank(wordStock.getWordVideo()) && k12 == wordStock.getKtwelve()) {
                    dataMap.put("videoUrl", "/wordvideo/" + wordStock.getWordVideo());
                    break;
                }
            }
        }

        switch (practice.getDataType()) {
            case Constants.GameDataTemplate_GrammarData:
                break;
            case Constants.GameDataTemplate_ListenAndSpeakData:
                break;
            case Constants.GameDataTemplate_VocabularyListenData:
                fillChoice(mapper, st, k12, wordStocks);
                break;
            case Constants.GameDataTemplate_MultiuserFollowReadGame:
                break;
            case Constants.GameDataTemplate_VocabularySpellData:
                break;
            case Constants.GameDataTemplate_VocabularyAndParaphraseData:
                fillParaphraseData(mapper, st, k12, wordStocks);
                break;
            case Constants.GameDataTemplate_ReadingData:
                break;
            case Constants.GameDataTemplate_SentenceReadingData:
                fillSentenceReadingDataMapData(dataMap, st, k12, wordStocks);
                break;
            case Constants.GameDataTemplat_sentenceFillingData:
                break;
            default:
                break;
        }
        mapper.put("data", dataMap);
        return mapper;
    }

    /**
     * 填充单词配错及答案
     */
    private void fillChoice(Map<String, Object> sentenceMapper, Sentence st, Ktwelve k12, List<WordStock> wordStocks) {
        String enText = StringUtils.defaultString(st.getEnText());
        String cnText = StringUtils.defaultString(st.getCnText());
        List<Map<String, Object>> extraData = new ArrayList<>();
        WordStock wordStock = null;
        for (WordStock ws : wordStocks) {
            if (k12 != ws.getKtwelve()) {
                continue;
            }
            if (st.getMultiMeaning() != null && st.getMultiMeaning() > 0) {
                if (Objects.equals(st.getMultiMeaning(), ws.getMultiMeaning())) {
                    wordStock = ws;
                    break;
                }
            } else {
                if (StringUtils.equals(st.getEnText(), ws.getEnText())) {
                    wordStock = ws;
                    break;
                }
            }
        }
        // 随机取四个配词
        String[] wordList = wordStockLoaderClient.randomFourWords(cnText, enText, wordStock == null ? "" : wordStock.getTag());
        for (int i = 0; i < wordList.length; i++) {
            String st1 = wordList[i];
            extraData.add(MapUtils.m("text" + i, st1));
        }

        // 把答案随机替换掉选项
        int a = RandomUtils.nextInt(wordList.length);
        extraData.set(a, MapUtils.m("text" + a, EnglishWordStockUtils.stringChange(cnText)));
        extraData.add(MapUtils.m("answer", a));
        if (extraData.size() > 0) {
            sentenceMapper.put("extraData", extraData);
        }
    }

    /**
     * 填充填充单词或者句子的释义(即多意)
     */
    private void fillParaphraseData(Map<String, Object> sentenceMapper, Sentence st, Ktwelve k12, List<WordStock> wordStocks) {
        List<String> extraData = new LinkedList<>();
        for (WordStock wordStock : wordStocks) {
            if (k12 != wordStock.getKtwelve()) {
                continue;
            }
            if (st.getMultiMeaning() != null && st.getMultiMeaning() > 0) {
                if (st.getMultiMeaning().equals(wordStock.getMultiMeaning())) {
                    if (StringUtils.isNotBlank(wordStock.getParaphrase1())) {
                        extraData.add(wordStock.getParaphrase1());
                    }

                    if (StringUtils.isNotBlank(wordStock.getParaphrase2())) {
                        extraData.add(wordStock.getParaphrase2());
                    }

                    if (StringUtils.isNotBlank(wordStock.getParaphrase3())) {
                        extraData.add(wordStock.getParaphrase3());
                    }

                    if (StringUtils.isNotBlank(wordStock.getParaphrase4())) {
                        extraData.add(wordStock.getParaphrase4());
                    }
                    break;
                }

            } else {
                if (StringUtils.isNotBlank(wordStock.getParaphrase1())) {
                    extraData.add(wordStock.getParaphrase1());
                }

                if (StringUtils.isNotBlank(wordStock.getParaphrase2())) {
                    extraData.add(wordStock.getParaphrase2());
                }

                if (StringUtils.isNotBlank(wordStock.getParaphrase3())) {
                    extraData.add(wordStock.getParaphrase3());
                }

                if (StringUtils.isNotBlank(wordStock.getParaphrase4())) {
                    extraData.add(wordStock.getParaphrase4());
                }
                break;
            }
        }
        if (extraData.size() > 0) {
            sentenceMapper.put("extraData", extraData);
        }

    }

    /**
     * 填充跟读应用的sentences数据
     * pronounceUK 发音（目前以英式发音为准）
     * phoneticMapUK 音标（英式发音音标）
     */
    private void fillSentenceReadingDataMapData(Map<String, Object> dataMap, Sentence st, Ktwelve k12, List<WordStock> wordStocks) {
        for (WordStock wordStock : wordStocks) {
            if (k12 != wordStock.getKtwelve()) {
                continue;
            }
            if (st.getMultiMeaning() != null && st.getMultiMeaning() > 0) {
                if (st.getMultiMeaning().equals(wordStock.getMultiMeaning())) {
                    if (StringUtils.isNotBlank(wordStock.getPronounceUK())) {
                        dataMap.put("pronounceUK", wordStock.getPronounceUK());
                    }
                    if (StringUtils.isNotBlank(wordStock.getPhoneticMapUK())) {
                        dataMap.put("phoneticMapUK", JsonUtils.fromJsonToList(wordStock.getPhoneticMapUK(), Map.class));
                    }
                    break;
                }
            } else {
                if (st.getEnText().equals(wordStock.getEnText())) {
                    if (StringUtils.isNotBlank(wordStock.getPronounceUK())) {
                        dataMap.put("pronounceUK", wordStock.getPronounceUK());
                    }
                    if (StringUtils.isNotBlank(wordStock.getPhoneticMapUK())) {
                        dataMap.put("phoneticMapUK", JsonUtils.fromJsonToList(wordStock.getPhoneticMapUK(), Map.class));
                    }
                    break;
                }
            }
        }
    }

    /**
     * 填充短文填空数据
     */
    private Map<String, Object> fillSentenceFillingDataMapData(EnglishFlashGameComboData combo) {
        Map<String, Object> lessonMap = new LinkedHashMap<>();
        Lesson lesson = combo.getLesson();
        List<Lesson> lessons = englishContentLoaderClient.loadEnglishUnitLessons(lesson.getUnitId());
        List<String> wordList = new ArrayList<>();
        List<Long> lessonIds = new ArrayList<>();
        for (Lesson le : lessons) {
            lessonIds.add(le.getId());
        }
        Map<Long, List<Sentence>> sentenceMap = englishContentLoaderClient.loadEnglishLessonSentences(lessonIds);
        for (Long lid : sentenceMap.keySet()) {
            for (Sentence sentence : sentenceMap.get(lid)) {
                if (sentence.getType() == 1) {
                    wordList.add(sentence.getEnText());
                }
            }
        }
        lessonMap.put("wordList", wordList);
        return lessonMap;
    }

    private boolean isMathBaseDataMatch(MathBase mathBase, String dataType) {
        return mathBase != null && (StringUtils.isEmpty(dataType) || String.valueOf(mathBase.getBaseType()).equals(dataType));
    }
}
