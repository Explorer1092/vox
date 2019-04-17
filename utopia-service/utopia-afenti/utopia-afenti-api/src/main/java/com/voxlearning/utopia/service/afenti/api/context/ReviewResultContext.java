package com.voxlearning.utopia.service.afenti.api.context;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLearningPlanPushExamHistory;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLearningPlanUserRankStat;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author songtao
 * @since 2017/11/29
 */
@Getter
@Setter
@NoArgsConstructor
public class ReviewResultContext extends AbstractAfentiContext<ReviewResultContext> {
    private static final long serialVersionUID = 3073723083430028370L;

    // in
    private StudentDetail student; // 学生
    private Subject subject; // 学科
    private String bookId; // 教材id
    private String unitId; // 单元id
    private String questionId; // 试题id
    private Boolean master; // 是否做对了
    private Boolean finished; // 是否是最后一题
    private Integer wkpc; //  错误知识点数

    // middle
    private List<AfentiLearningPlanPushExamHistory> histories = new ArrayList<>();
    private AfentiLearningPlanUserRankStat stat;
    private List<Integer> correspondence = new ArrayList<>();
    private Integer star = 0;
    private Integer silver = 0;
    private Integer bonus = 0;

    // out
    private Map<String, Object> result = new HashMap<>();
}
