package com.voxlearning.utopia.service.afenti.api.context;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.service.afenti.api.data.AfentiBook;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import lombok.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Ruib
 * @since 2016/10/13
 */
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
public class FetchQuizInfoContext extends AbstractAfentiContext<FetchQuizInfoContext> {
    private static final long serialVersionUID = 2755210234439833090L;

    // in
    @NonNull private StudentDetail student;
    @NonNull private Subject subject;
    @NonNull private String unitId;

    // middle
    private AfentiBook book;

    // out
    Map<String, Object> result = new LinkedHashMap<>();
}
