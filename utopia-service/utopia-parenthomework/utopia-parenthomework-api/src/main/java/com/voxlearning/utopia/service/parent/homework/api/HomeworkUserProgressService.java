package com.voxlearning.utopia.service.parent.homework.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.parent.homework.api.entity.UserProgress;

import java.util.concurrent.TimeUnit;

/**
 * 用户进度服务接口
 *
 * @author Wenlong Meng
 * @since Feb 22, 2019
 */
@ServiceVersion(version = "20190214")
@ServiceTimeout(timeout = 3, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
public interface HomeworkUserProgressService {

    /**
     * 保存用户进度
     *
     * @param userId 学生id
     * @param bizType 业务id
     * @param userProgress 用户进度
     * @return
     */
    MapMessage save(Long userId, String bizType, UserProgress userProgress);
}
