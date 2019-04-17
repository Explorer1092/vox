package com.voxlearning.utopia.service.afenti.api.context;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.service.afenti.api.AfentiWrongQuestionSource;
import com.voxlearning.utopia.service.afenti.api.AfentiWrongQuestionStateType;
import com.voxlearning.utopia.service.afenti.api.entity.WrongQuestionLibrary;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author songtao
 * @since 2017/10/31
 */
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
public class FetchElfPageContext extends AbstractAfentiContext<FetchElfPageContext> {
    private static final long serialVersionUID = 8790420663580151209L;

    // in
    @NonNull private Long studentId;
    @NonNull private Subject subject;
    @NonNull private AfentiWrongQuestionStateType stateType;
    @NonNull private AfentiWrongQuestionSource source;
    @NonNull private Integer page;
    @NonNull private Integer pageSize;

    // middle
    private List<WrongQuestionLibrary> pageContent = new ArrayList<>();
    private List<WrongQuestionLibrary> libraryList = new ArrayList<>();
    private List<String> disableIds = new ArrayList<>();

    // out
    private List<Map<String, String>> result = new ArrayList<>(); //  错题
    private Integer pageNum = 1;
    private Integer totalNum = 1;
    private Integer afentiNum = 0;
    private Integer homeworkNum = 0;
}
