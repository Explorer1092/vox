package com.voxlearning.utopia.service.reward.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.service.reward.entity.RewardActivityImage;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * Created by ganhaitian on 2017/2/6.
 */
@Named
@CacheBean(type = RewardActivityImage.class)
public class RewardActivityImageDao extends AlpsStaticJdbcDao<RewardActivityImage,Long>{

    @CacheMethod
    public List<RewardActivityImage> loadActivityImages(@CacheParameter("ACID") Long activityId){
        Criteria criteria = Criteria.where("ACTIVITY_ID").is(activityId);
        Query query = Query.query(criteria);
        return query(query);
    }

    @Override
    protected void calculateCacheDimensions(RewardActivityImage document, Collection<String> dimensions) {
        dimensions.add(RewardActivityImage.ck_acid(document.getActivityId()));
    }
}
