package com.voxlearning.utopia.service.reward.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.alps.dao.mongo.persistence.AsyncStaticMongoPersistence;
import com.voxlearning.utopia.service.reward.entity.newversion.PrizeClawWinningRecord;
import com.voxlearning.utopia.service.reward.entity.newversion.TobyPropsCVRecord;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

@Named
@CacheBean(type = PrizeClawWinningRecord.class)
public class PrizeClawWinningRecordDao extends AsyncStaticMongoPersistence<PrizeClawWinningRecord, String> {
    @Override
    protected void calculateCacheDimensions(PrizeClawWinningRecord document, Collection<String> dimensions) {
        dimensions.add(PrizeClawWinningRecord.ck_userId(document.getUserId()));
    }

    @CacheMethod
    public List<PrizeClawWinningRecord> loadByUserId(@CacheParameter(value = "USER_ID") Long userId) {
        Criteria criteria = Criteria.where("userId").is(userId);
        return query(Query.query(criteria));
    }
}
