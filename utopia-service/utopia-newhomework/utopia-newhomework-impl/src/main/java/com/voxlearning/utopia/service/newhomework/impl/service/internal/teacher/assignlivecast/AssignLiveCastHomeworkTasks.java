package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assignlivecast;

import java.lang.annotation.*;

/**
 * @author xuesong.zhang
 * @since 2016/10/18
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AssignLiveCastHomeworkTasks {
    Class<? extends AssignLiveCastHomeworkTask>[] value();
}
