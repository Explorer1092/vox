package com.voxlearning.utopia.agent.listener.handler;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.service.task.TaskService;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentOrderType;
import com.voxlearning.utopia.service.order.api.entity.OrderRefundHistory;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;

/**
 * Created by Summer on 2017/3/28.
 */
@Named
public class AliRefundHandler extends SpringContainerSupport {
    @Inject UserOrderLoaderClient userOrderLoaderClient;
    @Inject TaskService taskService;

    public void executeCommand(List<String> successIds, List<String> failIds) {
        if (CollectionUtils.isNotEmpty(successIds)) {
            Map<String, OrderRefundHistory> historyMap = userOrderLoaderClient.loadOrderRefundHistoryByIds(successIds);
            if (MapUtils.isNotEmpty(historyMap)) {
                for (OrderRefundHistory history : historyMap.values()) {
                    // 处理任务
                    AuthCurrentUser currentUser = new AuthCurrentUser();
                    currentUser.setUserId(history.getAgentUserId());
                    currentUser.setRealName(history.getAgentUserName());
                    taskService.approveOrder(history.getAgentProcessId(), AgentOrderType.REFUND.getType(), currentUser, "系统回调");
                }
            }
        }

        if (CollectionUtils.isNotEmpty(failIds)) {
            Map<String, OrderRefundHistory> historyMap = userOrderLoaderClient.loadOrderRefundHistoryByIds(failIds);
            if (MapUtils.isNotEmpty(historyMap)) {
                for (OrderRefundHistory history : historyMap.values()) {
                    // 处理任务
                    AuthCurrentUser currentUser = new AuthCurrentUser();
                    currentUser.setUserId(history.getAgentUserId());
                    currentUser.setRealName(history.getAgentUserName());
                    taskService.rejectOrder(history.getAgentProcessId(), currentUser, "系统回调");
                }
            }
        }

    }
}
