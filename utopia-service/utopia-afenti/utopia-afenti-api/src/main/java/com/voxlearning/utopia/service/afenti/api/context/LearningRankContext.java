package com.voxlearning.utopia.service.afenti.api.context;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.service.afenti.api.constant.UseAppStatus;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import lombok.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author peng.zhang.a
 * @since 16-7-27
 */
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
public class LearningRankContext extends AbstractAfentiContext<LearningRankContext> {
    private static final long serialVersionUID = 4491982882902815440L;
    // in
    @NonNull private StudentDetail user;
    @NonNull private Subject subject;
    @NonNull private Date calculateDate;
    @NonNull private Date likedSummaryDate;

    //mid
    Map<Long, Integer> nationalLikedSummary;
    Map<Long, Integer> schoolLikedSummary;
    Map<Long, Integer> nationalLikeRankFlag;
    Map<Long, Integer> schoolLikeRankFlag;

    //out
    List<Map<String, Object>> nationalList;
    List<Map<String, Object>> schoolList;
    Integer selfSchoolRank;
    Integer selfNationalRank;
    UseAppStatus useAppStatus;
}





