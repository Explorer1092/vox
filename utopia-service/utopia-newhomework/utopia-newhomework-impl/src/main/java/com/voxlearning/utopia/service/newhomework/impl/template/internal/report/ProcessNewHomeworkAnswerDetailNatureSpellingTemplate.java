package com.voxlearning.utopia.service.newhomework.impl.template.internal.report;

import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;

@Named
public class ProcessNewHomeworkAnswerDetailNatureSpellingTemplate extends ProcessNewHomeworkAnswerDetailBasicAppTemplate {

    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.NATURAL_SPELLING;
    }
}
