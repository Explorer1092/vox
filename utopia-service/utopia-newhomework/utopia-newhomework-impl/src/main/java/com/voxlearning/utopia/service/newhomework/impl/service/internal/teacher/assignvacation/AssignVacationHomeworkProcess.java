package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assignvacation;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.context.AssignVacationHomeworkContext;

import javax.inject.Named;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by tanguohong on 2016/11/29.
 */
@Named
@AssignVacationHomeworkTasks({
        AHV_CheckRequiredParameters.class,
        AHV_CheckClazzGroup.class,
        AHV_CheckExistHomework.class,
        AHV_CheckHomeworkData.class,
        AHV_SaveHomework.class,
        AHV_Callback.class
})
public class AssignVacationHomeworkProcess extends SpringContainerSupport {
    private final List<AssignVacationHomeworkTask> tasks = new LinkedList<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        AssignVacationHomeworkTasks annotation = getClass().getAnnotation(AssignVacationHomeworkTasks.class);
        Objects.requireNonNull(annotation, "@AssignVacationHomeworkTasks is required");
        for(Class<? extends AssignVacationHomeworkTask> beanClass : annotation.value()){
            AssignVacationHomeworkTask task = null;
            Map<String, ? extends AssignVacationHomeworkTask> beans = applicationContext.getBeansOfType(beanClass);
            for(AssignVacationHomeworkTask bean : beans.values()){
                if(bean.getClass() == beanClass){
                    task = bean;
                    break;
                }
            }
            if(task == null) throw new IllegalStateException("No bean " + beanClass.getName() +" defined");
            tasks.add(task);
        }
    }

    public AssignVacationHomeworkContext process(AssignVacationHomeworkContext context) {
        if(context == null) return new AssignVacationHomeworkContext().errorResponse();
        for(AssignVacationHomeworkTask task : tasks){
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
