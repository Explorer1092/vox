package com.voxlearning.utopia.service.piclisten.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.service.vendor.api.entity.FltrpMobile;

import javax.inject.Named;
import java.util.Date;
import java.util.List;

/**
 * @author xinxin
 * @since 3/17/17.
 */
@Named
@CacheBean(type = FltrpMobile.class)
public class FltrpMobileDao extends StaticCacheDimensionDocumentMongoDao<FltrpMobile, String> {

    @CacheMethod(type = FltrpMobile.class)
    public FltrpMobile getByUserId(@CacheParameter("UID") Long userId) {
        Criteria criteria = Criteria.where("uid").is(userId);
        Query query = Query.query(criteria);

        List<FltrpMobile> fltrpMobiles = executeQuery(createMongoConnection(), query);
        if (CollectionUtils.isEmpty(fltrpMobiles)) {
            return null;
        }

        return fltrpMobiles.get(0);
    }

    //预计缓存命中率极低，先不做缓存，观察一下
    public List<FltrpMobile> getByMobile(String mobile) {
        Criteria criteria = Criteria.where("mobile").is(mobile);
        Query query = Query.query(criteria);

        return executeQuery(createMongoConnection(), query);
    }

    public boolean setMobileChecked(Long userId, String mobile, Boolean real) {
        Criteria criteria = Criteria.where("uid").is(userId);
        Update update = Update.update("updateTime", new Date())
                .set("checked", true)
                .set("mobile", mobile)
                .set("real", real);
        long count = executeUpdateOne(createMongoConnection(), criteria, update);
        if (count > 0) {
            String key = CacheKeyGenerator.generateCacheKey(FltrpMobile.class, "UID", userId);
            getCache().delete(key);
        }
        return count > 0;
    }
}
