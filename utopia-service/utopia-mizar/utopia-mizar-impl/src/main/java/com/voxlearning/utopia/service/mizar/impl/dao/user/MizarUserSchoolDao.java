package com.voxlearning.utopia.service.mizar.impl.dao.user;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.mizar.api.entity.user.MizarUserSchool;

import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 用户学校关联
 *
 * @author chunlin.yu
 * 2017-06-22 14:05
 **/
@Named
@CacheBean(type = MizarUserSchool.class)
public class MizarUserSchoolDao  extends AlpsStaticMongoDao<MizarUserSchool, String> {

    @Override
    protected void calculateCacheDimensions(MizarUserSchool document, Collection<String> dimensions) {
        dimensions.add(MizarUserSchool.ck_user(document.getUserId()));
        dimensions.add(MizarUserSchool.ck_school(document.getSchoolId()));
    }

    /**
     * 根据schoolId 查询
     * @param schoolId
     * @return
     */
    @CacheMethod
    public MizarUserSchool loadBySchoolId(@CacheParameter(value = "sid") Long schoolId){
        if (null == schoolId) {
            return null;
        }
        Criteria criteria = Criteria.where("schoolId").is(schoolId).and("disabled").is(false);
        Query query = Query.query(criteria);
        return query(query).stream().findFirst().orElse(null);
    }

    @CacheMethod
    public List<MizarUserSchool> loadByUserId(@CacheParameter(value = "uid") String userId) {
        if (null == userId) {
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("userId").is(userId).and("disabled").is(false);
        Query query = Query.query(criteria);
        return query(query);
    }

    public void disableUserSchool(String userId, Long schoolId) {
        Criteria criteria = new Criteria();
        is(criteria, "userId", userId);
        is(criteria, "schoolId", schoolId);
        is(criteria, "disabled", false);
        Query query = Query.query(criteria);
        query(query).forEach(item -> {
            item.setDisabled(true);
            upsert(item);
        });
    }

    private Criteria is(Criteria criteria, String key, Object obj) {
        if (obj == null) {
            return criteria;
        }
        return criteria.and(key).is(obj);
    }

    /**
     * 查询所有disable=false的数据
     * @return
     */
    public List<MizarUserSchool> loadAll() {
        Criteria criteria = Criteria.where("disabled").is(false);
        Query query = Query.query(criteria);
        return query(query);
    }
}
