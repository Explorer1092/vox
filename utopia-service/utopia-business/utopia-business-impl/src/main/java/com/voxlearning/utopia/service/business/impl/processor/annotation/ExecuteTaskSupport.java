package com.voxlearning.utopia.service.business.impl.processor.annotation;


import com.voxlearning.utopia.service.business.impl.processor.ExecuteTask;

import java.lang.annotation.*;

/**
 * @author peng.zhang.a
 * @since 16-10-12
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExecuteTaskSupport {
    Class<? extends ExecuteTask>[] value();
}
