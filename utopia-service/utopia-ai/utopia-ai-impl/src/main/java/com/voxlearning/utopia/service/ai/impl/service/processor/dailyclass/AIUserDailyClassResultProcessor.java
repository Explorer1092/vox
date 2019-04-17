package com.voxlearning.utopia.service.ai.impl.service.processor.dailyclass;

import com.voxlearning.utopia.service.ai.context.AIUserDailyClassContext;
import com.voxlearning.utopia.service.ai.impl.service.processor.AITask;
import com.voxlearning.utopia.service.ai.impl.service.processor.AbstractAIProcessor;

import javax.inject.Named;

@AITask(value = {
        ADC_LoadUserProduct.class,
        ADC_LoadUnit.class,
        ADC_LoadCurrentUnit.class,
        ADC_ClassInfo.class
})
@Named
public class AIUserDailyClassResultProcessor extends AbstractAIProcessor<AIUserDailyClassContext> {
}
