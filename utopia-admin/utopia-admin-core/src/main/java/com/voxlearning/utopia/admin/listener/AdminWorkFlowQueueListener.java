package com.voxlearning.utopia.admin.listener;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.queue.*;
import com.voxlearning.utopia.admin.listener.handler.CrmModifyDictSchoolApplyHandler;
import com.voxlearning.utopia.admin.listener.handler.CrmProductFeedbackHandler;
import com.voxlearning.utopia.admin.listener.handler.CrmSendAppMessageHandler;
import com.voxlearning.utopia.admin.listener.handler.WechatQueueHandler;
import com.voxlearning.utopia.service.workflow.api.constants.WorkFlowProcessResult;
import com.voxlearning.utopia.service.workflow.api.constants.WorkFlowType;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;
import java.util.Objects;

/**
 * @author fugui.chang
 * @since 2016/11/14
 * FIXME: Bad design here
 */
@Named
@QueueConsumer(
        destinations = {
                @QueueDestination(
                        system = QueueSystem.KFK,
                        config = "primary",
                        queue = "utopia.workflow.admin.queue"
                ),
                @QueueDestination(
                        system = QueueSystem.KFK,
                        config = "main-backup",
                        queue = "utopia.workflow.admin.queue"
                )
        }
)
public class AdminWorkFlowQueueListener extends SpringContainerSupport implements MessageListener {

    @Inject private CrmModifyDictSchoolApplyHandler crmModifyDictSchoolApplyHandler;
    @Inject private CrmProductFeedbackHandler crmProductFeedbackHandler;
    @Inject private CrmSendAppMessageHandler crmSendAppMessageHandler;
    @Inject private WechatQueueHandler wechatQueueHandler;


    @Override
    public void onMessage(Message message) {
        Object decoded = message.decodeBody();
        if (decoded instanceof Map) {
            Map<String, String> map = (Map<String, String>) decoded;
            String configName = map.get("configName");
            String mqmsg = map.get("mqmsg");
            String status = map.get("status"); // 工作流状态
            Long workflowId = Long.valueOf(map.get("recordId"));
            Boolean hasFollowStatus = Boolean.valueOf(map.get("hasFollowStatus")); // 工作流是否
            WorkFlowProcessResult processResult = WorkFlowProcessResult.nameOf(map.get("processResult"));
            if (Objects.equals(configName, "admin_wechat_batch_send")) {
                wechatQueueHandler.handle(mqmsg, workflowId, status);
            } else if (Objects.equals(configName, WorkFlowType.AGENT_MODIFY_DICT_SCHOOL.getWorkflowName())) {
                crmModifyDictSchoolApplyHandler.handle(workflowId, processResult, hasFollowStatus);
            } else if (Objects.equals(configName, WorkFlowType.AGENT_PRODUCT_FEEDBACK.getWorkflowName())) {
                crmProductFeedbackHandler.handle(workflowId, processResult, hasFollowStatus, status);
            } else if (Objects.equals(configName, WorkFlowType.ADMIN_SEND_APP_PUSH.getWorkflowName())) {
                crmSendAppMessageHandler.handle(mqmsg, workflowId, status);
            }
        }
    }
}
