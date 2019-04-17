package com.voxlearning.utopia.service.afenti.impl.service.processor.learningRank;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiRankType;
import com.voxlearning.utopia.service.afenti.api.context.LearningRankContext;
import com.voxlearning.utopia.service.afenti.impl.service.AsyncAfentiCacheServiceImpl;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author peng.zhang.a
 * @since 16-7-27
 */
@Named
public class ALRP_AssembleRankResult extends SpringContainerSupport implements IAfentiTask<LearningRankContext> {

    @Inject private AsyncAfentiCacheServiceImpl asyncAfentiCacheService;

    @Inject UserLoaderClient userLoaderClient;

    @Override
    public void execute(LearningRankContext context) {
        assembleRankMsg(context.getNationalList(), context.getNationalLikedSummary(), AfentiRankType.national, context.getUser(), context.getSubject());
        assembleRankMsg(context.getSchoolList(), context.getSchoolLikedSummary(), AfentiRankType.school, context.getUser(), context.getSubject());

    }

    private List<Map<String, Object>> assembleRankMsg(List<Map<String, Object>> rankList,
                                                      Map<Long, Integer> likedSummary,
                                                      AfentiRankType afentiRankType,
                                                      StudentDetail studentDetail,
                                                      Subject subject) {
        if (rankList == null) {
            return Collections.emptyList();
        }
        Set<Long> clickUserSet = asyncAfentiCacheService.AfentiClickLikedCacheManager_loadTodayClickLikedSet(studentDetail, subject, afentiRankType)
                .take();
        rankList.forEach(p -> {
            Long userId = (Long) p.getOrDefault("userId", 0);
            boolean isLiked = CollectionUtils.isNotEmpty(clickUserSet) && clickUserSet.contains(userId);
            Integer likedNum = MapUtils.isEmpty(likedSummary) ? 0 : likedSummary.getOrDefault(userId, 0);
            p.put("userId", String.valueOf(userId));
            p.put("isLiked", isLiked);
            p.put("likedNum", likedNum);
        });
        return rankList;
    }
}
