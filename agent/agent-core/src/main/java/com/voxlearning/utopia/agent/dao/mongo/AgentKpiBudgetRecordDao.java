package com.voxlearning.utopia.agent.dao.mongo;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.agent.persist.entity.AgentKpiBudgetRecord;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AgentKpiBudgetRecordDao
 *
 * @author song.wang
 * @date 2018/2/27
 */
@Named
@CacheBean(type = AgentKpiBudgetRecord.class)
public class AgentKpiBudgetRecordDao extends StaticCacheDimensionDocumentMongoDao<AgentKpiBudgetRecord, String> {

    @CacheMethod
    public Map<String, List<AgentKpiBudgetRecord>> loadByKpiBudgetIds(@CacheParameter(value = "kbId", multiple = true) Collection<String> kpiBudgetIds){
        Criteria criteria = Criteria.where("kpiBudgetId").in(kpiBudgetIds);
        Query query = Query.query(criteria);
        return query(query).stream().collect(Collectors.groupingBy(AgentKpiBudgetRecord::getKpiBudgetId, Collectors.toList()));
    }

}
