package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.finish;

import java.lang.annotation.*;

/**
 * @author Ruib
 * @version 0.1
 * @since 2016/1/14
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface FinishHomeworkTasks {
    Class<? extends FinishHomeworkTask>[] value();
}
