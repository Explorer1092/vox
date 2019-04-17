package com.voxlearning.utopia.service.parent.homework.impl.annotation;

import java.lang.annotation.*;

/**
 * 支持业务类型
 *
 * @author Wenlong Meng
 * @since Feb 18, 2019
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SupportType {
    String bizType(); // 业务类型
    String op();//操作
}
