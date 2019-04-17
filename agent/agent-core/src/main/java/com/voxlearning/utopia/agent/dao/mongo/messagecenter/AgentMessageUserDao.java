package com.voxlearning.utopia.agent.dao.mongo.messagecenter;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.agent.persist.entity.messagecenter.AgentMessageUser;

import javax.inject.Named;
import java.util.List;
import java.util.stream.Collectors;

@Named
@CacheBean(type = AgentMessageUser.class)
public class AgentMessageUserDao extends StaticCacheDimensionDocumentMongoDao<AgentMessageUser,String> {

    @CacheMethod
    public List<AgentMessageUser> findByMessageId(@CacheParameter("messageId")String messageId){
        Criteria criteria = Criteria.where("messageId").is(messageId);
        Query query = Query.query(criteria);
        return query(query).stream().filter(p -> p.getDisabled() == false).collect(Collectors.toList());
    }
    @CacheMethod
    public AgentMessageUser findByUserIdAndMessageId(@CacheParameter("userId")Long userId, @CacheParameter("messageId")String messageId){
        Criteria criteria = Criteria.where("userId").is(userId).and("messageId").is(messageId);
        Query query = Query.query(criteria);
        return query(query).stream().findFirst().orElse(null);
    }
}
