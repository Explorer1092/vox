package com.voxlearning.utopia.service.ai.internal.result.unit;

import com.alibaba.fastjson.JSONObject;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.ai.constant.ChipsQuestionType;
import com.voxlearning.utopia.service.ai.constant.ChipsUnitType;
import com.voxlearning.utopia.service.ai.entity.AIUserLessonResultHistory;
import com.voxlearning.utopia.service.ai.entity.AIUserQuestionResultHistory;
import com.voxlearning.utopia.service.ai.entity.AIUserUnitResultHistory;
import com.voxlearning.utopia.service.ai.util.CourseRuleUtil;
import com.voxlearning.utopia.service.question.api.entity.StoneData;
import com.voxlearning.utopia.service.question.consumer.StoneDataLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Named
public class SpecialConsolidationHandler extends UnitResultHandler {

    @Inject
    private StoneDataLoaderClient stoneDataLoaderClient;

    @Override
    protected ChipsUnitType type() {
        return ChipsUnitType.special_consolidation;
    }

    @Override
    protected void doHandle(AIUserUnitResultHistory newUnitResult, List<AIUserLessonResultHistory> lessonResultHistoryList, List<AIUserQuestionResultHistory> questionResultHistoryList) {
        List<Map<String, Object>> wrongQuestions = new ArrayList<>();

        Set<String> questionIdSet = questionResultHistoryList.stream()
                .filter(e -> Boolean.FALSE.equals(e.getMaster()) || (e.getQuestionType() == ChipsQuestionType.qa_sentence && e.getScore() != null && Integer.compare(e.getScore(), 60) < 0))
                .map(AIUserQuestionResultHistory::getQid)
                .collect(Collectors.toSet());
        Map<String, StoneData> stoneDataMap = stoneDataLoaderClient.loadStoneDataIncludeDisabled(questionIdSet);
        if (MapUtils.isNotEmpty(stoneDataMap)) {
            Set<String> questionSet = new HashSet<>();
            for(String qid : questionIdSet) {
                StoneData stoneData = stoneDataMap.get(qid);
                if (stoneData == null || StringUtils.isBlank(stoneData.getSchemaName())) {
                    continue;
                }
                String content = "";
                String contentAudio = "";
                String translate = "";
                ChipsQuestionType type = ChipsQuestionType.valueOf(stoneData.getSchemaName());
                switch (type) {
                    case choice_word2pic:
                    case choice_sentence2pic:
                    case choice_word2trans:
                        content = Optional.ofNullable(stoneData.getJsonData())
                                .map(e -> JSONObject.parseObject(e))
                                .map(e -> SafeConverter.toString(e.get("content")))
                                .orElse("");
                        contentAudio = Optional.ofNullable(stoneData.getJsonData())
                                .map(e -> JSONObject.parseObject(e))
                                .map(e -> SafeConverter.toString(e.get("content_audio")))
                                .orElse("");
                        translate = Optional.ofNullable(stoneData.getJsonData())
                                .map(e -> JSONObject.parseObject(e))
                                .map(e -> SafeConverter.toString(e.get("content_cn")))
                                .orElse("");
                        break;
                    case qa_sentence : //句子问答
                        content = Optional.ofNullable(stoneData.getJsonData())
                                .map(e -> JSONObject.parseObject(e))
                                .map(e -> SafeConverter.toString(e.get("answer_en")))
                                .orElse("");
                        contentAudio = Optional.ofNullable(stoneData.getJsonData())
                                .map(e -> JSONObject.parseObject(e))
                                .map(e -> SafeConverter.toString(e.get("answer_audio")))
                                .orElse("");
                        translate = Optional.ofNullable(stoneData.getJsonData())
                                .map(e -> JSONObject.parseObject(e))
                                .map(e -> SafeConverter.toString(e.get("answer_cn")))
                                .orElse("");
                        break;

                }
                if (StringUtils.isAnyBlank(content, contentAudio, translate) || questionSet.contains(content)) {
                    continue;
                }
                questionSet.add(content);
                Map<String, Object> map = new HashMap<>();
                map.put("content", content);
                map.put("contentAudio", contentAudio);
                map.put("translate", translate);
                wrongQuestions.add(map);
            }
        }
        Map<String, Object> ext = new HashMap<>();
        ext.put("wrongQuestion", wrongQuestions);
        newUnitResult.setExt(ext);

        int totalScore = lessonResultHistoryList.stream().mapToInt(AIUserLessonResultHistory::getScore).sum();
        // 计算单元分数
        int unitSCore = new BigDecimal(totalScore)
                .divide(new BigDecimal(lessonResultHistoryList.size()), 0, BigDecimal.ROUND_HALF_UP).intValue();
        unitSCore = Math.min(100, Math.max(0, unitSCore));
        newUnitResult.setScore(unitSCore);
        newUnitResult.setStar(CourseRuleUtil.scoreToStar(unitSCore));
    }

}
