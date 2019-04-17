package com.voxlearning.utopia.service.afenti.impl.service.processor.ranks;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.context.FetchUnitRanksContext;
import com.voxlearning.utopia.service.afenti.api.data.UnitRank;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Named;
import java.util.Collections;

/**
 * @author Ruib
 * @since 2016/7/15
 */
@Named
public class FURD_SortRanksAndLock extends SpringContainerSupport implements IAfentiTask<FetchUnitRanksContext> {

    @Override
    public void execute(FetchUnitRanksContext context) {
        // 关卡排序
        Collections.sort(context.getRanks(), (o1, o2) -> Integer.compare(o1.rank, o2.rank));

        Boolean locked = false;
        for (UnitRank unitRank : context.getRanks()) {
            unitRank.isLocked = locked;
            if (unitRank.star == 0) locked = true;
        }
    }
}
