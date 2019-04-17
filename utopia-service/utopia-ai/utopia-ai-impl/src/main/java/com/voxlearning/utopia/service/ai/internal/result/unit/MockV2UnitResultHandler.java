package com.voxlearning.utopia.service.ai.internal.result.unit;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.ai.constant.ChipsQuestionType;
import com.voxlearning.utopia.service.ai.constant.ChipsUnitType;
import com.voxlearning.utopia.service.ai.entity.AIUserLessonResultHistory;
import com.voxlearning.utopia.service.ai.entity.AIUserQuestionResultHistory;
import com.voxlearning.utopia.service.ai.entity.AIUserUnitResultHistory;
import com.voxlearning.utopia.service.ai.util.CourseRuleUtil;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class MockV2UnitResultHandler extends MockTestResultHandler {

    protected abstract ChipsUnitType type();

    @Override
    protected void doHandle(AIUserUnitResultHistory unitResultHistory, List<AIUserLessonResultHistory> lessonResultHistoryList, List<AIUserQuestionResultHistory> questionResultHistoryList) {
        // 计算单元分数
        int unitSCore = calScore(questionResultHistoryList);
        unitResultHistory.setScore(unitSCore);
        unitResultHistory.setStar(CourseRuleUtil.scoreToStar(unitSCore));
        Map<String, Object> ext = new HashMap<>();
        ext.put("grade", scoreToGrade(unitSCore));
        unitResultHistory.setExt(ext);
        unitResultHistory.setExpress(calExpress(questionResultHistoryList));
        unitResultHistory.setFluency(calFluency(questionResultHistoryList));
        unitResultHistory.setIndependent(calSelf(questionResultHistoryList));
        unitResultHistory.setListening(calListening(questionResultHistoryList));
        unitResultHistory.setPronunciation(calPronunciation(questionResultHistoryList));
    }

    private int calExpress(List<AIUserQuestionResultHistory> questionResultHistoryList) {
        List<AIUserQuestionResultHistory> list = questionResultHistoryList.stream()
                .filter(e -> e.getQuestionType().equals(ChipsQuestionType.mock_qa) || e.getQuestionType().equals(ChipsQuestionType.mock_qa_audio))
                .filter(h -> h.getScore() != null && h.getScore() >= 0)
                .filter(h -> h.getCompleteScore() != null && h.getCompleteScore() >= 0)
                .filter(h -> StringUtils.isNotBlank(h.getAnswerLevel()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(list)) {
            return 59;
        }
        // sum[回答的句子类型*50%+引擎评分中的完整性*50%] / 题目数量
        int score = new BigDecimal(list.stream().mapToInt(e -> answerLevelScore(e.getAnswerLevel()) + e.getCompleteScore()).sum()).multiply(new BigDecimal(0.5D)).divide(new BigDecimal(list.size()), 2, BigDecimal.ROUND_HALF_UP).intValue();
        return Math.min(100, Math.max(59, score));
    }

    private int calSelf(List<AIUserQuestionResultHistory> questionResultHistoryList) {
        List<AIUserQuestionResultHistory> list = questionResultHistoryList.stream()
                .filter(e -> e.getQuestionType().equals(ChipsQuestionType.mock_qa) || e.getQuestionType().equals(ChipsQuestionType.mock_qa_audio))
                .filter(h -> h.getIndependent() != null)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(list)) {
            return 59;
        }
        int inde = new BigDecimal(list.stream().mapToInt(AIUserQuestionResultHistory::getIndependent).sum()).divide(new BigDecimal(list.size()), 2, BigDecimal.ROUND_HALF_UP).intValue();
        return Math.min(100, Math.max(59, inde));
    }


    private int calFluency(List<AIUserQuestionResultHistory> questionResultHistoryList) {
        List<AIUserQuestionResultHistory> list = questionResultHistoryList.stream()
                .filter(e -> e.getQuestionType().equals(ChipsQuestionType.mock_qa) || e.getQuestionType().equals(ChipsQuestionType.mock_qa_audio))
                .filter(h -> h.getFluency() != null && h.getFluency() > 0 )
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(list)) {
            return 59;
        }
        int fluncy = new BigDecimal(list.stream().mapToInt(AIUserQuestionResultHistory::getFluency).sum()).divide(new BigDecimal(list.size()), 2, BigDecimal.ROUND_HALF_UP).intValue();
        return Math.min(100, Math.max(59, fluncy));
    }

    private int calListening(List<AIUserQuestionResultHistory> questionResultHistoryList) {
        List<AIUserQuestionResultHistory> list = questionResultHistoryList.stream()
                .filter(e -> e.getQuestionType().equals(ChipsQuestionType.mock_qa) || e.getQuestionType().equals(ChipsQuestionType.mock_qa_audio))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(list)) {
            return 59;
        }
        int fluncy = new BigDecimal(list.stream().mapToInt(e -> {
            int levelScore = answerLevelScore(e.getAnswerLevel());
            int deductScore = e.getDeductScore() == null ? 0 : e.getDeductScore();
            int score = levelScore - deductScore;
            return score > 0 ? score : 0;
        }).sum()).divide(new BigDecimal(list.size()), 2, BigDecimal.ROUND_HALF_UP).intValue();
        return Math.min(100, Math.max(59, fluncy));
    }
}
