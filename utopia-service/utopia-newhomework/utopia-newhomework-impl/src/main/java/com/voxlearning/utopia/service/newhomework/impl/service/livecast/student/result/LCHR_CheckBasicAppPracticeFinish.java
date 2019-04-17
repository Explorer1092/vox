package com.voxlearning.utopia.service.newhomework.impl.service.livecast.student.result;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.context.livecast.LiveCastHomeworkResultContext;
import com.voxlearning.utopia.service.newhomework.impl.service.livecast.student.result.template.LiveCastHomeworkResultUpdateFactory;
import com.voxlearning.utopia.service.newhomework.impl.service.livecast.student.result.template.LiveCastHomeworkResultUpdateTemplate;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author xuesong.zhang
 * @since 2017/7/11
 */
@Named
public class LCHR_CheckBasicAppPracticeFinish extends SpringContainerSupport implements LiveCastHomeworkResultTask {

    @Inject private LiveCastHomeworkResultUpdateFactory liveCastHomeworkResultUpdateFactory;

    @Override
    public void execute(LiveCastHomeworkResultContext context) {
        if (ObjectiveConfigType.BASIC_APP.equals(context.getObjectiveConfigType())) {
            LiveCastHomeworkResultUpdateTemplate template = liveCastHomeworkResultUpdateFactory.getTemplate(context.getObjectiveConfigType().getNewHomeworkContentProcessTemp());
            template.checkLiveCastHomeworkAppFinish(context);
        }
    }
}
