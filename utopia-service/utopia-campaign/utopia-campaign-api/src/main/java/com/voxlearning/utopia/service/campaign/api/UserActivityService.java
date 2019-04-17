package com.voxlearning.utopia.service.campaign.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.utopia.service.campaign.api.entity.TeacherActivityRef;

import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20190311")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface UserActivityService {

    TeacherActivityRef loadUserActivity(Long userId, String activityEnumType);

    TeacherActivityRef saveUserActivity(Long userId, String activityEnumType);
}
