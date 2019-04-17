package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.selfstudy.finish;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.context.selfstudy.FinishSelfStudyHomeworkContext;

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
@FinishSelfStudyHomeworkTasks({
        // 获取必要信息
        // 检查作业形式是否做完
        // 检查是否全部完成
        // 计算得分和时间
        // 更新属性完成属性
        // 加学分不是学豆

        FSS_LoadSelfStudyHomeworkResult.class,
        FSS_CheckPracticeFinished.class,
        FSS_CheckSelfStudyHomeworkFinished.class,
        FSS_CalculateScoreAndDuration.class,
        FSS_UpdateSelfStudyHomeworkResult.class,
        FSS_GenerateSelfStudyHomeworkReport.class,
})
public class FinishSelfStudyHomeworkProcessor extends SpringContainerSupport {
    private final List<FinishSelfStudyHomeworkTask> tasks = new LinkedList<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        FinishSelfStudyHomeworkTasks annotation = getClass().getAnnotation(FinishSelfStudyHomeworkTasks.class);
        Objects.requireNonNull(annotation, "@FinishSelfStudyHomeworkTasks is required");

        for (Class<? extends FinishSelfStudyHomeworkTask> beanClass : annotation.value()) {
            FinishSelfStudyHomeworkTask task = null;
            Map<String, ? extends FinishSelfStudyHomeworkTask> beans = applicationContext.getBeansOfType(beanClass);
            for (FinishSelfStudyHomeworkTask bean : beans.values()) {
                if (bean.getClass() == beanClass) {
                    task = bean;
                    break;
                }
            }
            if (task == null) throw new IllegalStateException("No bean " + beanClass.getName() + " defined");
            tasks.add(task);
        }
    }

    public FinishSelfStudyHomeworkContext process(FinishSelfStudyHomeworkContext context) {
        if (context == null) return new FinishSelfStudyHomeworkContext().errorResponse();
        for (FinishSelfStudyHomeworkTask task : tasks) {
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
