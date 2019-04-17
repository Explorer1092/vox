package com.voxlearning.utopia.service.newhomework.impl.service.livecast.student.result;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.context.livecast.LiveCastHomeworkResultContext;
import com.voxlearning.utopia.service.newhomework.impl.service.livecast.student.result.template.LiveCastHomeworkResultUpdateFactory;
import com.voxlearning.utopia.service.newhomework.impl.service.livecast.student.result.template.LiveCastHomeworkResultUpdateTemplate;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author xuesong.zhang
 * @since 2017/1/3
 */
@Named
public class LCHR_UpdateHomeworkResult extends SpringContainerSupport implements LiveCastHomeworkResultTask {

    @Inject private LiveCastHomeworkResultUpdateFactory liveCastHomeworkResultUpdateFactory;

    @Override
    public void execute(LiveCastHomeworkResultContext context) {
        LiveCastHomeworkResultUpdateTemplate template = liveCastHomeworkResultUpdateFactory.getTemplate(context.getObjectiveConfigType().getNewHomeworkContentProcessTemp());
        if (template == null) {
            context.errorResponse("missing {}'s template", context.getObjectiveConfigType());
            return;
        }
        template.processLiveCastHomeworkContext(context);
    }
}
