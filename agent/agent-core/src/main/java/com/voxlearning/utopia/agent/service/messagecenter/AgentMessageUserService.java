package com.voxlearning.utopia.agent.service.messagecenter;

import com.voxlearning.utopia.agent.dao.mongo.messagecenter.AgentMessageUserDao;
import com.voxlearning.utopia.agent.persist.entity.messagecenter.AgentMessageUser;
import com.voxlearning.utopia.agent.service.AbstractAgentService;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Named
public class AgentMessageUserService extends AbstractAgentService {
    @Inject private AgentMessageUserDao agentMessageUserDao;
    public List<AgentMessageUser> findUserListByMessageId(String messageId){
        return agentMessageUserDao.findByMessageId(messageId);
    }
}
