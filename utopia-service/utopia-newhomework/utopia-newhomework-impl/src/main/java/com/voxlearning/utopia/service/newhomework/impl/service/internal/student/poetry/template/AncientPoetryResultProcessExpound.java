package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.poetry.template;

import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.newhomework.api.constant.ModelType;
import com.voxlearning.utopia.service.newhomework.api.context.AncientPoetryProcessContext;
import com.voxlearning.utopia.service.newhomework.api.entity.poetry.AncientPoetryMissionResult;

import javax.inject.Named;
import java.util.Collections;

@Named
public class AncientPoetryResultProcessExpound extends AncientPoetryResultProcessTemplate {

    @Override
    public ModelType getProcessResultModel() {
        return ModelType.EXPOUND;
    }

    @Override
    public void processResult(AncientPoetryProcessContext context) {
        AncientPoetryMissionResult missionResult = context.getMissionResult();
        if (missionResult != null) {
            context.errorResponse();
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_DUPLICATE_OPERATION);
            return;
        }

        missionResult = new AncientPoetryMissionResult();
        missionResult.setId(context.getMissionResultId());
        missionResult.setStudentId(context.getStudentId());
        missionResult.setActivityId(context.getActivityId());
        missionResult.setMissionId(context.getMissionId());
        missionResult.setModelFinishAt(Collections.singletonList(context.getCurrentDate()));
        context.setMissionResult(missionResult);
    }
}
