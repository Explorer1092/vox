package com.voxlearning.utopia.service.workflow.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowProcess;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowRecord;
import com.voxlearning.utopia.service.workflow.api.constants.WorkFlowType;

import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author fugui.chang
 * @since 2016/11/7
 */
@Named
@CacheBean(type = WorkFlowRecord.class)
public class WorkFlowRecordPersistence extends AlpsStaticJdbcDao<WorkFlowRecord,Long> {

    @Override
    protected void calculateCacheDimensions(WorkFlowRecord document, Collection<String> dimensions) {
        dimensions.add(WorkFlowRecord.cacheKeyFromCreatorAccount(document.getSourceApp(),document.getCreatorAccount()));
    }

    @CacheMethod
    public List<WorkFlowRecord> loadByCreatorAccount(@CacheParameter("sourceApp") String sourceApp,@CacheParameter("creatorAccount") String creatorAccount){
        if(StringUtils.isBlank(sourceApp) || StringUtils.isBlank(creatorAccount)){
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("SOURCE_APP").is(sourceApp)
                .and("creator_account").is(creatorAccount)
                .and("DISABLED").is(false);
        return query(Query.query(criteria));
    }

    public int disableByWorkflowRecordId(Long workflowRecordId){
        if(workflowRecordId == null){
            return 0;
        }
        Criteria criteria = Criteria.where("ID").is(workflowRecordId);
        List<WorkFlowRecord> list = query(Query.query(criteria));
        if(CollectionUtils.isEmpty(list)){
            return 0;
        }
        list.forEach(p -> {
            p.setDisabled(true);
            super.replace(p);
        });
        return list.size();
    }

}
