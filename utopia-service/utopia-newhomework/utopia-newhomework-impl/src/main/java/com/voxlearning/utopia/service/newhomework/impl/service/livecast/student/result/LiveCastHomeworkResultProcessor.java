package com.voxlearning.utopia.service.newhomework.impl.service.livecast.student.result;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.context.livecast.LiveCastHomeworkResultContext;

import javax.inject.Named;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author xuesong.zhang
 * @since 2017/1/3
 */
@Named
@LiveCastHomeworkResultTasks({
        LCHR_LoadHomework.class,
        LCHR_LoadGroup.class,
        LCHR_CalculateStandardScore.class,
        LCHR_CalculateScore.class,
        LCHR_ProcessFile.class,
        LCHR_CreateHomeworkProcessResult.class,
        LCHR_UpdateHomeworkResult.class,
        LCHR_CheckBasicAppPracticeFinish.class,
        LCHR_FinishHomework.class
})
public class LiveCastHomeworkResultProcessor extends SpringContainerSupport {
    private final List<LiveCastHomeworkResultTask> tasks = new LinkedList<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        LiveCastHomeworkResultTasks annotation = getClass().getAnnotation(LiveCastHomeworkResultTasks.class);
        Objects.requireNonNull(annotation, "@LiveCastHomeworkResultTasks is required");

        for (Class<? extends LiveCastHomeworkResultTask> beanClass : annotation.value()) {
            LiveCastHomeworkResultTask task = null;
            Map<String, ? extends LiveCastHomeworkResultTask> beans = applicationContext.getBeansOfType(beanClass);
            for (LiveCastHomeworkResultTask bean : beans.values()) {
                if (bean.getClass() == beanClass) {
                    task = bean;
                    break;
                }
            }
            if (task == null) throw new IllegalStateException("No bean " + beanClass.getName() + " defined");
            tasks.add(task);
        }
    }

    public LiveCastHomeworkResultContext process(LiveCastHomeworkResultContext context) {
        if (context == null) return new LiveCastHomeworkResultContext().errorResponse();
        for (LiveCastHomeworkResultTask task : tasks) {
            task.execute(context);
            if (!context.isSuccessful()) {
                logger.debug("Task {} return false, terminate", task.getClass().getName());
                break;
            }
            if (context.isTerminateTask()) {
                logger.debug("Task {} set terminateTask true, terminate", task.getClass().getName());
                break;
            }
        }
        return context.clearAdditions();
    }
}
