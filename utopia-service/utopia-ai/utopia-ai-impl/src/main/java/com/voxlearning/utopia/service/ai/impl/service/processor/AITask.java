package com.voxlearning.utopia.service.ai.impl.service.processor;

import java.lang.annotation.*;


@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AITask {
    Class<? extends IAITask>[] value();
}