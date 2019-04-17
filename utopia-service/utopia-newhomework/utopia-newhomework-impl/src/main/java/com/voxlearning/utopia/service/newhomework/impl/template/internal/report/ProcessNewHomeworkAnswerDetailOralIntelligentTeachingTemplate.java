package com.voxlearning.utopia.service.newhomework.impl.template.internal.report;

import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;

@Named
public class ProcessNewHomeworkAnswerDetailOralIntelligentTeachingTemplate extends ProcessNewHomeworkAnswerDetailOralPracticeTemplate {

    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.ORAL_INTELLIGENT_TEACHING;
    }
}
