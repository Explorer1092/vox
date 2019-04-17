package com.voxlearning.utopia.agent.workflow;

import com.voxlearning.utopia.service.crm.api.constants.agent.AgentOrderType;
import com.voxlearning.utopia.agent.workflow.cashdeposit.CashDepositProcessorFactory;
import com.voxlearning.utopia.agent.workflow.cashwithdraw.refund.CashWithdrawProcessorFactory;
import com.voxlearning.utopia.agent.workflow.purchase.PurchaseProcessorFactory;
import com.voxlearning.utopia.agent.workflow.refund.RefundProcessorFactory;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by Alex on 14-8-8.
 */
@Named
public class WorkFlowEngine {

    @Inject private CashDepositProcessorFactory cashDepositProcessorFactory;
    @Inject private PurchaseProcessorFactory purchaseProcessorFactory;
    @Inject private RefundProcessorFactory refundProcessorFactory;
    @Inject private CashWithdrawProcessorFactory cashWithdrawProcessorFactory;

    public WorkFlowProcessorFactory getWorkFlowProcessorFactory(WorkFlowContext context) {
        AgentOrderType orderType = AgentOrderType.of(context.getOrder().getOrderType());

        switch (orderType) {
            case DEPOSIT:
                return cashDepositProcessorFactory;
            case BUY_MATERIAL:
                return purchaseProcessorFactory;
            case REFUND:
                return refundProcessorFactory;
            case CASH_WITHDRAW:
                return cashWithdrawProcessorFactory;
            default:
                return null;
        }
    }
}
