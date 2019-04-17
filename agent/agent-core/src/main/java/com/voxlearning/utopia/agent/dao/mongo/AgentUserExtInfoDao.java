package com.voxlearning.utopia.agent.dao.mongo;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.agent.persist.entity.AgentUserCashRecord;
import com.voxlearning.utopia.agent.persist.entity.AgentUserExtInfo;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * 用户扩展信息Dao
 * Created by yaguang.wang on 2016/9/27.
 */
@Named
@CacheBean(type = AgentUserExtInfo.class)
public class AgentUserExtInfoDao  extends AlpsStaticMongoDao<AgentUserExtInfo, Long> {
    @Override
    protected void calculateCacheDimensions(AgentUserExtInfo document, Collection<String> dimensions) {
        dimensions.add(AgentUserExtInfo.ck_uid(document.getId()));
    }


    public List<AgentUserExtInfo> findByPage(int page,int size) {
        Criteria criteria = new Criteria();
        Sort sort = new Sort(Sort.Direction.DESC, "createTime");
        Pageable pageable = new PageRequest(page, size);
        Query query = Query.query(criteria).with(sort).with(pageable);
        return query(query);
    }
}
