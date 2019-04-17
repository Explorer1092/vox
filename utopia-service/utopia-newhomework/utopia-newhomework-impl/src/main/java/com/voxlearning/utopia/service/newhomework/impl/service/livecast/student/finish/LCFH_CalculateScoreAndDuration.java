package com.voxlearning.utopia.service.newhomework.impl.service.livecast.student.finish;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.context.livecast.FinishLiveCastHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.impl.dao.livecast.LiveCastHomeworkProcessResultDao;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.student.calculator.CalculateResult;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xuesong.zhang
 * @since 2017/1/3
 */
@Named
public class LCFH_CalculateScoreAndDuration extends SpringContainerSupport implements FinishLiveCastHomeworkTask {

    @Inject private LiveCastHomeworkProcessResultDao liveCastHomeworkProcessResultDao;

    @Override
    public void execute(FinishLiveCastHomeworkContext context) {
        ObjectiveConfigType objectiveConfigType = context.getObjectiveConfigType();

        if (ObjectiveConfigType.BASIC_APP.equals(objectiveConfigType)
                || ObjectiveConfigType.READING.equals(objectiveConfigType)
                || ObjectiveConfigType.LEVEL_READINGS.equals(objectiveConfigType)) {
            LinkedHashMap<String, NewHomeworkResultAppAnswer> nhraMap = context.getLiveCastHomeworkResult().getPractices().get(objectiveConfigType).getAppAnswers();
            double totalScore = 0d;
            Long totalDuration = 0L;
            for (NewHomeworkResultAppAnswer nhra : nhraMap.values()) {
                totalDuration += nhra.getDuration();
                totalScore += nhra.getScore();
            }
            double avgScore = new BigDecimal(totalScore).divide(new BigDecimal(nhraMap.size()), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
            context.setPracticeScore(avgScore);
            context.setPracticeDureation(totalDuration);
        } else if (ObjectiveConfigType.DUBBING.equals(objectiveConfigType)) {
            // 趣味配音不再计算分数
            LinkedHashMap<String, NewHomeworkResultAppAnswer> nhraMap = context.getLiveCastHomeworkResult().getPractices().get(objectiveConfigType).getAppAnswers();
            Long totalDuration = 0L;
            for (NewHomeworkResultAppAnswer nhra : nhraMap.values()) {
                totalDuration += nhra.getDuration();
            }
            context.setPracticeDureation(totalDuration);
        } else {
            NewHomeworkResultAnswer answer = context.getLiveCastHomeworkResult().getPractices().get(objectiveConfigType);
            Set<String> processIds = new HashSet<>(answer.getAnswers().values());
            // 计算分数和耗时

            if (CollectionUtils.isNotEmpty(processIds)) {
                CalculateResult result = calculate(processIds);
                if (result != null) {
                    context.setPracticeScore(result.getScore());
                    context.setPracticeDureation(result.getDuration());
                }
            }
        }
    }

    private CalculateResult calculate(Set<String> processIds) {
        if (CollectionUtils.isEmpty(processIds)) return null;

        Map<String, LiveCastHomeworkProcessResult> results = liveCastHomeworkProcessResultDao.loads(processIds);
        if (MapUtils.isEmpty(results)) return null;

        // 计算总用时
        Long duration = results.values().stream().mapToLong(LiveCastHomeworkProcessResult::getDuration).sum();

        // 计算总得分
        Double score = null;
        List<LiveCastHomeworkProcessResult> nonNull = results.values().stream().filter(r -> r.getScore() != null).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(nonNull)) {
            score = nonNull.stream().mapToDouble(LiveCastHomeworkProcessResult::getScore).sum();
            //当题目全部正确时，但是总分计算结果却不是100分时就把总分设置为100分
            boolean allQuestionsRight = nonNull.stream().allMatch(p -> SafeConverter.toBoolean(p.getGrasp()));
            if (allQuestionsRight && score < 100D) {
                score = 100D;
            }
        }


        CalculateResult result = new CalculateResult();
        result.setScore(score);
        result.setDuration(duration);
        return result;
    }
}
