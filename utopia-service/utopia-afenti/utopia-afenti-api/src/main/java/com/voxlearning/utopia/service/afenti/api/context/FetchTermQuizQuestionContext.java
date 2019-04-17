package com.voxlearning.utopia.service.afenti.api.context;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiQuizResult;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import lombok.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Ruib
 * @since 2016/10/19
 */
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
public class FetchTermQuizQuestionContext extends AbstractAfentiContext<FetchTermQuizQuestionContext> {
    private static final long serialVersionUID = -1287720559276792469L;

    // in
    @NonNull private StudentDetail student;
    @NonNull private Subject subject;
    @NonNull private String bookId;

    // middle
    private List<AfentiQuizResult> qrs = new ArrayList<>();

    // out
    private List<Map<String, Object>> questions = new LinkedList<>(); // 题目列表
}
