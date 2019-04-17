package com.voxlearning.utopia.service.mizar.consumer.service;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.mizar.api.entity.user.MizarUserSchool;
import com.voxlearning.utopia.service.mizar.api.service.MizarUserSchoolService;

import javax.inject.Inject;
import java.util.Collection;

/**
 * @author chunlin.yu
 * @create 2017-06-22 17:01
 **/
public class MizarUserSchoolServiceClient implements MizarUserSchoolService {

    @ImportService(interfaceClass = MizarUserSchoolService.class)
    private MizarUserSchoolService mizarUserSchoolService;

    @Override
    public MapMessage addUserSchools(Collection<MizarUserSchool> userSchools) {
        return mizarUserSchoolService.addUserSchools(userSchools);
    }

    @Override
    public MapMessage deleteUserSchool(String userId, Long schoolId) {
        return mizarUserSchoolService.deleteUserSchool(userId, schoolId);
    }

    @Override
    public MapMessage deleteUserSchool(String userId) {
        return mizarUserSchoolService.deleteUserSchool(userId);
    }
}
