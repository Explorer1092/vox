package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.outside;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface OutsideReadingResultTasks {
    Class<? extends OutsideReadingResultTask>[] value();
}
