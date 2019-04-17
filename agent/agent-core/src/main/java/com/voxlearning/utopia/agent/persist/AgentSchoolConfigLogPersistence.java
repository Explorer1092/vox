package com.voxlearning.utopia.agent.persist;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.agent.persist.entity.AgentSchoolConfigLog;

import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 *
 * Created by yaguang.wang
 * on 2017/3/28.
 */
@Named
@CacheBean(type = AgentSchoolConfigLog.class)
public class AgentSchoolConfigLogPersistence extends AlpsStaticJdbcDao<AgentSchoolConfigLog, Long> {
    @Override
    protected void calculateCacheDimensions(AgentSchoolConfigLog document, Collection<String> dimensions) {

    }

    @CacheMethod(key = "ALL")
    public List<AgentSchoolConfigLog> findAllSchoolConfigLog() {
        Criteria criteria = new Criteria();
        Query query = Query.query(criteria);
        return query(query);
    }

    public List<AgentSchoolConfigLog> findSchoolConfigLogs(Long schoolId, Integer operationType) {
        Criteria criteria = Criteria.where("SCHOOL_ID").is(schoolId);
        criteria.and("OPERATION_TYPE").is(operationType);
        Sort sort = new Sort(Sort.Direction.DESC, "OPERATING_TIME");
        return query(Query.query(criteria).with(sort));
    }


    public AgentSchoolConfigLog findSchoolConfigLogLastOne(Long schoolId, Integer operationType) {
        Criteria criteria = Criteria.where("SCHOOL_ID").is(schoolId);
        criteria.and("OPERATION_TYPE").is(operationType);
        Sort sort = new Sort(Sort.Direction.DESC, "OPERATING_TIME");
        return query(Query.query(criteria).with(sort)).stream().findFirst().orElse(null);
    }

    public List<AgentSchoolConfigLog> findSchoolConfigLogByTime(Date startDate, Date endDate) {
        Criteria criteria = new Criteria();
        smartFilter(criteria,"OPERATING_TIME",startDate,endDate);
        Query query = Query.query(criteria);
        return query(query);
    }


    private void smartFilter(Criteria criteria, String key, Object foot, Object top) {
        if (foot != null && top != null) {
            criteria.and(key).gte(foot).lt(top);
        } else if (foot != null) {
            criteria.and(key).gte(foot);
        } else if (top != null) {
            criteria.and(key).lt(top);
        }
    }
}
