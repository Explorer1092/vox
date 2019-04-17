package com.voxlearning.utopia.service.mizar.api.mapper.talkfun;

import java.lang.annotation.*;

/**
 * 用于注解欢拓返回data的字段映射
 * 如果没有标注则表示不与欢拓的任何字段映射
 * Created by Yuechen.Wang on 2017/1/10.
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TkField {
    /**
     * 欢拓方返回字段名称
     */
    String value() default "";

    /**
     * 是否为时间戳字段
     */
    boolean timestamp() default false;
}
