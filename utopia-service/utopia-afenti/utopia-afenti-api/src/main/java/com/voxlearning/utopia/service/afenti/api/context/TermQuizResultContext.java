package com.voxlearning.utopia.service.afenti.api.context;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiQuizResult;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiQuizStat;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

/**
 * @author Ruib
 * @since 2016/12/20
 */
@Getter
@Setter
@NoArgsConstructor
public class TermQuizResultContext extends AbstractAfentiContext<TermQuizResultContext> {
    private static final long serialVersionUID = -7432566827536477750L;

    // in
    private StudentDetail student; // 学生
    private Subject subject; // 学科
    private String bookId; // 教材id
    private String unitId; // 单元id
    private String questionId; // 试题id
    private Boolean master; // 是否做对了
    private Boolean finished; // 是否是最后一题

    // middle
    private Map<Long, AfentiQuizResult> qrm = new LinkedHashMap<>();
    private AfentiQuizStat stat;
    private int score = 0;
    private int integral = 0;

    // out
    private Map<String, Object> result = new HashMap<>();
}
