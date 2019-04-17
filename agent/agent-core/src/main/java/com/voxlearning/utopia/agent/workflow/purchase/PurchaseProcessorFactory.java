package com.voxlearning.utopia.agent.workflow.purchase;

import com.voxlearning.utopia.service.crm.api.constants.agent.AgentOrderStatus;
import com.voxlearning.utopia.agent.workflow.WorkFlowContext;
import com.voxlearning.utopia.agent.workflow.WorkFlowProcessor;
import com.voxlearning.utopia.agent.workflow.WorkFlowProcessorFactory;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by Alex on 14-8-14.
 */
@Named
public class PurchaseProcessorFactory implements WorkFlowProcessorFactory {

    @Inject
    private PurchaseInitProcessor purchaseInitProcessor;
    @Inject
    private PurchasePaymentRecvProcessor purchasePaymentRecvProcessor;
    @Inject
    private PurchaseDeliveryProcessor purchaseDeliveryProcessor;

    public WorkFlowProcessor getProcessor(WorkFlowContext context) {
        switch (AgentOrderStatus.of(context.getOrder().getOrderStatus())) {
            case INIT:
                return purchaseInitProcessor;
            case PENDING_FINANCIAL:
                return purchasePaymentRecvProcessor;
            case UNCHECKED:
                return purchasePaymentRecvProcessor;
            case REJECTED:
                return purchasePaymentRecvProcessor;
            case PENDING_REGION_MANAGER:
                return purchaseDeliveryProcessor;
            default:
                return null;
        }
    }
}
