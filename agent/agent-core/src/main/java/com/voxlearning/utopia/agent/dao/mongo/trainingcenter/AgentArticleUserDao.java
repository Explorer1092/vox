package com.voxlearning.utopia.agent.dao.mongo.trainingcenter;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.agent.persist.entity.trainingcenter.AgentArticleUser;

import javax.inject.Named;
import java.util.List;


@Named
@CacheBean(type = AgentArticleUser.class)
public class AgentArticleUserDao extends StaticCacheDimensionDocumentMongoDao<AgentArticleUser, String> {

    @CacheMethod
    public List<AgentArticleUser> loadByArticleId(@CacheParameter(value = "aid") String articleId){
        Criteria criteria = Criteria.where("disabled").is(false);
        criteria.and("articleId").is(articleId);
        return query(Query.query(criteria));
    }

}