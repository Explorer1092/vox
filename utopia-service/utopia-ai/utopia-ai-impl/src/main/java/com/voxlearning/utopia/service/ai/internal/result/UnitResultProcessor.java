package com.voxlearning.utopia.service.ai.internal.result;

import com.voxlearning.utopia.service.ai.data.StoneUnitData;
import com.voxlearning.utopia.service.ai.entity.AIUserLessonResultHistory;
import com.voxlearning.utopia.service.ai.entity.AIUserQuestionResultHistory;
import com.voxlearning.utopia.service.ai.entity.AIUserUnitResultHistory;

import java.util.List;

public interface UnitResultProcessor {
    AIUserUnitResultHistory process(Long userId, StoneUnitData unitData, String bookId, List<AIUserLessonResultHistory> lessonResultHistoryList, List<AIUserQuestionResultHistory> questionResultHistoryList);
}
