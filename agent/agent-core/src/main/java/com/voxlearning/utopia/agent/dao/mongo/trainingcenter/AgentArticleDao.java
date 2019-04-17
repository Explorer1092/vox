package com.voxlearning.utopia.agent.dao.mongo.trainingcenter;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.agent.persist.entity.trainingcenter.AgentArticle;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupSchool;

import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Named
@CacheBean(type = AgentArticle.class)
public class AgentArticleDao extends StaticCacheDimensionDocumentMongoDao<AgentArticle, String> {

    public List<AgentArticle> loadArticleByCondition(String oneLevelColumnId,String twoLevelColumnId, String title){
        Criteria criteria = Criteria.where("disabled").is(false);
        if (StringUtils.isNotBlank(oneLevelColumnId)){
            criteria.and("oneLevelColumnId").is(oneLevelColumnId);
        }
        if (StringUtils.isNotBlank(twoLevelColumnId)){
            criteria.and("twoLevelColumnId").is(twoLevelColumnId);
        }
        if (StringUtils.isNotBlank(title)){
            criteria.and("title").is(title);
        }
        return query(Query.query(criteria));
    }

    @CacheMethod
    public List<AgentArticle> loadArticleByTwoLevelColumnId(@CacheParameter(value = "tcid") String twoLevelColumnId){
        Criteria criteria = Criteria.where("disabled").is(false);
        criteria.and("twoLevelColumnId").is(twoLevelColumnId);
        return query(Query.query(criteria));
    }

    @CacheMethod
    public Map<String,List<AgentArticle>> loadArticleByTwoLevelColumnIds(@CacheParameter(value = "tcid" ,multiple = true) Collection<String> twoLevelColumnIds){
        Criteria criteria = Criteria.where("disabled").is(false);
        criteria.and("twoLevelColumnId").in(twoLevelColumnIds);
        return query(Query.query(criteria)).stream().collect(Collectors.groupingBy(AgentArticle::getTwoLevelColumnId));
    }

}