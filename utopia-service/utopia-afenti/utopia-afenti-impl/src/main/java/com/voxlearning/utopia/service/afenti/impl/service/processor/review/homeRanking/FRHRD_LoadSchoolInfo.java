package com.voxlearning.utopia.service.afenti.impl.service.processor.review.homeRanking;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.context.FetchReviewHomeRankingContext;
import com.voxlearning.utopia.service.afenti.api.data.ReviewUserRanking;
import com.voxlearning.utopia.service.afenti.base.cache.managers.AfentiReviewFamilySchoolRankingCacheManager;
import com.voxlearning.utopia.service.afenti.impl.service.AsyncAfentiCacheServiceImpl;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author songtao
 * @since 2017/12/1
 */
@Named
public class FRHRD_LoadSchoolInfo extends SpringContainerSupport implements IAfentiTask<FetchReviewHomeRankingContext> {
    @Inject
    private AsyncAfentiCacheServiceImpl asyncAfentiCacheService;
    @Inject
    private StudentLoaderClient studentLoaderClient;
    @Inject
    private AfentiReviewFamilySchoolRankingCacheManager afentiReviewFamilySchoolRankingCacheManager;

    @Override
    public void execute(FetchReviewHomeRankingContext context) {
        List<ReviewUserRanking> schoolRankingList = new ArrayList<>();
        Set<Long> schoolSet = afentiReviewFamilySchoolRankingCacheManager.loadRecord(context.getStudent().getClazz().getSchoolId());
        if (CollectionUtils.isEmpty(schoolSet)) {
            context.getRanking().put("schoolRanking", schoolRankingList);
            return;
        }

        Map<Long, Integer> schoolMap = new HashMap<>();
        schoolSet.forEach(e -> {
            Integer num = asyncAfentiCacheService.getAfentiReviewFamilyJoinCacheManager().loadRecord(e);
            schoolMap.put(e, num);
        });

        List<Map.Entry<Long, Integer>> entryList = new ArrayList<>(schoolMap.entrySet());
        entryList.sort((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()));

        Set<Long> studentIds = entryList.stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e)).keySet();

        Map<Long, StudentDetail> students = studentLoaderClient.loadStudentDetails(studentIds);

        entryList.forEach(e -> {
            Long stuId = e.getKey();
            Integer totalNum = e.getValue();
            StudentDetail student = students.get(stuId);
            if (student != null && student.getClazz() != null && student.getClazz().getSchoolId().equals(context.getStudent().getClazz().getSchoolId())) {
                ReviewUserRanking ranking = new ReviewUserRanking();
                ranking.totalNum = totalNum == null ? 0 : totalNum;
                ranking.className = student.getClazz().formalizeClazzName();
                ranking.userName = student.fetchRealname();
                schoolRankingList.add(ranking);
            }
        });

        //取学校前100名
        context.getRanking().put("schoolRanking", schoolRankingList.subList(0, Math.min(schoolRankingList.size(), 100)));
    }
}
