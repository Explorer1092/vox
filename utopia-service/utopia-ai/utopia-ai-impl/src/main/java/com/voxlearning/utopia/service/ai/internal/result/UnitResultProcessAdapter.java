package com.voxlearning.utopia.service.ai.internal.result;

import com.voxlearning.utopia.service.ai.data.StoneUnitData;
import com.voxlearning.utopia.service.ai.entity.AIUserLessonResultHistory;
import com.voxlearning.utopia.service.ai.entity.AIUserQuestionResultHistory;
import com.voxlearning.utopia.service.ai.entity.AIUserUnitResultHistory;
import com.voxlearning.utopia.service.ai.impl.persistence.AIUserUnitResultHistoryDao;
import com.voxlearning.utopia.service.ai.internal.result.unit.UnitResultHandleManager;
import com.voxlearning.utopia.service.ai.internal.result.unit.UnitResultHandler;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Optional;

@Named
public final class UnitResultProcessAdapter implements UnitResultProcessor {

    @Inject
    private UnitResultHandleManager unitResultHandleManager;

    @Inject
    protected AIUserUnitResultHistoryDao aiUserUnitResultHistoryDao;


    @Override
    public AIUserUnitResultHistory process(Long userId, StoneUnitData unitData, String bookId, List<AIUserLessonResultHistory> lessonResultHistoryList, List<AIUserQuestionResultHistory> questionResultHistoryList) {
        UnitResultHandler unitResultHandler = unitResultHandleManager.get(unitData.getJsonData().getUnit_type());
        if (unitResultHandler == null) {
            return null;
        }

        AIUserUnitResultHistory aiUserUnitResultHistory = unitResultHandler.handle(userId, unitData.getId(), bookId, lessonResultHistoryList, questionResultHistoryList);
        if (aiUserUnitResultHistory == null) {
            return null;
        }
        Integer star = aiUserUnitResultHistory.getStar();
        aiUserUnitResultHistory.setCurrentStar(star);
        AIUserUnitResultHistory lastHistory = aiUserUnitResultHistoryDao.load(userId, unitData.getId());//该用户该单元上次回答结果
        Integer maxStar = Optional.ofNullable(lastHistory).map(e -> e.getStar()).map(e -> star == null || e > star ? e : star).orElse(star);
        aiUserUnitResultHistory.setStar(maxStar);//设置成历史星星最大值
        aiUserUnitResultHistoryDao.disableOld(userId, unitData.getId());
        aiUserUnitResultHistoryDao.insert(aiUserUnitResultHistory);
        return aiUserUnitResultHistory;
    }
}
