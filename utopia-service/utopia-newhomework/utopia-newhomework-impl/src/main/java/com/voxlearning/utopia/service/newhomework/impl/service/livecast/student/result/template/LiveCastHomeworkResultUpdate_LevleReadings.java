package com.voxlearning.utopia.service.newhomework.impl.service.livecast.student.result.template;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.api.constant.AppOralScoreLevel;
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

@Named
public class LiveCastHomeworkResultUpdate_LevleReadings extends LiveCastHomeworkResultUpdateTemplate {

    @Inject
    private LiveCastHomeworkResultDao liveCastHomeworkResultDao;

    @Override
    public NewHomeworkContentProcessTemp getNewHomeworkResultUpdateTemp() {
        return NewHomeworkContentProcessTemp.LEVEL_READINGS;
    }

    @Override
    public void processLiveCastHomeworkContext(LiveCastHomeworkResultContext context) {
        LiveCastHomework.Location location = context.getLiveCastHomework().toLocation();
        Map<String, LiveCastHomeworkProcessResult> processResultMap = context.getProcessResult();
        Map<String, LiveCastHomeworkProcessResult> processOralResultMap = context.getProcessOralResult();
        NewHomeworkResultAppAnswer nhraa = new NewHomeworkResultAppAnswer();

        long duration = context.getDurations().values()
                .stream()
                .mapToLong(SafeConverter::toLong)
                .sum();

        if (duration > NewHomeworkConstants.LEVEL_READINGS_MAX_DURATION_MILLISECONDS) {
            duration = NewHomeworkConstants.LEVEL_READINGS_MAX_DURATION_MILLISECONDS;
        }

        boolean allQuestionRight = true;

        // 新绘本基准分60
        Double score = 60d;
        LinkedHashMap<String, String> answers = new LinkedHashMap<>();
        if (MapUtils.isNotEmpty(processResultMap)) {
            for (LiveCastHomeworkProcessResult npr : processResultMap.values()) {
                score += npr.getScore();
                answers.put(npr.getQuestionId(), npr.getId());
                if (!SafeConverter.toBoolean(npr.getGrasp())) {
                    allQuestionRight = false;
                }
            }
        }

        // 绘本特殊属性
        LinkedHashMap<String, String> oralAnswers = new LinkedHashMap<>();
        if (MapUtils.isNotEmpty(processOralResultMap)) {
            for (LiveCastHomeworkProcessResult npr : processOralResultMap.values()) {
                score += npr.getScore();
                oralAnswers.put(npr.getQuestionId(), npr.getId());
                if (AppOralScoreLevel.A != npr.getAppOralScoreLevel()) {
                    allQuestionRight = false;
                }
            }
        }

        if (allQuestionRight) {
            score = 100D;
        }

        if (score > 100D) {
            score = 100D;
        }

        nhraa.setFinishAt(new Date());
        Double avgScore = score;
        nhraa.setScore(avgScore);
        nhraa.setDuration(duration);
        nhraa.setPictureBookId(context.getPictureBookId());
        nhraa.setConsumeTime(context.getConsumeTime());
        nhraa.setAnswers(answers);
        nhraa.setOralAnswers(oralAnswers);
        nhraa.setDurations(context.getDurations());
        nhraa.setDubbingId(context.getDubbingId());
        nhraa.setDubbingScore(context.getDubbingScore());
        nhraa.setDubbingScoreLevel(context.getDubbingScoreLevel());
        String key = context.getPictureBookId();
        liveCastHomeworkResultDao.doHomeworkBasicApp(
                location,
                context.getUserId(),
                context.getObjectiveConfigType(),
                key,
                nhraa);
    }

    @Override
    public void checkLiveCastHomeworkAppFinish(LiveCastHomeworkResultContext context) {

    }
}
