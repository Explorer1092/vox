package com.voxlearning.utopia.agent.workflow.cashwithdraw.refund;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.agent.constants.AgentNotifyType;
import com.voxlearning.utopia.agent.service.common.BaseGroupService;
import com.voxlearning.utopia.agent.service.notify.AgentNotifyService;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentOrder;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

/**
 * 余额提现流程发送通知
 * Created by Wang Yuechen on 2016/4/13.
 */
@Named
public class CashWithdrawNotifySender {

    @Inject private BaseGroupService baseGroupService;
    @Inject private AgentNotifyService agentNotifyService;

    // 生成退货单的时候给财务发送通知
    public void initCashWithdrawNotify(AgentOrder agentOrder) {
        List<AgentGroupUser> financeUsers = baseGroupService.getGroupUsersByGroupId(getFinanceGroupId());
        List<Long> receiverList = new ArrayList<>();
        for (AgentGroupUser groupUser : financeUsers) {
            if (!receiverList.contains(groupUser.getUserId())) {
                receiverList.add(groupUser.getUserId());
            }
        }

        StringBuilder notifyContent = new StringBuilder();
        notifyContent.append("CRM提交了余额提取申请:");
        String productInfo = StringUtils.join(
                "支付流水号:", agentOrder.getOrderNotes()
        );
        notifyContent.append(productInfo);
        notifyContent.deleteCharAt(notifyContent.length() - 1);
        agentNotifyService.sendNotify(AgentNotifyType.REFUND_NOTICE.getType(), notifyContent.toString(), receiverList);
    }

    private Long getFinanceGroupId(){
        List<AgentGroup> fianceGroups = baseGroupService.getAgentGroupByRoleId(AgentRoleType.Finance.getId());
        if (CollectionUtils.isNotEmpty(fianceGroups)) {
            return fianceGroups.get(0).getId();
        }
        // FIXME 这里的 id=3 是从 AGENT_GROUP 里直接取的
        return 3L;
    }
}
