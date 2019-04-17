package com.voxlearning.utopia.service.campaign.impl.dao;

import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.service.campaign.api.entity.YiqiJTCourse;
import com.voxlearning.utopia.service.campaign.api.entity.YiqiJTCourseCatalog;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

@Named
public class YiqiJTCourseCatalogDao extends AlpsStaticJdbcDao<YiqiJTCourseCatalog,Long>{
    @Override
    protected void calculateCacheDimensions(YiqiJTCourseCatalog document, Collection<String> dimensions) {

    }

    public List<YiqiJTCourseCatalog> getCourseCatalogsByCourseId(long courseId) {
        Criteria criteria = Criteria.where("COURSE_ID").is(courseId);
        return query(Query.query(criteria));
    }
}
