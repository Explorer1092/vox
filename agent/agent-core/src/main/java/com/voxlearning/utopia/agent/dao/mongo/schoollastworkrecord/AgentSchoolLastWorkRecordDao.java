package com.voxlearning.utopia.agent.dao.mongo.schoollastworkrecord;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.agent.persist.entity.schoollastworkrecord.AgentSchoolLastWorkRecord;

import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * AgentSchoolLastVisitTimeDao
 *
 * @author deliang.che
 * @since  2019/3/4
 */
@Named
@CacheBean(type = AgentSchoolLastWorkRecord.class)
public class AgentSchoolLastWorkRecordDao extends StaticCacheDimensionDocumentMongoDao<AgentSchoolLastWorkRecord, String> {

    public Map<Long,AgentSchoolLastWorkRecord> loadBySchoolIds(Collection<Long> schoolIds){
        if (CollectionUtils.isEmpty(schoolIds)){
            return Collections.emptyMap();
        }
        Criteria criteria = Criteria.where("schoolId").in(schoolIds);
        List<AgentSchoolLastWorkRecord> schoolLastVisitTimeList = query(Query.query(criteria));
        if (CollectionUtils.isEmpty(schoolLastVisitTimeList)){
            return Collections.emptyMap();
        }
        return schoolLastVisitTimeList.stream().collect(Collectors.toMap(AgentSchoolLastWorkRecord::getSchoolId,Function.identity(),(o1, o2) -> o1));
    }

    public AgentSchoolLastWorkRecord loadBySchoolId(Long schoolId){
        if (schoolId == null){
            return null;
        }
        Criteria criteria = Criteria.where("schoolId").is(schoolId);
        return query(Query.query(criteria)).stream().filter(Objects::nonNull).findFirst().orElse(null);
    }
}
