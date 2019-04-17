package com.voxlearning.utopia.service.crm.impl.service.agent;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.crm.api.constants.ApplyStatus;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentOrderStatus;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentOrder;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentOrderProduct;
import com.voxlearning.utopia.service.crm.api.service.agent.AgentOrderService;
import com.voxlearning.utopia.service.crm.impl.dao.agent.AgentOrderPersistence;
import com.voxlearning.utopia.service.crm.impl.dao.agent.AgentOrderProductPersistence;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * AgentOrderServiceImpl
 *
 * @author song.wang
 * @date 2017/1/11
 */
@Named
@Service(interfaceClass = AgentOrderService.class)
@ExposeService(interfaceClass = AgentOrderService.class)
public class AgentOrderServiceImpl extends SpringContainerSupport implements AgentOrderService {

    @Inject private AgentOrderProductPersistence agentOrderProductPersistence;
    @Inject private AgentOrderPersistence agentOrderPersistence;

    @Override
    public void removeOrderById(Long orderId) {
        List<AgentOrderProduct> orderProductList = agentOrderProductPersistence.findByOrderId(orderId);
        if (CollectionUtils.isNotEmpty(orderProductList)) {
            orderProductList.forEach(p -> deleteAgentOrderProduct(p.getId()));
        }
        removeAgentOrder(orderId);
    }



    @Override
    public AgentOrder replaceAgentOrder(AgentOrder agentOrder) {
        return agentOrderPersistence.replace(agentOrder);
    }

    @Override
    public Long insertAgentOrder(AgentOrder agentOrder) {
        agentOrderPersistence.insert(agentOrder);
        return agentOrder.getId();
    }

    @Override
    public AgentOrderProduct replaceAgentOrderProduct(AgentOrderProduct orderProduct) {
        return agentOrderProductPersistence.replace(orderProduct);
    }

    @Override
    public Long insertAgentOrderProduct(AgentOrderProduct orderProduct) {
        agentOrderProductPersistence.insert(orderProduct);
        return orderProduct.getId();
    }

    @Override
    public Boolean updateCrmUserInfo(Long orderId, String creator, String latestProcessor) {
        return agentOrderPersistence.updateCrmUserInfo(orderId, creator, latestProcessor);
    }

    @Override
    public Integer deleteAgentOrderProduct(Long productId) {
        return agentOrderProductPersistence.delete(productId);
    }

    @Override
    public Boolean removeAgentOrder(Long orderId) {
        return agentOrderPersistence.remove(orderId);
    }

    @Override
    public AgentOrder upsertAgentOrder(AgentOrder agentOrder) {
        return agentOrderPersistence.upsert(agentOrder);
    }

    @Override
    public Long saveOrder(AgentOrder agentOrder) {
        Long orderId = agentOrder.getId();
        if (orderId != null) {
            agentOrderPersistence.replace(agentOrder);
        } else {
            agentOrderPersistence.insert(agentOrder);
            orderId = agentOrder.getId();
        }

        List<AgentOrderProduct> orderProductList = agentOrder.getOrderProductList();
        if (orderProductList != null && orderProductList.size() > 0) {
            for (AgentOrderProduct orderProduct : orderProductList) {
                orderProduct.setOrderId(orderId);
                if (orderProduct.getId() != null) {
                    agentOrderProductPersistence.replace(orderProduct);
                } else {
                    agentOrderProductPersistence.insert(orderProduct);
                }
            }
        }
        return orderId;
    }

    @Override
    public Boolean updateWorkflowId(Long id, Long workflowId) {
        AgentOrder item = agentOrderPersistence.load(id);
        if (item == null) {
            return false;
        }
        item.setWorkflowId(workflowId);
        return agentOrderPersistence.replace(item) != null;
    }

    @Override
    public Boolean updateApplyStatus(Long id, ApplyStatus status) {
        AgentOrder item = agentOrderPersistence.load(id);
        if (item == null) {
            return false;
        }
        item.setStatus(status);
        return agentOrderPersistence.replace(item) != null;
    }

    @Override
    public Boolean updateOrderStatus(Long id, AgentOrderStatus orderStatus) {
        AgentOrder item = agentOrderPersistence.load(id);
        if (item == null) {
            return false;
        }
        item.setOrderStatus(orderStatus.getStatus());
        return agentOrderPersistence.replace(item) != null;
    }

    @Override
    public int updateAgentOrderInvoiceId(Long id) {
        return agentOrderPersistence.updateAgentOrderInvoiceId(id);
    }


}
