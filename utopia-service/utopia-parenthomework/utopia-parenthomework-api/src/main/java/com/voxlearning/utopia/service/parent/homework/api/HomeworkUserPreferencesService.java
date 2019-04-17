package com.voxlearning.utopia.service.parent.homework.api;

import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkUserPreferences;
import com.voxlearning.utopia.service.parent.homework.api.mapper.UserPreference;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20190114")
@ServiceTimeout(timeout = 3, unit = TimeUnit.SECONDS)
public interface HomeworkUserPreferencesService {

    /**
     * 保存设置偏好
     *
     * @param userPreferences 偏好设置
     * @return
     */
    MapMessage upsertHomeworkUserPreferences(Collection<UserPreference> userPreferences);

    /**
     * 保存设置偏好
     *
     * @param userPreferences 偏好设置
     * @return
     */
    MapMessage upsertUserPreferencesNoMessage(Collection<UserPreference> userPreferences);

}
