package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.finish.vacation;

import java.lang.annotation.*;

/**
 * @author guoqiang.li
 * @since 2016/12/7
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface FinishVacationHomeworkTasks {
    Class<? extends FinishVacationHomeworkTask>[] value();
}
