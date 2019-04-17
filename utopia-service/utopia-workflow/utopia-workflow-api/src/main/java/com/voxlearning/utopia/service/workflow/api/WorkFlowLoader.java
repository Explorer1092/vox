package com.voxlearning.utopia.service.workflow.api;

import com.voxlearning.alps.annotation.remote.Idempotent;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.utopia.service.workflow.api.bean.WorkFlowTargetUserProcessData;
import com.voxlearning.utopia.service.workflow.api.constants.WorkFlowProcessResult;
import com.voxlearning.utopia.service.workflow.api.constants.WorkFlowType;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowProcess;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowProcessHistory;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowRecord;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author fugui.chang
 * @since 2016/11/7
 */
@ServiceVersion(version = "20170322")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
public interface WorkFlowLoader {

    @Idempotent
    Map<Long, WorkFlowRecord> loadWorkFlowRecords(Collection<Long> workFlowRecordIds);

    @Idempotent
    List<WorkFlowRecord> loadWorkFlowRecordsByCreatorAccount(String sourceApp, String createAccount);

    @Idempotent
    List<WorkFlowProcess> loadWorkFlowProcessByTargetUser(String sourceApp, String targetUser);

    @Idempotent
    List<WorkFlowProcessHistory> loadWorkFlowProcessHistoriesByProcessorAccount(String sourceApp, String processorAccount);

    @Idempotent
    List<WorkFlowProcess> loadWorkFlowProcessByWorkFlowId(Long workFlowId);

    @Idempotent
    List<WorkFlowProcessHistory> loadWorkFlowProcessHistoryByWorkFlowId(Long workFlowId);

    /**
     * 获取用户待处理的工作流列表
     *
     * @param sourceApp   系统平台
     * @param userAccount 用户账号
     * @return 工作流列表
     */
    @Idempotent
    Page<WorkFlowTargetUserProcessData> fetchTodoWorkflowList(String sourceApp, String userAccount, WorkFlowType workFlowType, Date startDate, Date endDate, String applicant, int pageNo, int pageSize);

    /**
     * 获取用户已经处理的工作流列表
     *
     * @param sourceApp   系统平台
     * @param userAccount 用户账号
     * @return 工作流列表
     */
    @Idempotent
    Page<WorkFlowTargetUserProcessData> fetchDoneWorkflowList(String sourceApp, String userAccount, WorkFlowType workFlowType, WorkFlowProcessResult processResult, Date startDate, Date endDate, int pageNo, int pageSize);

    @Idempotent
    WorkFlowRecord loadWorkFlowRecord(Long workFlowRecordId);

    @Idempotent
    Map<Long, List<WorkFlowProcessHistory>> loadWorkFlowProcessHistoriesByWorkFlowId(Collection<Long> workFlowRecordIds);
}
