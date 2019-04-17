package com.voxlearning.utopia.agent.persist.exam;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.utopia.agent.persist.entity.exam.AgentKlxScanPaper;

import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author chunlin.yu
 * @create 2018-03-15 18:21
 **/

@Named
@CacheBean(type = AgentKlxScanPaper.class)
public class AgentKlxScanPaperPersistence extends AgentExamBasePersistence<AgentKlxScanPaper, Long> {

    @Override
    protected void calculateCacheDimensions(AgentKlxScanPaper document, Collection<String> dimensions) {

    }

    public AgentKlxScanPaper getPaperByPaperId(String paperId){
        if (null == paperId) {
            return null;
        }
        Criteria criteria = generateCriteriaWithDisabledField(null).and("PAPER_ID").is(paperId);
        return query(Query.query(criteria)).stream().findFirst().orElse(null);

    }

    public List<AgentKlxScanPaper> searchPaper(Long schoolId, Integer grade, String nameKey, Date scanBeginTime, Date scanEndTime) {
        if (null == schoolId || null == grade) {
            return Collections.emptyList();
        }
        Criteria criteria = generateCriteriaWithDisabledField(null).and("SCHOOL_ID").is(schoolId).and("PAPER_GRADE").is(grade);
        if (StringUtils.isNotEmpty(nameKey)) {
            criteria.and("PAPER_TITLE").like("%" + nameKey + "%");
        }
        if (null != scanBeginTime || null != scanEndTime) {
            criteria.and("LAST_SCAN_TIME");
            if (scanBeginTime != null) {
                criteria.gte(scanBeginTime);
            }
            if (scanEndTime != null) {
                criteria.lte(scanEndTime);
            }
        }
        Query query = Query.query(criteria);
        return query(query);
    }
}
