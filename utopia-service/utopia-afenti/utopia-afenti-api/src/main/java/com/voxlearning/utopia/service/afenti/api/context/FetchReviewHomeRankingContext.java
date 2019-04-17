package com.voxlearning.utopia.service.afenti.api.context;

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
public class FetchReviewHomeRankingContext extends AbstractAfentiContext<FetchReviewHomeRankingContext> {
    private static final long serialVersionUID = 3040397001776289392L;

    // in
    @NonNull private StudentDetail student;

    // out
    private Map<String, List<ReviewUserRanking>> ranking = new HashMap<>();
    private int homeJoinNum = 0;
    private String userName;
}
