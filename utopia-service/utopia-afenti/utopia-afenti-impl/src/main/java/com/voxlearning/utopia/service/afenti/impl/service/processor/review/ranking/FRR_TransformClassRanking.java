package com.voxlearning.utopia.service.afenti.impl.service.processor.review.ranking;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.context.FetchReviewRankingContext;
import com.voxlearning.utopia.service.afenti.api.data.ReviewUserRanking;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author songtao
 * @since 2017/12/5
 */
@Named
public class FRR_TransformClassRanking extends SpringContainerSupport implements IAfentiTask<FetchReviewRankingContext> {
    @Inject
    private StudentLoaderClient studentLoaderClient;

    @Override
    public void execute(FetchReviewRankingContext context) {
        List<ReviewUserRanking> userRankingList = new ArrayList<>();
        if (context.getClassRankingMap() != null && !context.getClassRankingMap().isEmpty()) {
            Map<Long, StudentDetail>  map = studentLoaderClient.loadStudentDetails(context.getClassRankingMap().keySet());
            context.getClassRankingMap().entrySet().forEach(e -> {
                Long stuId = e.getKey();
                Integer num = e.getValue();
                StudentDetail student = map.get(stuId);
                if (student != null && student.getClazz() != null && student.getClazzId().equals(context.getStudent().getClazzId())) {
                    ReviewUserRanking userRanking = new ReviewUserRanking();
                    userRanking.userName = student.fetchRealname();
                    userRanking.className = student.getClazz().formalizeClazzName();
                    userRanking.totalNum = num;
                    userRankingList.add(userRanking);
                }
            });
        }

        Collections.sort(userRankingList, (o1, o2) -> Integer.compare(o2.totalNum, o1.totalNum));

        context.getRanking().put("classRanking", userRankingList);
    }
}
