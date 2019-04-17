package com.voxlearning.utopia.service.newhomework.impl.service.processor.doData;

import com.voxlearning.utopia.service.newhomework.api.constant.AssignmentConfigType;
import com.voxlearning.utopia.service.newhomework.api.entity.bonus.AbilityExamBasic;

import javax.inject.Named;

/**
 * @author lei.liu
 * @version 18-11-1
 */
@Named
public class AbilityExamDo_InitData extends AbstractAbilityExamDoChainProcessor {

    @Override
    protected void doProcess(AbilityExamDoContext context) {
        AbilityExamBasic ad = abilityExamBasicDao.load(String.valueOf(context.getStudentId()));
        if (ad == null) {
            context.errorResponse("试题不存在");
            context.setErrorCode("900");
            context.setTerminateTask(true);
            return;
        }
        context.setType(AssignmentConfigType.INTELLIGENCE_EXAM);
    }

}
