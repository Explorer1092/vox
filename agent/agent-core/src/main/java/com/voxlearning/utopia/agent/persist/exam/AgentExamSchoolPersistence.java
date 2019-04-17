package com.voxlearning.utopia.agent.persist.exam;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.agent.persist.entity.exam.AgentExamPaper;
import com.voxlearning.utopia.agent.persist.entity.exam.AgentExamSchool;

import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author chunlin.yu
 * @create 2018-03-15 12:12
 **/

@Named
@CacheBean(type = AgentExamSchool.class)
public class AgentExamSchoolPersistence extends AgentExamBasePersistence<AgentExamSchool, Long> {

    /**
     * 按照月份查询
     * @param month
     * @return
     */
    public List<AgentExamSchool> loadByMonth(Integer month) {
        if (null == month){
            return Collections.emptyList();
        }
        Criteria criteria = generateCriteriaWithDisabledField(false).and("MONTH").is(month);
        Query query = Query.query(criteria);
        return query(query);
    }

    /**
     * 按照学校ID和月份月份查询
     * @param month
     * @return
     */
    public AgentExamSchool loadBySchoolIdAndMonth(Long schoolId,Integer month) {
        Criteria criteria = generateCriteriaWithDisabledField(false).and("MONTH").is(month).and("SCHOOL_ID").is(schoolId);
        Query query = Query.query(criteria);
        return query(query).stream().findFirst().orElse(null);
    }

    public List<AgentExamSchool> loadBySchoolId(Long schoolId) {
        Criteria criteria = generateCriteriaWithDisabledField(false).and("SCHOOL_ID").is(schoolId);
        Query query = Query.query(criteria);
        return query(query);
    }


    public List<AgentExamSchool> loads(List<Long> schoolIds, Integer cityCode, Integer month) {
        Criteria criteria = generateCriteriaWithDisabledField(false);
        if (CollectionUtils.isNotEmpty(schoolIds)){
            criteria.and("SCHOOL_ID").in(schoolIds);
        }
        if (null != cityCode){
            criteria.and("CITY_CODE").is(cityCode);
        }
        if (null != month){
            criteria.and("MONTH").is(month);
        }
        Query query = Query.query(criteria);
        return query(query);
    }


    @Override
    protected void calculateCacheDimensions(AgentExamSchool document, Collection<String> dimensions) {

    }

}
