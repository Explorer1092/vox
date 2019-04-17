package com.voxlearning.utopia.agent.dao.mongo.publish;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.agent.persist.entity.publish.AgentPublish;

import javax.inject.Named;
import java.util.List;

/**
 * AgentPublishDao
 *
 * @author song.wang
 * @date 2018/4/23
 */
@Named
@CacheBean(type = AgentPublish.class)
public class AgentPublishDao extends StaticCacheDimensionDocumentMongoDao<AgentPublish, String> {

    public List<AgentPublish> loadAll(){
        Criteria criteria = Criteria.where("disabled").is(false);
        Sort sort = new Sort(Sort.Direction.DESC, "updateTime");
        Query query = Query.query(criteria).with(sort);
        return query(query);
    }
}
