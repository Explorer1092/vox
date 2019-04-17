package com.voxlearning.utopia.service.workflow.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.service.workflow.api.constants.WorkFlowType;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowProcess;

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
@CacheBean(type = WorkFlowProcess.class)
public class WorkFlowProcessPersistence extends AlpsStaticJdbcDao<WorkFlowProcess, Long> {
    @Override
    protected void calculateCacheDimensions(WorkFlowProcess document, Collection<String> dimensions) {
        dimensions.add(WorkFlowProcess.cacheKeyFromTargetUser(document.getSourceApp(), document.getTargetUser()));
        dimensions.add(WorkFlowProcess.cacheKeyFromWorkflowRecordId(document.getWorkflowRecordId()));
    }

    public List<WorkFlowProcess> loadByTargetUser(String sourceApp, String targetUser) {
        if (StringUtils.isBlank(sourceApp) || StringUtils.isBlank(targetUser)) {
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("SOURCE_APP").is(sourceApp)
                .and("TARGET_USER").is(targetUser)
                .and("DISABLED").is(false);
        return query(Query.query(criteria));
    }

    @CacheMethod
    public List<WorkFlowProcess> loadByWorkflowRecordId(@CacheParameter("workflowRecordId") Long workflowRecordId) {
        if (workflowRecordId == null) {
            return null;
        }
        Criteria criteria = Criteria.where("WORKFLOW_RECORD_ID").is(workflowRecordId)
                .and("DISABLED").is(false);
        return query(Query.query(criteria));
    }

    public int deleteByWorkflowRecordId(Long workflowRecordId) {
        if (workflowRecordId == null) {
            return 0;
        }
        Criteria criteria = Criteria.where("WORKFLOW_RECORD_ID").is(workflowRecordId);
        List<WorkFlowProcess> list = query(Query.query(criteria));
        if (CollectionUtils.isEmpty(list)) {
            return 0;
        }
        List<Long> idList = list.stream().map(WorkFlowProcess::getId).collect(Collectors.toList());
        return (int) super.removes(idList);
    }

    public List<WorkFlowProcess> loadByUserAndType(String sourceApp, String targetUser, WorkFlowType workFlowType, Date startDate, Date endDate) {
        if (StringUtils.isBlank(sourceApp) || StringUtils.isBlank(targetUser)) {
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("SOURCE_APP").is(sourceApp)
                .and("TARGET_USER").is(targetUser)
                .and("DISABLED").is(false);
        if (workFlowType != null) {
            criteria.and("WORK_FLOW_TYPE").is(workFlowType);
        }
        if(startDate != null){
            criteria.and("CREATE_DATETIME").gte(startDate);
            if(endDate != null){
                criteria.lt(endDate);
            }
        }else{
            if(endDate != null){
                criteria.and("CREATE_DATETIME").lt(endDate);
            }
        }

        return query(Query.query(criteria));
    }

}
