package com.voxlearning.utopia.agent.workflow;

import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentOrder;
import lombok.Getter;
import lombok.Setter;

/**
 * 简易工作流的上下文环境
 * Created by Alex on 14-8-8.
 */
public class WorkFlowContext {

    @Getter
    private AgentOrder order;
    @Getter
    private AuthCurrentUser currentUser;
    @Getter
    @Setter
    private String processNotes;

    public WorkFlowContext(AgentOrder order, AuthCurrentUser currentUser) {
        this.order = order;
        this.currentUser = currentUser;
    }

}
