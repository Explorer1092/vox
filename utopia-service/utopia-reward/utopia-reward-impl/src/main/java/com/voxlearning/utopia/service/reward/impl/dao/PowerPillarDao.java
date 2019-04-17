package com.voxlearning.utopia.service.reward.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.alps.dao.mongo.persistence.AsyncStaticMongoPersistence;
import com.voxlearning.utopia.service.reward.entity.newversion.PowerPillar;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

@Named
@CacheBean(type = PowerPillar.class)
public class PowerPillarDao extends AsyncStaticMongoPersistence<PowerPillar, String> {
    @Override
    protected void calculateCacheDimensions(PowerPillar document, Collection<String> dimensions) {
        dimensions.add(PowerPillar.ck_userId(document.getUserId()));
    }

    @CacheMethod
    public PowerPillar loadByUserId(@CacheParameter(value = "USER_ID") Long userId) {
        PowerPillar result = null;
        Criteria criteria = Criteria.where("userId").is(userId);
        List<PowerPillar> powerPillarList = query(Query.query(criteria));
        if (powerPillarList != null && !powerPillarList.isEmpty()) {
            result = powerPillarList.get(0);
        }
        return result;
    }

    public void addPowerPillarNum(Long userId, Integer num) {
        Criteria criteria = Criteria.where("userId").is(userId);
        Update update = new Update().inc("powerPillar", num);
        this.$executeUpdateOne(createMongoConnection(), criteria, update);

        String cacheKey = PowerPillar.ck_userId(userId);
        getCache().delete(cacheKey);
    }
}
