package com.voxlearning.utopia.agent.dao.mongo;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.agent.persist.entity.AgentRegionMessage;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * AgentRegionMessageDao
 *
 * @author song.wang
 * @date 2016/7/27
 */
@Named
@CacheBean(type = AgentRegionMessage.class)
public class AgentRegionMessageDao extends AlpsStaticMongoDao<AgentRegionMessage, String> {
    @Override
    protected void calculateCacheDimensions(AgentRegionMessage document, Collection<String> dimensions) {
        dimensions.add(AgentRegionMessage.ck_gid(document.getGroupId()));
    }

    @CacheMethod
    public List<AgentRegionMessage> findByGroupId(@CacheParameter("gid") Long groupId){
        Criteria criteria = Criteria.where("groupId").is(groupId);
        Sort sort = new Sort(Sort.Direction.DESC, "createTime");
        Query query = Query.query(criteria).with(sort);
        return query(query);
    }


}
