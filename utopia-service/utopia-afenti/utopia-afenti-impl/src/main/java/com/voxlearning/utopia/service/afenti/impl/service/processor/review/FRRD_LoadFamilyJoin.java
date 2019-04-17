package com.voxlearning.utopia.service.afenti.impl.service.processor.review;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.context.FetchReviewRanksContext;
import com.voxlearning.utopia.service.afenti.impl.service.AsyncAfentiCacheServiceImpl;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author songtao
 * @since 2017/11/28
 */
@Named
public class FRRD_LoadFamilyJoin extends SpringContainerSupport implements IAfentiTask<FetchReviewRanksContext> {

    @Inject
    private AsyncAfentiCacheServiceImpl asyncAfentiCacheServiceImpl;

    @Override
    public void execute(FetchReviewRanksContext context) {
        Integer num = asyncAfentiCacheServiceImpl.getAfentiReviewFamilyJoinCacheManager().loadRecord(context.getStudent().getId());
        context.setHomeJoined(num != null ? num : 0);
    }
}
