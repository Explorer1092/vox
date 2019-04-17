package com.voxlearning.utopia.service.crm.api.service.agent;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentProductFeedback;

import java.util.concurrent.TimeUnit;

/**
 * 产品反馈操作
 * Created by yaguang.wang on 2017/2/21.
 */
@ServiceVersion(version = "2017.02.21")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface AgentProductFeedbackService {
    Long saveAgentProductFeedback(AgentProductFeedback feedback);

    AgentProductFeedback replaceAgentProductFeedback(AgentProductFeedback feedback);

    Boolean updateWorkFlowId(Long id, Long workflowId);

    Boolean setRelationCode(Long id, Long relationCode);
}
