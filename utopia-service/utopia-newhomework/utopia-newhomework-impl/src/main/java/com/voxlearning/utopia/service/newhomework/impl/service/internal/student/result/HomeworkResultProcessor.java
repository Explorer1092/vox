package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.context.HomeworkResultContext;

import javax.inject.Named;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * 大作业结果处理器
 *
 * @author Ruib
 * @version 0.1
 * @since 2016/1/13
 */
@Named
@HomeworkResultTasks({
        HR_LoadHomework.class,
        HR_CheckHomeworkFinished.class,
        HR_LoadGroup.class,
        HR_CalculateStandardScore.class,
        HR_CalculateScore.class,
        HR_ImmediateIntervention.class,
        HR_ProcessFile.class,
        HR_CreateHomeworkProcessResult.class,
        HR_SaveAfentiWrongQuestionLibrary.class,
        HR_UpdateHomeworkResult.class,
        HR_CheckBasicAppPracticeFinish.class,
        HR_CreateStudentExamResult.class,
        HR_FinishHomework.class
})
public class HomeworkResultProcessor extends SpringContainerSupport {
    private final List<HomeworkResultTask> tasks = new LinkedList<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        HomeworkResultTasks annotation = getClass().getAnnotation(HomeworkResultTasks.class);
        Objects.requireNonNull(annotation, "@HomeworkResultTasks is required");

        for (Class<? extends HomeworkResultTask> beanClass : annotation.value()) {
            HomeworkResultTask task = null;
            Map<String, ? extends HomeworkResultTask> beans = applicationContext.getBeansOfType(beanClass);
            for (HomeworkResultTask bean : beans.values()) {
                if (bean.getClass() == beanClass) {
                    task = bean;
                    break;
                }
            }
            if (task == null) throw new IllegalStateException("No bean " + beanClass.getName() + " defined");
            tasks.add(task);
        }
    }

    public HomeworkResultContext process(HomeworkResultContext context) {
        if (context == null) return new HomeworkResultContext().errorResponse();
        for (HomeworkResultTask task : tasks) {
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
