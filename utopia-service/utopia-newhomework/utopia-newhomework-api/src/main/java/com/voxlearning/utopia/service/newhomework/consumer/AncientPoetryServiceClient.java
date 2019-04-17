package com.voxlearning.utopia.service.newhomework.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.newhomework.api.context.AncientPoetryProcessContext;
import com.voxlearning.utopia.service.newhomework.api.entity.poetry.AncientPoetryActivity;
import com.voxlearning.utopia.service.newhomework.api.entity.poetry.AncientPoetryMission;
import com.voxlearning.utopia.service.newhomework.api.service.AncientPoetryService;
import lombok.Getter;

/**
 * @author majianxin
 * @version V1.0
 * @date 2019/2/20
 */
public class AncientPoetryServiceClient implements AncientPoetryService {

    @Getter
    @ImportService(interfaceClass = AncientPoetryService.class)
    private AncientPoetryService remoteReference;

    @Override
    public MapMessage registerPoetryActivity(Long teacherId, String activityId, Long clazzGroupId) {
        return remoteReference.registerPoetryActivity(teacherId, activityId, clazzGroupId);
    }

    @Override
    public MapMessage processResult(AncientPoetryProcessContext context) {
        return remoteReference.processResult(context);
    }

    @Override
    public MapMessage viewActivity(Long teacherId) {
        return remoteReference.viewActivity(teacherId);
    }

    @Override
    public MapMessage updateActivityStatus(String activityId, boolean status) {
        return remoteReference.updateActivityStatus(activityId, status);
    }

    @Override
    public MapMessage upsertAncientPoetryActivity(AncientPoetryActivity ancientPoetryActivity) {
        return remoteReference.upsertAncientPoetryActivity(ancientPoetryActivity);
    }

    @Override
    public MapMessage upsertAncientPoetryMission(AncientPoetryMission ancientPoetryMission) {
        return remoteReference.upsertAncientPoetryMission(ancientPoetryMission);
    }

    @Override
    public MapMessage insertsAncientPoetryMission(String jsonStr) {
        return remoteReference.insertsAncientPoetryMission(jsonStr);
    }

    @Override
    public void generateGlobalRankBySchoolIdAndClazzLevel(Long schoolId, Integer clazzLevel) {
        remoteReference.generateGlobalRankBySchoolIdAndClazzLevel(schoolId, clazzLevel);
    }

    @Override
    public void generateGlobalRankByRegionIdAndClazzLevel(Integer regionId, Integer clazzLevel) {
        remoteReference.generateGlobalRankByRegionIdAndClazzLevel(regionId, clazzLevel);
    }
}
