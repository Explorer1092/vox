package com.voxlearning.utopia.service.mizar.api.constants.cjlschool;

import java.lang.annotation.*;

/**
 * 用于注解陈经纶返回data的字段映射
 * 如果没有标注则表示不与任何字段映射
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CJLField {
    /**
     * 对方字段名称
     */
    String field() default "";

}
