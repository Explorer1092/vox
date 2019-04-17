package com.voxlearning.utopia.service.newhomework.impl.dao.ocr;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.persistence.StaticMongoShardPersistence;
import com.voxlearning.utopia.service.newhomework.api.entity.ocr.OcrStudentWorkbook;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * @author majianxin
 * @version V1.0
 * @date 2019/3/22
 */
@Named
@CacheBean(type = OcrStudentWorkbook.class, cacheName = "utopia-homework-cache")
@CacheDimension(CacheDimensionDistribution.ID_AND_OTHER_FIELDS)
public class OcrStudentWorkbookDao extends StaticMongoShardPersistence<OcrStudentWorkbook, String> {

    @Override
    protected void calculateCacheDimensions(OcrStudentWorkbook document, Collection<String> dimensions) {
        dimensions.add(OcrStudentWorkbook.ck_id(document.getId()));
        dimensions.add(OcrStudentWorkbook.ck_studentId(document.getStudentId()));
    }

    @CacheMethod
    public List<OcrStudentWorkbook> loadStudentBooks(@CacheParameter(value = "SID") Long studentId) {
        Criteria criteria = Criteria.where("studentId").is(studentId).and("disabled").is(Boolean.FALSE);
        Query query = Query.query(criteria);
        return query(query);
    }
}
