package com.voxlearning.utopia.service.newhomework.impl.template;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.api.constant.AppOralScoreLevel;
import com.voxlearning.utopia.api.constant.VoiceEngineType;
import com.voxlearning.utopia.core.helper.VoiceEngineTypeUtils;
import com.voxlearning.utopia.service.content.api.entity.PracticeType;
import com.voxlearning.utopia.service.content.api.entity.Sentence;
import com.voxlearning.utopia.service.newhomework.api.constant.NatureSpellingType;
import com.voxlearning.utopia.service.newhomework.api.entity.base.BaseHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.NaturalSpellingSentence;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.CategoryClazzHandlerContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.CategoryHandlerContext;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.api.entity.NewQuestionOralDict;
import com.voxlearning.utopia.service.question.api.entity.NewQuestionsSubContents;
import com.voxlearning.utopia.service.user.api.entities.User;

import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Named
public class ProcessAppDetailTongueTwisterTemplate extends ProcessAppDetailByCategoryIdTemplate {
    @Override
    public NatureSpellingType getNatureSpellingType() {
        return NatureSpellingType.TONGUE_TWISTER;
    }

    @Override
    public void processPersonalCategory(CategoryHandlerContext categoryHandlerContext) {
        List<String> qIds = categoryHandlerContext.getQIds();
        Map<String, NewHomeworkProcessResult> processResultMap = categoryHandlerContext.getProcessResultMap();
        List<Map<String, Object>> tongueTwisterData = categoryHandlerContext.getResult();
        Map<String, NewQuestion> newQuestionMap = categoryHandlerContext.getNewQuestionMap();
        for (String qId : qIds) {
            NewHomeworkProcessResult n = processResultMap.get(qId);
            if (n == null) continue;
            NewQuestion newQuestion = newQuestionMap.get(qId);
            if (newQuestion == null)
                continue;
            Map<String, Object> tongueTwisterQuestion = new LinkedHashMap<>();
            tongueTwisterData.add(tongueTwisterQuestion);
            //每个句子的数据
            List<Map<String, Object>> sentenceData = new LinkedList<>();
            //音频地址
            List<String> voiceUrls = new LinkedList<>();
            //句子部分
            List<Map<String, Object>> __sentences = Collections.emptyList();
            if (newQuestion.getContent() != null && CollectionUtils.isNotEmpty(newQuestion.getContent().getSubContents())) {
                __sentences = newQuestion.getContent()
                        .getSubContents()
                        .stream()
                        .filter(o -> o.getOralDict() != null)
                        .map(NewQuestionsSubContents::getOralDict)
                        .filter(o -> CollectionUtils.isNotEmpty(o.getOptions()))
                        .map(NewQuestionOralDict::getOptions)
                        .flatMap(Collection::stream)
                        .filter(Objects::nonNull)
                        .filter(o -> StringUtils.isNotBlank(o.getVoiceText()))
                        .map(o -> MapUtils.m(
                                "sentenceId", "",
                                "sentenceContent", o.getVoiceText()))
                        .collect(Collectors.toList());
            }
            //音频地址
            if (CollectionUtils.isNotEmpty(n.getOralDetails())) {
                for (List<BaseHomeworkProcessResult.OralDetail> oralDetails : n.getOralDetails()) {
                    if (CollectionUtils.isNotEmpty(oralDetails)) {
                        for (BaseHomeworkProcessResult.OralDetail oralDetail : oralDetails) {
                            String voiceUrl = oralDetail.getAudio();
                            VoiceEngineType voiceEngineType = n.getVoiceEngineType();
                            voiceUrl = VoiceEngineTypeUtils.getAudioUrl(voiceUrl, voiceEngineType);
                            voiceUrls.add(voiceUrl);
                        }
                    }
                }
            }

            List<NaturalSpellingSentence> allSentence = n.findAllSentence();
            if (CollectionUtils.isNotEmpty(allSentence)) {
                for (NaturalSpellingSentence sentence : allSentence) {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("text", SafeConverter.toString(sentence.getSample()));
                    String appOralScoreLevel;
                    if (StringUtils.isBlank(sentence.getStandardScoreLevel())) {
                        //兼容老数据，老数据并没有这个字段
                        //兼容新数据，没有这个字段的情况
                        double score = SafeConverter.toDouble(sentence.getScore());
                        appOralScoreLevel = NewHomeworkUtils.handleAppOralScoreLevel(score).name();
                    } else {
                        appOralScoreLevel = sentence.getStandardScoreLevel();
                    }
                    map.put("appOralScoreLevel", appOralScoreLevel);
                    sentenceData.add(map);
                }
            }
            tongueTwisterQuestion.put("recordInfo", MapUtils.m(
                    "userVoiceUrl", voiceUrls,
                    "voiceScoringMode", n.getVoiceScoringMode()
            ));
            tongueTwisterQuestion.put("sentences", __sentences);
            tongueTwisterQuestion.put("data", sentenceData);
        }
    }

