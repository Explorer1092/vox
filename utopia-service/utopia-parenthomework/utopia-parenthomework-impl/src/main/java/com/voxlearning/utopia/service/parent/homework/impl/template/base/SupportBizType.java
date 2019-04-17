package com.voxlearning.utopia.service.parent.homework.impl.template.base;

import com.voxlearning.utopia.service.parent.homework.api.model.BizType;

import java.lang.annotation.*;

/**
 * 支持业务类型
 *
 * @author Wenlong Meng
 * @since Feb 20, 2019
 */
@Documented
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SupportBizType {
    BizType value();//操作
}
