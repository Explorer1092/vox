package com.voxlearning.utopia.service.afenti.api.context;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.api.constant.AfentiState;
import com.voxlearning.utopia.service.afenti.api.AfentiWrongQuestionStateType;
import com.voxlearning.utopia.service.afenti.api.entity.WrongQuestionLibrary;
import lombok.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ruib
 * @since 2016/7/24
 */
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
public class FetchElfContext extends AbstractAfentiContext<FetchElfContext> {
    private static final long serialVersionUID = 8790420663580151209L;

    // in
    @NonNull private Long studentId;
    @NonNull private Subject subject;
    @NonNull private AfentiWrongQuestionStateType stateType;


    // middle
    //private Map<AfentiState, List<WrongQuestionLibrary>> questions = new HashMap<>();
    private List<WrongQuestionLibrary> questionList = new ArrayList<>();

    // out
    private Map<String, List<String>> incorrect = new HashMap<>(); // 错题
    private List<Map<String, String>> similar = new ArrayList<>(); // 类题
    private List<String> rescued = new ArrayList<>(); // 已经拯救的
}
