package com.voxlearning.utopia.service.newhomework.impl.service.livecast.student.result.template;

import com.voxlearning.utopia.service.newhomework.api.context.livecast.LiveCastHomeworkResultContext;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.impl.dao.livecast.LiveCastHomeworkResultDao;
import com.voxlearning.utopia.service.question.api.constant.NewHomeworkContentProcessTemp;
import org.springframework.context.annotation.Lazy;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

/**
 * @author xuesong.zhang
 * @since 2017/7/10
 */
@Named
@Lazy(false)
public class LiveCastHomeworkResultUpdate_Exam extends LiveCastHomeworkResultUpdateTemplate {

    @Inject private LiveCastHomeworkResultDao liveCastHomeworkResultDao;

    @Override
    public NewHomeworkContentProcessTemp getNewHomeworkResultUpdateTemp() {
        return NewHomeworkContentProcessTemp.EXAM;
    }

    @Override
    public void processLiveCastHomeworkContext(LiveCastHomeworkResultContext context) {
        LiveCastHomework.Location location = context.getLiveCastHomework().toLocation();
        Map<String, LiveCastHomeworkProcessResult> processResultMap = context.getProcessResult();
        for (LiveCastHomeworkProcessResult processResult : processResultMap.values()) {
            liveCastHomeworkResultDao.doHomework(
                    location,
                    context.getUserId(),
                    processResult.getObjectiveConfigType(),
                    processResult.getQuestionId(),
                    processResult.getId());
        }
    }

    @Override
    public void checkLiveCastHomeworkAppFinish(LiveCastHomeworkResultContext context) {

    }
}
