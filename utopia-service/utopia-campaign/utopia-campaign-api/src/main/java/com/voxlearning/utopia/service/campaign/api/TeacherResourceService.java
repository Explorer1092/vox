package com.voxlearning.utopia.service.campaign.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.campaign.api.entity.TeacherResourceRef;

import java.util.List;
import java.util.concurrent.TimeUnit;

@ServiceRetries
@ServiceVersion(version = "20181022")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
public interface TeacherResourceService {

    List<TeacherResourceRef> loadTeacherResourceByUserId(Long teacherId);

    List<TeacherResourceRef> loadCoursewareResourceByUserId(Long teacherId);

    MapMessage saveCoursewareResource(Long teacherId, String coursewareId, String coursewareName, String resourceUrl);

    MapMessage updateCoursewareUrl(String coursewardId, String resourceUrl);
}
