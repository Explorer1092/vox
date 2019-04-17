package com.voxlearning.utopia.service.afenti.impl.service.processor.review.ranking;

import com.voxlearning.utopia.service.afenti.api.context.FetchReviewRankingContext;
import com.voxlearning.utopia.service.afenti.impl.service.processor.AbstractAfentiProcessor;
import com.voxlearning.utopia.service.afenti.impl.service.processor.AfentiTasks;

import javax.inject.Named;

/**
 * @author songtao
 * @since 2017/12/1
 */
@Named
@AfentiTasks({
        FRR_LoadRankingInfo.class,
        FRR_TransformClassRanking.class,
        FRR_TransformSchoolRanking.class
})
public class FetchReviewRankingProcessor extends AbstractAfentiProcessor<FetchReviewRankingContext> {
}
