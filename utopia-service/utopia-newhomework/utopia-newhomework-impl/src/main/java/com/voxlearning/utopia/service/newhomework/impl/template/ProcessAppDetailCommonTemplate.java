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
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.CategoryClazzHandlerContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.CategoryHandlerContext;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.api.entity.NewQuestionsSubContents;
import com.voxlearning.utopia.service.user.api.entities.User;

import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Named
public class ProcessAppDetailCommonTemplate extends ProcessAppDetailByCategoryIdTemplate {
    @Override
    public NatureSpellingType getNatureSpellingType() {
        return NatureSpellingType.COMMON;
    }

    @Override
    public void processPersonalCategory(CategoryHandlerContext categoryHandlerContext) {
        List<String> qIds = categoryHandlerContext.getQIds();
        PracticeType practiceType = categoryHandlerContext.getPracticeType();
        Map<String, NewHomeworkProcessResult> processResultMap = categoryHandlerContext.getProcessResultMap();
        Map<Long, Sentence> sentenceMap = categoryHandlerContext.getSentenceMap();
        Map<String, NewQuestion> newQuestionMap = categoryHandlerContext.getNewQuestionMap();
        User user = categoryHandlerContext.getUser();
        List<Map<String, Object>> questionInfo = categoryHandlerContext.getResult();
        for (String qId : qIds) {
            if (!processResultMap.containsKey(qId))
                continue;
            NewQuestion newQuestion = newQuestionMap.get(qId);
            if (newQuestion == null)
                continue;
            NewHomeworkProcessResult n = processResultMap.get(qId);
            //题目正确与否
            Boolean answerInfo = null;
            //口语
            Map<String, Object> recordInfo = null;
            //显示文案
            String answerResultWord = null;
            //是否是口语题
            if (practiceType.getNeedRecord()) {
                //口语题
                int score = 0;
                AppOralScoreLevel appOralScoreLevel = n.getAppOralScoreLevel();
                if (appOralScoreLevel == null && Objects.nonNull(n.getScore())) {
                    score = new BigDecimal(n.getScore()).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
                }
                //音频地址
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
                recordInfo = MapUtils.m(
                        "score", appOralScoreLevel == null ?
                                (score + "分") :
                                appOralScoreLevel.name(),
                        "userId", n.getUserId(),
                        "userName", user.fetchRealnameIfBlankId(),
                        "userVoiceUrl", voiceUrls,
                        "voiceScoringMode", n.getVoiceScoringMode(),
                        "engineName",n.getVoiceEngineType(),
                        "engineScore",n.getActualScore()
                );

            } else {
                //非口语题
                boolean grasp = SafeConverter.toBoolean(n.getGrasp());
                answerInfo = grasp;
                //趣味拼写和读音归类 显示的文案，并没有正确与错误，只有完成
                if (grasp) {
                    answerResultWord = "我答对了";
                } else {
                    if (SafeConverter.toDouble(n.getScore()) > 0) {
                        answerResultWord = "部分正确";
                    } else {
                        answerResultWord = "我答错了";
                    }
                }
            }
            List<Long> _sentenceIds = newQuestion.getSentenceIds();
            _sentenceIds = _sentenceIds == null ? Collections.emptyList() : _sentenceIds;
            //题的句子文案
            List<Map<String, Object>> sentences = _sentenceIds
                    .stream()
                    .map(l ->
                            MapUtils.m(
                                    "sentenceId", l,
                                    "sentenceContent", Objects.nonNull(sentenceMap.get(l)) ? sentenceMap.get(l).getEnText() : ""))
                    .collect(Collectors.toList());
            //兼容句子没有文案
            if (CollectionUtils.isEmpty(sentences)) {
                sentences.add(MapUtils.m(
                        "sentenceId", "1",
                        "sentenceContent", "单词正在赶来中"));//词穷
            }
            String voiceText = null;
            if (newQuestion.getContent() != null
                    && CollectionUtils.isNotEmpty(newQuestion.getContent().getSubContents())
                    && newQuestion.getContent().getSubContents().get(0) != null
                    ) {
                NewQuestionsSubContents subContents = newQuestion.getContent().getSubContents().get(0);
                voiceText = subContents.getOralDict() != null && subContents.getOralDict().getOptions() != null && subContents.getOralDict().getOptions().get(0) != null
                        ? subContents.getOralDict().getOptions().get(0).getVoiceText()
                        : "";
            }
            questionInfo.add(
                    MapUtils.m(
                            "questionId", qId,
                            "answerResultWord", answerResultWord,
                            "sentences", sentences,
                            "voiceText", voiceText,
                            "answerInfo", answerInfo,
                            "needRecord", practiceType.getNeedRecord(),
                            "recordInfo", recordInfo
                    ));
        }
    }

