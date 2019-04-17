package com.voxlearning.utopia.service.newhomework.impl.service.processor.doData;

import com.voxlearning.utopia.service.newhomework.api.constant.AssignmentConfigType;
import com.voxlearning.utopia.service.newhomework.api.context.bonus.AbilityExamConstant;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lei.liu
 * @version 18-11-1
 */
@Named
public class AbilityExamDo_DoData extends AbstractAbilityExamDoChainProcessor {

    @Override
    protected void doProcess(AbilityExamDoContext context) {
        Map<String, Object> dataMap = new HashMap<>();

        dataMap.put("hid", context.getStudentId());
        dataMap.put("homeworkId", context.getStudentId());
        dataMap.put("userId", context.getStudentId());
        dataMap.put("uid", context.getStudentId());

        dataMap.put("subject", "");
        dataMap.put("questionUrl", currentBaseUrl() + "/bonus/ability/question.vpage");
        dataMap.put("completedUrl", currentBaseUrl() + "/bonus/ability/answer.vpage");
        dataMap.put("processResultUrl", currentBaseUrl() + "/bonus/ability/processresult.vpage");

        dataMap.put("sendDetailLog", false);

        dataMap.put("objectiveConfigType", AssignmentConfigType.INTELLIGENCE_EXAM.getType());
        dataMap.put("objectiveConfigTypeName", AssignmentConfigType.INTELLIGENCE_EXAM.getName());
        dataMap.put("learningType", AbilityExamConstant.ABILITY_EXAM_STUDY_TYPE);

        context.setVars(dataMap);
    }

}
