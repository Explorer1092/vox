package com.voxlearning.utopia.service.afenti.api.context;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.service.afenti.api.data.ReviewUserRanking;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import lombok.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author songtao
 * @since 2017/11/28
 */
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
public class FetchReviewRankingContext extends AbstractAfentiContext<FetchReviewRankingContext> {
    private static final long serialVersionUID = 3040397001776289392L;

    // in
    @NonNull private StudentDetail student;
    @NonNull private Subject subject;

    // middle
    private Map<Long, Integer> classRankingMap = new HashMap<>();   //班级排名信息
    private Map<Long, Integer> schoolRankingMap = new HashMap<>();  //学校排名信息

    // out
    private Map<String, List<ReviewUserRanking>> ranking = new HashMap<>();
    private int totalRanks = 0;
    private int totalQuestions = 0;
    private String rightRate = "0";

}
