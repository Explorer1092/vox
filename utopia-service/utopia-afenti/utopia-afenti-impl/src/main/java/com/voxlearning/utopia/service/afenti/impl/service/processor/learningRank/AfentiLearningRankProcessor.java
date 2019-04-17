package com.voxlearning.utopia.service.afenti.impl.service.processor.learningRank;

import com.voxlearning.utopia.service.afenti.api.context.LearningRankContext;
import com.voxlearning.utopia.service.afenti.impl.service.processor.AbstractAfentiProcessor;
import com.voxlearning.utopia.service.afenti.impl.service.processor.AfentiTasks;

import javax.inject.Named;

/**
 * @author peng.zhang.a
 * @since 16-7-27
 */
@Named
@AfentiTasks({
        ALRP_LoadLikedList.class,
        ALRP_LoadRankList.class,
        ALRP_AssembleRankResult.class
})
public class AfentiLearningRankProcessor extends AbstractAfentiProcessor<LearningRankContext> {

}
