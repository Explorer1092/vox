package com.voxlearning.utopia.service.campaign.impl.dao;

import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.service.campaign.api.entity.YiqiJTChoiceNote;
import com.voxlearning.utopia.service.campaign.api.entity.YiqiJTCourse;
import com.voxlearning.utopia.service.campaign.api.entity.YiqiJTCourseCatalog;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

@Named
public class YiqiJTCourseChoiceNoteDao extends AlpsStaticJdbcDao<YiqiJTChoiceNote,Long>{
    @Override
    protected void calculateCacheDimensions(YiqiJTChoiceNote document, Collection<String> dimensions) {

    }

    public List<YiqiJTChoiceNote> getCourseNotesByCourseId(long courseId) {
        List<YiqiJTChoiceNote> result = null;
        Criteria criteria = Criteria.where("COURSE_ID").is(courseId);
        return query(Query.query(criteria));
    }
}
