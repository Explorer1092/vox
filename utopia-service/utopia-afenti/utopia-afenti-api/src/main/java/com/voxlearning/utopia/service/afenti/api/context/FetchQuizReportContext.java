package com.voxlearning.utopia.service.afenti.api.context;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiQuizType;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiQuizStat;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ruib
 * @since 2016/10/14
 */
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
public class FetchQuizReportContext extends AbstractAfentiContext<FetchQuizReportContext> {
    private static final long serialVersionUID = 1707132912144351784L;

    // in
    @NonNull private StudentDetail student;
    @NonNull private Subject subject;
    @NonNull private AfentiQuizType type;
    @NonNull private String contentId; // 单元id或者教材id

    // middle
    private boolean skipStat = false; // 是否跳过FQR_LoadQuizStat
    private String bookId;
    private String unitId;
    private AfentiQuizStat stat;

    // out
    private Map<String, Object> result = new HashMap<>();
}
