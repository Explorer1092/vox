package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.poetry.template;

import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.newhomework.api.constant.ModelType;
import com.voxlearning.utopia.service.newhomework.api.context.AncientPoetryProcessContext;
import com.voxlearning.utopia.service.newhomework.api.entity.poetry.AncientPoetryMissionResult;

import javax.inject.Named;
import java.util.Date;
import java.util.List;

@Named
public class AncientPoetryResultProcessApprectiate extends AncientPoetryResultProcessTemplate {

    @Override
    public ModelType getProcessResultModel() {
        return ModelType.APPRECIATE;
    }

    @Override
    public void processResult(AncientPoetryProcessContext context) {
        AncientPoetryMissionResult missionResult = context.getMissionResult();
        if (missionResult == null) {
            context.errorResponse();
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_ANCIENT_POETRY_MISSION_RESULT_NOT_EXIST);
            return;
        }
        List<Date> modelFinishAt = missionResult.getModelFinishAt();
        if (modelFinishAt.size() == 1) {
            modelFinishAt.add(context.getCurrentDate());
        }
        missionResult.setModelFinishAt(modelFinishAt);
    }
}
