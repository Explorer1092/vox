
package com.voxlearning.utopia.service.newhomework.impl.template.vacation;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.api.constant.AppOralScoreLevel;
import com.voxlearning.utopia.api.constant.ChiVoxScoreLevel;
import com.voxlearning.utopia.api.constant.UnisoundScoreLevel;
import com.voxlearning.utopia.api.constant.VoiceEngineType;
import com.voxlearning.utopia.core.helper.VoiceEngineTypeUtils;
import com.voxlearning.utopia.service.newhomework.api.constant.NatureSpellingType;
import com.voxlearning.utopia.service.newhomework.api.entity.base.BaseHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.NaturalSpellingSentence;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.vacationhomework.CategoryHandlerContext;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.api.entity.NewQuestionOralDict;
import com.voxlearning.utopia.service.question.api.entity.NewQuestionsSubContents;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Named
public class VacationProcessAppDetailTongueTwisterTemplate extends VacationProcessAppDetailByCategoryIdTemplate {
    @Override
    public NatureSpellingType getNatureSpellingType() {
        return NatureSpellingType.TONGUE_TWISTER;
    }

    @Override
    public void processPersonalCategory(CategoryHandlerContext categoryHandlerContext) {
        List<String> qIds = categoryHandlerContext.getQIds();
        Map<String, VacationHomeworkProcessResult> processResultMap = categoryHandlerContext.getProcessResultMap();
        List<Map<String, Object>> tongueTwisterData = categoryHandlerContext.getResult();
        Map<String, NewQuestion> newQuestionMap = categoryHandlerContext.getNewQuestionMap();
        for (String qId : qIds) {
            VacationHomeworkProcessResult n = processResultMap.get(qId);
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
                        if (VoiceEngineType.ChiVox.equals(n.getVoiceEngineType())) {
                            appOralScoreLevel = AppOralScoreLevel.of(ChiVoxScoreLevel.processLevel(score).name()).name();
                        } else {
                            appOralScoreLevel = AppOralScoreLevel.of(UnisoundScoreLevel.processLevel(score).name()).name();
                        }
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

}
