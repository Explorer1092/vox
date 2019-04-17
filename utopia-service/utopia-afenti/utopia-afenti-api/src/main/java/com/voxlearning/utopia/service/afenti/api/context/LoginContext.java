package com.voxlearning.utopia.service.afenti.api.context;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.service.afenti.api.data.AfentiBook;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import lombok.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Ruib
 * @since 2016/7/11
 */
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
public class LoginContext extends AbstractAfentiContext<LoginContext> {
    private static final long serialVersionUID = 3056126170214993478L;

    // in
    @NonNull private StudentDetail student;
    @NonNull private Subject subject;

    // middle
    private AfentiBook book;

    // out
    Map<String, Object> result = new LinkedHashMap<>();
}
