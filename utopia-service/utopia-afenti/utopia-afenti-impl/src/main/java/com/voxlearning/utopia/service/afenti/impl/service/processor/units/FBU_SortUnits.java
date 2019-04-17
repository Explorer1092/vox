package com.voxlearning.utopia.service.afenti.impl.service.processor.units;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.context.FetchBookUnitsContext;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Named;
import java.util.Collections;

/**
 * @author Ruib
 * @since 2016/7/14
 */
@Named
public class FBU_SortUnits extends SpringContainerSupport implements IAfentiTask<FetchBookUnitsContext> {

    @Override
    public void execute(FetchBookUnitsContext context) {
        // 按单元顺序排序
        Collections.sort(context.getUnits(), (o1, o2) -> Integer.compare(o1.unitRank, o2.unitRank));
    }
}
