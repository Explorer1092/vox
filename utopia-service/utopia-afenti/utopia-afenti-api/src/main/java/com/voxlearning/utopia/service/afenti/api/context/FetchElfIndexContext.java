package com.voxlearning.utopia.service.afenti.api.context;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.api.constant.AfentiState;
import com.voxlearning.utopia.service.afenti.api.entity.WrongQuestionLibrary;
import lombok.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author songtao
 * @since 2017/9/20
 */
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
public class FetchElfIndexContext extends AbstractAfentiContext<FetchElfIndexContext> {
    private static final long serialVersionUID = 8790420663580151209L;

    // in
    @NonNull private Long studentId;
    @NonNull private Subject subject;
    @NonNull private Boolean limited;

    // middle
    private Map<AfentiState, List<WrongQuestionLibrary>> questions = new HashMap<>();

    // out
    private Integer incorrect = 0; // 错题
    private Integer similar = 0; // 类题
    private Integer rescued = 0; // 已经拯救的
}
