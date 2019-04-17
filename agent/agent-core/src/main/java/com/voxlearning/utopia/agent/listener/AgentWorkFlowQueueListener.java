package com.voxlearning.utopia.agent.listener;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.queue.*;
import com.voxlearning.utopia.agent.listener.handler.*;
import com.voxlearning.utopia.service.workflow.api.constants.WorkFlowProcessResult;
import com.voxlearning.utopia.service.workflow.api.constants.WorkFlowType;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;
import java.util.Objects;

/**
 * AgentWorkFlowQueueListener
 *
 * @author song.wang
 * @date 2016/12/27
 */
@Named
@QueueConsumer(
        destinations = {
                @QueueDestination(
                        system = QueueSystem.KFK,
                        config = "primary",
                        queue = "utopia.workflow.agent.queue"
                ),
                @QueueDestination(
                        system = QueueSystem.KFK,
                        config = "main-backup",
                        queue = "utopia.workflow.agent.queue"
                )
        }
)
public class AgentWorkFlowQueueListener extends SpringContainerSupport implements MessageListener {

    @Inject
    private AgentModifyDictSchoolApplyHandler agentModifyDictSchoolApplyHandler;
    @Inject
    private AgentOrderApplyHandler agentOrderApplyHandler;
    @Inject
    private UnifiedExamApplyHandler unifiedExamApplyHandler;
    @Inject
    private AgentDataReportApplyHandler agentDataReportApplyHandler;


    @Override
    public void onMessage(Message message) {
        Object decoded = message.decodeBody();
        if (decoded instanceof Map) {
            Map<String, String> map = (Map<String, String>) decoded;
            String configName = map.get("configName");
            String mqmsg = map.get("mqmsg");
            String workflowStatus = map.get("status");
            Long workflowId = Long.valueOf(map.get("recordId"));
            Boolean hasFollowStatus = Boolean.valueOf(map.get("hasFollowStatus"));
            WorkFlowProcessResult processResult = WorkFlowProcessResult.nameOf(map.get("processResult"));
            if (Objects.equals(configName, WorkFlowType.AGENT_MODIFY_DICT_SCHOOL.getWorkflowName())) { // 字典表修改申请
                agentModifyDictSchoolApplyHandler.handle(workflowId, processResult, workflowStatus, hasFollowStatus);
            } else if (Objects.equals(configName, WorkFlowType.AGENT_MATERIAL_APPLY.getWorkflowName())) {
                agentOrderApplyHandler.handle(workflowId, processResult, hasFollowStatus);
            }else if(Objects.equals(configName, WorkFlowType.AGENT_UNIFIED_EXAM_APPLY.getWorkflowName())){
                unifiedExamApplyHandler.handle(workflowId, processResult, workflowStatus, hasFollowStatus);
            }else if(Objects.equals(configName, WorkFlowType.AGENT_DATA_REPORT_APPLY.getWorkflowName())){
                agentDataReportApplyHandler.handle(workflowId, processResult, hasFollowStatus);
            }
        }
    }
}
