package com.voxlearning.utopia.agent.dao.mongo;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.agent.persist.entity.AgentUserMaterialBudgetRecord;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * AgentUserMaterialBudgetRecordDao
 *
 * @author song.wang
 * @date 2017/1/15
 */
@Named
@CacheBean(type = AgentUserMaterialBudgetRecord.class)
public class AgentUserMaterialBudgetRecordDao extends AlpsStaticMongoDao<AgentUserMaterialBudgetRecord, String> {
    @Override
    protected void calculateCacheDimensions(AgentUserMaterialBudgetRecord document, Collection<String> dimensions) {
        dimensions.add(AgentUserMaterialBudgetRecord.ck_uid(document.getUserId()));
    }

    public List<AgentUserMaterialBudgetRecord> findByUserId(Long userId) {
        Criteria criteria = Criteria.where("userId").is(userId);
        Sort sort = new Sort(Sort.Direction.DESC, "createTime");
        Query query = Query.query(criteria).with(sort);
        return query(query);
    }
}
