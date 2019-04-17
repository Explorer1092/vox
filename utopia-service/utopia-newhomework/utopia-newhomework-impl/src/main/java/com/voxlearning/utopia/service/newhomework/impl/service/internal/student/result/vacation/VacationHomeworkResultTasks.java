package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result.vacation;

import java.lang.annotation.*;

/**
 * @author guoqiang.li
 * @since 2016/12/7
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface VacationHomeworkResultTasks {
    Class<? extends VacationHomeworkResultTask>[] value();
}
