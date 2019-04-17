package com.voxlearning.utopia.service.afenti.api.context;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.service.afenti.api.data.AfentiBook;
import com.voxlearning.utopia.service.afenti.api.data.ReviewRank;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import lombok.*;

import java.util.*;

/**
 * @author songtao
 * @since 2017/11/28
 */
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
public class FetchReviewRanksContext extends AbstractAfentiContext<FetchReviewRanksContext> {
    private static final long serialVersionUID = 3040397001776289392L;

    // in
    @NonNull private StudentDetail student;
    @NonNull private Subject subject;

    // middle
    private AfentiBook book;
    private Map<String, Integer> rank_star_map = new HashMap<>();
    private Set<String> pushed = new HashSet<>();

    // out
    private List<ReviewRank> ranks = new ArrayList<>();
    private String bookName = "";
    private int homeJoined = 0;
    private long count = 0;
    private boolean paid = true;
}
