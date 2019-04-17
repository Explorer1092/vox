package com.voxlearning.utopia.service.campaign.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.campaign.api.entity.NewTermPlanActivity;

import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20190215")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface TeacherNewTermPlanPointService {

    NewTermPlanActivity load(Long userId);

    MapMessage delete(Long userId);

    MapMessage studentShow(Long userId);

    MapMessage teacherShow(Long userId);

    MapMessage parentShow(Long parentId, Long studentId);

    MapMessage studentClickGoAssignBtn(Long userId);

    MapMessage parentClickGoAssignBtn(Long parent);

    MapMessage parentAssign(Long parentId, Long studentId);

}