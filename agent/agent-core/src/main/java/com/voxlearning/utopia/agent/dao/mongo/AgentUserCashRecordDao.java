package com.voxlearning.utopia.agent.dao.mongo;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.agent.persist.entity.AgentUserCashRecord;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * AgentUserCashRecordDao
 *
 * @author song.wang
 * @date 2017/1/15
 */
@Named
@CacheBean(type = AgentUserCashRecord.class)
public class AgentUserCashRecordDao extends AlpsStaticMongoDao<AgentUserCashRecord, String> {
    @Override
    protected void calculateCacheDimensions(AgentUserCashRecord document, Collection<String> dimensions) {
        dimensions.add(AgentUserCashRecord.ck_uid(document.getUserId()));
    }

    public List<AgentUserCashRecord> findByUserId(Long userId) {
        Criteria criteria = Criteria.where("userId").is(userId);
        Sort sort = new Sort(Sort.Direction.DESC, "createTime");
        Query query = Query.query(criteria).with(sort);
        return query(query);
    }
}
