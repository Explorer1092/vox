package com.voxlearning.utopia.service.newhomework.impl.template.report.factory;



import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.impl.template.report.template.AppObjectiveConfigTypeProcessorTemplate;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;
@Named
public class AppObjectiveConfigTypeProcessorFactory extends SpringContainerSupport {

    private Map<ObjectiveConfigType, AppObjectiveConfigTypeProcessorTemplate> templates = null;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        this.templates = new HashMap<>();
        Map<String, AppObjectiveConfigTypeProcessorTemplate> beans = applicationContext.getBeansOfType(AppObjectiveConfigTypeProcessorTemplate.class);
        for (AppObjectiveConfigTypeProcessorTemplate bean : beans.values()) {
            this.templates.put(bean.getObjectiveConfigType(), bean);
        }
    }

    public AppObjectiveConfigTypeProcessorTemplate getTemplate(ObjectiveConfigType objectiveConfigType) {
        if (objectiveConfigType == null) {
            objectiveConfigType = ObjectiveConfigType.COMMON;
        }
        return this.templates.get(!this.templates.containsKey(objectiveConfigType) ? ObjectiveConfigType.COMMON : objectiveConfigType);
    }


}
