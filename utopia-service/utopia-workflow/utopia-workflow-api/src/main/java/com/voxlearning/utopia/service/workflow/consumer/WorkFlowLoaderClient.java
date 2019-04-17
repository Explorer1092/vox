package com.voxlearning.utopia.service.workflow.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.utopia.service.workflow.api.WorkFlowLoader;
import com.voxlearning.utopia.service.workflow.api.bean.WorkFlowTargetUserProcessData;
import com.voxlearning.utopia.service.workflow.api.constants.WorkFlowProcessResult;
import com.voxlearning.utopia.service.workflow.api.constants.WorkFlowType;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowProcess;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowProcessHistory;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowRecord;

import java.util.*;

/**
 * @author fugui.chang
 * @since 2016/11/7
 */
public class WorkFlowLoaderClient implements WorkFlowLoader {

    @ImportService(interfaceClass = WorkFlowLoader.class)
    private WorkFlowLoader remoteReference;

    @Override
    public Map<Long, WorkFlowRecord> loadWorkFlowRecords(Collection<Long> workFlowRecordIds) {
        if (CollectionUtils.isEmpty(workFlowRecordIds)) {
            return Collections.emptyMap();
        }
        return remoteReference.loadWorkFlowRecords(workFlowRecordIds);
    }

    @Override
    public List<WorkFlowRecord> loadWorkFlowRecordsByCreatorAccount(String sourceApp, String createAccount) {
        if (StringUtils.isBlank(createAccount) || StringUtils.isBlank(sourceApp)) {
            return Collections.emptyList();
        }
        return remoteReference.loadWorkFlowRecordsByCreatorAccount(sourceApp, createAccount);
    }

    @Override
    public List<WorkFlowProcess> loadWorkFlowProcessByTargetUser(String sourceApp, String targetUser) {
        if (StringUtils.isBlank(targetUser) || StringUtils.isBlank(sourceApp)) {
            return Collections.emptyList();
        }
        return remoteReference.loadWorkFlowProcessByTargetUser(sourceApp, targetUser);
    }

    @Override
    public List<WorkFlowProcessHistory> loadWorkFlowProcessHistoriesByProcessorAccount(String sourceApp, String processorAccount) {
        if (StringUtils.isBlank(processorAccount) || StringUtils.isBlank(sourceApp)) {
            return Collections.emptyList();
        }
        return remoteReference.loadWorkFlowProcessHistoriesByProcessorAccount(sourceApp, processorAccount);
    }


    @Override
    public List<WorkFlowProcess> loadWorkFlowProcessByWorkFlowId(Long workFlowId) {
        return remoteReference.loadWorkFlowProcessByWorkFlowId(workFlowId);
    }

    @Override
    public List<WorkFlowProcessHistory> loadWorkFlowProcessHistoryByWorkFlowId(Long workFlowId) {
        return remoteReference.loadWorkFlowProcessHistoryByWorkFlowId(workFlowId);
    }

    @Override
    public Page<WorkFlowTargetUserProcessData> fetchTodoWorkflowList(String sourceApp, String userAccount, WorkFlowType workFlowType, Date startDate, Date endDate, String applicant, int pageNo, int pageSize) {
        return remoteReference.fetchTodoWorkflowList(sourceApp, userAccount, workFlowType, startDate, endDate, applicant, pageNo, pageSize);
    }

    @Override
    public Page<WorkFlowTargetUserProcessData> fetchDoneWorkflowList(String sourceApp, String userAccount, WorkFlowType workFlowType, WorkFlowProcessResult processResult, Date startDate, Date endDate, int pageNo, int pageSize) {
        return remoteReference.fetchDoneWorkflowList(sourceApp, userAccount, workFlowType, processResult, startDate, endDate, pageNo, pageSize);
    }

    @Override
    public WorkFlowRecord loadWorkFlowRecord(Long workFlowRecordId) {
        return remoteReference.loadWorkFlowRecord(workFlowRecordId);
    }


    @Override
    public Map<Long, List<WorkFlowProcessHistory>> loadWorkFlowProcessHistoriesByWorkFlowId(Collection<Long> workFlowRecordIds) {
        if (CollectionUtils.isEmpty(workFlowRecordIds)) {
            return Collections.emptyMap();
        }
        return remoteReference.loadWorkFlowProcessHistoriesByWorkFlowId(workFlowRecordIds);
    }
}
