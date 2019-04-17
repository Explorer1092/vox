package com.voxlearning.utopia.service.ai.internal.result.unit;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.service.ai.constant.ChipsUnitType;
import com.voxlearning.utopia.service.ai.entity.AIUserLessonResultHistory;
import com.voxlearning.utopia.service.ai.entity.AIUserQuestionResultHistory;
import com.voxlearning.utopia.service.ai.entity.AIUserUnitResultHistory;
import com.voxlearning.utopia.service.ai.util.CourseRuleUtil;

import javax.inject.Named;
import java.util.List;
import java.util.stream.Collectors;

@Named
public class ReviewHandler extends UnitResultHandler {

    @Override
    protected ChipsUnitType type() {
        return ChipsUnitType.review_unit;
    }

    @Override
    protected void doHandle(AIUserUnitResultHistory newUnitResult, List<AIUserLessonResultHistory> lessonResultHistoryList, List<AIUserQuestionResultHistory> questionResultHistoryList) {

        // 计算单元分数
        int unitSCore = calScore(questionResultHistoryList);
        newUnitResult.setScore(unitSCore);
        newUnitResult.setStar(CourseRuleUtil.scoreToStar(unitSCore));
    }

    protected int calScore(List<AIUserQuestionResultHistory> questionResultHistoryList) {
        List<AIUserQuestionResultHistory> list = questionResultHistoryList.stream()
                .filter(h -> h.getScore() != null && h.getScore() >= 0)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(list)) {
            return 0;
        }

        int c_score = list.stream().mapToInt(AIUserQuestionResultHistory::getScore).sum();
        return Math.min(100, Math.max(0,  c_score / list.size()));
    }
}
