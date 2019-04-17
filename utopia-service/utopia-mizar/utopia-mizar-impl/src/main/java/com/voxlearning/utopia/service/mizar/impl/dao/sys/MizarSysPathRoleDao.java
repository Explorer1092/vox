package com.voxlearning.utopia.service.mizar.impl.dao.sys;

import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.UtopiaCacheKey;
import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.annotation.cache.UtopiaCacheable;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.mizar.api.entity.sys.MizarSysPathRole;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 *
 * Created by alex on 2016/8/15.
 */
@Named
@UtopiaCacheSupport(MizarSysPathRole.class)
public class MizarSysPathRoleDao extends AlpsStaticMongoDao<MizarSysPathRole, String> {

    @Override
    protected void calculateCacheDimensions(MizarSysPathRole document, Collection<String> dimensions) {
        dimensions.add(MizarSysPathRole.ck_all());
        dimensions.add(MizarSysPathRole.ck_role(document.getRoleGroupId()));
        dimensions.add(MizarSysPathRole.ck_path(document.getPath()));
    }

    @CacheMethod(key = "ALL")
    public List<MizarSysPathRole> findAll() {
        return query();
    }

    @UtopiaCacheable
    public List<MizarSysPathRole> findByRole(@UtopiaCacheKey(name = "R") Integer roleId) {
        Criteria criteria = Criteria.where("role").is(roleId);
        return query(Query.query(criteria));
    }

    @UtopiaCacheable
    public List<MizarSysPathRole> findByPath(@UtopiaCacheKey(name = "P") String pathId) {
        Criteria criteria = Criteria.where("path").is(pathId);
        return query(Query.query(criteria));
    }

    public int delete(final String id) {
        return remove(id) ? 1 : 0;
    }

}