package com.voxlearning.utopia.service.afenti.api.context;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.service.afenti.api.data.AfentiBook;
import com.voxlearning.utopia.service.afenti.api.data.SectionInfo;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLearningPlanPushExamHistory;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import lombok.*;

import java.util.*;

/**
 * @author songtao
 * @since 2017/11/29
 */
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
public class FetchReviewQuestionsContext extends AbstractAfentiContext<FetchReviewQuestionsContext> {
    private static final long serialVersionUID = 8121076482052868045L;

    // in
    @NonNull private StudentDetail student;
    @NonNull private Subject subject;
    @NonNull private String unitId;

    // middle
    private AfentiBook book;
    private List<AfentiLearningPlanPushExamHistory> histories = new ArrayList<>();

    // out
    private Set<String> knowledges = new LinkedHashSet<>();
    private List<Map<String, Object>> questions = new LinkedList<>();
    private SectionInfo sectionInfo = null;
}
