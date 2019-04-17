package com.voxlearning.utopia.service.newhomework.impl.template.vacation;

import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;

@Named
public class VacationProcessNewHomeworkAnswerDetailNatureSpellingTemplate extends VacationProcessNewHomeworkAnswerDetailBasicAppTemplate {
    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.NATURAL_SPELLING;
    }
}
