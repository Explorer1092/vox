package com.voxlearning.utopia.service.newhomework.impl.template.vacation;

import com.voxlearning.utopia.service.newhomework.api.constant.NatureSpellingType;

import javax.inject.Named;

@Named
public class VacationProcessAppDetailPronunciationClassficationTemplate extends VacationProcessAppDetailFunnySpellingTemplate {
    @Override
    public NatureSpellingType getNatureSpellingType() {
        return NatureSpellingType.PRONUNCIATION_CLASSIFICATION;
    }
}
