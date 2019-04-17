package com.voxlearning.utopia.service.afenti.impl.service.processor.review.homeRanking;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.context.FetchReviewHomeRankingContext;
import com.voxlearning.utopia.service.afenti.impl.service.AsyncAfentiCacheServiceImpl;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author songtao
 * @since 2017/12/1
 */
@Named
public class FRHRD_LoadUserInfo extends SpringContainerSupport implements IAfentiTask<FetchReviewHomeRankingContext> {
    @Inject
    private AsyncAfentiCacheServiceImpl asyncAfentiCacheService;

    @Override
    public void execute(FetchReviewHomeRankingContext context) {

        Integer value = asyncAfentiCacheService.getAfentiReviewFamilyJoinCacheManager().loadRecord(context.getStudent().getId());

        context.setHomeJoinNum(value == null ? 0 : value);
        context.setUserName(context.getStudent().fetchRealname());
    }
}
