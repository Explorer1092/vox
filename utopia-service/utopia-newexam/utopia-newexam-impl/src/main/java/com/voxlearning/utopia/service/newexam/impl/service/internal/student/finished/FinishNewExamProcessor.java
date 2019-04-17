package com.voxlearning.utopia.service.newexam.impl.service.internal.student.finished;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newexam.api.context.FinishNewExamContext;

import javax.inject.Named;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by tanguohong on 2016/3/11.
 */
@Named
@FinishNewExamTasks({
        FER_SaveJournalNewExamProcessResult.class
})
public class FinishNewExamProcessor extends SpringContainerSupport{
    private final List<FinishNewExamTask> tasks = new LinkedList<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        FinishNewExamTasks annotation = getClass().getAnnotation(FinishNewExamTasks.class);
        Objects.requireNonNull(annotation, "@FinishNewExamTasks is required");

        for (Class<? extends FinishNewExamTask> beanClass : annotation.value()) {
            FinishNewExamTask task = null;
            Map<String, ? extends FinishNewExamTask> beans = applicationContext.getBeansOfType(beanClass);
            for (FinishNewExamTask bean : beans.values()) {
                if (bean.getClass() == beanClass) {
                    task = bean;
                    break;
                }
            }
            if (task == null) throw new IllegalStateException("No bean " + beanClass.getName() + " defined");
            tasks.add(task);
        }
    }

    public FinishNewExamContext process(FinishNewExamContext context) {
        if (context == null) return new FinishNewExamContext().errorResponse();
        for (FinishNewExamTask task : tasks) {
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
