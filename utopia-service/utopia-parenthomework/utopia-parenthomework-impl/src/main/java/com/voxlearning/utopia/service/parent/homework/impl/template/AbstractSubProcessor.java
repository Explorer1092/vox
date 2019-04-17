package com.voxlearning.utopia.service.parent.homework.impl.template;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.parent.homework.impl.template.base.Processors;
import com.voxlearning.utopia.service.parent.homework.impl.annotation.SubType;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public abstract class AbstractSubProcessor extends SpringContainerSupport {
    protected Map<String, Consumer> subProcessor;
    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        subProcessor = new LinkedHashMap<>();
        Processors annotation = getClass().getAnnotation(Processors.class);
        for (Class<? extends Consumer> beanClass : annotation.value()) {
            Consumer loader = applicationContext.getBean(beanClass);
            for (ObjectiveConfigType type : loader.getClass().getAnnotation(SubType.class).value()) {
                subProcessor.put(type.name(), loader) ;
            }
        }
    }
}
