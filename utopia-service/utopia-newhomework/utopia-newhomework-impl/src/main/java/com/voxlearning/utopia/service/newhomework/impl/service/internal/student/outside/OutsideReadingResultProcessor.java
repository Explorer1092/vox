package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.outside;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.context.outside.OutsideReadingContext;

import javax.inject.Named;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author majianxin
 */
@Named
@OutsideReadingResultTasks({
        // 获取信息
        // 计算标准分
        // 计算得分
        // 处理主观题文件
        // 写入做题结果
        // 更新中间表
        // 上报
        // 完成
        OS_LoadOutsideReading.class,
        OS_CalculateStandardScore.class,
        OS_CalculateScore.class,
        OS_ProcessFile.class,
        OS_CreateOutsideReadingProcessResult.class,
        OS_UpdateOutsideReadingResult.class,
        OS_CreateStudentExamResult.class,
        OS_FinishOutsideReading.class
})
public class OutsideReadingResultProcessor extends SpringContainerSupport {

    private final List<OutsideReadingResultTask> tasks = new LinkedList<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        OutsideReadingResultTasks annotation = getClass().getAnnotation(OutsideReadingResultTasks.class);
        Objects.requireNonNull(annotation, "@OutsideReadingResultTask is required");

        for (Class<? extends OutsideReadingResultTask> beanClass : annotation.value()) {
            OutsideReadingResultTask task = null;
            Map<String, ? extends OutsideReadingResultTask> beans = applicationContext.getBeansOfType(beanClass);
            for (OutsideReadingResultTask bean : beans.values()) {
                if (bean.getClass() == beanClass) {
                    task = bean;
                    break;
                }
            }
            if (task == null) throw new IllegalStateException("No bean " + beanClass.getName() + " defined");
            tasks.add(task);
        }
    }

    public OutsideReadingContext process(OutsideReadingContext context) {
        if (context == null) return new OutsideReadingContext().errorResponse();
        for (OutsideReadingResultTask task : tasks) {
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
