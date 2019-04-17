package com.voxlearning.utopia.service.reward.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.reward.entity.newversion.TobyAccessoryCVRecord;
import com.voxlearning.utopia.service.reward.entity.newversion.TobyCountenanceCVRecord;
import com.voxlearning.utopia.service.reward.entity.newversion.TobyImageCVRecord;
import com.voxlearning.utopia.service.reward.entity.newversion.TobyPropsCVRecord;

import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Named
@CacheBean(type = TobyAccessoryCVRecord.class)
public class TobyAccessoryCVRecordDao extends AlpsStaticMongoDao<TobyAccessoryCVRecord, String> {
    @Override
    protected void calculateCacheDimensions(TobyAccessoryCVRecord document, Collection<String> dimensions) {
        dimensions.add(TobyAccessoryCVRecord.ck_userId(document.getUserId()));
        dimensions.add(TobyAccessoryCVRecord.ck_accessoryId(document.getAccessoryId()));
    }

    @CacheMethod
    public List<TobyAccessoryCVRecord> loadByUserId(@CacheParameter(value = "USER_ID") long userId) {
        Criteria criteria = Criteria.where("userId").is(userId);
        return query(Query.query(criteria));
    }

    @CacheMethod
    public List<TobyAccessoryCVRecord> loadByAccessoryId(@CacheParameter(value = "ACCESSORY_ID") Long accessoryId) {
        Criteria criteria = Criteria.where("accessoryId").is(accessoryId);
        return query(Query.query(criteria));
    }

}
