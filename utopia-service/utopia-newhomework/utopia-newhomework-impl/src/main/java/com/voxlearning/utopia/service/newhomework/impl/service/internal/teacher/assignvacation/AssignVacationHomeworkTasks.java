package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assignvacation;

import java.lang.annotation.*;

/**
 * Created by tanguohong on 2016/11/29.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AssignVacationHomeworkTasks {
    Class<? extends AssignVacationHomeworkTask>[] value();
}
