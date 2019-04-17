package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.calculator;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkProcessResultLoaderImpl;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author tanguohong
 * @since 2016/10/27
 */
@Named
@RequireObjectiveConfigTypes({ObjectiveConfigType.ORAL_PRACTICE, ObjectiveConfigType.ORAL_INTELLIGENT_TEACHING})
public class Calculator_OralPractice extends SpringContainerSupport implements Calculator {

    @Inject private NewHomeworkProcessResultLoaderImpl newHomeworkProcessResultLoader;

    @Override
    public CalculateResult calculate(String homeworkId, Set<String> processIds) {
        if (CollectionUtils.isEmpty(processIds)) return null;

        Map<String, NewHomeworkProcessResult> results = newHomeworkProcessResultLoader.loads(homeworkId, processIds);
        if (MapUtils.isEmpty(results)) return null;

        // 计算总用时
        Long duration = results.values().stream().mapToLong(NewHomeworkProcessResult::getDuration).sum();

        // 计算总得分
        Double score = null;
        List<NewHomeworkProcessResult> nonNull = results.values().stream().filter(r -> r.getScore() != null).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(nonNull)) {
            score = nonNull.stream().mapToDouble(NewHomeworkProcessResult::getScore).sum();
            score = new BigDecimal(score).divide(new BigDecimal(nonNull.size()), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
        }

        CalculateResult result = new CalculateResult();
        result.setScore(score);
        result.setDuration(duration);
        return result;
    }
}
