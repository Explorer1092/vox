package com.voxlearning.utopia.service.newhomework.impl.template.report.template;

import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;

@Named
public class AppOralIntelligentTeachingObjectiveConfigTypeProcessorTemplate extends AppOralPraticeObjectiveConfigTypeProcessorTemplate {
    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.ORAL_INTELLIGENT_TEACHING;
    }
}
