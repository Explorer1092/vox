package com.voxlearning.utopia.service.crm.consumer.service.agent;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.crm.api.constants.ApplyStatus;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentOrderStatus;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentOrder;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentOrderProduct;
import com.voxlearning.utopia.service.crm.api.service.agent.AgentOrderService;

/**
 * AgentOrderServiceClient
 *
 * @author song.wang
 * @date 2017/1/11
 */
public class AgentOrderServiceClient implements AgentOrderService {

    @ImportService(interfaceClass = AgentOrderService.class)
    private AgentOrderService agentOrderService;

    @Override
    public void removeOrderById(Long orderId) {
        agentOrderService.removeOrderById(orderId);
    }

    @Override
    public AgentOrder replaceAgentOrder(AgentOrder agentOrder) {
        return agentOrderService.replaceAgentOrder(agentOrder);
    }

    @Override
    public Long insertAgentOrder(AgentOrder agentOrder) {
        return agentOrderService.insertAgentOrder(agentOrder);
    }

    @Override
    public AgentOrderProduct replaceAgentOrderProduct(AgentOrderProduct orderProduct) {
        return agentOrderService.replaceAgentOrderProduct(orderProduct);
    }

    @Override
    public Long insertAgentOrderProduct(AgentOrderProduct orderProduct) {
        return agentOrderService.insertAgentOrderProduct(orderProduct);
    }

    @Override
    public Boolean updateCrmUserInfo(Long orderId, String creator, String latestProcessor) {
        return agentOrderService.updateCrmUserInfo(orderId, creator, latestProcessor);
    }

    @Override
    public Integer deleteAgentOrderProduct(Long productId) {
        return agentOrderService.deleteAgentOrderProduct(productId);
    }

    @Override
    public Boolean removeAgentOrder(Long orderId) {
        return agentOrderService.removeAgentOrder(orderId);
    }

    @Override
    public AgentOrder upsertAgentOrder(AgentOrder agentOrder) {
        return agentOrderService.upsertAgentOrder(agentOrder);
    }

    @Override
    public Long saveOrder(AgentOrder agentOrder) {
        return agentOrderService.saveOrder(agentOrder);
    }

    @Override
    public Boolean updateWorkflowId(Long id, Long workflowId) {
        return agentOrderService.updateWorkflowId(id, workflowId);
    }

    @Override
    public Boolean updateApplyStatus(Long id, ApplyStatus status) {
        return agentOrderService.updateApplyStatus(id, status);
    }

    @Override
    public Boolean updateOrderStatus(Long id, AgentOrderStatus orderStatus) {
        return agentOrderService.updateOrderStatus(id, orderStatus);
    }

    @Override
    public int updateAgentOrderInvoiceId(Long id) {
        return agentOrderService.updateAgentOrderInvoiceId(id);
    }


}
