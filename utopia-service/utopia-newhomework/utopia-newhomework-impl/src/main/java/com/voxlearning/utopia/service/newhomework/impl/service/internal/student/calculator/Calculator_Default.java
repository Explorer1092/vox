package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.calculator;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkProcessResultLoaderImpl;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Ruib
 * @version 0.1
 * @since 2016/1/28
 */
@Named
@RequireObjectiveConfigTypes({})
public class Calculator_Default extends SpringContainerSupport implements Calculator {
    @Inject private NewHomeworkProcessResultLoaderImpl newHomeworkProcessResultLoader;

    @Override
    public CalculateResult calculate(String homeworkId, Set<String> processIds) {
        if (CollectionUtils.isEmpty(processIds)) return null;

        Map<String, NewHomeworkProcessResult> results = newHomeworkProcessResultLoader.loads(homeworkId, processIds);
        if (MapUtils.isEmpty(results)) return null;

        Long duration = 0L; // 计算总用时
        Double score = 0D;  // 计算总得分
        boolean allQuestionsRight = true;   //所有的题目都答对了
        for (NewHomeworkProcessResult newHomeworkProcessResult : results.values()) {
            if (newHomeworkProcessResult.getDuration() != null) {
                duration += newHomeworkProcessResult.getDuration();
            }
            if (newHomeworkProcessResult.getScore() != null) {
                score += newHomeworkProcessResult.getScore();
            }
            if (!SafeConverter.toBoolean(newHomeworkProcessResult.getGrasp())) {
                allQuestionsRight = false;
            }
        }
        //当题目全部正确时，但是总分计算结果却不是100分时就把总分设置为100分
        if (allQuestionsRight && score != null && score < 100D) {
            score = 100D;
        }

        CalculateResult result = new CalculateResult();
        result.setScore(score);
        result.setDuration(duration);
        return result;
    }
}
