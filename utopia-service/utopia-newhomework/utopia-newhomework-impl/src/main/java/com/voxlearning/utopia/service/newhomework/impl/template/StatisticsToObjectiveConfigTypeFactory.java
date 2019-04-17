package com.voxlearning.utopia.service.newhomework.impl.template;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

@Named
public class StatisticsToObjectiveConfigTypeFactory extends SpringContainerSupport {

    private Map<ObjectiveConfigType, StatisticsToObjectiveConfigTypeTemple> templates = null;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        this.templates = new HashMap<>();
        Map<String, StatisticsToObjectiveConfigTypeTemple> beans = applicationContext.getBeansOfType(StatisticsToObjectiveConfigTypeTemple.class);
        for (StatisticsToObjectiveConfigTypeTemple bean : beans.values()) {
            this.templates.put(bean.getObjectiveConfigType(), bean);
        }
    }

    public StatisticsToObjectiveConfigTypeTemple getTemplate(ObjectiveConfigType objectiveConfigType) {
        if (objectiveConfigType == null) {
            objectiveConfigType = ObjectiveConfigType.COMMON;
        }
        if (!this.templates.containsKey(objectiveConfigType)) {
            objectiveConfigType = ObjectiveConfigType.COMMON;
        }
        return this.templates.get(objectiveConfigType);
    }

}
