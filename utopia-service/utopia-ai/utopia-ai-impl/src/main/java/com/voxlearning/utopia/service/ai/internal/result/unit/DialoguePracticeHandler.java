package com.voxlearning.utopia.service.ai.internal.result.unit;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.service.ai.constant.ChipsQuestionType;
import com.voxlearning.utopia.service.ai.constant.ChipsUnitType;
import com.voxlearning.utopia.service.ai.entity.AIUserLessonResultHistory;
import com.voxlearning.utopia.service.ai.entity.AIUserQuestionResultHistory;
import com.voxlearning.utopia.service.ai.entity.AIUserUnitResultHistory;
import com.voxlearning.utopia.service.ai.util.CourseRuleUtil;

import javax.inject.Named;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Named
public class DialoguePracticeHandler extends UnitResultHandler {

    @Override
    protected ChipsUnitType type() {
        return ChipsUnitType.dialogue_practice;
    }

    @Override
    protected void doHandle(AIUserUnitResultHistory newUnitResult, List<AIUserLessonResultHistory> lessonResultHistoryList, List<AIUserQuestionResultHistory> questionResultHistoryList) {
        int totalScore = lessonResultHistoryList.stream().mapToInt(AIUserLessonResultHistory::getScore).sum();
        // 计算单元分数
        int unitSCore = new BigDecimal(totalScore).divide(new BigDecimal(lessonResultHistoryList.size()), 0, BigDecimal.ROUND_HALF_UP).intValue();
        unitSCore = Math.min(100, Math.max(0, unitSCore));
        newUnitResult.setScore(unitSCore);
        newUnitResult.setStar(CourseRuleUtil.scoreToStar(unitSCore));

        newUnitResult.setExpress(calExpress(questionResultHistoryList));
        newUnitResult.setFluency(calFluency(questionResultHistoryList));
        newUnitResult.setIndependent(calSelf(questionResultHistoryList));
        newUnitResult.setListening(calListening(questionResultHistoryList));
        newUnitResult.setPronunciation(calPronunciation(questionResultHistoryList));
    }


    private int calListening(List<AIUserQuestionResultHistory> questionResultHistoryList) {
        List<AIUserQuestionResultHistory> list = questionResultHistoryList.stream()
                .filter(e -> e.getQuestionType() == ChipsQuestionType.task_conversation || e.getQuestionType() == ChipsQuestionType.task_topic || e.getQuestionType() == ChipsQuestionType.video_dialogue)
                .filter(h -> h.getScore() != null && h.getScore() > 0 )
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(list)) {
            return 59;
        }
        int score = new BigDecimal(list.stream().mapToInt(AIUserQuestionResultHistory::getScore).sum()).divide(new BigDecimal(list.size()), 2, BigDecimal.ROUND_HALF_UP).intValue();
        return Math.min(100, Math.max(59, score));
    }

    private int calPronunciation(List<AIUserQuestionResultHistory> questionResultHistoryList) {
        List<AIUserQuestionResultHistory> list = questionResultHistoryList.stream().filter(e -> e.getQuestionType() == ChipsQuestionType.task_conversation || e.getQuestionType() == ChipsQuestionType.task_topic || e.getQuestionType() == ChipsQuestionType.video_dialogue|| e.getQuestionType() == ChipsQuestionType.sentence_repeat).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(list)) {
            return 59;
        }
        // 发音--这里有个坑:发音APP传来的分数是8分制的需要转一下
        int t_p_score = list.stream().mapToInt(e -> {
            if (Integer.compare(e.getPronunciation(), 8) <= 0) {
                return new BigDecimal(e.getPronunciation()).multiply(new BigDecimal(100)).divide(new BigDecimal(8), 2, BigDecimal.ROUND_HALF_UP).intValue();
            }
            return e.getPronunciation();
        }).sum();
        int p_score = new BigDecimal(t_p_score).divide(new BigDecimal(list.size()), 2, BigDecimal.ROUND_HALF_UP).intValue();
        p_score = Math.min(100, Math.max(59, p_score));
        return p_score;
    }

    private int calFluency(List<AIUserQuestionResultHistory> questionResultHistoryList) {
        List<AIUserQuestionResultHistory> fluencyList = questionResultHistoryList.stream()
                .filter(e -> e.getQuestionType() == ChipsQuestionType.task_conversation || e.getQuestionType() == ChipsQuestionType.task_topic || e.getQuestionType() == ChipsQuestionType.video_dialogue)
                .filter(h -> h.getFluency() != null && h.getFluency() > 0)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(fluencyList)) {
            return 59;
        }
        return Math.min(100, Math.max(59, new BigDecimal(fluencyList.stream().mapToInt(AIUserQuestionResultHistory::getFluency).sum())
                .divide(new BigDecimal(fluencyList.size()), 2, BigDecimal.ROUND_HALF_UP).intValue()));
    }

    private int calSelf(List<AIUserQuestionResultHistory> questionResultHistoryList) {
        List<AIUserQuestionResultHistory> list = questionResultHistoryList.stream()
                .filter(e -> e.getQuestionType() == ChipsQuestionType.task_conversation || e.getQuestionType() == ChipsQuestionType.task_topic || e.getQuestionType() == ChipsQuestionType.video_dialogue)
                .filter(h -> h.getScore() != null && h.getScore() > 0 )
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(list)) {
            return 59;
        }
        int deductScore = new BigDecimal(list.stream().filter(e -> e.getDeductScore() != null).mapToInt(AIUserQuestionResultHistory::getDeductScore).sum()).intValue();
        return Math.min(100, Math.max(59, 100 - deductScore));
    }

    private int calExpress(List<AIUserQuestionResultHistory> questionResultHistoryList) {
        List<AIUserQuestionResultHistory> list = questionResultHistoryList.stream()
                .filter(e -> e.getQuestionType() == ChipsQuestionType.task_conversation || e.getQuestionType() == ChipsQuestionType.task_topic || e.getQuestionType() == ChipsQuestionType.video_dialogue)
                .filter(h -> h.getScore() != null && h.getScore() > 0 )
                .filter(h -> h.getCompleteScore() != null && h.getCompleteScore() > 0 )
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(list)) {
            return 59;
        }
        int score = new BigDecimal(list.stream().mapToInt(AIUserQuestionResultHistory::getScore).sum()).divide(new BigDecimal(list.size()), 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(0.5D)).intValue();
        int complete = new BigDecimal(list.stream().mapToInt(AIUserQuestionResultHistory::getCompleteScore).sum()).divide(new BigDecimal(list.size()), 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(0.5D)).intValue();
        return Math.min(100, Math.max(59, score + complete));
    }
}
