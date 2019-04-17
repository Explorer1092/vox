package com.voxlearning.utopia.service.afenti.impl.service.activity;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.annotations.AfentiActivityTypeIdentification;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiActivityType;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ruib
 * @since 2016/8/15
 */
@Named
public class AfentiActivityDataAssemblerManager extends SpringContainerSupport {
    private final Map<AfentiActivityType, AfentiActivityDataAssembler> assemblers;

    public AfentiActivityDataAssemblerManager() {
        this.assemblers = new HashMap<>();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        // 获取所有assembler
        Map<String, AfentiActivityDataAssembler> beans = applicationContext.getBeansOfType(AfentiActivityDataAssembler.class);
        for (AfentiActivityDataAssembler assembler : beans.values()) {
            AfentiActivityTypeIdentification annotation = assembler.getClass().getAnnotation(AfentiActivityTypeIdentification.class);
            if (null == annotation) {
                throw new IllegalStateException("No @AfentiActivityTypeIdentification presented on "
                        + assembler.getClass().getName());
            }
            AfentiActivityType type = annotation.value();
            if (assemblers.containsKey(type)) {
                throw new IllegalStateException("Duplicated assembler type " + type);
            }
            assemblers.put(type, assembler);
        }
    }

    public AfentiActivityDataAssembler getAssembler(AfentiActivityType type) {
        if (null == type) return null;
        return this.assemblers.get(type);
    }
}
