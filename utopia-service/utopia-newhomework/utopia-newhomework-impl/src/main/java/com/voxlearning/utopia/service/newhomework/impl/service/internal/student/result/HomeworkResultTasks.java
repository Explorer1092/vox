package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result;

import java.lang.annotation.*;

/**
 * @author Ruib
 * @version 0.1
 * @since 2016/1/13
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface HomeworkResultTasks {
    Class<? extends HomeworkResultTask>[] value();
}
