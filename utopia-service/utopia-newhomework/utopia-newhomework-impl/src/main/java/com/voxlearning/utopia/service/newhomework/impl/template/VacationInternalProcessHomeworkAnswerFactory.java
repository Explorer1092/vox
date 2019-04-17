package com.voxlearning.utopia.service.newhomework.impl.template;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

@Named
public class VacationInternalProcessHomeworkAnswerFactory extends SpringContainerSupport {

    private Map<ObjectiveConfigType, VacationInternalProcessHomeworkAnswerTemple> templates = null;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        this.templates = new HashMap<>();
        Map<String, VacationInternalProcessHomeworkAnswerTemple> beans = applicationContext.getBeansOfType(VacationInternalProcessHomeworkAnswerTemple.class);
        for (VacationInternalProcessHomeworkAnswerTemple bean : beans.values()) {
            this.templates.put(bean.getObjectiveConfigType(), bean);
        }
    }

    public VacationInternalProcessHomeworkAnswerTemple getTemplate(ObjectiveConfigType objectiveConfigType) {
        if (objectiveConfigType == null) {
            return null;
        }
        VacationInternalProcessHomeworkAnswerTemple temple = this.templates.get(objectiveConfigType);
        return temple != null ? temple : this.templates.get(ObjectiveConfigType.COMMON);
    }
}
