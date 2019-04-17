package com.voxlearning.utopia.service.crm.impl.dao.agent.work;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.dao.StaticCacheDimensionDocumentMongoDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentWorkRecordType;
import com.voxlearning.utopia.service.crm.api.entities.agent.work.WorkRecordOuterResource;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * WorkRecordOuterResourceDao
 *
 * @author deliang.che
 * @since  2018/12/17
 */
@Named
@CacheBean(type = WorkRecordOuterResource.class)
public class WorkRecordOuterResourceDao extends StaticCacheDimensionDocumentMongoDao<WorkRecordOuterResource, String> {

    public void updateWorkRecordIdAndType(Collection<String> ids, String workRecordId, AgentWorkRecordType workRecordType){
        if (CollectionUtils.isNotEmpty(ids)){
            Update update = Update.update("workRecordId", workRecordId);
            update.set("workRecordType",workRecordType);
            Criteria criteria = Criteria.where("_id").in(ids);

            Query query = Query.query(criteria);
            List<WorkRecordOuterResource> list = query(query);

            executeUpdateMany(createMongoConnection(), criteria, update);
            // 清除缓存
            evictDocumentCache(list);
        }
    }


    @CacheMethod
    public List<WorkRecordOuterResource> findByResourceId(@CacheParameter("orid") Long resourceId){
        Criteria criteria = Criteria.where("outerResourceId").is(resourceId);
        Sort sort = new Sort(Sort.Direction.DESC, "createTime");
        return query(Query.query(criteria).with(sort));
    }

}
