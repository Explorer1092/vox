package com.voxlearning.utopia.service.parent.homework.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkUserPreferences;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkUserRef;

import java.util.Collection;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 作业学生关系查询接口
 *
 * @author Wenlong Meng
 * @since Dec 25, 2018
 */
@ServiceVersion(version = "20181212")
@ServiceTimeout(timeout = 3, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 1)
public interface HomeworkUserRefLoader {

    /**
     * 根据学生id查询最近一次布置作业
     *
     * @param userId 学生id
     * @return 作业学生关系
     */
    HomeworkUserRef last(Long userId);

    /**
     * 根据学生id和时间查询布置过的题
     * @param userId 学生id
     * @param time 时间
     * @return 作业学生关系
     */
    Collection<HomeworkUserRef> lastTime(Long userId, Date time);
}
