package com.voxlearning.utopia.service.afenti.api.context;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiLearningType;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ruib
 * @since 2016/7/13
 */
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
public class FetchGradeBookContext extends AbstractAfentiContext<FetchGradeBookContext> {
    private static final long serialVersionUID = -1916056315625167428L;

    // in
    @NonNull private ClazzLevel clazzLevel;
    @NonNull private Subject subject;
    @NonNull private AfentiLearningType afentiLearningType;

    // middle
    private List<String> candidates = new ArrayList<>(); // 已经生成阿分题关卡的教材id
    private List<String> preparationCandidates = new ArrayList<>(); // 已经生成阿分题关卡的教材id  -- 预习

    // out
    private List<NewBookProfile> books = new ArrayList<>();
}
