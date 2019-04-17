package com.voxlearning.utopia.service.afenti.api.context;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.service.afenti.api.data.AfentiBook;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiQuizResult;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import lombok.*;

import java.util.*;

/**
 * @author Ruib
 * @since 2016/10/14
 */
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
public class FetchQuizQuestionContext extends AbstractAfentiContext<FetchQuizQuestionContext> {
    private static final long serialVersionUID = 7960374340367139473L;

    // in
    @NonNull private StudentDetail student;
    @NonNull private Subject subject;
    @NonNull private String unitId;

    // middle
    private AfentiBook book;
    private Map<String, String> qid_kpid_map = new HashMap<>(); // 题目id和知识点id对应关系
    private List<AfentiQuizResult> results = new ArrayList<>();

    // out
    private int kpc = 0; // 知识点个数
    private int qc = 0; // 题数
    private int ic = 20; // 奖励
    private List<Map<String, Object>> questions = new LinkedList<>(); // 题目列表
}
