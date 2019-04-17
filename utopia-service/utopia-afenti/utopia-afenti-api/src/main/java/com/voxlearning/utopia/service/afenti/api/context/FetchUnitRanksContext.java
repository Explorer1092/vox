package com.voxlearning.utopia.service.afenti.api.context;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiLearningType;
import com.voxlearning.utopia.service.afenti.api.constant.UnitRankType;
import com.voxlearning.utopia.service.afenti.api.data.AfentiBook;
import com.voxlearning.utopia.service.afenti.api.data.UnitRank;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import lombok.*;

import java.util.*;

/**
 * @author Ruib
 * @since 2016/7/15
 */
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
public class FetchUnitRanksContext extends AbstractAfentiContext<FetchUnitRanksContext> {
    private static final long serialVersionUID = 3040397001776289392L;

    // in
    @NonNull private StudentDetail student;
    @NonNull private Subject subject;
    @NonNull private String unitId;
    @NonNull private AfentiLearningType learningType;
    @NonNull private UnitRankType unitRankType;

    // middle
    private AfentiBook book;
    private Boolean isNewRankBook = false; // 是否是新生成关卡的教材
    private String newRankBookId;  // 新生成关卡的教材ID  带前缀
    private Map<Integer, Integer> rank_star_map = new HashMap<>();
    private Map<Integer, List<Map<String, Object>>> rank_footprint_map = new HashMap<>();
    private Set<Integer> pushed = new HashSet<>();

    // out
    private List<UnitRank> ranks = new ArrayList<>();
    private long count = 0;
    private String unitName;
}
