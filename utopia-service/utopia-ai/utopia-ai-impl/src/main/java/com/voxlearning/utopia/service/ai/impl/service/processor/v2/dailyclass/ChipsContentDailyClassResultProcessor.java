package com.voxlearning.utopia.service.ai.impl.service.processor.v2.dailyclass;

import com.voxlearning.utopia.service.ai.impl.context.ChipsContentDailyClassContext;
import com.voxlearning.utopia.service.ai.impl.service.processor.AITask;
import com.voxlearning.utopia.service.ai.impl.service.processor.AbstractAIProcessor;

import javax.inject.Named;

@AITask(value = {
        CCDCR_LoadUserProduct.class,
        CCDCR_LoadUserStudyInfo.class,
        CCDCR_CurrentUnit.class,
        CCDCR_Unit.class,
        CCDCR_CourseInfo.class,
        CCDCR_LoadDrawingPopData.class,
        CCDCR_BottomBe_1.class,
        CCDCR_BottomBe_2.class
})
@Named
public class ChipsContentDailyClassResultProcessor extends AbstractAIProcessor<ChipsContentDailyClassContext> {
}
