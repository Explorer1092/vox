package com.voxlearning.utopia.service.crm.impl.dao.agent.work;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.service.crm.api.entities.agent.work.WorkRecordSchool;

import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * WorkRecordSchoolDao
 *
 * @author deliang.che
 * @since  2018/12/17
 */
@Named
@CacheBean(type = WorkRecordSchool.class)
public class WorkRecordSchoolDao extends StaticCacheDimensionDocumentMongoDao<WorkRecordSchool, String> {

    public List<WorkRecordSchool> findByWorkersAndTime(Collection<Long> userIds, Date startDate, Date endDate){
        Criteria criteria = Criteria.where("userId").in(userIds);
        criteria.and("workTime").gte(startDate).lt(endDate);
        return query(Query.query(criteria));
    }

    @CacheMethod
    public List<WorkRecordSchool> loadBySchoolId(@CacheParameter("sid") Long schoolId){
        Criteria criteria = Criteria.where("schoolId").is(schoolId);
        return query(Query.query(criteria));
    }
}
