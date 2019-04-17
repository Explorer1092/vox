package com.voxlearning.utopia.service.business.impl.processor.teachingdiagnosis;


import com.voxlearning.utopia.business.api.context.teachingdiagnosis.PreQuestionResultContext;
import com.voxlearning.utopia.service.business.impl.processor.AbstractProcessor;
import com.voxlearning.utopia.service.business.impl.processor.annotation.ExecuteTaskSupport;

import javax.inject.Named;

/**
 * @author songtao
 * @since 2018/02/08
 */
@Named
@ExecuteTaskSupport({
        PTDQR_SaveQuestionResult.class,
        PTDQR_LastQuestion.class,
        PTDQR_AddIntegral.class,
        PTDQR_CalculateCource.class,
        PTDQR_SaveTaskCourseResult.class
})
public class PreTeachingDiagnosisQuestionResultDataProcessor extends AbstractProcessor<PreQuestionResultContext> {
}
