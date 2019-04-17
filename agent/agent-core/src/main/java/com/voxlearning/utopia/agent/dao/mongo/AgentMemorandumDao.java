package com.voxlearning.utopia.agent.dao.mongo;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.agent.constants.AgentMemorandumGenre;
import com.voxlearning.utopia.agent.constants.MemorandumType;
import com.voxlearning.utopia.agent.persist.entity.memorandum.AgentMemorandum;

import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created by yaguang.wang
 * on 2017/5/10.
 */
@Named
@CacheBean(type = AgentMemorandum.class)
public class AgentMemorandumDao extends AlpsStaticMongoDao<AgentMemorandum, String> {
    @Override
    protected void calculateCacheDimensions(AgentMemorandum document, Collection<String> dimensions) {
        dimensions.add(AgentMemorandum.ck_sid(document.getSchoolId()));
        dimensions.add(AgentMemorandum.ck_uid(document.getCreateUserId()));
        dimensions.add(AgentMemorandum.ck_tid(document.getTeacherId()));
    }

    public List<AgentMemorandum> findAll(Integer page, Integer pageSize) {
        Criteria criteria = Criteria.where("disabled").is(false);
        Query query = Query.query(criteria);
        query.skip((page - 1) * (pageSize)).limit(pageSize);
        return query(query);
    }

    @CacheMethod
    public List<AgentMemorandum> findBySchoolId(@CacheParameter(value = "sid") Long schoolId) {
        Criteria criteria = Criteria.where("schoolId").is(schoolId);
        criteria.and("disabled").is(false);
        criteria.and("genre").is(AgentMemorandumGenre.SCHOOL);
        Sort sort = new Sort(Sort.Direction.DESC, "writeTime");
        Query query = Query.query(criteria).with(sort);
        return query(query);
    }

    public List<AgentMemorandum> findByIntoSchoolRecordId(String intoSchoolRecordId) {
        Criteria criteria = Criteria.where("intoSchoolRecordId").is(intoSchoolRecordId);
        criteria.and("disabled").is(false);
        Query query = Query.query(criteria);
        return query(query);
    }

    @CacheMethod
    public List<AgentMemorandum> findByTeacherId(@CacheParameter(value = "tid") Long teacherId) {
        Criteria criteria = Criteria.where("teacherId").is(teacherId);
        criteria.and("genre").is(AgentMemorandumGenre.TEACHER);
        criteria.and("disabled").is(false);
        Sort sort = new Sort(Sort.Direction.DESC, "writeTime");
        Query query = Query.query(criteria).with(sort);
        return query(query);
    }


    @CacheMethod
    public List<AgentMemorandum> findByCreateUserId(@CacheParameter(value = "uid") Long userId) {
        Criteria criteria = Criteria.where("createUserId").is(userId);
        criteria.and("disabled").is(false);
        Sort sort = new Sort(Sort.Direction.DESC, "writeTime");
        Query query = Query.query(criteria).with(sort);
        return query(query);
    }


    public List<AgentMemorandum> findBySchoolIdLimitPage(Long schoolId, Integer page, Integer pageSize, MemorandumType type) {
        Criteria criteria = Criteria.where("schoolId").is(schoolId);
        criteria.and("disabled").is(false);
        criteria.and("genre").is(AgentMemorandumGenre.SCHOOL);
        is(criteria, "type", type);
        Sort sort = new Sort(Sort.Direction.DESC, "writeTime");
        Query query = Query.query(criteria).with(sort);
        query.skip((page - 1) * (pageSize)).limit(pageSize);
        return query(query);
    }


    public List<AgentMemorandum> findByCreateUserIdLimitPage(Long userId, Integer page, Integer pageSize, MemorandumType type, Date startTime, Date endTime) {
        Criteria criteria = Criteria.where("createUserId").is(userId);
        criteria.and("disabled").is(false);
        is(criteria, "type", type);
        range(criteria, "writeTime", startTime, endTime);
        Sort sort = new Sort(Sort.Direction.DESC, "writeTime");
        Query query = Query.query(criteria).with(sort);
        query.skip((page - 1) * (pageSize)).limit(pageSize);
        return query(query);
    }

    public List<AgentMemorandum> findByTeacherIdLimitPage(Long teacherId, Integer page, Integer pageSize, MemorandumType type) {
        Criteria criteria = Criteria.where("teacherId").is(teacherId);
        criteria.and("genre").is(AgentMemorandumGenre.TEACHER);
        criteria.and("disabled").is(false);
        is(criteria, "type", type);
        Sort sort = new Sort(Sort.Direction.DESC, "writeTime");
        Query query = Query.query(criteria).with(sort);
        query.skip((page - 1) * (pageSize)).limit(pageSize);
        return query(query);
    }

    public boolean deleteAgentMemorandum(String id) {
        AgentMemorandum memorandum = load(id);
        if (memorandum == null) {
            return false;
        }
        memorandum.setDisabled(true);
        memorandum = replace(memorandum);
        return memorandum != null;
    }

    private void is(Criteria criteria, String key, Object object) {
        if (object != null) {
            criteria.and(key).is(object);
        }
    }

    private void range(Criteria criteria, String key, Date gte, Date lte) {
        if (gte != null && lte != null) {
            criteria.and(key).gte(gte).lte(lte);
        } else if (gte != null) {
            criteria.and(key).gte(gte);
        } else if (lte != null) {
            criteria.and(key).lte(lte);
        }
    }
}
