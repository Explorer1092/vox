package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.check;

import java.lang.annotation.*;

/**
 * @author Ruib
 * @version 0.1
 * @since 2016/1/25
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckHomeworkTasks {
    Class<? extends CheckHomeworkTask>[] value();
}
