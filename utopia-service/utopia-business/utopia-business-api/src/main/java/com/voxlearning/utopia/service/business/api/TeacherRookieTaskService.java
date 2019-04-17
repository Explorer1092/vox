package com.voxlearning.utopia.service.business.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.entity.task.TeacherRookieTask;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20181026")
@ServiceTimeout(timeout = 20, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface TeacherRookieTaskService {

    Boolean allowRookieTask(Long teacherId);

    MapMessage receiveRookieTask(Long teacherId);

    TeacherRookieTask loadRookieTask(Long teacherId);

    MapMessage loadHomePagePop(Long teacherId);

    MapMessage loadCenterDetailPop(Long teacherId, String flag);

    MapMessage updateProgress(Long teacherId, TeacherRookieTask.RookieTaskTrigger rookieTaskTrigger, Map<String, Object> exts);

    Boolean oldRookieFinished(TeacherDetail teacherDetail);

    Boolean oldRookieFinished(Long teacher);

    Boolean rookieFinished(Long teacher);
}
