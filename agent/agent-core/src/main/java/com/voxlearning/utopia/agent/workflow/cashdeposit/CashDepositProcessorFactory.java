package com.voxlearning.utopia.agent.workflow.cashdeposit;

import com.voxlearning.utopia.service.crm.api.constants.agent.AgentOrderStatus;
import com.voxlearning.utopia.agent.workflow.WorkFlowContext;
import com.voxlearning.utopia.agent.workflow.WorkFlowProcessor;
import com.voxlearning.utopia.agent.workflow.WorkFlowProcessorFactory;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by Alex on 14-8-8.
 */
@Named
public class CashDepositProcessorFactory implements WorkFlowProcessorFactory {

    @Inject
    private CashDepositInitProcessor cashDepositInitProcessor;
    @Inject
    private CashDepositPaymentRecvProcessor cashDepositPaymentRecvProcessor;

    public WorkFlowProcessor getProcessor(WorkFlowContext context) {
        switch (AgentOrderStatus.of(context.getOrder().getOrderStatus())) {
            case INIT:
                return cashDepositInitProcessor;
            case PENDING_FINANCIAL:
                return cashDepositPaymentRecvProcessor;
            default:
                return null;
        }
    }
}
