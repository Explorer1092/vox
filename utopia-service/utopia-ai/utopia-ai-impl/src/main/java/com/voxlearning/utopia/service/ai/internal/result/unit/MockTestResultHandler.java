package com.voxlearning.utopia.service.ai.internal.result.unit;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.ai.constant.ChipsQuestionType;
import com.voxlearning.utopia.service.ai.constant.ChipsUnitType;
import com.voxlearning.utopia.service.ai.entity.AIUserQuestionResultHistory;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public abstract class MockTestResultHandler extends UnitResultHandler {

    protected abstract ChipsUnitType type();

    protected int calScore(List<AIUserQuestionResultHistory> questionResultHistoryList) {
        List<AIUserQuestionResultHistory> mockList = questionResultHistoryList.stream()
                .filter(e -> e.getQuestionType().equals(ChipsQuestionType.mock_qa) || e.getQuestionType().equals(ChipsQuestionType.mock_qa_audio))
                .filter(h -> h.getScore() != null && h.getScore() >= 0)
                .collect(Collectors.toList());

        List<AIUserQuestionResultHistory> chioceList = questionResultHistoryList.stream()
                .filter(e -> e.getQuestionType().equals(ChipsQuestionType.mock_choice) || e.getQuestionType().equals(ChipsQuestionType.mock_choice_audio))
                .filter(h -> h.getScore() != null && h.getScore() >= 0)
                .collect(Collectors.toList());
        int size = (CollectionUtils.isNotEmpty(mockList) ? mockList.size() : 0) + (CollectionUtils.isNotEmpty(chioceList) ? chioceList.size() : 0) * 2;
        if (size == 0) {
            return 0;
        }
        int m_score = mockList.stream().mapToInt(AIUserQuestionResultHistory::getScore).sum();
        int c_score = chioceList.stream().mapToInt(AIUserQuestionResultHistory::getScore).sum() * 2;
        return Math.min(100, Math.max(0, (m_score + c_score) / size));
    }

    protected int calPronunciation(List<AIUserQuestionResultHistory> questionResultHistoryList) {
        List<AIUserQuestionResultHistory> list = questionResultHistoryList.stream()
                .filter(e -> e.getQuestionType().equals(ChipsQuestionType.mock_qa) || e.getQuestionType().equals(ChipsQuestionType.mock_qa_audio))
                .filter(h -> h.getScore() != null && h.getScore() >= 0)
                .collect(Collectors.toList());
        // 发音--这里有个坑:发音APP传来的分数是8分制的需要转一下
        int t_p_score = list.stream().mapToInt(e -> {
            if (Integer.compare(e.getPronunciation(), 8) <= 0) {
                return new BigDecimal(e.getPronunciation()).multiply(new BigDecimal(100)).divide(new BigDecimal(8), 2, BigDecimal.ROUND_HALF_UP).intValue();
            }
            return e.getPronunciation();
        }).sum();
        int p_score = new BigDecimal(t_p_score).divide(new BigDecimal(list.size()), 2, BigDecimal.ROUND_HALF_UP).intValue();
        p_score = Math.min(100, p_score);
        p_score = Math.max(59, p_score);
        return p_score;
    }


    protected String scoreToGrade(int score) {
        if (score < 40) {
            return "D";
        } else if (score < 60) {
            return "C";
        } else if (score < 85) {
            return "B";
        } else if (score <= 100) {
            return "A";
        }
        return "D";
    }

    protected int answerLevelScore(String level) {
        int res = 0;
        if (StringUtils.isBlank(level)) {
            return res;
        }
        switch (level) {
            case "A+":
                res = 100;
                break;
            case "A":
                res = 90;
                break;
            case "B":
                res = 75;
                break;
            case "C":
                res = 45;
                break;
            case "D":
                res = 30;
                break;
        }
        return res;
    }
}
