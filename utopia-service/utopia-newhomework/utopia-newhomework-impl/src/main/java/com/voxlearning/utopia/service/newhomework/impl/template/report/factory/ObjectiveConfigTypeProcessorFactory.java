package com.voxlearning.utopia.service.newhomework.impl.template.report.factory;


import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.impl.template.report.template.ObjectiveConfigTypeProcessorTemplate;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

@Named
public class ObjectiveConfigTypeProcessorFactory extends SpringContainerSupport {

    private Map<ObjectiveConfigType, ObjectiveConfigTypeProcessorTemplate> templates = null;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        this.templates = new HashMap<>();
        Map<String, ObjectiveConfigTypeProcessorTemplate> beans = applicationContext.getBeansOfType(ObjectiveConfigTypeProcessorTemplate.class);
        for (ObjectiveConfigTypeProcessorTemplate bean : beans.values()) {
            this.templates.put(bean.getObjectiveConfigType(), bean);
        }
    }

    public ObjectiveConfigTypeProcessorTemplate getTemplate(ObjectiveConfigType objectiveConfigType) {
        if (objectiveConfigType == null) {
            objectiveConfigType = ObjectiveConfigType.COMMON;
        }
        return this.templates.get(!this.templates.containsKey(objectiveConfigType) ? ObjectiveConfigType.COMMON : objectiveConfigType);
    }


}
