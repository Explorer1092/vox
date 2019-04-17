package com.voxlearning.utopia.agent.persist.exam;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.utopia.agent.persist.entity.exam.AgentExamGrade;

import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 大考
 *
 * @author chunlin.yu
 * @create 2018-04-18 21:31
 **/

@Named
@CacheBean(type = AgentExamGrade.class)
public class AgentExamGradePersistence  extends AgentExamBasePersistence<AgentExamGrade, Long> {

    public List<AgentExamGrade> loadByExamSchoolIds(Collection<Long> examSchoolIds) {
        if (CollectionUtils.isEmpty(examSchoolIds)) {
            return Collections.emptyList();
        }
        Criteria criteria = generateCriteriaWithDisabledField(false).and("AGENT_EXAM_SCHOOL_ID").in(examSchoolIds);
        Query query = Query.query(criteria);
        return query(query);
    }
    @Override
    protected void calculateCacheDimensions(AgentExamGrade document, Collection<String> dimensions) {

    }

    public List<AgentExamGrade> loads(Long agentExamSchoolId, Integer grade) {
        Criteria criteria = generateCriteriaWithDisabledField(false).and("AGENT_EXAM_SCHOOL_ID").is(agentExamSchoolId).and("GRADE").is(grade);
        Query query = Query.query(criteria);
        return query(query);
    }
}
