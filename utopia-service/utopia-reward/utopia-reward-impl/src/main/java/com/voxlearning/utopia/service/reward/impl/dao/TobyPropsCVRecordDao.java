package com.voxlearning.utopia.service.reward.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.reward.entity.newversion.TobyCountenanceCVRecord;
import com.voxlearning.utopia.service.reward.entity.newversion.TobyImageCVRecord;
import com.voxlearning.utopia.service.reward.entity.newversion.TobyPropsCVRecord;

import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Named
@CacheBean(type = TobyPropsCVRecord.class)
public class TobyPropsCVRecordDao extends AlpsStaticMongoDao<TobyPropsCVRecord, String> {
    @Override
    protected void calculateCacheDimensions(TobyPropsCVRecord document, Collection<String> dimensions) {
        dimensions.add(TobyPropsCVRecord.ck_userId(document.getUserId()));
        dimensions.add(TobyPropsCVRecord.ck_propsId(document.getPropsId()));
    }

    @CacheMethod
    public List<TobyPropsCVRecord> loadByUserId(@CacheParameter(value = "USER_ID") long userId) {
        Criteria criteria = Criteria.where("userId").is(userId);
        return query(Query.query(criteria));
    }

    @CacheMethod
    public List<TobyPropsCVRecord> loadByPropsId(@CacheParameter(value = "PROPS_ID") Long propsId) {
        Criteria criteria = Criteria.where("propsId").is(propsId);
        return query(Query.query(criteria));
    }

}
