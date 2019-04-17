package com.voxlearning.utopia.service.mizar.impl.loader;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.utopia.service.mizar.api.entity.user.MizarUserSchool;
import com.voxlearning.utopia.service.mizar.api.loader.MizarUserSchoolLoader;
import com.voxlearning.utopia.service.mizar.impl.dao.user.MizarUserSchoolDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * MizarUserSchoolLoader的实现
 *
 * @author chunlin.yu create on 2017-06-22 13:16
 **/
@Named
@Service(interfaceClass = MizarUserSchoolLoader.class)
@ExposeService(interfaceClass = MizarUserSchoolLoader.class)
public class MizarUserSchoolLoaderImpl implements MizarUserSchoolLoader {

    @Inject
    MizarUserSchoolDao mizarUserSchoolDao ;

    @Override
    public MizarUserSchool loadBySchoolId(Long schoolId) {
        return mizarUserSchoolDao.loadBySchoolId(schoolId);
    }

    @Override
    public List<MizarUserSchool> loadByUserId(String userId) {
        return mizarUserSchoolDao.loadByUserId(userId);
    }

    @Override
    public List<MizarUserSchool> loadAll() {
        return mizarUserSchoolDao.loadAll();
    }
}
