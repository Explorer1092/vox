/**
 * Author:   xianlong.zhang
 * Date:     2018/8/3 12:17
 * Description: agent用户修改教研员日志表
 * History:
 */
package com.voxlearning.utopia.agent.dao.mongo;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.agent.persist.entity.AgentResearchersUpdateLog;

import javax.inject.Named;
import java.util.List;

@Named
@CacheBean(type = AgentResearchersUpdateLog.class)
public class AgentResearchersUpdateLogDao extends StaticCacheDimensionDocumentMongoDao<AgentResearchersUpdateLog, String> {

    @CacheMethod
    public List<AgentResearchersUpdateLog> findByUserIdAndResearchersId(@CacheParameter("researchersId")Long researchersId){
        Criteria criteria = Criteria.where("researchersId").is(researchersId);
        Sort sort = new Sort(Sort.Direction.DESC, "createTime");
        Query query = new Query(criteria).with(sort);

        return query(query);
    }
}
