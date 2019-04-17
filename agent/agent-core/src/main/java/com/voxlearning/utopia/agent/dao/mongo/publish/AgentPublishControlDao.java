package com.voxlearning.utopia.agent.dao.mongo.publish;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.utopia.agent.persist.entity.publish.AgentPublishControl;
import org.springframework.util.CollectionUtils;

import javax.inject.Named;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * AgentPublishControlDao
 *
 * @author song.wang
 * @date 2018/4/23
 */
@Named
@CacheBean(type = AgentPublishControl.class)
public class AgentPublishControlDao extends StaticCacheDimensionDocumentMongoDao<AgentPublishControl, String> {

    @CacheMethod
    public Map<String,AgentPublishControl> loadByPublishIds(@CacheParameter(value = "pid",multiple = true) List<String> publishIds){
        Criteria criteria = Criteria.where("publishId").in(publishIds).and("disabled").is(false);
        Query query = Query.query(criteria);
        List<AgentPublishControl> resultList = query(query);
        if(CollectionUtils.isEmpty(resultList)){
            return Collections.emptyMap();
        }
        return resultList.stream().collect(Collectors.toMap(AgentPublishControl::getPublishId, Function.identity(),(o1, o2) -> o1));
    }

    @CacheMethod
    public AgentPublishControl loadByPublishId(@CacheParameter(value = "pid") String publishId){
        Criteria criteria = Criteria.where("publishId").is(publishId).and("disabled").is(false);
        Query query = Query.query(criteria);
        return query(query).stream().findFirst().orElse(null);
    }

    public void deleteByPublishId(String publishId){
        Update update = Update.update("disabled", true);
        Criteria criteria = Criteria.where("publishId").is(publishId);
        executeUpdateMany(createMongoConnection(), criteria, update);

        // 清除缓存
        String key = CacheKeyGenerator.generateCacheKey(AgentPublishControl.class, "pid", publishId);
        getCache().delete(key);
    }
}
