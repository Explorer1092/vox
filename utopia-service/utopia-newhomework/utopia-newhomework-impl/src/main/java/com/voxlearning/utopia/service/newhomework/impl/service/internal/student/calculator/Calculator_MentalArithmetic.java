package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.calculator;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.constant.MentalArithmeticTimeLimit;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkProcessResultLoaderImpl;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author zhangbin
 * @since 2018/1/15
 */

@Named
@RequireObjectiveConfigTypes({ObjectiveConfigType.MENTAL_ARITHMETIC})
public class Calculator_MentalArithmetic extends SpringContainerSupport implements Calculator {
    @Inject
    private NewHomeworkProcessResultLoaderImpl newHomeworkProcessResultLoader;
    @Inject
    private NewHomeworkLoaderImpl newHomeworkLoader;

    @Override
    public CalculateResult calculate(String homeworkId, Set<String> processIds) {

        if (CollectionUtils.isEmpty(processIds)) {
            return null;
        }

        Map<String, NewHomeworkProcessResult> results = newHomeworkProcessResultLoader.loads(homeworkId, processIds);
        if (MapUtils.isEmpty(results)) {
            return null;
        }

        Long duration = 0L; // 计算总用时
        Double score = 0D;  // 计算总得分
        boolean finishedAll = true; // 作答了所有题目
        boolean allRight = true; // 是否全部正确
        for (NewHomeworkProcessResult newHomeworkProcessResult : results.values()) {
            if (newHomeworkProcessResult.getDuration() != null) {
                if (newHomeworkProcessResult.getDuration() == 0) {
                    finishedAll = false;
                }
                duration += newHomeworkProcessResult.getDuration();
            }
            if (newHomeworkProcessResult.getScore() != null) {
                score += newHomeworkProcessResult.getScore();
            }
            if (!SafeConverter.toBoolean(newHomeworkProcessResult.getGrasp())) {
                allRight = false;
            }
        }

        // 一道题都未作答或者作答一部分，倒计时结束后时间为作业限时时长；
        if (duration == 0L || !finishedAll) {
            NewHomework newHomework = newHomeworkLoader.load(homeworkId);
            if (newHomework != null) {
                List<NewHomeworkPracticeContent> practices = newHomework.getPractices();
                if (CollectionUtils.isNotEmpty(practices)) {
                    for (NewHomeworkPracticeContent practiceContent : practices) {
                        if (practiceContent.getType() != null && ObjectiveConfigType.MENTAL_ARITHMETIC.equals(practiceContent.getType())) {
                            MentalArithmeticTimeLimit timeLimit = practiceContent.getTimeLimit();
                            if (timeLimit != null && timeLimit.getTime() != null) {
                                duration = timeLimit.getTime().longValue() * 60 * 1000;
                            }
                            break;
                        }
                    }
                }
            }
        }
        // 如果全作答并且全对，但是总分不是100时把总分设置为100分
        if (finishedAll && allRight && score < 100D) {
            score = 100D;
        }
        CalculateResult result = new CalculateResult();
        result.setScore(score);
        result.setDuration(duration);
        return result;
    }
}
