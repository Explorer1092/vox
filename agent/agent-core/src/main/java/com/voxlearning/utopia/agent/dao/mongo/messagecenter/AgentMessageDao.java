package com.voxlearning.utopia.agent.dao.mongo.messagecenter;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.agent.persist.entity.messagecenter.AgentMessage;

import javax.inject.Named;
import java.util.Date;
import java.util.List;

@Named
@CacheBean(type = AgentMessage.class)
public class AgentMessageDao extends StaticCacheDimensionDocumentMongoDao<AgentMessage,String> {

    public List<AgentMessage> findAgentNotifyList(Long createUserId, Integer msgStatus, Date beginDate, Date endDate){
        Criteria criteria = new Criteria();
        if (createUserId != null && createUserId > 0) {
            criteria.and("createUserId").is(createUserId);
        }
        if (msgStatus != null) {
            criteria.and("msgStatus").is(msgStatus);
        }
        criteria.and("createTime");
        if (null != beginDate || null != endDate) {
            if (beginDate != null ) {
                criteria.gte(beginDate);
            }
            if (endDate != null) {
                criteria.lte(endDate);
            }
        }
        Sort sort = new Sort(Sort.Direction.DESC, "createTime");
        Query query = Query.query(criteria).with(sort);
        return query(query);
    }
}
