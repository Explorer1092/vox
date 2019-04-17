package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.calculator.Vacation;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.impl.dao.vacation.VacationHomeworkProcessResultDao;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.student.calculator.CalculateResult;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.student.calculator.RequireObjectiveConfigTypes;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author guoqiang.li
 * @since 2016/12/20
 */
@Named
@RequireObjectiveConfigTypes({})
public class VacationCalculator_Default extends SpringContainerSupport implements VacationCalculator {
    @Inject
    private VacationHomeworkProcessResultDao vacationHomeworkProcessResultDao;

    @Override
    public CalculateResult calculate(Set<String> processIds) {
        if (CollectionUtils.isEmpty(processIds)) return null;

        Map<String, VacationHomeworkProcessResult> results = vacationHomeworkProcessResultDao.loads(processIds);
        if (MapUtils.isEmpty(results)) return null;

        // 计算总用时
        Long duration = results.values().stream().mapToLong(VacationHomeworkProcessResult::getDuration).sum();

        // 计算总得分
        Double score = null;
        List<VacationHomeworkProcessResult> nonNull = results.values().stream().filter(r -> r.getScore() != null).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(nonNull)) {
            score = nonNull.stream().mapToDouble(VacationHomeworkProcessResult::getScore).sum();
            boolean allRight = nonNull.stream().allMatch(p -> SafeConverter.toBoolean(p.getGrasp()));
            // 针对口算训练
            boolean allFinished = nonNull.stream().noneMatch(p -> p .getDuration() != null && p.getDuration() == 0);
            // 如果全作答并且全对，但是总分不是100时把总分设置为100分
            if (allFinished && allRight && score < 100D) {
                score = 100D;
            }
        }

        CalculateResult result = new CalculateResult();
        result.setScore(score);
        result.setDuration(duration);
        return result;
    }
}
