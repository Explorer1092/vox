package com.voxlearning.utopia.service.parent.homework.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkUserProgress;

import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20190214")
@ServiceTimeout(timeout = 3, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
public interface HomeworkUserProgressLoader {

    /**
     * 根据业务类型查询用户进度
     *
     * @param userId 用户id
     * @param bizType 业务类型
     * @return
     */
    HomeworkUserProgress loadUserProgress(Long userId, String bizType);

    /**
     * 根据业务类型查询课程、进度
     *
     * @param userId 用户id
     * @param sectionId 课时id
     * @param bizType 业务类型
     * @return
     */
    MapMessage loadCourseProgresses(Long userId, String bookId, String unitId, String sectionId, String bizType);
}
