package com.voxlearning.utopia.agent.persist.exam;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.utopia.agent.persist.entity.exam.AgentExamSubject;

import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author chunlin.yu
 * @create 2018-03-15 12:12
 **/

@Named
@CacheBean(type = AgentExamSubject.class)
public class AgentExamSubjectPersistence extends AgentExamBasePersistence<AgentExamSubject, Long> {


  /*  public List<AgentExamSubject> loads(Long agentExamSchoolId, Subject subject, Integer grade) {
        Criteria criteria = generateCriteriaWithDisabledField(false);
        if (agentExamSchoolId != null) {
            criteria.and("AGENT_EXAM_SCHOOL_ID").is(agentExamSchoolId);
        }
        if (subject != null) {
            criteria.and("SUBJECT").is(subject);
        }
        if (grade != null) {
            criteria.and("GRADE").is(grade);
        }
        Query query = Query.query(criteria);
        return query(query);
    }

    public List<AgentExamSubject> loadByExamSchoolIds(Collection<Long> examSchoolIds) {
        if (CollectionUtils.isEmpty(examSchoolIds)) {
            return Collections.emptyList();
        }
        Criteria criteria = generateCriteriaWithDisabledField(false).and("AGENT_EXAM_SCHOOL_ID").in(examSchoolIds);
        Query query = Query.query(criteria);
        return query(query);
    }
*/

    public List<AgentExamSubject> loadByExamGradeIds(Collection<Long> examGradeIds) {
        if (CollectionUtils.isEmpty(examGradeIds)) {
            return Collections.emptyList();
        }
        Criteria criteria = generateCriteriaWithDisabledField(false).and("AGENT_EXAM_GRADE_ID").in(examGradeIds);
        Query query = Query.query(criteria);
        return query(query);
    }

    @Override
    protected void calculateCacheDimensions(AgentExamSubject document, Collection<String> dimensions) {

    }

    public List<AgentExamSubject> loads(Long agentExamGradeId, Subject subject) {
        Criteria criteria = generateCriteriaWithDisabledField(false);
        if (agentExamGradeId != null) {
            criteria.and("AGENT_EXAM_GRADE_ID").is(agentExamGradeId);
        }
        if (subject != null) {
            criteria.and("SUBJECT").is(subject);
        }
        Query query = Query.query(criteria);
        return query(query);
    }
}
