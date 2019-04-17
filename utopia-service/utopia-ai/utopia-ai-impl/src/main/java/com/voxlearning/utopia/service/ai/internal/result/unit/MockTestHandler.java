package com.voxlearning.utopia.service.ai.internal.result.unit;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.service.ai.constant.ChipsQuestionType;
import com.voxlearning.utopia.service.ai.constant.ChipsUnitType;
import com.voxlearning.utopia.service.ai.entity.AIUserLessonResultHistory;
import com.voxlearning.utopia.service.ai.entity.AIUserQuestionResultHistory;
import com.voxlearning.utopia.service.ai.entity.AIUserUnitResultHistory;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishPageContentConfig;
import com.voxlearning.utopia.service.ai.impl.service.AiChipsEnglishConfigServiceImpl;
import com.voxlearning.utopia.service.ai.util.CourseRuleUtil;
import lombok.Getter;
import lombok.Setter;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Named
public class MockTestHandler extends MockTestResultHandler {

    @Inject
    private AiChipsEnglishConfigServiceImpl chipsEnglishConfigService;

    private final static String SUMMARY_KET_PRE = "Mock_Summary_";

    @Override
    protected ChipsUnitType type() {
        return ChipsUnitType.mock_test;
    }

    @Override
    protected void doHandle(AIUserUnitResultHistory newUnitResult, List<AIUserLessonResultHistory> lessonResultHistoryList, List<AIUserQuestionResultHistory> questionResultHistoryList) {
        // 计算单元分数
        int unitSCore = calScore(questionResultHistoryList);
        newUnitResult.setScore(unitSCore);
        newUnitResult.setStar(CourseRuleUtil.scoreToStar(unitSCore));

        Map<String, Object> ext = new HashMap<>();
        ext.put("grade", scoreToGrade(unitSCore));

        List<AbilityBean> abilityBeans = new ArrayList<>();
        int score_p = calPronunciation(questionResultHistoryList);
        abilityBeans.add(new AbilityBean("P", score_p));

        int score_cs = calExpress(questionResultHistoryList);
        abilityBeans.add(new AbilityBean("CS", score_cs));

        int score_g = calG(questionResultHistoryList);
        abilityBeans.add(new AbilityBean("G", score_g));

        int score_l = calL(questionResultHistoryList);
        abilityBeans.add(new AbilityBean("L", score_l));

        ext.put("P", score_p);
        ext.put("CS", score_cs);
        ext.put("G", score_g);
        ext.put("L", score_l);

        List<ChipsEnglishPageContentConfig> summaryList = chipsEnglishConfigService.loadAllChipsConfig()
                .stream().filter(e -> e.getName().contains(SUMMARY_KET_PRE)).collect(Collectors.toList());
        ext.put("summary", calSummary(summaryList, abilityBeans, unitSCore));

        newUnitResult.setExt(ext);

    }

