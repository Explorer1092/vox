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
public class EssentialToPassHandler extends UnitResultHandler {

    @Override
    protected ChipsUnitType type() {
        return ChipsUnitType.essential_to_pass;
    }

    @Override
    protected void doHandle(AIUserUnitResultHistory newUnitResult, List<AIUserLessonResultHistory> lessonResultHistoryList, List<AIUserQuestionResultHistory> questionResultHistoryList) {
        Integer score = calScore(questionResultHistoryList);
        if (score != null) {
            newUnitResult.setScore(score);
            newUnitResult.setStar(CourseRuleUtil.scoreToStar(score));
        }
    }

    private Integer calScore(List<AIUserQuestionResultHistory> questionResultHistoryList) {
        if (CollectionUtils.isEmpty(questionResultHistoryList)) {
            return null;
        }
        List<AIUserQuestionResultHistory> caList = questionResultHistoryList.stream().filter(e -> e.getScore() != null && e.getScore() > 0).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(caList)) {
            return null;
        }
        int total = caList.stream().mapToInt(AIUserQuestionResultHistory::getScore).sum();
        return total / caList.size();
    }
}
