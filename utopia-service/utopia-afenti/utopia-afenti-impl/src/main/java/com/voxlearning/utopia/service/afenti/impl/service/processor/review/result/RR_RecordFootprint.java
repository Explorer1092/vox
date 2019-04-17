package com.voxlearning.utopia.service.afenti.impl.service.processor.review.result;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.context.ReviewResultContext;
import com.voxlearning.utopia.service.afenti.impl.service.AsyncAfentiCacheServiceImpl;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author songtao
 * @since 2017/11/29
 */
@Named
public class RR_RecordFootprint extends SpringContainerSupport implements IAfentiTask<ReviewResultContext> {

    @Inject
    private AsyncAfentiCacheServiceImpl asyncAfentiCacheServiceImpl;

    @Override
    public void execute(ReviewResultContext context) {
        try {
            asyncAfentiCacheServiceImpl.getAfentiReviewRankFootprintCacheManager().addRecord(context.getStudent(), context.getUnitId());
        } catch (Exception e) {
            logger.error("RR_RecordFootprint error. userId:{}, unitId:{}", context.getStudent().getId(), context.getUnitId());
        }
    }
}
