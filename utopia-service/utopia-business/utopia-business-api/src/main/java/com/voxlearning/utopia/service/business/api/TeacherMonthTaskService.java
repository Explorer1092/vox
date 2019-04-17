package com.voxlearning.utopia.service.business.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.entity.task.TeacherMonthTask;

import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20181026")
@ServiceTimeout(timeout = 20, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface TeacherMonthTaskService {

    Boolean allowMonthTask(Long teacherId);

    MapMessage receiveMonthTask(Long teacherId);

    TeacherMonthTask loadMonthTask(Long teacherId);

    MapMessage loadMonthTaskMsg(Long teacherId);

    MapMessage updateProgress(Long teacherId, Long groupId, TeacherMonthTask.Homework homework);
}
