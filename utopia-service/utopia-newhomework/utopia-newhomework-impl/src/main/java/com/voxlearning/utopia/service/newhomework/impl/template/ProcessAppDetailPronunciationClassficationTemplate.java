package com.voxlearning.utopia.service.newhomework.impl.template;

import com.voxlearning.utopia.service.newhomework.api.constant.NatureSpellingType;

import javax.inject.Named;

@Named
public class ProcessAppDetailPronunciationClassficationTemplate extends ProcessAppDetailFunnySpellingTemplate {
    @Override
    public NatureSpellingType getNatureSpellingType() {
        return NatureSpellingType.PRONUNCIATION_CLASSIFICATION;
    }
}
