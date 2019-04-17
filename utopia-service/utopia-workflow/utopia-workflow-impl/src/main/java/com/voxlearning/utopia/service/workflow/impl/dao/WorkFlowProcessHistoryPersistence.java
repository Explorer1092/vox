package com.voxlearning.utopia.service.workflow.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.workflow.api.constants.WorkFlowProcessResult;
import com.voxlearning.utopia.service.workflow.api.constants.WorkFlowType;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowProcessHistory;

import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author fugui.chang
 * @since 2016/11/7
 */
@Named
@CacheBean(type = WorkFlowProcessHistory.class)
public class WorkFlowProcessHistoryPersistence extends AlpsStaticJdbcDao<WorkFlowProcessHistory, Long> {
    @Override
    protected void calculateCacheDimensions(WorkFlowProcessHistory document, Collection<String> dimensions) {
        dimensions.add(WorkFlowProcessHistory.cacheKeyFromProcessorAccount(document.getSourceApp(), document.getProcessorAccount()));
        dimensions.add(WorkFlowProcessHistory.cacheKeyFromWorkFlowRecordId(document.getWorkFlowRecordId()));
    }

    public List<WorkFlowProcessHistory> loadByProcessorAccount(String sourceApp, String processorAccount) {
        if (StringUtils.isBlank(sourceApp) || StringUtils.isBlank(processorAccount)) {
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("SOURCE_APP").is(sourceApp)
                .and("PROCESSOR_ACCOUNT").is(processorAccount);
        return query(Query.query(criteria));
    }

    public List<WorkFlowProcessHistory> loadByWorkFlowRecordId(Long workFlowRecordId) {
        if (workFlowRecordId == null) {
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("WORKFLOW_RECORD_ID").is(workFlowRecordId);
        List<WorkFlowProcessHistory> retList = query(Query.query(criteria));
        if (CollectionUtils.isNotEmpty(retList)) {
            Collections.sort(retList, ((o1, o2) -> o2.getId().compareTo(o1.getId())));
        }
        return retList;
    }

    public List<WorkFlowProcessHistory> loadByUserAndType(String sourceApp, String processorAccount, WorkFlowType workFlowType, WorkFlowProcessResult processResult, Date startDate, Date endDate) {
        if (StringUtils.isBlank(sourceApp) || StringUtils.isBlank(processorAccount)) {
            return Collections.emptyList();
        }
        Criteria criteria = Criteria.where("SOURCE_APP").is(sourceApp)
                .and("PROCESSOR_ACCOUNT").is(processorAccount);
        if (workFlowType != null) {
            criteria.and("WORK_FLOW_TYPE").is(workFlowType);
        }
        if (processResult != null) {
            criteria.and("RESULT").is(processResult);
        }
        if (startDate != null) {
            criteria.and("CREATE_DATETIME").gte(startDate);
            if (endDate != null) {
                criteria.lt(endDate);
            }
        } else {
            if (endDate != null) {
                criteria.and("CREATE_DATETIME").lt(endDate);
            }
        }
        return query(Query.query(criteria));
    }

    public Page<WorkFlowProcessHistory> loadPageByUserAndType(String sourceApp,
                                                              String processorAccount,
                                                              WorkFlowType workFlowType,
                                                              WorkFlowProcessResult processResult,
                                                              Date startDate,
                                                              Date endDate,
                                                              Pageable pageable) {
        if (StringUtils.isBlank(sourceApp) || StringUtils.isBlank(processorAccount)) {
            return new PageImpl<>(Collections.emptyList());
        }
        Criteria criteria = Criteria.where("SOURCE_APP").is(sourceApp)
                .and("PROCESSOR_ACCOUNT").is(processorAccount);
        if (workFlowType != null) {
            criteria.and("WORK_FLOW_TYPE").is(workFlowType);
        }
        if (processResult != null) {
            criteria.and("RESULT").is(processResult);
        }
        if (startDate != null) {
            criteria.and("CREATE_DATETIME").gte(startDate);
            if (endDate != null) {
                criteria.lt(endDate);
            }
        } else {
            if (endDate != null) {
                criteria.and("CREATE_DATETIME").lt(endDate);
            }
        }
        Query query = Query.query(criteria);
        Sort sort = new Sort(Sort.Direction.DESC, "CREATE_DATETIME");
        return new PageImpl<>(query(query.with(pageable).with(sort)), pageable, count(query));
    }
}
