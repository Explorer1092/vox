package com.voxlearning.utopia.service.newhomework.impl.service.livecast.student.result;

import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.context.livecast.FinishLiveCastHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.context.livecast.LiveCastHomeworkResultContext;
import com.voxlearning.utopia.service.newhomework.impl.service.livecast.student.finish.FinishLiveCastHomeworkProcessor;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author xuesong.zhang
 * @since 2017/1/3
 */
@Named
public class LCHR_FinishHomework extends SpringContainerSupport implements LiveCastHomeworkResultTask {

    @Inject private FinishLiveCastHomeworkProcessor finishLiveCastHomeworkProcessor;

    @Override
    public void execute(LiveCastHomeworkResultContext context) {
        FinishLiveCastHomeworkContext ctx = new FinishLiveCastHomeworkContext();
        ctx.setUserId(context.getUserId());
        ctx.setUser(context.getUser());
        ctx.setClazzGroupId(context.getClazzGroupId());
        // ctx.setThirdPartyGroupMapper(context.getThirdPartyGroupMapper());
        ctx.setHomeworkId(context.getHomeworkId());
        ctx.setLiveCastHomework(context.getLiveCastHomework());
        ctx.setNewHomeworkType(context.getNewHomeworkType());
        ctx.setObjectiveConfigType(context.getObjectiveConfigType());
        ctx.setClientType(context.getClientType());
        ctx.setClientName(context.getClientName());
        ctx.setIpImei(context.getIpImei());
        ctx.setSupplementaryData(false);
        AlpsThreadPool.getInstance().submit(() -> finishLiveCastHomeworkProcessor.process(ctx));
    }
}
