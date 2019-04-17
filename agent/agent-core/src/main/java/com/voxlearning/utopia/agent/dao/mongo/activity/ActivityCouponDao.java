package com.voxlearning.utopia.agent.dao.mongo.activity;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.agent.persist.entity.activity.ActivityCoupon;

import javax.inject.Named;
import java.util.Collections;
import java.util.List;

@Named
@CacheBean(type =  ActivityCoupon.class)
public class ActivityCouponDao extends StaticCacheDimensionDocumentMongoDao<ActivityCoupon, String> {

    @CacheMethod
    public List<ActivityCoupon> loadByCoupon(@CacheParameter("cid") String couponId, @CacheParameter("cuid")Long couponUserId){
        if(StringUtils.isBlank(couponId) || couponUserId == null){
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("couponId").is(couponId).and("couponUserId").is(couponUserId);
        return query(Query.query(criteria));
    }

    @CacheMethod
    public List<ActivityCoupon> loadByAidAndUid(@CacheParameter("aid") String activityId, @CacheParameter("uid")Long userId){
        if(StringUtils.isBlank(activityId) || userId == null){
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("activityId").is(activityId).and("userId").is(userId);
        return query(Query.query(criteria));
    }
}