    private int calG(List<AIUserQuestionResultHistory> questionResultHistoryList) {
        List<AIUserQuestionResultHistory> fluencyList = questionResultHistoryList.stream()
                .filter(e -> e.getQuestionType().equals(ChipsQuestionType.mock_qa))
                .filter(h -> h.getFluency() != null && h.getFluency() >= 0)
                .filter(h -> h.getPronunciation() != null && h.getPronunciation() >= 0)
                .filter(h -> h.getCompleteScore() != null && h.getCompleteScore() >= 0)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(fluencyList)) {
            return 0;
        }
        //sum[(百分制的发音得分 * 0.8 + 流利度得分 * 0.2)*完整度得分，]*0.01 / 题目数量
        int score = new BigDecimal(fluencyList.stream().mapToInt(e -> {
            int p_s = new BigDecimal(e.getPronunciation()).multiply(new BigDecimal(100)).multiply(new BigDecimal(0.8D)).divide(new BigDecimal(8), 2, BigDecimal.ROUND_HALF_UP).intValue();
            int f_s = e.getFluency() / 5;
            return (p_s + f_s) * e.getCompleteScore();
        }).sum()).multiply(new BigDecimal(0.01D)).divide(new BigDecimal(fluencyList.size()), 2, BigDecimal.ROUND_HALF_UP).intValue();
        return Math.min(100, score);
    }

    private int calExpress(List<AIUserQuestionResultHistory> questionResultHistoryList) {
        List<AIUserQuestionResultHistory> list = questionResultHistoryList.stream()
                .filter(e -> e.getQuestionType().equals(ChipsQuestionType.mock_qa))
                .filter(h -> h.getScore() != null && h.getScore() >= 0)
                .filter(h -> h.getCompleteScore() != null && h.getCompleteScore() >= 0)
                .filter(h -> StringUtils.isNotBlank(h.getAnswerLevel()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(list)) {
            return 0;
        }
        // sum[（类型得分*完整度得分)+ （类型得分*完整度得分)]* 0.01 / 题目数量
        int score = new BigDecimal(list.stream().mapToInt(e -> answerLevelScore(e.getAnswerLevel()) * e.getCompleteScore()).sum()).multiply(new BigDecimal(0.01D)).divide(new BigDecimal(list.size()), 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(0.5D)).intValue();
        return Math.min(100, score);
    }

    private int calL(List<AIUserQuestionResultHistory> questionResultHistoryList) {
        List<AIUserQuestionResultHistory> list = questionResultHistoryList.stream()
                .filter(e -> e.getQuestionType().equals(ChipsQuestionType.mock_qa))
                .filter(h -> h.getPronunciation() != null && h.getPronunciation() >= 0)
                .filter(h -> h.getCompleteScore() != null && h.getCompleteScore() >= 0)
                .filter(h -> StringUtils.isNotBlank(h.getAnswerLevel()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(list)) {
            return 0;
        }

        //（发音*0.7+类型*0.3）*完整性*0.01
        int sent = new BigDecimal(list.stream().mapToInt(e -> {
            int p_s = new BigDecimal(e.getPronunciation()).multiply(new BigDecimal(100)).multiply(new BigDecimal(0.7D)).divide(new BigDecimal(8), 2, BigDecimal.ROUND_HALF_UP).intValue();
            int f_s = answerLevelScore(e.getAnswerLevel()) * 10 / 3;
            return (p_s + f_s) * e.getCompleteScore();
        }).sum()).multiply(new BigDecimal(0.01D)).divide(new BigDecimal(list.size()), 2, BigDecimal.ROUND_HALF_UP).intValue();

        return Math.min(100, sent);
    }


    private String calSummary(List<ChipsEnglishPageContentConfig> summaryConfigList, List<AbilityBean> abilityBeans, Integer score) {
        List<String> defaultSummary = Optional.ofNullable(summaryConfigList)
                .map(e -> e.stream().filter(e1 -> e1.getName().equals(SUMMARY_KET_PRE + "Total")).findFirst().orElse(null))
                .map(e -> JsonUtils.fromJsonToList(e.getValue(), Summary.class))
                .filter(e -> CollectionUtils.isNotEmpty(e))
                .map(e -> {
                    e.sort(Comparator.comparing(Summary::getScore));
                    for (Summary summary : e) {
                        if (Integer.compare(score, summary.getScore()) < 0) {
                            return summary;
                        }
                    }
                    return e.get(0);
                })
                .map(Summary::getValue)
                .orElse(Collections.emptyList());
        //开始
        String begin = Optional.ofNullable(defaultSummary).filter(e -> CollectionUtils.isNotEmpty(e)).map(e -> e.get(0)).orElse("");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(begin);

        abilityBeans.sort(Comparator.comparing(AbilityBean::getScore).reversed());
        stringBuilder.append(levelSummary(summaryConfigList, abilityBeans, 0, 0));
        stringBuilder.append(levelSummary(summaryConfigList, abilityBeans, 1, 1));
        stringBuilder.append(levelSummary(summaryConfigList, abilityBeans, abilityBeans.size() - 1, 2));

        //结尾
        String end = Optional.ofNullable(defaultSummary).filter(e -> CollectionUtils.isNotEmpty(e) && e.size() > 1).map(e -> e.get(1)).orElse("");
        stringBuilder.append(end);
        return stringBuilder.toString();
    }

    private String levelSummary(List<ChipsEnglishPageContentConfig> summaryConfigList, List<AbilityBean> abilityBeans, int beanIndex, int summaryIndex) {
        return Optional.ofNullable(abilityBeans)
                .filter(e -> e.size() > beanIndex)
                .map(e -> e.get(beanIndex))
                .map(e -> summaryConfigList.stream().filter(e1 -> e1.getName().equals(SUMMARY_KET_PRE + e.key)).findFirst().orElse(null))
                .map(e -> JsonUtils.fromJsonToList(e.getValue(), SummaryMap.class))
                .filter(e -> CollectionUtils.isNotEmpty(e))
                .map(e -> e.get(Math.min(summaryIndex, e.size() - 1)))
                .map(e -> {
                    int beanScore = Math.min(99, abilityBeans.get(beanIndex).getScore());
                    int min = 100;
                    String val = "";
                    for (String key : e.keySet()) {
                        int keyScore = SafeConverter.toInt(key);
                        if (keyScore > 0 && beanScore < keyScore && keyScore <= min) {
                            val = e.get(key);
                            min = keyScore;
                        }
                    }
                    return val;
                }).orElse("");
    }

    @Getter
    @Setter
    private class AbilityBean {
        private String key;
        private Integer score;

        public AbilityBean(String key, Integer score) {
            this.key = key;
            this.score = score;
        }
    }

    @Getter
    @Setter
    private static class Summary implements Serializable {
        private Integer score;
        private List<String> value;
    }

    private static class SummaryMap extends HashMap<String, String> {
    }


}
