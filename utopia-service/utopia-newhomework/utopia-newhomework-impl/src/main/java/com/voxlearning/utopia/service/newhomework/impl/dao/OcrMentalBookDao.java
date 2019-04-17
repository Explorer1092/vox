package com.voxlearning.utopia.service.newhomework.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.persistence.StaticMongoShardPersistence;
import com.voxlearning.utopia.service.newhomework.api.entity.OcrMentalBook;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

@Named
@CacheBean(type = OcrMentalBook.class, cacheName = "utopia-homework-cache")
@CacheDimension(CacheDimensionDistribution.ID_AND_OTHER_FIELDS)
public class OcrMentalBookDao extends StaticMongoShardPersistence<OcrMentalBook, String> {
    @Override
    protected void calculateCacheDimensions(OcrMentalBook document, Collection<String> dimensions) {
        dimensions.add(OcrMentalBook.ck_id(document.getId()));
        dimensions.add(OcrMentalBook.ck_teacherId(document.getTeacherId()));
    }

    @CacheMethod
    public List<OcrMentalBook> loadTeacherBooks(@CacheParameter(value = "TID") Long teacherId) {
        Criteria criteria = Criteria.where("teacherId").is(teacherId).and("disabled").is(Boolean.FALSE);
        Query query = Query.query(criteria);
        return query(query);
    }
}
