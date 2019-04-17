package com.voxlearning.utopia.service.campaign.client;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.campaign.api.TeacherActivityService;
import com.voxlearning.utopia.service.campaign.api.entity.TeacherVocationLottery;
import lombok.Getter;

import java.util.Map;

public class TeacherLotteryServiceClient {

    @Getter
    @ImportService(interfaceClass = TeacherActivityService.class)
    private TeacherActivityService remoteReference;

    public TeacherVocationLottery loadTeacherVocationLottery(Long teacherId) {
        return remoteReference.loadTeacherVocationLottery(teacherId);
    }

    public MapMessage updateTeacherVocationLottery(TeacherVocationLottery lotteryRecord) {
        return remoteReference.updateTeacherVocationLottery(lotteryRecord);
    }

    public MapMessage incTVLRecordFields(Long teacherId, Map<String,Object> delta) {
        return remoteReference.incTVLRecordFields(teacherId,delta);
    }

}
