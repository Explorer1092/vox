package com.voxlearning.utopia.service.newhomework.impl.service.livecast.student.result.template;

import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.context.livecast.LiveCastHomeworkResultContext;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.impl.dao.livecast.LiveCastHomeworkResultDao;
import com.voxlearning.utopia.service.question.api.constant.NewHomeworkContentProcessTemp;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author guoqiang.li
 * @since 2017/10/31
 */
@Named
public class LiveCastHomeworkResultUpdate_Dubbing extends LiveCastHomeworkResultUpdateTemplate {

    @Inject
    private LiveCastHomeworkResultDao liveCastHomeworkResultDao;

    @Override
    public NewHomeworkContentProcessTemp getNewHomeworkResultUpdateTemp() {
        return NewHomeworkContentProcessTemp.DUBBING;
    }

    @Override
    public void processLiveCastHomeworkContext(LiveCastHomeworkResultContext context) {
        LiveCastHomework.Location location = context.getLiveCastHomework().toLocation();
        Map<String, LiveCastHomeworkProcessResult> processResultMap = context.getProcessResult();

        Long duration = 0L;
        LinkedHashMap<String, String> answers = new LinkedHashMap<>();
        for (LiveCastHomeworkProcessResult processResult : processResultMap.values()) {
            answers.put(processResult.getQuestionId(), processResult.getId());
            duration += processResult.getDuration();
        }
        if (duration != null && duration > NewHomeworkConstants.DUBBING_MAX_DURATION_MILLISECONDS) {
            duration = NewHomeworkConstants.DEFAULT_DURATION_MILLISECONDS;
        }
        Long consumerTime = context.getConsumeTime();
        if (consumerTime != null && consumerTime > NewHomeworkConstants.DUBBING_MAX_DURATION_MILLISECONDS) {
            consumerTime = NewHomeworkConstants.DUBBING_MAX_DURATION_MILLISECONDS;
        }
        NewHomeworkResultAppAnswer appAnswer = new NewHomeworkResultAppAnswer();
        appAnswer.setFinishAt(new Date());
        appAnswer.setDuration(duration);
        appAnswer.setConsumeTime(consumerTime);
        appAnswer.setDubbingId(context.getDubbingId());
        appAnswer.setVideoUrl(context.getVideoUrl());
        appAnswer.setAnswers(answers);
        String key = context.getDubbingId();
        liveCastHomeworkResultDao.doHomeworkBasicApp(
                location,
                context.getUserId(),
                context.getObjectiveConfigType(),
                key,
                appAnswer
        );
    }

    @Override
    public void checkLiveCastHomeworkAppFinish(LiveCastHomeworkResultContext context) {

    }
}
