package com.voxlearning.utopia.service.crm.consumer.service.agent;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentProductFeedback;
import com.voxlearning.utopia.service.crm.api.service.agent.AgentProductFeedbackService;

/**
 * Created by Administrator
 * on 2017/2/21.
 */
public class AgentProductFeedbackServiceClient implements AgentProductFeedbackService {
    @ImportService(interfaceClass = AgentProductFeedbackService.class)
    private AgentProductFeedbackService agentProductFeedbackService;

    @Override
    public Long saveAgentProductFeedback(AgentProductFeedback feedback) {
        return agentProductFeedbackService.saveAgentProductFeedback(feedback);
    }

    @Override
    public AgentProductFeedback replaceAgentProductFeedback(AgentProductFeedback feedback) {
        return agentProductFeedbackService.replaceAgentProductFeedback(feedback);
    }

    @Override
    public Boolean  updateWorkFlowId(Long id, Long workflowId) {
        return agentProductFeedbackService.updateWorkFlowId(id, workflowId);
    }

    @Override
    public Boolean setRelationCode(Long id, Long relationCode) {
        return agentProductFeedbackService.setRelationCode(id, relationCode);
    }
}
