package com.voxlearning.utopia.service.mizar.impl.dao.sys;

import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.mizar.api.entity.sys.MizarSysPath;

import javax.inject.Named;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * MizarSysPathPersistence Class
 * Created by alex on 2016/8/15.
 */
@Named
@UtopiaCacheSupport(MizarSysPath.class)
public class MizarSysPathDao extends AlpsStaticMongoDao<MizarSysPath, String> {

    @Override
    protected void calculateCacheDimensions(MizarSysPath document, Collection<String> dimensions) {
        dimensions.add(MizarSysPath.ck_id(document.getId()));
        dimensions.add(MizarSysPath.ck_all());
    }

    @CacheMethod(key = "ALL")
    public List<MizarSysPath> findAll() {
        return query();
    }

    public int delete(String id) {
        int rows = $remove(id) ? 1 : 0;
        if (rows > 0) {
            Set<String> cacheKeys = new HashSet<>();
            cacheKeys.add(MizarSysPath.ck_id(id));
            cacheKeys.add(MizarSysPath.ck_all());
            getCache().delete(cacheKeys);
        }
        return rows;
    }

}
