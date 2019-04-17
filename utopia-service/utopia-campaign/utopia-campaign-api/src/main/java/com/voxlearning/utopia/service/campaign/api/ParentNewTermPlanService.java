package com.voxlearning.utopia.service.campaign.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20190309")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface ParentNewTermPlanService {

    List<Map<String, Object>> saveNewTermActivityPlans(Long studentId, String plans);

    /**
     * 新学期计划第二轮报名
     */
    MapMessage signUp(Long id);

    Long getSignUpCount();

    Long incrSignUpCount(Long incr);

    Long setSignUpCount(Long count);

    Boolean getSignUpStatus(Long parentId);
}
