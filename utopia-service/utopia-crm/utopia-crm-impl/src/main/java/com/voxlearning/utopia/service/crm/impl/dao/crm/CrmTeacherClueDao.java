package com.voxlearning.utopia.service.crm.impl.dao.crm;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.crm.api.constants.crm.CrmClueType;
import com.voxlearning.utopia.service.crm.api.entities.crm.CrmTeacherClue;

import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * CrmTeacherClueDao
 *
 * @author song.wang
 * @date 2016/8/6
 */
@Named
public class CrmTeacherClueDao extends AlpsStaticMongoDao<CrmTeacherClue, String> {
    @Override
    protected void calculateCacheDimensions(CrmTeacherClue document, Collection<String> dimensions) {
    }

    public List<CrmTeacherClue> findBySchoolId(Long schoolId, CrmClueType type, Date startDate, Date endDate) {
        Criteria criteria = Criteria.where("schoolId").is(schoolId).and("type").is(type).and("createTime").gt(startDate).lt(endDate);
        Query query = Query.query(criteria);
        return query(query);
    }

    public List<CrmTeacherClue> findByTeacherIds(Collection<Long> teacherIds, CrmClueType type, Date startDate, Date endDate) {
        if (CollectionUtils.isEmpty(teacherIds)) {
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("teacherId").in(teacherIds).and("type").is(type).and("createTime").gt(startDate).lt(endDate);
        Query query = Query.query(criteria);
        return query(query);
    }

    public List<CrmTeacherClue> findByType(CrmClueType type, Date startDate, Date endDate) {
        Criteria criteria = Criteria.where("type").is(type).and("createTime").gt(startDate).lt(endDate);
        Query query = Query.query(criteria);
        return query(query);
    }


}
