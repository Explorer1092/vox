package com.voxlearning.utopia.service.business.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.business.api.entity.SchoolReportSituation;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author fugui.chang
 * @since 2016/9/24
 */
@Named
@CacheBean(type = SchoolReportSituation.class)
public class SchoolReportSituationDao extends AlpsStaticMongoDao<SchoolReportSituation, String> {

    @Override
    protected void calculateCacheDimensions(SchoolReportSituation document, Collection<String> dimensions) {
        dimensions.add(SchoolReportSituation.generateCacheKey(document.getSchoolId(),document.getYearmonth()));
    }

    public List<SchoolReportSituation> loadSchoolReportSituationBySchoolIdAndDt( Long schoolId,Long  dt ){
        Criteria criteria = Criteria.where("school_id").is(schoolId)
                .and("dt").is(dt);
        return query(Query.query(criteria));
    }

}
