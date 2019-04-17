package com.voxlearning.utopia.service.parent.homework.impl.template.base;

import java.lang.annotation.*;
import java.util.function.Consumer;

/**
 * 处理流程
 *
 * @author Wenlong Meng
 * @version 20181111
 * @date 2018-11-13
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Processors {

    /**
     * 流程列表
     *
     * @return
     */
    Class<? extends Consumer>[] value();
}
