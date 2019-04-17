package com.voxlearning.utopia.agent.listener.handler;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.agent.constants.AgentNotifyType;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.mobile.AgentAuditService;
import com.voxlearning.utopia.agent.service.notify.AgentNotifyService;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import com.voxlearning.utopia.service.workflow.api.constants.WorkFlowType;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 系统消息中待我审批消息汇总器
 *
 * @author chunlin.yu
 * @create 2017-07-31 15:01
 **/
@Named
public class PendingMessageHandler extends SpringContainerSupport {

    @Inject
    AgentAuditService agentAuditService;
    @Inject
    private BaseOrgService baseOrgService;

    @Inject
    AgentNotifyService agentNotifyService;

    public void handle(){
        Set<Long> angentIds = new HashSet<>();
        angentIds.addAll(baseOrgService.getGroupUserByRole(AgentRoleType.CityManager.getId()).stream().map(AgentGroupUser::getUserId).collect(Collectors.toSet()));
        angentIds.addAll(baseOrgService.getGroupUserByRole(AgentRoleType.BUManager.getId()).stream().map(AgentGroupUser::getUserId).collect(Collectors.toSet()));
        angentIds.addAll(baseOrgService.getGroupUserByRole(AgentRoleType.Region.getId()).stream().map(AgentGroupUser::getUserId).collect(Collectors.toSet()));
        angentIds.addAll(baseOrgService.getGroupUserByRole(AgentRoleType.AreaManager.getId()).stream().map(AgentGroupUser::getUserId).collect(Collectors.toSet()));
        angentIds.addAll(baseOrgService.getGroupUserByRole(AgentRoleType.Country.getId()).stream().map(AgentGroupUser::getUserId).collect(Collectors.toSet()));
        sendMessage(angentIds);
    }

    private void sendMessage(Set<Long> angentIds){
        if (CollectionUtils.isNotEmpty(angentIds)){
            angentIds.forEach(userId -> {
                Map<WorkFlowType, Integer> map = agentAuditService.getTodoWorkflowCount(userId, Arrays.asList(WorkFlowType.AGENT_MODIFY_DICT_SCHOOL, WorkFlowType.AGENT_MATERIAL_APPLY, WorkFlowType.AGENT_DATA_REPORT_APPLY));
                int todoCount = map.values().stream().reduce(0, (x, y) -> (x + y));
                if (todoCount > 0){
                    String messgeContent = StringUtils.formatMessage("尚有{}条申请等待您的审批。",todoCount);
                    agentNotifyService.sendNotify(AgentNotifyType.PENDING_AUDIT.getType(),"待我审批",messgeContent, Collections.singleton(userId),"/mobile/audit/todo_list.vpage");
                }
            });
        }
    }
}
