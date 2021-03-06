package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result.template;

import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.context.HomeworkResultContext;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import com.voxlearning.utopia.service.question.api.constant.NewHomeworkContentProcessTemp;

import javax.inject.Named;
import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author guoqiang.li
 * @since 2017/10/31
 */
@Named
public class NewHomeworkResultUpdate_Dubbing extends NewHomeworkResultUpdateTemplate {
    @Override
    public NewHomeworkContentProcessTemp getNewHomeworkResultUpdateTemp() {
        return NewHomeworkContentProcessTemp.DUBBING;
    }

    @Override
    public void processHomeworkContent(HomeworkResultContext context) {
        NewHomework.Location location = context.getHomework().toLocation();
        Map<String, NewHomeworkProcessResult> processResultMap = context.getProcessResult();

        Long duration = 0L;
        Double score;
        double totalScore = 0D;
        LinkedHashMap<String, String> answers = new LinkedHashMap<>();
        for (NewHomeworkProcessResult processResult : processResultMap.values()) {
            answers.put(processResult.getQuestionId(), processResult.getId());
            duration += processResult.getDuration();
            totalScore += processResult.getScore() != null ? processResult.getScore() : 0;
        }
        score = processResultMap.values().size() == 0 ? 0 : new BigDecimal(totalScore)
                .divide(new BigDecimal(processResultMap.values().size()), 0, BigDecimal.ROUND_HALF_UP)
                .doubleValue();
        if (duration != null && duration > NewHomeworkConstants.DUBBING_MAX_DURATION_MILLISECONDS) {
            duration = NewHomeworkConstants.DEFAULT_DURATION_MILLISECONDS;
        }
        Long consumerTime = context.getConsumeTime();
        if (consumerTime != null && consumerTime > NewHomeworkConstants.DUBBING_MAX_DURATION_MILLISECONDS) {
            consumerTime = NewHomeworkConstants.DUBBING_MAX_DURATION_MILLISECONDS;
        }
        NewHomeworkResultAppAnswer appAnswer = new NewHomeworkResultAppAnswer();
        appAnswer.setFinishAt(new Date());
        appAnswer.setScore(score);
        appAnswer.setDuration(duration);
        appAnswer.setConsumeTime(consumerTime);
        appAnswer.setDubbingId(context.getDubbingId());
        appAnswer.setVideoUrl(context.getVideoUrl());
        appAnswer.setSkipUploadVideo(context.getSkipUploadVideo());
        appAnswer.setAnswers(answers);
        String key = context.getDubbingId();
        newHomeworkResultService.doHomeworkBasicAppPractice(
                location,
                context.getUserId(),
                context.getObjectiveConfigType(),
                key,
                appAnswer
        );
    }

    @Override
    public void checkNewHomeworkAppFinish(HomeworkResultContext context) {

    }
}
