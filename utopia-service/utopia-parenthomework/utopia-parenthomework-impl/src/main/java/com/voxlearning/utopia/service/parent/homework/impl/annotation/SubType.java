package com.voxlearning.utopia.service.parent.homework.impl.annotation;

import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SubType {
    ObjectiveConfigType[] value(); // 业务类型
}
