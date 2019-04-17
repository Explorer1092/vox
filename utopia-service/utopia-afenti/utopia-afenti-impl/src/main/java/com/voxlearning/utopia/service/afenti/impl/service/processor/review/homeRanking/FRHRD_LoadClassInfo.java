package com.voxlearning.utopia.service.afenti.impl.service.processor.review.homeRanking;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.context.FetchReviewHomeRankingContext;
import com.voxlearning.utopia.service.afenti.api.data.ReviewUserRanking;
import com.voxlearning.utopia.service.afenti.base.cache.managers.AfentiReviewFamilyClassRankingCacheManager;
import com.voxlearning.utopia.service.afenti.impl.service.AsyncAfentiCacheServiceImpl;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * @author songtao
 * @since 2017/12/1
 */
@Named
public class FRHRD_LoadClassInfo extends SpringContainerSupport implements IAfentiTask<FetchReviewHomeRankingContext> {
    @Inject
    private AsyncAfentiCacheServiceImpl asyncAfentiCacheService;
    @Inject
    private StudentLoaderClient studentLoaderClient;
    @Inject
    private AfentiReviewFamilyClassRankingCacheManager afentiReviewFamilyClassRankingCacheManager;

    @Override
    public void execute(FetchReviewHomeRankingContext context) {
        List<ReviewUserRanking> classRankingList = new ArrayList<>();
        Set<Long> studentSet = afentiReviewFamilyClassRankingCacheManager.loadRecord(context.getStudent().getClazzId());
        if (CollectionUtils.isEmpty(studentSet)) {
            context.getRanking().put("classRanking", classRankingList);
            return;
        }

        Map<Long, StudentDetail> students = studentLoaderClient.loadStudentDetails(studentSet);
        studentSet.forEach(e -> {
            Integer num = asyncAfentiCacheService.getAfentiReviewFamilyJoinCacheManager().loadRecord(e);
            StudentDetail student = students.get(e);
            if (student != null && student.getClazz() != null && student.getClazzId().equals(context.getStudent().getClazzId())) {
                ReviewUserRanking ranking = new ReviewUserRanking();
                ranking.totalNum = num == null ? 0 : num;
                ranking.className = student.getClazz().formalizeClazzName();
                ranking.userName = student.fetchRealname();
                classRankingList.add(ranking);
            }
        });

        Collections.sort(classRankingList, (o1, o2) -> Integer.compare(o2.totalNum, o1.totalNum));
        context.getRanking().put("classRanking", classRankingList);
    }
}
