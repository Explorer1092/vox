package com.voxlearning.utopia.service.newexam.impl.service.internal.teacher.correction;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newexam.api.context.CorrectNewExamContext;

import javax.inject.Named;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by tanguohong on 2016/3/23.
 */
@Named
@CorrectNewExamTasks({
        CE_LoadNewExam.class,
        CE_LoadNewQuestion.class,
        CE_LoadNewExamResult.class,
        CE_CalculateStandardScore.class,
        CE_LoadNewExamProcessResult.class,
        CE_LoadNewExamRegistration.class,
        CE_CorrectNewExam.class

})
public class CorrectNewExamProcessor extends SpringContainerSupport {

    private final List<CorrectNewExamTask> tasks = new LinkedList<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        CorrectNewExamTasks annotation = getClass().getAnnotation(CorrectNewExamTasks.class);
        Objects.requireNonNull(annotation, "@CorrectNewExamTasks is required");

        for (Class<? extends CorrectNewExamTask> beanClass : annotation.value()) {
            CorrectNewExamTask task = null;
            Map<String, ? extends CorrectNewExamTask> beans = applicationContext.getBeansOfType(beanClass);
            for (CorrectNewExamTask bean : beans.values()) {
                if (bean.getClass() == beanClass) {
                    task = bean;
                    break;
                }
            }
            if (task == null) throw new IllegalStateException("No bean " + beanClass.getName() + " defined");
            tasks.add(task);
        }
    }

    public CorrectNewExamContext process(CorrectNewExamContext context) {
        if (context == null) return new CorrectNewExamContext().errorResponse();
        for (CorrectNewExamTask task : tasks) {
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
