package com.voxlearning.utopia.service.afenti.api.context;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiQuizResult;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiQuizStat;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ruib
 * @since 2016/10/14
 */
@Getter
@Setter
@NoArgsConstructor
public class QuizResultContext extends AbstractAfentiContext<QuizResultContext> {
    private static final long serialVersionUID = -3487956405429242157L;

    // in
    private StudentDetail student; // 学生
    private Subject subject; // 学科
    private String bookId; // 教材id
    private String unitId; // 单元id
    private String questionId; // 试题id
    private Boolean master; // 是否做对了
    private Boolean finished; // 是否是最后一题

    // middle
    private List<AfentiQuizResult> qrs = new ArrayList<>();
    private AfentiQuizStat stat;
    private int score = 0;
    private int integral = 0;

    // out
    private Map<String, Object> result = new HashMap<>();
}
