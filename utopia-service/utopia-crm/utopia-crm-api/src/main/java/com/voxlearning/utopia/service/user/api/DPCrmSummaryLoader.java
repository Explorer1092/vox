package com.voxlearning.utopia.service.user.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.utopia.entity.crm.CrmTeacherSummary;

import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "1.0.0")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface DPCrmSummaryLoader {

    CrmTeacherSummary loadTeacherSummary(Long teacherId);

    CrmTeacherSummary loadTeacherSummaryByMobile(String teacherMobile);
}
