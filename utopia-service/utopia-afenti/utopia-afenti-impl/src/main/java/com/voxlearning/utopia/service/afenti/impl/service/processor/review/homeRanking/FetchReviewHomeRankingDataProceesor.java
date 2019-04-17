package com.voxlearning.utopia.service.afenti.impl.service.processor.review.homeRanking;

import com.voxlearning.utopia.service.afenti.api.context.FetchReviewHomeRankingContext;
import com.voxlearning.utopia.service.afenti.impl.service.processor.AbstractAfentiProcessor;
import com.voxlearning.utopia.service.afenti.impl.service.processor.AfentiTasks;

import javax.inject.Named;

/**
 * @author songtao
 * @since 2017/12/1
 */
@Named
@AfentiTasks({
        FRHRD_LoadUserInfo.class,
        FRHRD_LoadClassInfo.class,
        FRHRD_LoadSchoolInfo.class
})
public class FetchReviewHomeRankingDataProceesor extends AbstractAfentiProcessor<FetchReviewHomeRankingContext> {

}
