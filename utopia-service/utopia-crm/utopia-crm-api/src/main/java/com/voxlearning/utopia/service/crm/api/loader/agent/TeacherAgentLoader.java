package com.voxlearning.utopia.service.crm.api.loader.agent;

import com.voxlearning.alps.annotation.remote.Idempotent;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Teacher
 *  re
 * Created by alex on 2017/1/9.
 */
@ServiceVersion(version = "20170109")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
public interface TeacherAgentLoader extends IPingable {

    @Idempotent
    Map<String, Object> getSchoolManager(Long schoolId);

}
