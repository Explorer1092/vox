package com.voxlearning.utopia.service.newhomework.impl.template;


import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

@Named
public class ProcessLiveCastHomeworkAnswerDetailFactory extends SpringContainerSupport {

    private Map<ObjectiveConfigType, ProcessLiveCastHomeworkAnswerDetailTemplate> templates = null;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        this.templates = new HashMap<>();
        Map<String, ProcessLiveCastHomeworkAnswerDetailTemplate> beans = applicationContext.getBeansOfType(ProcessLiveCastHomeworkAnswerDetailTemplate.class);
        for (ProcessLiveCastHomeworkAnswerDetailTemplate bean : beans.values()) {
            this.templates.put(bean.getObjectiveConfigType(), bean);
        }
    }

    public ProcessLiveCastHomeworkAnswerDetailTemplate getTemplate(ObjectiveConfigType objectiveConfigType) {
        if (objectiveConfigType == null) {
            objectiveConfigType = ObjectiveConfigType.COMMON;
        }
        if (!this.templates.containsKey(objectiveConfigType)) {
            objectiveConfigType = ObjectiveConfigType.COMMON;
        }
        return this.templates.get(objectiveConfigType);
    }
}
