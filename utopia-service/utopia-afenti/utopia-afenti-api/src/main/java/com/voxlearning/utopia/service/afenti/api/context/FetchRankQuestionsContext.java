package com.voxlearning.utopia.service.afenti.api.context;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiLearningType;
import com.voxlearning.utopia.service.afenti.api.data.AfentiBook;
import com.voxlearning.utopia.service.afenti.api.data.SectionInfo;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLearningPlanPushExamHistory;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLearningPlanUnitRankManager;
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
public class FetchRankQuestionsContext extends AbstractAfentiContext<FetchRankQuestionsContext> {
    private static final long serialVersionUID = 8121076482052868045L;

    // in
    @NonNull private StudentDetail student;
    @NonNull private Subject subject;
    @NonNull private String unitId;
    @NonNull private Integer rank;
    @NonNull private AfentiLearningType learningType; // 学习类型

    // middle
    private AfentiBook book;
    private boolean paid;
    private Boolean isNewRankBook = false; // 是否是新生成关卡的教材
    private String newRankBookId;  // 新生成关卡的教材ID  带前缀
    private String sectionId;
    private List<AfentiLearningPlanPushExamHistory> histories = new ArrayList<>();
    private List<AfentiLearningPlanUnitRankManager> unitRanks = new ArrayList<>();  // 本单元的关卡


    // out
    private Set<String> knowledges = new LinkedHashSet<>();
    private List<Map<String, Object>> questions = new LinkedList<>();
    private SectionInfo sectionInfo = null;
}
