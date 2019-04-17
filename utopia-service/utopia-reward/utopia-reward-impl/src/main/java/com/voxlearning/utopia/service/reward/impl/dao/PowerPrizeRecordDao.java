package com.voxlearning.utopia.service.reward.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.persistence.AsyncStaticMongoPersistence;
import com.voxlearning.utopia.service.reward.entity.newversion.PowerPrizeRecord;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

@Named
@CacheBean(type = PowerPrizeRecord.class)
public class PowerPrizeRecordDao extends AsyncStaticMongoPersistence<PowerPrizeRecord, String> {
    @Override
    protected void calculateCacheDimensions(PowerPrizeRecord document, Collection<String> dimensions) {
        dimensions.add(PowerPrizeRecord.ck_userId(document.getUserId()));
    }

    @CacheMethod
    public List<PowerPrizeRecord> loadByUserId(@CacheParameter(value = "USER_ID") Long userId) {
        Criteria criteria = Criteria.where("userId").is(userId);
        return query(Query.query(criteria));
    }
}
