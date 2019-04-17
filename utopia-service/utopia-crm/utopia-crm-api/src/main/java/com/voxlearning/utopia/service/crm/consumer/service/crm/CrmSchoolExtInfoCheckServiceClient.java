package com.voxlearning.utopia.service.crm.consumer.service.crm;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.EduSystemType;
import com.voxlearning.utopia.service.crm.api.service.crm.CrmSchoolExtInfoCheckService;

/**
 * Created by yaguang.wang
 * on 2017/10/18.
 */
public class CrmSchoolExtInfoCheckServiceClient implements CrmSchoolExtInfoCheckService {
    @ImportService(interfaceClass = CrmSchoolExtInfoCheckService.class)
    private CrmSchoolExtInfoCheckService remoteReference;

    @Override
    public MapMessage beforeUpdateSchoolExtInfoEduSystem(Long schoolId, EduSystemType eduSystemType) {
        return remoteReference.beforeUpdateSchoolExtInfoEduSystem(schoolId, eduSystemType);
    }

    @Override
    public MapMessage updateSchoolExtInfoEduSystem(Long schoolId, EduSystemType eduSystemType, String desc, String modifier) {
        return remoteReference.updateSchoolExtInfoEduSystem(schoolId,eduSystemType,desc,modifier);
    }

    @Override
    public MapMessage afterUpdateSchoolExtInfoEduSystem(Long schoolId, String oldSystemType, EduSystemType newSystemType, String desc, String modifier, Boolean needMail) {
        return remoteReference.afterUpdateSchoolExtInfoEduSystem(schoolId, oldSystemType, newSystemType, desc, modifier, needMail);
    }

}
