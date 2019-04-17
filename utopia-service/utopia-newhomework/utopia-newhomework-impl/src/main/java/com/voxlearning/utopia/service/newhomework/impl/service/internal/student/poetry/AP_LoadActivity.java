package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.poetry;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.newhomework.api.context.AncientPoetryProcessContext;
import com.voxlearning.utopia.service.newhomework.api.entity.poetry.AncientPoetryActivity;
import com.voxlearning.utopia.service.newhomework.api.entity.poetry.AncientPoetryMission;
import com.voxlearning.utopia.service.newhomework.api.entity.poetry.AncientPoetryMissionResult;
import com.voxlearning.utopia.service.newhomework.impl.dao.poetry.AncientPoetryActivityDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.poetry.AncientPoetryMissionDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.poetry.AncientPoetryMissionResultDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;

/**
 * @author majianxin
 */
@Named
public class AP_LoadActivity extends SpringContainerSupport implements AncientPoetryResultTask {

    @Inject private AncientPoetryActivityDao ancientPoetryActivityDao;
    @Inject private AncientPoetryMissionDao missionDao;
    @Inject private AncientPoetryMissionResultDao missionResultDao;

    @Override
    public void execute(AncientPoetryProcessContext context) {
        AncientPoetryActivity poetryActivity = ancientPoetryActivityDao.load(context.getActivityId());
        if (poetryActivity == null) {
            logger.error("AncientPoetryActivity {} not found", context.getActivityId());
            context.errorResponse();
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_ANCIENT_POETRY_ACTIVITY_NOT_EXIST);
            return;
        }
        context.setCurrentDate(new Date());
        context.setPoetryActivity(poetryActivity);
        AncientPoetryMission poetryMission = missionDao.load(context.getMissionId());
        context.setMission(poetryMission);
        String missionResultId = AncientPoetryMissionResult.generateId(context.getActivityId(), context.getMissionId(), context.getStudentId(), context.isParentMission());
        context.setMissionResultId(missionResultId);
        AncientPoetryMissionResult missionResult = missionResultDao.load(missionResultId);

        //如果已经完成四个模块了，不允许重复提交
        if (!context.isCorrect() && !context.isParentMission() && missionResult != null && missionResult.isFinished()) {
            context.errorResponse();
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_ANCIENT_POETRY_MISSION_IS_FINISHED);
            return;
        }
        context.setMissionResult(missionResult);
    }
}