    @Override
    public void processClazzCategory(CategoryClazzHandlerContext categoryClazzHandlerContext) {
        Map<String, NewQuestion> newQuestionMap = categoryClazzHandlerContext.getNewQuestionMap();
        Map<String, List<NewHomeworkProcessResult>> qIdMapNewHomeworkProcessResult = categoryClazzHandlerContext.getNewHomeworkProcessResultMap();
        PracticeType practiceType = categoryClazzHandlerContext.getPracticeType();
        Map<Long, Sentence> sentenceMap = categoryClazzHandlerContext.getSentenceMap();
        Map<Long, User> userMap = categoryClazzHandlerContext.getUserMap();
        List<Map<String, Object>> questionInfo = categoryClazzHandlerContext.getResult();

        for (String qId : newQuestionMap.keySet()) {
            NewQuestion newQuestion = newQuestionMap.get(qId);
            if (newQuestion == null)
                continue;
            List<NewHomeworkProcessResult> ls = qIdMapNewHomeworkProcessResult.get(qId);
            if (CollectionUtils.isEmpty(ls)) {
                continue;
            }
            //
            List<String> answerErrorInfo = new LinkedList<>();
            List<String> answerRightInfo = new LinkedList<>();
            List<Map<String, Object>> recordInfo = new LinkedList<>();
            int answerNum = 0;
            if (MapUtils.isNotEmpty(qIdMapNewHomeworkProcessResult)) {
                //是否是口语题
                if (practiceType.getNeedRecord()) {
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
                        recordInfo.add(MapUtils.m(
                                "score", appOralScoreLevel == null ? (score + "分") : appOralScoreLevel.name(),
                                "realScore", score,
                                "userId", n.getUserId(),
                                "userName", userMap.get(n.getUserId()).fetchRealname(),
                                "userVoiceUrl", voiceUrls,
                                "voiceScoringMode", n.getVoiceScoringMode(),
                                "tongueTwisterData", Collections.emptyList()
                        ));
                    }
                } else {
                    for (NewHomeworkProcessResult n : ls) {
                        answerNum++;
                        //读音归类和趣味拼写只有完成，没有正确错误之分
                        if (SafeConverter.toBoolean(n.getGrasp())) {
                            answerRightInfo.add(userMap.get(n.getUserId()).fetchRealnameIfBlankId());
                        } else {
                            answerErrorInfo.add(userMap.get(n.getUserId()).fetchRealnameIfBlankId());
                        }
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
            int errorProportion = 0;
            int rightProportion = 0;
            if (answerNum > 0) {
                errorProportion = new BigDecimal(answerErrorInfo.size())
                        .divide(new BigDecimal(answerNum), 2, BigDecimal.ROUND_UP)
                        .multiply(new BigDecimal(100))
                        .intValue();
                rightProportion = 100 - errorProportion;
            }
            recordInfo.sort((o1, o2) -> Integer.compare(SafeConverter.toInt(o2.get("realScore")), SafeConverter.toInt(o1.get("realScore"))));
            questionInfo.add(
                    MapUtils.m(
                            "questionId", qId,
                            "sentences", sentences,
                            "needRecord", practiceType.getNeedRecord(),
                            "errorProportion", errorProportion,
                            "rightProportion", rightProportion,
                            "answerRightInfo", answerRightInfo,
                            "answerErrorInfo", answerErrorInfo,
                            "recordInfo", CollectionUtils.isEmpty(recordInfo) ? null : recordInfo
                    ));
        }


    }
}
