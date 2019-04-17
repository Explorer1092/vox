package com.voxlearning.utopia.service.afenti.api.context;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiQuizStat;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ruib
 * @since 2016/12/20
 */
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
public class FetchTermQuizReportContext extends AbstractAfentiContext<FetchTermQuizReportContext> {
    private static final long serialVersionUID = -1635734150928907127L;

    // in
    @NonNull private StudentDetail student;
    @NonNull private Subject subject;

    // middle
    private String bookId;
    private String unitId;
    private AfentiQuizStat stat;

    // out
    private Map<String, Object> result = new HashMap<>();
}
