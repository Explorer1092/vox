package com.voxlearning.utopia.service.afenti.impl.service.processor.learningRank;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.context.LearningRankContext;
import com.voxlearning.utopia.service.afenti.impl.service.AsyncAfentiCacheServiceImpl;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.Map;

/**
 * @author peng.zhang.a
 * @since 16-7-27
 */
@Named
public class ALRP_LoadLikedList extends SpringContainerSupport implements IAfentiTask<LearningRankContext> {

    @Inject private AsyncAfentiCacheServiceImpl asyncAfentiCacheService;

    @Override
    public void execute(LearningRankContext context) {
        Map<Long, Integer> nationalMap = asyncAfentiCacheService.AfentiRankLikedSummaryCacheManager_loadNationRank(context.getLikedSummaryDate())
                .take();
        Map<Long, Integer> schoolMap = asyncAfentiCacheService.AfentiRankLikedSummaryCacheManager_loadSchoolRank(context.getUser(), context.getLikedSummaryDate())
                .take();
        context.setNationalLikedSummary(nationalMap != null ? nationalMap : Collections.emptyMap());
        context.setSchoolLikedSummary(schoolMap != null ? schoolMap : Collections.emptyMap());
    }
}
