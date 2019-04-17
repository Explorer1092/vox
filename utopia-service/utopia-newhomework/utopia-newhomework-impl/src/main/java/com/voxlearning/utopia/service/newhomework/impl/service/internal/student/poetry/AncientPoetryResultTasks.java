package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.poetry;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AncientPoetryResultTasks {
    Class<? extends AncientPoetryResultTask>[] value();
}
