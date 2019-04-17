package com.voxlearning.utopia.service.crm.api.service.crm;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.api.constant.EduSystemType;

import java.util.concurrent.TimeUnit;

/**
 * Created by yaguang.wang
 * on 2017/10/18.
 */
@ServiceVersion(version = "2017.10.18")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface CrmSchoolExtInfoCheckService extends IPingable {
    MapMessage beforeUpdateSchoolExtInfoEduSystem(Long schoolId, EduSystemType eduSystemType);

    MapMessage updateSchoolExtInfoEduSystem(Long schoolId, EduSystemType eduSystemType, String desc, String modifier);

    MapMessage afterUpdateSchoolExtInfoEduSystem(Long schoolId, String oldSystemType, EduSystemType newSystemType, String desc, String modifier, Boolean needMail);

}
