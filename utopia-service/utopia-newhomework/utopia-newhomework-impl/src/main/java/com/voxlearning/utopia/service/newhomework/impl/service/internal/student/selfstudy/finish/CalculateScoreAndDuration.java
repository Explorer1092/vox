package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.selfstudy.finish;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkProcessResultLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.student.calculator.CalculateResult;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author xuesong.zhang
 * @since 2017/2/6
 */
@Named
public class CalculateScoreAndDuration {

    @Inject private NewHomeworkProcessResultLoaderImpl newHomeworkProcessResultLoader;

    public CalculateResult calculate(Set<String> processIds) {
        if (CollectionUtils.isEmpty(processIds)) return null;

        Map<String, SubHomeworkProcessResult> results = newHomeworkProcessResultLoader.loadSubHomeworkProcessResults(processIds);
        if (MapUtils.isEmpty(results)) return null;

        // 计算总用时
        Long duration = results.values().stream().mapToLong(SubHomeworkProcessResult::getDuration).sum();

        // 计算总得分
        Double score = null;
        List<SubHomeworkProcessResult> nonNull = results.values().stream().filter(r -> r.getScore() != null).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(nonNull)) {
            score = nonNull.stream().mapToDouble(SubHomeworkProcessResult::getScore).average().orElse(0D);
        }

        CalculateResult result = new CalculateResult();
        result.setScore(score);
        result.setDuration(duration);
        return result;
    }
}
