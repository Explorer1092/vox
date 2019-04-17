package com.voxlearning.utopia.service.crm.consumer.loader.agent;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.crm.api.bean.ProductFeedbackListCondition;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentProductFeedbackStatus;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentProductFeedback;
import com.voxlearning.utopia.service.crm.api.loader.agent.AgentProductFeedbackLoader;

import java.util.List;

/**
 * Created by yaguang.wang
 * on 2017/2/21.
 */
public class AgentProductFeedbackLoadClient implements AgentProductFeedbackLoader {

    @ImportService(interfaceClass = AgentProductFeedbackLoader.class)
    private AgentProductFeedbackLoader agentProductFeedbackLoader;

    @Override
    public AgentProductFeedback loadByFeedbackId(Long feedbackId) {
        return agentProductFeedbackLoader.loadByFeedbackId(feedbackId);
    }

    @Override
    public List<AgentProductFeedback> loadByNeedNotification(AgentProductFeedbackStatus feedbackStatus) {
        return agentProductFeedbackLoader.loadByNeedNotification(feedbackStatus);
    }

    @Override
    public AgentProductFeedback findByWorkflowId(Long workflowId) {
        return agentProductFeedbackLoader.findByWorkflowId(workflowId);
    }

    @Override
    public List<AgentProductFeedback> findByRelationCode(Long relationCode) {
        return agentProductFeedbackLoader.findByRelationCode(relationCode);
    }

    @Override
    public List<AgentProductFeedback> findFeedbackByCondition(ProductFeedbackListCondition condition, Integer page, Integer pageSize) {
        return agentProductFeedbackLoader.findFeedbackByCondition(condition, page, pageSize);
    }

    @Override
    public Long findFeedbackByConditionCount(ProductFeedbackListCondition condition) {
        return agentProductFeedbackLoader.findFeedbackByConditionCount(condition);
    }

    @Override
    public List<AgentProductFeedback> findFeedbackByTeacherId(Long teacherId) {
        return agentProductFeedbackLoader.findFeedbackByTeacherId(teacherId);
    }
}
