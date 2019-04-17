package com.voxlearning.utopia.service.mizar.api.service;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.mizar.api.entity.user.MizarUserSchool;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by yaguang.wang on 2017/6/22.
 */
@ServiceVersion(version = "20170627")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface MizarUserSchoolService {

    /**
     * 添加用户学校对应关系
     * @return
     */
    MapMessage addUserSchools(Collection<MizarUserSchool> userSchools);

    /**
     * 根据用户ID和学校ID移除用户学校对应关系
     * @param userId
     * @param schoolId
     * @return
     */
    MapMessage deleteUserSchool(String userId, Long schoolId);


    /**
     * 根据用户ID移除用户学校对应关系
     * @param userId
     * @return
     */
    MapMessage deleteUserSchool(String userId);
}
