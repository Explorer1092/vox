package com.voxlearning.utopia.admin.listener.handler;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.admin.queue.AgentCommandQueueProducer;
import com.voxlearning.utopia.service.crm.api.constants.ApplyStatus;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentProductFeedbackStatus;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentProductFeedback;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentProductFeedbackLoadClient;
import com.voxlearning.utopia.service.crm.consumer.service.agent.AgentProductFeedbackServiceClient;
import com.voxlearning.utopia.service.workflow.api.constants.WorkFlowProcessResult;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * CrmProductFeedbackHandler
 *
 * @author song.wang
 * @date 2017/2/24
 */
@Named
public class CrmProductFeedbackHandler extends SpringContainerSupport {

    @Inject
    private AgentProductFeedbackLoadClient agentProductFeedbackLoadClient;
    @Inject
    private AgentProductFeedbackServiceClient agentProductFeedbackServiceClient;
    @Inject
    private AgentCommandQueueProducer agentCommandQueueProducer;

    private final static String COMMAND = "notice_product_feedback";

    public void handle(Long workflowId, WorkFlowProcessResult processResult, Boolean hasFollowStatus, String workflowStatus){

        if(workflowId == null || processResult == null || hasFollowStatus == null || StringUtils.isBlank(workflowStatus)){
            return;
        }
        AgentProductFeedback agentProductFeedback = agentProductFeedbackLoadClient.findByWorkflowId(workflowId);
        if(agentProductFeedback == null){
            return;
        }

        AgentProductFeedbackStatus feedbackStatus = agentProductFeedback.getFeedbackStatus();
        if(WorkFlowProcessResult.agree == processResult){
            if(Objects.equals(workflowStatus, "lv1")){
                feedbackStatus = AgentProductFeedbackStatus.PM_PENDING;
            }else if(Objects.equals(workflowStatus, "lv2")){
                feedbackStatus = AgentProductFeedbackStatus.PM_APPROVED;
            }else if(Objects.equals(workflowStatus, "lv3")){
                feedbackStatus = AgentProductFeedbackStatus.PM_APPROVED;
            }
        }else if(WorkFlowProcessResult.reject == processResult){
            if(Objects.equals(workflowStatus, "reject_lv1")){
                feedbackStatus = AgentProductFeedbackStatus.SO_REJECTED;
            }else if(Objects.equals(workflowStatus, "reject_lv2")){ // PM 驳回后，销运做最终确认
                feedbackStatus = AgentProductFeedbackStatus.SO_CONFIRMING;
            }else if(Objects.equals(workflowStatus, "reject_lv3")){
                feedbackStatus = AgentProductFeedbackStatus.PM_REJECTED;
            }
        }

        ApplyStatus applyStatus = null;
        boolean updateApplyStatus = false;
        if(!hasFollowStatus){ // 工作流已审批结束，没有后续状态
            if(WorkFlowProcessResult.agree == processResult){
                applyStatus = ApplyStatus.APPROVED;
            }else if(WorkFlowProcessResult.reject == processResult){
                applyStatus = ApplyStatus.REJECTED;
            }else if(WorkFlowProcessResult.revoke == processResult){
                applyStatus = ApplyStatus.REVOKED;
            }
            updateApplyStatus = true;
        }

        agentProductFeedback.setFeedbackStatus(feedbackStatus);
        if(updateApplyStatus){
            agentProductFeedback.setStatus(applyStatus);
        }
        agentProductFeedbackServiceClient.replaceAgentProductFeedback(agentProductFeedback);
        Map<String, Object> map = new HashMap<>();
        map.put("recordId", workflowId);
        map.put("command", COMMAND);
        Message message = Message.newMessage();
        message.withPlainTextBody(JsonUtils.toJson(map));
        agentCommandQueueProducer.getProducer().produce(message);
    }
}
