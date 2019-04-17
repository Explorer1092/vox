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
import com.voxlearning.utopia.agent.persist.entity.publish.AgentPublishData;
import org.springframework.util.CollectionUtils;

import javax.inject.Named;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * AgentPublishDataDao
 *
 * @author song.wang
 * @date 2018/4/23
 */
@Named
@CacheBean(type = AgentPublishData.class)
public class AgentPublishDataDao extends StaticCacheDimensionDocumentMongoDao<AgentPublishData, String> {

    public List<AgentPublishData> loadByPublishId(String publishId){
        Criteria criteria = Criteria.where("publishId").is(publishId).and("disabled").is(false);
        return query(Query.query(criteria));
    }

    public void deleteByPublishId(String publishId){
        Update update = Update.update("disabled", true);
        Criteria criteria = Criteria.where("publishId").is(publishId);
        executeUpdateMany(createMongoConnection(), criteria, update);
    }
}
