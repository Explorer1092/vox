package com.voxlearning.utopia.service.reward.impl.dao;

import com.voxlearning.alps.annotation.cache.*;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.reward.entity.PublicGoodReward;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

@Named
@CacheBean(type = PublicGoodReward.class,expiration = @UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today))
public class PublicGoodRewardDao extends AlpsStaticMongoDao<PublicGoodReward,Long> {

    @CacheMethod
    public List<PublicGoodReward> loadByModel(@CacheParameter("MODEL") String model){
        Criteria criteria = Criteria.where("model").is(model);
        return query(Query.query(criteria));
    }

    @Override
    protected void calculateCacheDimensions(PublicGoodReward document, Collection<String> dimensions) {
        dimensions.add(PublicGoodReward.ck_all());

    }

    @CacheMethod(key = "ALL")
    public List<PublicGoodReward> loadAll(){
        return query();
    }
}
