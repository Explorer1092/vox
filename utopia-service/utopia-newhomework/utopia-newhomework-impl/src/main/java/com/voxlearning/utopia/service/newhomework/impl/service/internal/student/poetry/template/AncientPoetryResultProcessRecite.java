package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.poetry.template;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.newhomework.api.constant.ModelType;
import com.voxlearning.utopia.service.newhomework.api.context.AncientPoetryProcessContext;
import com.voxlearning.utopia.service.newhomework.api.entity.poetry.AncientPoetryMission;
import com.voxlearning.utopia.service.newhomework.api.entity.poetry.AncientPoetryMissionResult;

import javax.inject.Named;
import java.util.Date;
import java.util.List;

@Named
public class AncientPoetryResultProcessRecite extends AncientPoetryResultProcessTemplate {

    @Override
    public ModelType getProcessResultModel() {
        return ModelType.RECITE;
    }

    @Override
    public void processResult(AncientPoetryProcessContext context) {
        AncientPoetryMissionResult missionResult = context.getMissionResult();
        double addStar;
        if (context.isParentMission()) {
            if (missionResult != null && missionResult.isFinished()) {
                context.errorResponse("家长已助力");
                context.setErrorCode(ErrorCodeConstants.ERROR_CODE_DUPLICATE_OPERATION);
                return;
            }
            missionResult = new AncientPoetryMissionResult();
            missionResult.setId(context.getMissionResultId());
            missionResult.setActivityId(context.getActivityId());
            missionResult.setStudentId(context.getStudentId());
            missionResult.setMissionId(context.getMissionId());
            missionResult.setStudentAudioUrls(context.getStudentAudioUrls());
            missionResult.setParentAudioUrls(context.getParentAudioUrls());
            missionResult.setParentId(context.getParentId());
            missionResult.setFinishAt(context.getCurrentDate());
            missionResult.setStar(10D);
            addStar = 10D;
            context.setMissionResult(missionResult);
        } else {
            AncientPoetryMission mission = context.getMission();
            AncientPoetryMission.Model model = mission.getModels().get(ModelType.RECITE);
            missionResult.setStudentAudioUrls(context.getStudentAudioUrls());
            int star = model.getReciteContent().getSentenceList().size();
            List<Date> modelFinishAt = missionResult.getModelFinishAt();
            // 重复提交,直接终止任务链
            if (modelFinishAt.size() == 2) {
                addStar = SafeConverter.toDouble(star);
                modelFinishAt.add(context.getCurrentDate());
                missionResult.setModelFinishAt(modelFinishAt);
                missionResult.setStar(addStar);
            } else {
                context.errorResponse();
                context.setErrorCode(ErrorCodeConstants.ERROR_CODE_DUPLICATE_OPERATION);
                return;
            }
        }
        if (addStar != 0) {
            context.setAddStar(addStar);
        }
    }
}
