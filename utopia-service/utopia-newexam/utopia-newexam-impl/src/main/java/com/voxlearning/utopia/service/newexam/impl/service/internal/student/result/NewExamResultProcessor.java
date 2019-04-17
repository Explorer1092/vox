package com.voxlearning.utopia.service.newexam.impl.service.internal.student.result;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newexam.api.context.NewExamResultContext;

import javax.inject.Named;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by tanguohong on 2016/3/10.
 */
@Named
@NewExamResultTasks({
        ER_LoadClazz.class,
        ER_LoadNewExam.class,
        ER_LoadNewPaper.class,
        ER_LoadNewExamResult.class,
        ER_LoadNewExamProcessorResult.class,
        ER_CalculateStandardScore.class,
        ER_ProcessFile.class,
        ER_CalculateScore.class,
        ER_InitNewExamProcessResult.class,
        ER_CalculateTotalScore.class,
        ER_CalculateTotalDuration.class,
        ER_CheckFinished.class,
        ER_CreateNewExamProcessResult.class,
        ER_UpdateNewExamRegistration.class,
        ER_UpdateNewExamResult.class,
        ER_CreateStudentExamResult.class,
        ER_FinishNewExam.class
})
public class NewExamResultProcessor extends SpringContainerSupport {
    private final List<NewExamResultTask> tasks = new LinkedList<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        NewExamResultTasks annotation = getClass().getAnnotation(NewExamResultTasks.class);
        Objects.requireNonNull(annotation, "@NewExamResultTasks is required");

        for (Class<? extends NewExamResultTask> beanClass : annotation.value()) {
            NewExamResultTask task = null;
            Map<String, ? extends NewExamResultTask> beans = applicationContext.getBeansOfType(beanClass);
            for (NewExamResultTask bean : beans.values()) {
                if (bean.getClass() == beanClass) {
                    task = bean;
                    break;
                }
            }
            if (task == null) throw new IllegalStateException("No bean " + beanClass.getName() + " defined");
            tasks.add(task);
        }
    }

    public NewExamResultContext process(NewExamResultContext context) {
        if (context == null) return new NewExamResultContext().errorResponse();
        for (NewExamResultTask task : tasks) {
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
