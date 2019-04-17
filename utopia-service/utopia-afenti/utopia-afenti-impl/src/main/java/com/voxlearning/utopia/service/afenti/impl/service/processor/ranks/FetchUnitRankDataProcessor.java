package com.voxlearning.utopia.service.afenti.impl.service.processor.ranks;

import com.voxlearning.utopia.service.afenti.api.context.FetchUnitRanksContext;
import com.voxlearning.utopia.service.afenti.impl.service.processor.AbstractAfentiProcessor;
import com.voxlearning.utopia.service.afenti.impl.service.processor.AfentiTasks;

import javax.inject.Named;

/**
 * @author Ruib
 * @since 2016/7/15
 */
@Named
@AfentiTasks({
        FURD_LoadAfentiBook.class,
        FURD_LoadUnit.class,
        FURD_LoadCurrentDayPushCount.class,
        FURD_LoadNewRank.class,
        FURD_LoadRankStar.class,
//        FURD_LoadLatestFootprint.class,
        FURD_LoadPushExamHistory.class,
        FURD_LoadCommonUnitRanks.class,
        FURD_LoadSpecificUnitRanks.class,
        FURD_SortRanksAndLock.class
})
public class FetchUnitRankDataProcessor extends AbstractAfentiProcessor<FetchUnitRanksContext> {
}
