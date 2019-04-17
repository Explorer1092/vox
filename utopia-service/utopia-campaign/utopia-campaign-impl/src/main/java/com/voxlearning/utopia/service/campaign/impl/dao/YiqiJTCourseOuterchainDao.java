package com.voxlearning.utopia.service.campaign.impl.dao;

import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.service.campaign.api.entity.YiqiJTCourseOuterchain;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

@Named
public class YiqiJTCourseOuterchainDao extends AlpsStaticJdbcDao<YiqiJTCourseOuterchain,Long> {
    @Override
    protected void calculateCacheDimensions(YiqiJTCourseOuterchain document, Collection<String> dimensions) {

    }

    public List<YiqiJTCourseOuterchain> getCourseOuterchainByCourseId(long courseId) {
        Criteria criteria = Criteria.where("COURSE_ID").is(courseId);
        return query(Query.query(criteria));
    }

}