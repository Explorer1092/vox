package com.voxlearning.utopia.agent.dao.mongo;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.agent.persist.entity.AgentNeedFollowUp;

import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AgentNeedFollowUpDao
 *
 * @author song.wang
 * @date 2016/7/28
 */
@Named
@CacheBean(type = AgentNeedFollowUp.class)
public class AgentNeedFollowUpDao extends AlpsStaticMongoDao<AgentNeedFollowUp, String> {
    @Override
    protected void calculateCacheDimensions(AgentNeedFollowUp document, Collection<String> dimensions) {
        dimensions.add(AgentNeedFollowUp.ck_sid_day(document.getSchoolId(), document.getDay()));
    }

    @CacheMethod
    public Map<Long, List<AgentNeedFollowUp>> findBySchoolList(@CacheParameter(value = "schoolId", multiple = true)List<Long> schoolList, @CacheParameter("day")Integer day){
        Criteria criteria = Criteria.where("schoolId").in(schoolList).and("day").is(day);
        Query query = Query.query(criteria);
        List<AgentNeedFollowUp> resultList = query(query);
        if(CollectionUtils.isEmpty(resultList)){
            return Collections.emptyMap();
        }
        return resultList.stream().collect(Collectors.groupingBy(AgentNeedFollowUp::getSchoolId, Collectors.toList()));
    }

}
