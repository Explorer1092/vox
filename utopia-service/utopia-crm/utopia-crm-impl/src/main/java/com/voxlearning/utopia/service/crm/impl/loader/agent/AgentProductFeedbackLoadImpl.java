package com.voxlearning.utopia.service.crm.impl.loader.agent;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.crm.api.bean.ProductFeedbackListCondition;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentProductFeedbackStatus;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentProductFeedback;
import com.voxlearning.utopia.service.crm.api.loader.agent.AgentProductFeedbackLoader;
import com.voxlearning.utopia.service.crm.impl.dao.agent.AgentProductFeedbackPersistence;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * Created by yaguang.wang
 * on 2017/2/21.
 */
@Named
@Service(interfaceClass = AgentProductFeedbackLoader.class)
@ExposeService(interfaceClass = AgentProductFeedbackLoader.class)
public class AgentProductFeedbackLoadImpl extends SpringContainerSupport implements AgentProductFeedbackLoader {
    @Inject
    private AgentProductFeedbackPersistence agentProductFeedbackPersistence;

    @Override
    public AgentProductFeedback loadByFeedbackId(Long feedbackId) {
        return agentProductFeedbackPersistence.load(feedbackId);
    }

    @Override
    public List<AgentProductFeedback> loadByNeedNotification(AgentProductFeedbackStatus feedbackStatus) {
        return agentProductFeedbackPersistence.findByStatusAndUpdateTime(feedbackStatus);
    }

    @Override
    public AgentProductFeedback findByWorkflowId(Long workflowId) {
        return agentProductFeedbackPersistence.findByWorkflowId(workflowId);
    }

    @Override
    public List<AgentProductFeedback> findByRelationCode(Long relationCode) {
        return agentProductFeedbackPersistence.findByRelationCode(relationCode);
    }

    @Override
    public List<AgentProductFeedback> findFeedbackByCondition(ProductFeedbackListCondition condition, Integer page, Integer pageSize) {
        return agentProductFeedbackPersistence.findFeedbackByCondition(condition.getStartDate(), condition.getEndDate(), condition.getSubject(),
                condition.getType(), condition.getStatus(), condition.getFirstCategory(), condition.getSecondCategory(), condition.getThirdCategory(),
                condition.getPmData(), condition.getOnlineFlag(), condition.getContent(), condition.getFeedbackPeople(), condition.getTeacherName(),
                condition.getTeacherIds(), page, pageSize, condition.getFeedbackPeopleId());
    }

    @Override
    public Long findFeedbackByConditionCount(ProductFeedbackListCondition condition) {
        return agentProductFeedbackPersistence.findFeedbackByConditionCount(condition.getStartDate(), condition.getEndDate(), condition.getSubject(),
                condition.getType(), condition.getStatus(), condition.getFirstCategory(), condition.getSecondCategory(), condition.getThirdCategory(),
                condition.getPmData(), condition.getOnlineFlag(), condition.getContent(), condition.getFeedbackPeople(), condition.getTeacherName(),
                condition.getTeacherIds(), condition.getFeedbackPeopleId());
    }

    @Override
    public List<AgentProductFeedback> findFeedbackByTeacherId(Long teacherId) {
        return agentProductFeedbackPersistence.findByTeacherId(teacherId);
    }
}
