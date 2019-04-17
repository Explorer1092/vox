package com.voxlearning.utopia.service.afenti.impl.service.processor.review.ranking;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.context.FetchReviewRankingContext;
import com.voxlearning.utopia.service.afenti.api.data.ReviewUserRanking;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author songtao
 * @since 2017/12/5
 */
@Named
public class FRR_TransformSchoolRanking extends SpringContainerSupport implements IAfentiTask<FetchReviewRankingContext> {
    @Inject
    private StudentLoaderClient studentLoaderClient;

    @Override
    public void execute(FetchReviewRankingContext context) {
        List<ReviewUserRanking> userRankingList = new ArrayList<>();
        if (context.getSchoolRankingMap() != null && !context.getSchoolRankingMap().isEmpty()) {
            List<Map.Entry<Long, Integer>> entryList = new ArrayList<>(context.getSchoolRankingMap().entrySet());
            entryList.sort((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()));

            Set<Long> studentIds = entryList.stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e)).keySet();

            Map<Long, StudentDetail> studentDetailMap = studentLoaderClient.loadStudentDetails(studentIds);

            entryList.forEach(e -> {
                Long stuId = e.getKey();
                Integer num = e.getValue();
                StudentDetail student = studentDetailMap.get(stuId);
                if (student != null && student.getClazz() != null &&
                        student.getClazz().getSchoolId().equals(context.getStudent().getClazz().getSchoolId())) {
                    ReviewUserRanking userRank = new ReviewUserRanking();
                    userRank.userName = student.fetchRealname();
                    userRank.className = student.getClazz().formalizeClazzName();
                    userRank.totalNum = num;
                    userRankingList.add(userRank);
                }
            });
        }

        context.getRanking().put("schoolRanking", userRankingList.subList(0, Math.min(userRankingList.size(), 100)));
    }
}
