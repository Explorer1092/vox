
package com.voxlearning.utopia.admin.dao;

import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.campaign.api.entity.TeacherCourseware;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

@Named
public class CrmTeacherCoursewareDao extends AlpsStaticMongoDao<TeacherCourseware,String> {

    @Override
    protected void calculateCacheDimensions(TeacherCourseware document, Collection<String> dimensions) {

    }

    public List<TeacherCourseware> findExamStatus(TeacherCourseware.ExamineStatus examineStatus) {
        Criteria criteria = new Criteria();
        criteria.and("examineStatus").is(examineStatus).and("disabled").is(false);
        Query query = Query.query(criteria);
        return query(query);
    }
}
