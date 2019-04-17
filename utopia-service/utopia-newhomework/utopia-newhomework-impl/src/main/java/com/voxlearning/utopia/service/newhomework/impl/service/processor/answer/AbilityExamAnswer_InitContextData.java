package com.voxlearning.utopia.service.newhomework.impl.service.processor.answer;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.newhomework.api.constant.AssignmentConfigType;
import com.voxlearning.utopia.service.newhomework.api.context.bonus.AbilityExamAnswerContext;
import com.voxlearning.utopia.service.newhomework.api.entity.bonus.AbilityExamBasic;

import javax.inject.Named;
import java.util.List;

/**
 * 初始化+检查数据
 *
 * @author lei.liu
 * @version 18-11-1
 */
@Named
public class AbilityExamAnswer_InitContextData extends AbstractAbilityExamAnswerChainProcessor {
    @Override
    protected void doProcess(AbilityExamAnswerContext context) {
        AbilityExamBasic basic = abilityExamBasicDao.load(String.valueOf(context.getUserId()));
        if (basic == null) {
            context.errorResponse("任务不存在");
            context.setErrorCode("900");
            context.setTerminateTask(true);
            return;
        }
        context.setAbilityExamBasic(basic);

        if (basic.fetchFinished()) {
            context.errorResponse("任务已经完成");
            context.setErrorCode("900");
            context.setTerminateTask(true);
            return;
        }

        List<String> qIds = basic.getQuestionIds();
        String doQid = context.getAnswer().getQuestionId();

        if (CollectionUtils.isNotEmpty(qIds) && StringUtils.isNotBlank(doQid) && !qIds.contains(doQid)) {
            context.errorResponse("题目错误");
            context.setErrorCode("900");
            context.setTerminateTask(true);
            return;
        }

        context.setType(AssignmentConfigType.INTELLIGENCE_EXAM);
    }
}
