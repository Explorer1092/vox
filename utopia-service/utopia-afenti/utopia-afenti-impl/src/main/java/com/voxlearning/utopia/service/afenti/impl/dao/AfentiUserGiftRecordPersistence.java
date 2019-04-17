package com.voxlearning.utopia.service.afenti.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.jdbc.persistence.StaticPersistence;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiUserGiftRecord;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * @author peng.zhang.a
 * @since 16-8-15
 */
@Named
@CacheBean(type = AfentiUserGiftRecord.class)
@CacheDimension(CacheDimensionDistribution.OTHER_FIELDS)
public class AfentiUserGiftRecordPersistence extends StaticPersistence<Long, AfentiUserGiftRecord> {

    @Override
    protected void calculateCacheDimensions(AfentiUserGiftRecord source, Collection<String> dimensions) {
        dimensions.add(AfentiUserGiftRecord.ck_us(source.getUserId(), source.getSubject()));
    }

    @CacheMethod
    public List<AfentiUserGiftRecord> findByUserIdAndSubject(@CacheParameter(value = "UID") Long userId,
                                                             @CacheParameter(value = "SJ") Subject subject) {
        return withSelectFromTable("WHERE USER_ID=? AND SUBJECT=?").useParamsArgs(userId, subject).queryAll();
    }
}
