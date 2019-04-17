package com.voxlearning.utopia.service.parent.homework.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkUserPreferences;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * 作业用户偏好查询接口
 *
 * @author Wenlong Meng
 * @version 20181111
 */
@ServiceVersion(version = "20181111")
@ServiceTimeout(timeout = 3, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
public interface HomeworkUserPreferencesLoader {

    /**
     * 根据用户id查询设置偏好
     *
     * @param userId 学生id
     *
     * @return 设置偏好
     */
    Collection<HomeworkUserPreferences> loadHomeworkUserPreferences(Long userId);

    /**
     * 根据学科查询设置偏好
     *
     * @param userId 学生id
     * @return 设置偏好
     */
    HomeworkUserPreferences loadHomeworkUserPreference(Long userId, String subject);

}
