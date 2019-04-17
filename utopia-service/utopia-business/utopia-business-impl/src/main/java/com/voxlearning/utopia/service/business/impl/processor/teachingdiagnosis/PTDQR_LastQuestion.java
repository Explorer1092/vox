package com.voxlearning.utopia.service.business.impl.processor.teachingdiagnosis;

import com.voxlearning.utopia.business.api.context.teachingdiagnosis.PreQuestionResultContext;
import com.voxlearning.utopia.service.business.impl.processor.ExecuteTask;
import com.voxlearning.utopia.service.business.impl.support.AbstractTeachingDiagnosisSupport;

import javax.inject.Named;

/**
 * @author songtao
 * @since 2018/02/08
 */
@Named
public class PTDQR_LastQuestion  extends AbstractTeachingDiagnosisSupport implements ExecuteTask<PreQuestionResultContext> {

    @Override
    public void execute(PreQuestionResultContext context) {
        if (!context.getLast()) {
            context.terminateTask();
        }
    }
}
