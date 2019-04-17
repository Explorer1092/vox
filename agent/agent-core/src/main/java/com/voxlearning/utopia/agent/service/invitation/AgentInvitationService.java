package com.voxlearning.utopia.agent.service.invitation;
import com.voxlearning.utopia.agent.persist.AgentInvitationPersistence;
import com.voxlearning.utopia.agent.persist.entity.AgentInvitation;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import javax.inject.Inject;
import javax.inject.Named;


/**
 * 邀请函Service
 *
 * @author deliang.che
 * @date 2018-04-13
 */
@Named
public class AgentInvitationService extends AbstractAgentService {

    @Inject
    private AgentInvitationPersistence agentInvitationPersistence;

    /**
     * 新增邀请函信息
     * @param agentInvitation
     */
    public void createInvitation(AgentInvitation agentInvitation){
        agentInvitationPersistence.insert(agentInvitation);
    }
}
