package com.voxlearning.utopia.service.reward.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.reward.entity.newversion.TobyAccessoryCVRecord;
import com.voxlearning.utopia.service.reward.entity.newversion.TobyCountenanceCVRecord;
import com.voxlearning.utopia.service.reward.entity.newversion.TobyImageCVRecord;

import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Named
@CacheBean(type = TobyCountenanceCVRecord.class)
public class TobyCountenanceCVRecordDao extends AlpsStaticMongoDao<TobyCountenanceCVRecord, String> {
    @Override
    protected void calculateCacheDimensions(TobyCountenanceCVRecord document, Collection<String> dimensions) {
        dimensions.add(TobyCountenanceCVRecord.ck_userId(document.getUserId()));
        dimensions.add(TobyCountenanceCVRecord.ck_countenanceId(document.getCountenanceId()));
    }

    @CacheMethod
    public List<TobyCountenanceCVRecord> loadByUserId(@CacheParameter(value = "USER_ID") long userId) {
        Criteria criteria = Criteria.where("userId").is(userId);
        return query(Query.query(criteria));
    }

    @CacheMethod
    public List<TobyCountenanceCVRecord> loadByCountenanceId(@CacheParameter(value = "COUNTENANCE_ID") Long countenanceId) {
        Criteria criteria = Criteria.where("countenanceId").is(countenanceId);
        return query(Query.query(criteria));
    }

}
