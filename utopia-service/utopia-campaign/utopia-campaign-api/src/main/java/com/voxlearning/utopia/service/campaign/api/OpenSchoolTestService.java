package com.voxlearning.utopia.service.campaign.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.campaign.api.mapper.OpenSchoolTest;

import java.util.concurrent.TimeUnit;

@ServiceRetries
@ServiceVersion(version = "20190220")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
public interface OpenSchoolTestService {

    MapMessage index(Long teacherId);

    OpenSchoolTest loadByTeacher(Long teacherId);

    void save(OpenSchoolTest openSchoolTest);

    void addGroupId(Long teacherId, Long groupId);

    Long incrStudentSubmit(Long teacherId);

    Long getStudentSubmit(Long teacherId);

    @Deprecated
    Long setStudentSubmit(Long teacherId, Long count);

    void sendTeacherReward(Long teacherId, Integer num, String comment, String uniqueKeyPrefix);

    Long getMainTeacher(Long teacherId);

    void teacherShare(Long teacherId);

}
