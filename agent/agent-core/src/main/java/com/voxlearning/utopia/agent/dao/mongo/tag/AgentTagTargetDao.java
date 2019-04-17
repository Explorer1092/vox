package com.voxlearning.utopia.agent.dao.mongo.tag;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.agent.constants.AgentTagTargetType;
import com.voxlearning.utopia.agent.persist.entity.tag.AgentTagTarget;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Named
@CacheBean(type = AgentTagTarget.class)
public class AgentTagTargetDao extends StaticCacheDimensionDocumentMongoDao<AgentTagTarget, String> {

    @CacheMethod
    public List<AgentTagTarget> loadByTagId(@CacheParameter(value = "tagId") Long tagId){
        Criteria criteria = Criteria.where("tagId").is(tagId);
        return query(Query.query(criteria));
    }

    @CacheMethod
    public Map<String,List<AgentTagTarget>> loadByTargetIdsAndType(@CacheParameter(value = "targetId",multiple = true) Collection<String> targetIds, @CacheParameter(value = "targetType")AgentTagTargetType targetType){
        Criteria criteria = Criteria.where("targetId").in(targetIds).and("targetType").is(targetType);
        return query(Query.query(criteria)).stream().collect(Collectors.groupingBy(AgentTagTarget::getTargetId));
    }

    @CacheMethod
    public Map<Long,List<AgentTagTarget>> loadByTagIds(@CacheParameter(value = "tagId",multiple = true) Collection<Long> tagIds){
        Criteria criteria = Criteria.where("tagId").in(tagIds);
        return query(Query.query(criteria)).stream().collect(Collectors.groupingBy(AgentTagTarget::getTagId));
    }
}