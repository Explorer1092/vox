package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.correction;

import java.lang.annotation.*;

/**
 * 批改作业
 *
 * @author xuesong.zhang
 * @since 2016-01-21
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CorrectHomeworkTasks {
    Class<? extends CorrectHomeworkTask>[] value();
}
