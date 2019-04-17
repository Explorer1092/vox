package com.voxlearning.utopia.service.mizar.impl.dao.user;

import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.annotation.cache.UtopiaCacheable;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.service.mizar.api.entity.user.MizarDepartment;

import javax.inject.Named;
import java.util.List;

/**
 * Mizar User DAO class
 * Created by alex on 2016/8/16.
 */
@Named
@UtopiaCacheSupport(MizarDepartment.class)
public class MizarDepartmentDao extends StaticCacheDimensionDocumentMongoDao<MizarDepartment, String> {

    @UtopiaCacheable(key = "ALL")
    public List<MizarDepartment> findAll() {
        return query();
    }

}
