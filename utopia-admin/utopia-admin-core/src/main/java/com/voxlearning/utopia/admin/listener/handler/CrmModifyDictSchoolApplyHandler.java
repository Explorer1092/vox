package com.voxlearning.utopia.admin.listener.handler;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.admin.queue.AgentCommandQueueProducer;
import com.voxlearning.utopia.service.crm.api.constants.ApplyStatus;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentModifyDictSchoolApply;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentModifyDictSchoolApplyLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.service.agent.AgentModifyDictSchoolApplyServiceClient;
import com.voxlearning.utopia.service.workflow.api.constants.WorkFlowProcessResult;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowProcessHistory;
import com.voxlearning.utopia.service.workflow.consumer.WorkFlowLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CrmModifyDictSchoolApplyHandler
 *
 * @author song.wang
 * @date 2017/1/8
 */
@Named
public class CrmModifyDictSchoolApplyHandler extends SpringContainerSupport {
    @Inject
    private AgentModifyDictSchoolApplyLoaderClient agentModifyDictSchoolApplyLoaderClient;
    @Inject
    private AgentModifyDictSchoolApplyServiceClient agentModifyDictSchoolApplyServiceClient;

    @Inject private AgentCommandQueueProducer agentCommandQueueProducer;

    @Inject private WorkFlowLoaderClient workFlowLoaderClient;

    public void handle(Long workflowId, WorkFlowProcessResult processResult, Boolean hasFollowStatus){
        if(workflowId == null || processResult == null || hasFollowStatus == null){
            return;
        }
        AgentModifyDictSchoolApply modifyDictSchoolApply = agentModifyDictSchoolApplyLoaderClient.findByWorkflowId(workflowId);
        if(modifyDictSchoolApply == null){
            return;
        }
        if(!hasFollowStatus){ // 工作流已审批结束，没有后续状态
            ApplyStatus applyStatus = null;
            if(WorkFlowProcessResult.agree == processResult){
                applyStatus = ApplyStatus.APPROVED;
            }else if(WorkFlowProcessResult.reject == processResult){
                applyStatus = ApplyStatus.REJECTED;
            }else if(WorkFlowProcessResult.revoke == processResult){
                applyStatus = ApplyStatus.REVOKED;
            }
            if(applyStatus != null){
                agentModifyDictSchoolApplyServiceClient.updateStatus(modifyDictSchoolApply.getId(), applyStatus);
                sendAgentModifyDictSchoolApplyMessage(modifyDictSchoolApply,processResult,workflowId);
            }
        }
    }

    // 发送消息到Agent
    private void sendAgentModifyDictSchoolApplyMessage(AgentModifyDictSchoolApply modifyDictSchoolApply,WorkFlowProcessResult processResult,Long workflowId){
        Map<String, Object> command = new HashMap<>();
        command.put("command", "crm_modify_dict_school_apply");
        command.put("modifyType", modifyDictSchoolApply.getModifyType());
        command.put("schoolName", modifyDictSchoolApply.getSchoolName());
        command.put("schoolId", modifyDictSchoolApply.getSchoolId());
        command.put("receiverId", modifyDictSchoolApply.getAccount());
        command.put("processResult", processResult.toString());
        if (WorkFlowProcessResult.reject == processResult){
            WorkFlowProcessHistory latestProcessHistory = getLatestProcessHistory(workflowId);
            String rejectNote = "";
            String rejectName = "";
            if(latestProcessHistory != null){
                rejectNote = latestProcessHistory.getProcessNotes();
                rejectName = latestProcessHistory.getProcessorName();
            }
            command.put("rejectName", rejectName);
            command.put("rejectNote", rejectNote);
        }
        Message message = Message.newMessage();
        message.withPlainTextBody(JsonUtils.toJson(command));
        agentCommandQueueProducer.getProducer().produce(message);
    }

    // 获取最近一次的处理记录
    private WorkFlowProcessHistory getLatestProcessHistory(Long workflowId){
        List<WorkFlowProcessHistory> processHistoryList = workFlowLoaderClient.loadWorkFlowProcessHistoryByWorkFlowId(workflowId);
        if(CollectionUtils.isEmpty(processHistoryList)){
            return null;
        }
        Collections.sort(processHistoryList, ((o1, o2) -> o2.getCreateDatetime().compareTo(o1.getCreateDatetime())));
        return processHistoryList.get(0);
    }
}
