package com.voxlearning.utopia.service.afenti.impl.service.processor.result;

import com.voxlearning.utopia.service.afenti.api.context.CastleResultContext;
import com.voxlearning.utopia.service.afenti.impl.service.processor.AbstractAfentiProcessor;
import com.voxlearning.utopia.service.afenti.impl.service.processor.AfentiTasks;

import javax.inject.Named;

/**
 * @author Ruib
 * @since 2016/7/21
 */
@Named
@AfentiTasks({
        CR_LoadNewRank.class,
        CR_LoadPushExamHistory.class,
        CR_UpdatePushExamHistory.class,
        CR_FinishQuestionTopic.class,
        CR_RecordWrongQuestionLibrary.class,
        CR_KpAchievement.class,
        CR_ActionSubmitAfentiAnswer.class,
        CR_ActionSelfLearning.class,
        CR_LastQuestion.class,
        CR_LoadAfentiOrder.class,
        CR_LoadPurchaseInfos.class,
        CR_CalculateKnowledge.class,
        CR_LoadUserRankStat.class,
        CR_CalculateStarCorrectNumber.class,
        CR_CalculateStar.class,
        CR_FinishRankTopic.class,
        CR_CalculateSilver.class,
        CR_CalculateBonusSilver.class,
        CR_CalculateMultiple.class,
        CR_CalculateCredit.class,
        CR_AddReward.class,
        CR_ActionTotalStarIncreased.class,
        CR_ParentFairylandRelative.class
})
public class CastleResultProcessor extends AbstractAfentiProcessor<CastleResultContext> {
}
