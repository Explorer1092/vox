package com.voxlearning.utopia.service.mizar.consumer.loader;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.mizar.api.entity.user.MizarUserSchool;
import com.voxlearning.utopia.service.mizar.api.loader.MizarUserSchoolLoader;

import java.util.List;

/**
 * @author chunlin.yu
 * @create 2017-06-22 14:26
 **/
public class MizarUserSchoolLoaderClient implements MizarUserSchoolLoader{


    @ImportService(interfaceClass = MizarUserSchoolLoader.class)
    private MizarUserSchoolLoader mizarUserSchoolLoader;

    @Override
    public MizarUserSchool loadBySchoolId(Long schoolId) {
       return mizarUserSchoolLoader.loadBySchoolId(schoolId);
    }

    @Override
    public List<MizarUserSchool> loadByUserId(String userId) {
        return mizarUserSchoolLoader.loadByUserId(userId);
    }

    @Override
    public List<MizarUserSchool> loadAll() {
        return mizarUserSchoolLoader.loadAll();
    }

}
