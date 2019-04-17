package com.voxlearning.utopia.service.ai.internal.result.unit;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.ai.constant.ChipsUnitType;
import com.voxlearning.utopia.service.ai.entity.AIUserLessonResultHistory;
import com.voxlearning.utopia.service.ai.entity.AIUserQuestionResultHistory;
import com.voxlearning.utopia.service.ai.entity.AIUserUnitResultHistory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Named
public abstract class UnitResultHandler extends SpringContainerSupport {

    @Inject
    private UnitResultHandleManager unitResultHandleManager;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        if (type() != null) {
            unitResultHandleManager.register(type(), this);
        }
    }

    protected abstract ChipsUnitType type();

    protected abstract void doHandle(AIUserUnitResultHistory unitResultHistory, List<AIUserLessonResultHistory> lessonResultHistoryList, List<AIUserQuestionResultHistory> questionResultHistoryList);

    public final AIUserUnitResultHistory handle(Long userId, String unitId, String bookId, List<AIUserLessonResultHistory> lessonResultHistoryList, List<AIUserQuestionResultHistory> questionResultHistoryList)  {
        AIUserUnitResultHistory newUnitResult = AIUserUnitResultHistory.build(userId, unitId, type(), bookId);
        doHandle(newUnitResult, lessonResultHistoryList, questionResultHistoryList);
        return newUnitResult;
    }
}