    @Override
    public void processClazzCategory(CategoryClazzHandlerContext categoryClazzHandlerContext) {
        Map<String, NewQuestion> newQuestionMap = categoryClazzHandlerContext.getNewQuestionMap();
        PracticeType practiceType = categoryClazzHandlerContext.getPracticeType();
        List<Map<String, Object>> questionInfo = categoryClazzHandlerContext.getResult();
        Map<String, List<NewHomeworkProcessResult>> qIdMapNewHomeworkProcessResult = categoryClazzHandlerContext.getNewHomeworkProcessResultMap();
        Map<Long, Sentence> sentenceMap = categoryClazzHandlerContext.getSentenceMap();
        Map<Long, User> userMap = categoryClazzHandlerContext.getUserMap();
        for (String qId : newQuestionMap.keySet()) {
            NewQuestion newQuestion = newQuestionMap.get(qId);
            if (newQuestion == null)
                continue;
            List<Map<String, Object>> recordInfo = new LinkedList<>();
            if (MapUtils.isNotEmpty(qIdMapNewHomeworkProcessResult)) {
                //是否是口语题
                if (CollectionUtils.isNotEmpty(qIdMapNewHomeworkProcessResult.get(qId))) {
                    List<NewHomeworkProcessResult> ls = qIdMapNewHomeworkProcessResult.get(qId);
                    for (NewHomeworkProcessResult n : ls) {
                        int score = 0;
                        AppOralScoreLevel appOralScoreLevel = n.getAppOralScoreLevel();
                        if (Objects.nonNull(n.getScore())) {
                            score = new BigDecimal(n.getScore())
                                    .setScale(0, BigDecimal.ROUND_HALF_UP)
                                    .intValue();
                        }
                        List<String> voiceUrls = new LinkedList<>();
                        if (CollectionUtils.isNotEmpty(n.getOralDetails())) {
                            for (List<BaseHomeworkProcessResult.OralDetail> oralDetails : n.getOralDetails()) {
                                if (CollectionUtils.isNotEmpty(oralDetails)) {
                                    for (BaseHomeworkProcessResult.OralDetail oralDetail : oralDetails) {
                                        String voiceUrl = oralDetail.getAudio();
                                        VoiceEngineType voiceEngineType = n.getVoiceEngineType();
                                        voiceUrl = VoiceEngineTypeUtils.getAudioUrl(voiceUrl, voiceEngineType);
                                        voiceUrls.add(voiceUrl);
                                    }
                                }
                            }
                        }
                        //绕口令需要特殊处理，一个题里面有多个句子的，按照一个一个句子来显示
                        List<Map<String, Object>> tongueTwisterData = new LinkedList<>();
                        List<NaturalSpellingSentence> allSentence = n.findAllSentence();
                        if (CollectionUtils.isNotEmpty(allSentence)) {
                            for (NaturalSpellingSentence sentence : allSentence) {
                                Map<String, Object> map = new LinkedHashMap<>();
                                map.put("text", SafeConverter.toString(sentence.getSample()));
                                String tongueTwisterAppOralScoreLevel;
                                if (StringUtils.isBlank(sentence.getStandardScoreLevel())) {
                                    //兼容老数据，老数据并没有这个字段
                                    //兼容新数据，没有这个字段的情况
                                    double tongueTwisterScore = SafeConverter.toDouble(sentence.getScore());
                                    tongueTwisterAppOralScoreLevel = NewHomeworkUtils.handleAppOralScoreLevel(tongueTwisterScore).name();
                                } else {
                                    tongueTwisterAppOralScoreLevel = sentence.getStandardScoreLevel();
                                }
                                map.put("appOralScoreLevel", tongueTwisterAppOralScoreLevel);
                                tongueTwisterData.add(map);
                            }
                        }
                        recordInfo.add(MapUtils.m(
                                "score", appOralScoreLevel == null ? (score + "分") : appOralScoreLevel.name(),
                                "realScore", score,
                                "userId", n.getUserId(),
                                "userName", userMap.get(n.getUserId()).fetchRealname(),
                                "userVoiceUrl", voiceUrls,
                                "voiceScoringMode", n.getVoiceScoringMode(),
                                "tongueTwisterData", tongueTwisterData
                        ));
                    }
                }
            }
            List<Long> _sentenceIds = newQuestion.getSentenceIds();
            List<Map<String, Object>> sentences = CollectionUtils.isNotEmpty(_sentenceIds) ?
                    _sentenceIds
                            .stream()
                            .map(l ->
                                    MapUtils.m(
                                            "sentenceId", l,
                                            "sentenceContent", Objects.isNull(sentenceMap.get(l)) ? "" : sentenceMap.get(l).getEnText()))
                            .collect(Collectors.toList()) :
                    Collections.emptyList();
            //题库没有数据的情况
            if (CollectionUtils.isEmpty(sentences)) {
                sentences.add(MapUtils.m(
                        "sentenceId", "1",
                        "sentenceContent", "单词正在赶来中"));//
            }
            recordInfo.sort((o1, o2) -> Integer.compare(SafeConverter.toInt(o2.get("realScore")), SafeConverter.toInt(o1.get("realScore"))));
            questionInfo.add(
                    MapUtils.m(
                            "questionId", qId,
                            "sentences", sentences,
                            "needRecord", practiceType.getNeedRecord(),
                            "errorProportion", 0,
                            "rightProportion", 0,
                            "answerRightInfo", Collections.emptyList(),
                            "answerErrorInfo", Collections.emptyList(),
                            "recordInfo", CollectionUtils.isEmpty(recordInfo) ? null : recordInfo
                    ));
        }
    }
}
