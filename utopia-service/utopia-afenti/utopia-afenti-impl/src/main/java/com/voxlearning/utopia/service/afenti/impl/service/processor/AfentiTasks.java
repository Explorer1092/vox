package com.voxlearning.utopia.service.afenti.impl.service.processor;

import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import java.lang.annotation.*;

/**
 * @author Ruib
 * @since 2016/7/11
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AfentiTasks {
    Class<? extends IAfentiTask>[] value();
}
