package com.voxlearning.utopia.service.business.impl.processor.teachingdiagnosis;

import com.voxlearning.utopia.business.api.context.teachingdiagnosis.PreQuestionResultContext;
import com.voxlearning.utopia.service.business.impl.processor.ExecuteTask;
import com.voxlearning.utopia.service.business.impl.support.AbstractTeachingDiagnosisSupport;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.user.consumer.support.IntegralHistoryBuilderFactory;

import javax.inject.Named;

/**
 * @author songtao
 * @since 2018/02/08
 */
@Named
public class PTDQR_AddIntegral extends AbstractTeachingDiagnosisSupport implements ExecuteTask<PreQuestionResultContext> {

    @Override
    public void execute(PreQuestionResultContext context) {
        if (context.getConfig().getBonus() == null || context.getConfig().getBonus() <= 0 || Boolean.FALSE.equals(context.getMaster())) {
            return;
        }
        IntegralHistory history = IntegralHistoryBuilderFactory.newBuilder(context.getStudent().getId(), IntegralType.PRIMARY_STUDENT_TEACHING_DIAGNOSIS_REWARD)
                .withIntegral(context.getConfig().getBonus())
                .withComment("完成教学诊断挑战")
                .build();
        try {
            userIntegralService.changeIntegral(context.getStudent(), history);
        } catch (Exception e) {
            logger.error("PTDQR_AddIntegral error. history:{}", history, e);
        }
    }
}
