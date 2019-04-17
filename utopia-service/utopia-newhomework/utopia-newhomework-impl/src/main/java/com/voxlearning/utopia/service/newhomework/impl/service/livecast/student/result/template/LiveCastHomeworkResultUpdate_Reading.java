package com.voxlearning.utopia.service.newhomework.impl.service.livecast.student.result.template;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.utopia.service.newhomework.api.context.livecast.LiveCastHomeworkResultContext;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAppAnswer;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.impl.dao.livecast.LiveCastHomeworkResultDao;
import com.voxlearning.utopia.service.question.api.constant.NewHomeworkContentProcessTemp;
import org.springframework.context.annotation.Lazy;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author xuesong.zhang
 * @since 2017/7/10
 */
@Named
@Lazy(false)
public class LiveCastHomeworkResultUpdate_Reading extends LiveCastHomeworkResultUpdateTemplate {

    @Inject private LiveCastHomeworkResultDao liveCastHomeworkResultDao;

    @Override
    public NewHomeworkContentProcessTemp getNewHomeworkResultUpdateTemp() {
        return NewHomeworkContentProcessTemp.READING;
    }

    @Override
    public void processLiveCastHomeworkContext(LiveCastHomeworkResultContext context) {
        LiveCastHomework.Location location = context.getLiveCastHomework().toLocation();
        Map<String, LiveCastHomeworkProcessResult> processResultMap = context.getProcessResult();
        Map<String, LiveCastHomeworkProcessResult> processOralResultMap = context.getProcessOralResult();
        // 绘本
        NewHomeworkResultAppAnswer nhraa = new NewHomeworkResultAppAnswer();
        Double score = 0d;
        Long duration = 0L;
        LinkedHashMap<String, String> answers = new LinkedHashMap<>();
        for (LiveCastHomeworkProcessResult npr : processResultMap.values()) {
            score += npr.getScore();
            duration += npr.getDuration();
            answers.put(npr.getQuestionId(), npr.getId());
        }

        // 绘本特殊属性
        LinkedHashMap<String, String> oralAnswers = new LinkedHashMap<>();
        if (MapUtils.isNotEmpty(processOralResultMap)) {
            for (LiveCastHomeworkProcessResult npr : processOralResultMap.values()) {
                // 绘本的跟读题时间计入
                duration += npr.getDuration();
                oralAnswers.put(npr.getQuestionId(), npr.getId());
            }
        }

        nhraa.setFinishAt(new Date());
        Double avgScore = score;
        nhraa.setScore(avgScore);
        nhraa.setDuration(duration);
        nhraa.setPictureBookId(context.getPictureBookId());
        nhraa.setConsumeTime(context.getConsumeTime());
        nhraa.setAnswers(answers);
        nhraa.setOralAnswers(oralAnswers);
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
