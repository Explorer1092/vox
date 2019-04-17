package com.voxlearning.utopia.service.newhomework.api.entity.basicreview;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedHashMap;

/**
 * @author guoqiang.li
 * @since 2017/11/14
 */
@Getter
@Setter
public class BasicReviewHomeworkReportDetail implements Serializable {

    private static final long serialVersionUID = -6990346744366854376L;

    // 同步习题类作业形式用到(期末基础复习只有口算)<questionId, BasicReviewHomeworkAnswer>
    private LinkedHashMap<String, BasicReviewHomeworkAnswer> answers;

    // 应用类作业形式用到(期末基础复习只有基础练习)<{categoryId-lessonId}, BasicReviewHomeworkAppAnswer>
    private LinkedHashMap<String, BasicReviewHomeworkAppAnswer> appAnswers;

}
