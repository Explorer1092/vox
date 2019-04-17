package com.voxlearning.utopia.service.afenti.impl.service.processor.review;

import com.voxlearning.utopia.service.afenti.api.context.FetchReviewRanksContext;
import com.voxlearning.utopia.service.afenti.impl.service.processor.AbstractAfentiProcessor;
import com.voxlearning.utopia.service.afenti.impl.service.processor.AfentiTasks;

import javax.inject.Named;

/**
 * @author songtao
 * @since 2017/11/28
 */
@Named
@AfentiTasks({
        FRRD_LoadAfentiBook.class,
        FRRD_LoadAfentiOrder.class,
        FRRD_LoadCurrentDayPushCount.class,
        FRRD_LoadFamilyJoin.class,
        FRRD_LoadPushExamHistory.class,
        FRRD_LoadRankStar.class,
        FRRD_LoadRank.class,
        FRRD_SortRanksAndLock.class
})
public class FetchReviewRanksDataProcessor extends AbstractAfentiProcessor<FetchReviewRanksContext> {
}
