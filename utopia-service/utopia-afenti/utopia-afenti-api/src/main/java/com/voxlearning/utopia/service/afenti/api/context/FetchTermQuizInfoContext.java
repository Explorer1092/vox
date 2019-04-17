package com.voxlearning.utopia.service.afenti.api.context;

import com.voxlearning.utopia.service.afenti.api.entity.AfentiQuizStat;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import lombok.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ruib
 * @since 2016/10/18
 */
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
public class FetchTermQuizInfoContext extends AbstractAfentiContext<FetchTermQuizInfoContext> {
    private static final long serialVersionUID = 1307762887886373400L;

    // in
    @NonNull private StudentDetail student;

    // middle
    private List<AfentiQuizStat> stats = new ArrayList<>();

    // out
    Map<String, Object> result = new HashMap<>();
}
