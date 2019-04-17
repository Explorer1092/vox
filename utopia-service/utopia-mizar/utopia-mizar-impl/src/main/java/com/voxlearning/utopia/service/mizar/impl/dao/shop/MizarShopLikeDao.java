package com.voxlearning.utopia.service.mizar.impl.dao.shop;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarShopLike;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * Created by Summer Yang on 2016/9/6.
 */
@Named
@CacheBean(type = MizarShopLike.class)
public class MizarShopLikeDao extends AlpsStaticMongoDao<MizarShopLike, String> {

    @Override
    protected void calculateCacheDimensions(MizarShopLike document, Collection<String> dimensions) {
        dimensions.add(MizarShopLike.ck_userId(document.getUserId()));
        dimensions.add(MizarShopLike.ck_activityId(document.getActivityId()));
        dimensions.add(MizarShopLike.ck_shopIdAndActivityId(document.getShopId(), document.getActivityId()));
    }

    @CacheMethod
    public List<MizarShopLike> loadByUserId(@CacheParameter(value = "userId") Long userId) {
        Criteria criteria = Criteria.where("user_id").is(userId);
        Query query = Query.query(criteria);
        return query(query);
    }

    @CacheMethod
    public List<MizarShopLike> loadByShopIdAndActivityId(@CacheParameter(value = "shopId") String shopId,
                                                         @CacheParameter(value = "activityId") Integer activityId) {
        Criteria criteria = Criteria.where("shop_id").is(shopId);
        criteria.and("activity_id").is(activityId);
        Query query = Query.query(criteria);
        return query(query);
    }

    @CacheMethod
    public List<MizarShopLike> loadByActivityId(@CacheParameter(value = "activityId") Integer activityId) {
        Criteria criteria = Criteria.where("activity_id").is(activityId);
        Query query = Query.query(criteria);
        return query(query);
    }

    public long loadCountByShopIdAndActivityId(String shopId, Integer activityId) {
        Criteria criteria = Criteria.where("shop_id").is(shopId);
        criteria.and("activity_id").is(activityId);
        Query query = Query.query(criteria);
        return count(query);
    }

}
