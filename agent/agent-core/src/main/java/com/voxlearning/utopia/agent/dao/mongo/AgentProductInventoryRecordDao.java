package com.voxlearning.utopia.agent.dao.mongo;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.agent.persist.entity.AgentProductInventoryRecord;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * AgentProductInventoryRecordDao
 *
 * @author song.wang
 * @date 2016/11/18
 */
@Named
@CacheBean(type = AgentProductInventoryRecord.class)
public class AgentProductInventoryRecordDao extends AlpsStaticMongoDao<AgentProductInventoryRecord, String> {
    @Override
    protected void calculateCacheDimensions(AgentProductInventoryRecord document, Collection<String> dimensions) {
        dimensions.add(AgentProductInventoryRecord.ck_pid(document.getProductId()));
    }

    @CacheMethod
    public List<AgentProductInventoryRecord> findByProductId(@CacheParameter("pid")Long productId){
        Criteria criteria = Criteria.where("productId").is(productId);
        Sort sort = new Sort(Sort.Direction.ASC, "createTime");
        Query query = Query.query(criteria).with(sort);
        return query(query);
    }

}
