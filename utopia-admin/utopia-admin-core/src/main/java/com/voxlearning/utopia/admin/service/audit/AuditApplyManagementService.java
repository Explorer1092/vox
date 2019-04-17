package com.voxlearning.utopia.admin.service.audit;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.utopia.admin.service.AbstractAdminService;
import com.voxlearning.utopia.admin.util.BeanMapUtils;
import com.voxlearning.utopia.service.crm.api.bean.ApplyWithProcessResultData;
import com.voxlearning.utopia.service.crm.api.constants.ApplyStatus;
import com.voxlearning.utopia.service.crm.api.constants.ApplyType;
import com.voxlearning.utopia.service.crm.api.constants.SystemPlatformType;
import com.voxlearning.utopia.service.crm.api.entities.AbstractBaseApply;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentModifyDictSchoolApply;
import com.voxlearning.utopia.service.crm.consumer.loader.ApplyManagementLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentModifyDictSchoolApplyLoaderClient;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowProcessHistory;
import com.voxlearning.utopia.service.workflow.consumer.WorkFlowLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * CrmApplyManagementService
 *
 * @author song.wang
 * @date 2017/1/8
 */
@Named
public class AuditApplyManagementService extends AbstractAdminService {
    @Inject
    private ApplyManagementLoaderClient applyManagementLoaderClient;

    @Inject
    private AgentModifyDictSchoolApplyLoaderClient agentModifyDictSchoolApplyLoaderClient;
    @Inject
    private WorkFlowLoaderClient workFlowLoaderClient;

    public List<Map<String, Object>> getDictSchoolApplyList(Long schoolId){
        List<AgentModifyDictSchoolApply> applyList = agentModifyDictSchoolApplyLoaderClient.findBySchoolId(schoolId);
        if(CollectionUtils.isEmpty(applyList)){
            return Collections.emptyList();
        }

        List<Map<String, Object>> retList = new ArrayList<>();
        Map<String, Object> itemMap;
        for(AgentModifyDictSchoolApply applyItem : applyList){
            itemMap = BeanMapUtils.tansBean2Map(applyItem);
            itemMap.put("status", applyItem.getStatus().getDesc());
            itemMap.put("createDatetime", DateUtils.dateToString(applyItem.getCreateDatetime(), "yyyy-MM-dd"));
            List<WorkFlowProcessHistory> processHistoryList = workFlowLoaderClient.loadWorkFlowProcessHistoryByWorkFlowId(applyItem.getWorkflowId());
            StringBuilder stringBuilder = new StringBuilder();
            if(CollectionUtils.isNotEmpty(processHistoryList)){
                processHistoryList.forEach(p -> {
                    stringBuilder.append("审核人：").append(p.getProcessorName()).append("  审核结果：").append(p.getResult().getDesc()).append("  审核意见：").append(p.getProcessNotes()).append("<br/>");
                });
            }
            itemMap.put("processFlow", stringBuilder.toString());
            retList.add(itemMap);
        }
        return retList;
    }

    public Page<AbstractBaseApply> fetchUserApplyList(String account, ApplyStatus status, Boolean includeRevokeData, int pageNo, int pageSize){
        return applyManagementLoaderClient.fetchUserApplyList(SystemPlatformType.ADMIN, account, status, includeRevokeData, pageNo, pageSize);
    }

    public ApplyWithProcessResultData getApplyDetailWithProcessResultByApplyId(ApplyType applyType, Long applyId){
        return applyManagementLoaderClient.fetchApplyWithProcessResultByApplyId(applyType, applyId, true);
    }

    public ApplyWithProcessResultData getApplyDetailByWorkflowId(ApplyType applyType, Long workflowId, Boolean withCurrentProcess){
        return applyManagementLoaderClient.fetchApplyWithProcessResultByWorkflowId(applyType, workflowId, withCurrentProcess);
    }
}
