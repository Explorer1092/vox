package com.voxlearning.utopia.service.afenti.impl.service.processor.review;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.context.FetchReviewRanksContext;
import com.voxlearning.utopia.service.afenti.api.data.ReviewRank;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Named;
import java.util.Collections;

/**
 * @author songtao
 * @since 2017/11/28
 */
@Named
public class FRRD_SortRanksAndLock extends SpringContainerSupport implements IAfentiTask<FetchReviewRanksContext> {

    @Override
    public void execute(FetchReviewRanksContext context) {
        // 关卡排序
        Collections.sort(context.getRanks(), (o1, o2) -> Integer.compare(o1.rank, o2.rank));

        Boolean locked = false;
        for (ReviewRank rank : context.getRanks()) {
            rank.locked = locked;
            if (rank.star == 0) locked = true;
        }
    }
}
