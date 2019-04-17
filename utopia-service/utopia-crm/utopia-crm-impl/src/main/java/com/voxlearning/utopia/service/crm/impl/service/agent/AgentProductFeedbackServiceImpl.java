package com.voxlearning.utopia.service.crm.impl.service.agent;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentProductFeedback;
import com.voxlearning.utopia.service.crm.api.service.agent.AgentProductFeedbackService;
import com.voxlearning.utopia.service.crm.impl.dao.agent.AgentProductFeedbackPersistence;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by yaguang.wang
 * on 2017/2/21.
 */
@Named
@Service(interfaceClass = AgentProductFeedbackService.class)
@ExposeService(interfaceClass = AgentProductFeedbackService.class)
public class AgentProductFeedbackServiceImpl extends SpringContainerSupport implements AgentProductFeedbackService {
    @Inject
    private AgentProductFeedbackPersistence agentProductFeedbackPersistence;

    @Override
    public Long saveAgentProductFeedback(AgentProductFeedback feedback) {
        agentProductFeedbackPersistence.insert(feedback);
        return feedback.getId();
    }

    @Override
    public AgentProductFeedback replaceAgentProductFeedback(AgentProductFeedback feedback) {
        return agentProductFeedbackPersistence.replace(feedback);
    }

    @Override
    public Boolean updateWorkFlowId(Long id, Long workflowId) {
        if (id == null || workflowId == null) {
            return false;
        }
        AgentProductFeedback feedback = agentProductFeedbackPersistence.load(id);
        if (feedback == null) {
            return false;
        }
        feedback.setWorkflowId(workflowId);
        return replaceAgentProductFeedback(feedback) != null;
    }

    @Override
    public Boolean setRelationCode(Long id, Long relationCode) {
        if (id == null) {
            return false;
        }

        return agentProductFeedbackPersistence.setRelationCode(id, relationCode);
    }
}
