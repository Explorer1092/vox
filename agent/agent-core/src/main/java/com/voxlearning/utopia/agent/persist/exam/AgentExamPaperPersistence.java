package com.voxlearning.utopia.agent.persist.exam;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.utopia.agent.persist.entity.exam.AgentExamPaper;

import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author chunlin.yu
 * @create 2018-03-15 12:10
 **/

@Named
@CacheBean(type = AgentExamPaper.class)
public class AgentExamPaperPersistence extends AgentExamBasePersistence<AgentExamPaper, Long> {

    public AgentExamPaper load(Long examSubjectId,String paperId){
        if (null == examSubjectId || StringUtils.isEmpty(paperId)){
            return null;
        }
        Criteria criteria = generateCriteriaWithDisabledField(false).and("AGENT_EXAM_SUBJECT_ID").is(examSubjectId).and("PAPER_ID").is(paperId);
        Query query = Query.query(criteria);
        return query(query).stream().findFirst().orElse(null);
    }

    public List<AgentExamPaper> loadByExamSubjectIds(Collection<Long> examSubjectIdSet) {
        if (CollectionUtils.isEmpty(examSubjectIdSet)){
            return Collections.emptyList();
        }
        Criteria criteria = generateCriteriaWithDisabledField(false).and("AGENT_EXAM_SUBJECT_ID").in(examSubjectIdSet);
        Query query = Query.query(criteria);
        return query(query);
    }


    @Override
    protected void calculateCacheDimensions(AgentExamPaper document, Collection<String> dimensions) {

    }

}
