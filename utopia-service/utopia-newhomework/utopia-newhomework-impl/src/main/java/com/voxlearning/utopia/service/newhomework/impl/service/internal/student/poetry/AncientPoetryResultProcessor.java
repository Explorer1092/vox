package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.poetry;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.context.AncientPoetryProcessContext;

import javax.inject.Named;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author majianxin
 */
@Named
@AncientPoetryResultTasks({
        // 获取信息
        // 处理名师讲解&名句赏析&每日朗诵&巩固练习
        // 更新关卡结果
        // 完成活动
        AP_LoadActivity.class,
        AP_AncientPoetryProcessResult.class,
        AP_FinishAncientPoetryMissionResult.class,
        AP_FinishActivity.class
})
public class AncientPoetryResultProcessor extends SpringContainerSupport {

    private final List<AncientPoetryResultTask> tasks = new LinkedList<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        AncientPoetryResultTasks annotation = getClass().getAnnotation(AncientPoetryResultTasks.class);
        Objects.requireNonNull(annotation, "@AncientPoetryResultTasks is required");

        for (Class<? extends AncientPoetryResultTask> beanClass : annotation.value()) {
            AncientPoetryResultTask task = null;
            Map<String, ? extends AncientPoetryResultTask> beans = applicationContext.getBeansOfType(beanClass);
            for (AncientPoetryResultTask bean : beans.values()) {
                if (bean.getClass() == beanClass) {
                    task = bean;
                    break;
                }
            }
            if (task == null) {
                throw new IllegalStateException("No bean " + beanClass.getName() + " defined");
            }
            tasks.add(task);
        }
    }

    public AncientPoetryProcessContext process(AncientPoetryProcessContext context) {
        if (context == null){
            return new AncientPoetryProcessContext().errorResponse();
        }
        for (AncientPoetryResultTask task : tasks) {
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
