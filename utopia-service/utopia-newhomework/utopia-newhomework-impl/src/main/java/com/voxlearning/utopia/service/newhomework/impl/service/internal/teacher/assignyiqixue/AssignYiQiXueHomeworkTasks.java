package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assignyiqixue;

import java.lang.annotation.*;

/**
 * @author xuesong.zhang
 * @since 2016/12/16
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AssignYiQiXueHomeworkTasks {
    Class<? extends AssignYiQiXueHomeworkTask>[] value();
}
