package com.voxlearning.utopia.service.afenti.impl.service.processor.units;

import com.voxlearning.utopia.service.afenti.api.context.FetchBookUnitsContext;
import com.voxlearning.utopia.service.afenti.impl.service.processor.AbstractAfentiProcessor;
import com.voxlearning.utopia.service.afenti.impl.service.processor.AfentiTasks;

import javax.inject.Named;

/**
 * @author ruib
 * @since 16/7/13
 */

@Named
@AfentiTasks({
        FBU_LoadAfentiBook.class,
        FBU_LoadNewRank.class,
        FBU_LoadUserRankStat.class,
        FBU_LoadCommonUnits.class,
        FBU_LoadUltimateUnit.class,
        FBU_SortUnits.class
})
public class FetchBookUnitsDataProcessor extends AbstractAfentiProcessor<FetchBookUnitsContext> {
}
