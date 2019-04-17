package com.voxlearning.utopia.service.afenti.impl.service.processor.review.result;

import com.voxlearning.utopia.service.afenti.api.context.ReviewResultContext;
import com.voxlearning.utopia.service.afenti.impl.service.processor.AbstractAfentiProcessor;
import com.voxlearning.utopia.service.afenti.impl.service.processor.AfentiTasks;

import javax.inject.Named;

/**
 * @author songtao
 * @since 2017/11/29
 */
@Named
@AfentiTasks({
        RR_LoadPushExamHistory.class,
        RR_UpdatePushExamHistory.class,
        RR_LastQuestion.class,
        RR_CalculateKnowledge.class,
        RR_LoadUserRankStat.class,
        RR_CalculateStarInterval.class,
        RR_CalculateStar.class,
        RR_CalculateReward.class,
        RR_AddReward.class,
        RR_RecordFootprint.class,
        RR_FinishRankTopic.class
})
public class ReviewResultProcessor extends AbstractAfentiProcessor<ReviewResultContext> {

}
