package com.voxlearning.utopia.agent.dao.mongo;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.agent.constants.AgentTargetType;
import com.voxlearning.utopia.agent.persist.entity.AgentTargetTag;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 *
 * @author song.wang
 * @date 2017/5/23
 */
@Named
@CacheBean(type = AgentTargetTag.class)
public class AgentTargetTagDao extends StaticCacheDimensionDocumentMongoDao<AgentTargetTag, String> {

    @CacheMethod
    public AgentTargetTag loadByTarget(@CacheParameter("TID") Long targetId, @CacheParameter("TYPE")AgentTargetType targetType) {
        Criteria criteria = Criteria.where("targetType").is(targetType).and("targetId").is(targetId);
        return query(Query.query(criteria)).stream().findFirst().orElse(null);
    }

    @CacheMethod
    public Map<Long, AgentTargetTag> loadTargetTags(@CacheParameter(value = "TID", multiple = true) Collection<Long> targetIds, @CacheParameter("TYPE")AgentTargetType targetType){
        Criteria criteria = Criteria.where("targetType").is(targetType).and("targetId").in(targetIds);
        return query(Query.query(criteria)).stream().collect(Collectors.toMap(AgentTargetTag::getTargetId, Function.identity(), (o1, o2) -> o1));
    }

    /**
     * 迁移历史数据使用
     * @return
     */
    public List<AgentTargetTag> loadAll(){
        Criteria criteria = new Criteria();
        return query(Query.query(criteria));
    }
}
