package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.selfstudy;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.context.selfstudy.SelfStudyHomeworkContext;

import javax.inject.Named;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author xuesong.zhang
 * @since 2017/2/3
 */
@Named
@SelfStudyHomeworkResultTasks({
        // 获取作业信息
        // 计算标准分
        // 计算得分
        // 写入做题结果
        // 更新中间表
        // 上报大数据（待定）
        // 完成
        SS_LoadHomework.class,
        SS_CalculateStandardScore.class,
        SS_CalculateScore.class,
        SS_CreateHomeworkProcessResult.class,
        SS_UpdateSelfStudyHomeworkResult.class,
        SS_QueueProcessHomeworkResult.class,
        SS_FinishHomework.class
})
public class SelfStudyHomeworkResultProcessor extends SpringContainerSupport {

    private final List<SelfStudyHomeworkResultTask> tasks = new LinkedList<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        SelfStudyHomeworkResultTasks annotation = getClass().getAnnotation(SelfStudyHomeworkResultTasks.class);
        Objects.requireNonNull(annotation, "@SelfStudyHomeworkResultTasks is required");

        for (Class<? extends SelfStudyHomeworkResultTask> beanClass : annotation.value()) {
            SelfStudyHomeworkResultTask task = null;
            Map<String, ? extends SelfStudyHomeworkResultTask> beans = applicationContext.getBeansOfType(beanClass);
            for (SelfStudyHomeworkResultTask bean : beans.values()) {
                if (bean.getClass() == beanClass) {
                    task = bean;
                    break;
                }
            }
            if (task == null) throw new IllegalStateException("No bean " + beanClass.getName() + " defined");
            tasks.add(task);
        }
    }

    public SelfStudyHomeworkContext process(SelfStudyHomeworkContext context) {
        if (context == null) return new SelfStudyHomeworkContext().errorResponse();
        for (SelfStudyHomeworkResultTask task : tasks) {
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
