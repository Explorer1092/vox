package com.voxlearning.utopia.service.crm.impl.dao.agent.work;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.service.crm.api.entities.agent.work.WorkRecordTeacher;

import javax.inject.Named;
import java.util.List;

/**
 * WorkRecordTeacherDao
 *
 * @author deliang.che
 * @since  2018/12/17
 */
@Named
@CacheBean(type = WorkRecordTeacher.class)
public class WorkRecordTeacherDao extends StaticCacheDimensionDocumentMongoDao<WorkRecordTeacher, String> {
    @CacheMethod
    public List<WorkRecordTeacher> getWorkRecordTeacherByTeacher(@CacheParameter("teacherId") Long teacherId){
        Criteria criteria = Criteria.where("teacherId").is(teacherId);
        Query query = Query.query(criteria);
        return query(query);
    }
}
