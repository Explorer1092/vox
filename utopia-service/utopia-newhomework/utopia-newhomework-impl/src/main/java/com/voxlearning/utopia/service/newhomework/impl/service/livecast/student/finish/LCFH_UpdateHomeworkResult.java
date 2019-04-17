package com.voxlearning.utopia.service.newhomework.impl.service.livecast.student.finish;

import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkPublishMessageType;
import com.voxlearning.utopia.service.newhomework.api.context.livecast.FinishLiveCastHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomeworkResult;
import com.voxlearning.utopia.service.newhomework.impl.dao.livecast.LiveCastHomeworkResultDao;
import com.voxlearning.utopia.service.newhomework.impl.pubsub.LiveCastHomeworkPublisher;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xuesong.zhang
 * @since 2017/1/3
 */
@Named
public class LCFH_UpdateHomeworkResult extends SpringContainerSupport implements FinishLiveCastHomeworkTask {

    @Inject private LiveCastHomeworkResultDao liveCastHomeworkResultDao;
    @Inject private LiveCastHomeworkPublisher liveCastHomeworkPublisher;


    @Override
    public void execute(FinishLiveCastHomeworkContext context) {
        LiveCastHomeworkResult modified = liveCastHomeworkResultDao.finishHomework(
                context.getLiveCastHomework().toLocation(),
                context.getUserId(),
                context.getObjectiveConfigType(),
                context.getPracticeScore(),
                context.getPracticeDureation(),
                context.isPracticeFinished(),
                context.isHomeworkFinished(),
                (context.isHomeworkFinished() && !context.getLiveCastHomework().getIncludeSubjective())
        );


        if (context.isHomeworkFinished() && modified != null) {
            context.setLiveCastHomeworkResult(modified);
            Map<String, Object> map = new HashMap<>();
            map.put("liveCastHomeworkResult", modified);
            map.put("messageType", HomeworkPublishMessageType.finished);
            liveCastHomeworkPublisher.getTeacherPublisher().publish(Message.newMessage().withPlainTextBody(JsonUtils.toJson(map)));
        }

        if (!context.isHomeworkFinished()) context.terminateTask();


    }
}

