package com.voxlearning.utopia.service.afenti.impl.service.processor.result;

import com.voxlearning.utopia.service.afenti.api.context.ElfResultContext;
import com.voxlearning.utopia.service.afenti.impl.service.processor.AbstractAfentiProcessor;
import com.voxlearning.utopia.service.afenti.impl.service.processor.AfentiTasks;

import javax.inject.Named;

/**
 * @author Ruib
 * @since 2016/7/24
 */
@Named
@AfentiTasks({
        SER_LoadWrongQuestionLibrary.class,
        SER_ChangeState.class,
        SER_GrowthWorldTopic.class,
        SER_LoadWaitingList.class,
        SER_ActionWrongQuestionRescued.class,
        SER_CalculateReward.class,
        SER_RewardAndChangeState.class
})
public class SimilarElfResultProcessor extends AbstractAfentiProcessor<ElfResultContext> {
}
