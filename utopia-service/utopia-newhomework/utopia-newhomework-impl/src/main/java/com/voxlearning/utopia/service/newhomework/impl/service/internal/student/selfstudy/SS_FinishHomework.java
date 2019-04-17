package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.selfstudy;

import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.context.selfstudy.FinishSelfStudyHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.context.selfstudy.SelfStudyHomeworkContext;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.student.selfstudy.finish.FinishSelfStudyHomeworkProcessor;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author xuesong.zhang
 * @since 2017/2/3
 */
@Named
public class SS_FinishHomework extends SpringContainerSupport implements SelfStudyHomeworkResultTask {

    @Inject private FinishSelfStudyHomeworkProcessor finishSelfStudyHomeworkProcessor;

    @Override
    public void execute(SelfStudyHomeworkContext context) {
        FinishSelfStudyHomeworkContext ctx = new FinishSelfStudyHomeworkContext();
        ctx.setUserId(context.getUserId());
        ctx.setHomeworkId(context.getHomeworkId());
        ctx.setSelfStudyHomework(context.getSelfStudyHomework());
        ctx.setObjectiveConfigType(context.getObjectiveConfigType());
        ctx.setAppChameleonId(context.getStudentHomeworkAnswer().getCourseId());
        if (context.getObjectiveConfigType().equals(ObjectiveConfigType.ORAL_INTERVENTIONS)) {
            ctx.setPracticeScore(context.getStudentHomeworkAnswer().getCourseGrasp() ? 100D : 0D);
            ctx.setPracticeDuration(context.getStudentHomeworkAnswer().getDurationMilliseconds());
        }

        AlpsThreadPool.getInstance().submit(() -> finishSelfStudyHomeworkProcessor.process(ctx));
    }
}
