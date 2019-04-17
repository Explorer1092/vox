package com.voxlearning.utopia.service.crm.api.loader.agent;

import com.voxlearning.alps.annotation.remote.Idempotent;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.utopia.service.crm.api.bean.ProductFeedbackListCondition;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentProductFeedbackStatus;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentProductFeedback;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 产品反馈
 * Created by yaguang.wang on 2017/2/21.
 */
@ServiceVersion(version = "2017.02.21")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
public interface AgentProductFeedbackLoader {

    @Idempotent
    AgentProductFeedback loadByFeedbackId(Long feedbackId);

    @Idempotent
    List<AgentProductFeedback> loadByNeedNotification(AgentProductFeedbackStatus feedbackStatus);

    @Idempotent
    AgentProductFeedback findByWorkflowId(Long workflowId);

    @Idempotent
    List<AgentProductFeedback> findByRelationCode(Long relationCode);

    @Idempotent
    List<AgentProductFeedback> findFeedbackByCondition(ProductFeedbackListCondition condition, Integer page, Integer pageSize);

    @Idempotent
    Long findFeedbackByConditionCount(ProductFeedbackListCondition condition);

    @Idempotent
    List<AgentProductFeedback> findFeedbackByTeacherId(Long teacherId);
}
