package com.voxlearning.utopia.service.afenti.impl.service.processor.review;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiLearningType;
import com.voxlearning.utopia.service.afenti.api.context.FetchReviewRanksContext;
import com.voxlearning.utopia.service.afenti.api.data.ReviewRank;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLearningPlanUnitRankManager;
import com.voxlearning.utopia.service.afenti.impl.service.AfentiLoaderImpl;
import com.voxlearning.utopia.service.afenti.impl.service.AsyncAfentiCacheServiceImpl;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author songtao
 * @since 2017/11/28
 */
@Named
public class FRRD_LoadRank extends SpringContainerSupport implements IAfentiTask<FetchReviewRanksContext> {
    @Inject
    private AfentiLoaderImpl afentiLoader;

    @Inject
    private StudentLoaderClient studentLoaderClient;

    @Inject
    private AsyncAfentiCacheServiceImpl asyncAfentiCacheServiceImpl;

    @Override
    public void execute(FetchReviewRanksContext context) {
        String bookId = context.getBook().book.getId();

        Map<String, Integer> rank_star_map = context.getRank_star_map();
        Set<String> pushed = context.getPushed();

        // 获取当前所有关卡
        List<AfentiLearningPlanUnitRankManager> ranks = afentiLoader
                .loadAfentiLearningPlanUnitRankManagerByNewBookId(bookId)
                .stream()
                .filter(r -> r.getType() == AfentiLearningType.review)
                .collect(Collectors.toList());
        Set<String> userUnits = asyncAfentiCacheServiceImpl.getAfentiReviewParrentRewardCacheManager().loadRecord(context.getStudent().getId());
        for (AfentiLearningPlanUnitRankManager rank : ranks) {
            ReviewRank unitRank = new ReviewRank();
            unitRank.unitId = rank.getNewUnitId();
            unitRank.rankUserNum = 0;
            unitRank.rank = rank.getUnitRank();

            //加载该关卡的同学
            Set<Long> users = asyncAfentiCacheServiceImpl.getAfentiReviewRankFootprintCacheManager().loadRecord(context.getStudent().getClazzId(), rank.getNewUnitId());
            if (CollectionUtils.isNotEmpty(users)) {
                unitRank.rankUserNum = users.size();
                Long userId = users.stream().filter(e -> !context.getStudent().getId().equals(e)).findFirst().orElse(null);
                if (userId != null) {
                    StudentDetail student = studentLoaderClient.loadStudentDetail(userId);
                    unitRank.rankUser = student.fetchRealname();
                }
            }

            //是否已经领取奖励
            unitRank.rewarded = CollectionUtils.isNotEmpty(userUnits) && userUnits.contains(rank.getNewUnitId());
            unitRank.rankType = rank.fetchRankType();
            unitRank.star = rank_star_map.containsKey(rank.getNewUnitId()) ? rank_star_map.get(rank.getNewUnitId()) : 0;
            unitRank.pushed = pushed.contains(rank.getNewUnitId());
            context.getRanks().add(unitRank);
        }
    }
}
