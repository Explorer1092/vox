package com.voxlearning.utopia.service.crm.impl.loader.agent;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.crm.api.constants.ApplyStatus;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentOrderStatus;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentOrder;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentOrderProduct;
import com.voxlearning.utopia.service.crm.api.loader.agent.AgentOrderLoader;
import com.voxlearning.utopia.service.crm.impl.dao.agent.AgentOrderPersistence;
import com.voxlearning.utopia.service.crm.impl.dao.agent.AgentOrderProductPersistence;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AgentOrderLoaderImpl
 *
 * @author song.wang
 * @date 2017/1/11
 */
@Named
@Service(interfaceClass = AgentOrderLoader.class)
@ExposeService(interfaceClass = AgentOrderLoader.class)
public class AgentOrderLoaderImpl extends SpringContainerSupport implements AgentOrderLoader {
    @Inject
    private AgentOrderPersistence agentOrderPersistence;
    @Inject
    private AgentOrderProductPersistence agentOrderProductPersistence;

    @Override
    public AgentOrder getOrderById(Long orderId) {
        AgentOrder retOrder = agentOrderPersistence.load(orderId);
        if (retOrder != null) {
            retOrder.setOrderProductList(agentOrderProductPersistence.findByOrderId(orderId));
        }
        return retOrder;
    }

    @Override
    public AgentOrder loadUserDraftOrder(Long userId) {
        List<AgentOrder> myOrderList = agentOrderPersistence.findByCreator(userId);
        if (myOrderList == null || myOrderList.size() == 0) {
            return null;
        }

        // 过滤，只取草稿状态订单
        myOrderList = myOrderList.stream()
                .filter(t -> Objects.equals(t.getOrderStatus(), AgentOrderStatus.DRAFT.getStatus()))
                .collect(Collectors.toList());

        if (myOrderList == null || myOrderList.size() == 0) {
            return null;
        }

        // 取订单商品列表
        AgentOrder draftOrder = myOrderList.get(0);
        draftOrder.setOrderProductList(agentOrderProductPersistence.findByOrderId(draftOrder.getId()));

        return draftOrder;
    }

    @Override
    public List<AgentOrder> findAgentOrderByStatus(AgentOrderStatus status) {
        return agentOrderPersistence.findByStatus(status);
    }

    @Override
    public List<AgentOrderProduct> findAgentOrderProductByOrderId(Long orderId) {
        return agentOrderProductPersistence.findByOrderId(orderId);
    }

    @Override
    public Map<Long,List<AgentOrderProduct>> findAgentOrderProductByOrderIds(List<Long> orderIds) {
        return agentOrderProductPersistence.findByOrderIds(orderIds);
    }

    @Override
    public AgentOrder loadAgentOrderById(Long orderId) {
        return agentOrderPersistence.load(orderId);
    }

    @Override
    public Map<Long, AgentOrder> loadAgentOrderByIds(Collection<Long> orderIds) {
        return agentOrderPersistence.loads(orderIds);
    }

    @Override
    public AgentOrderProduct loadAgentOrderProductById(Long productId) {
        return agentOrderProductPersistence.load(productId);
    }

    @Override
    public List<AgentOrder> findAgentOrderByCreator(Long creator) {
        return agentOrderPersistence.findByCreator(creator);
    }

    @Override
    public List<AgentOrder> findAgentOrderByInvoiceId(Long invoiceId) {
        return agentOrderPersistence.findByInvoiceId(invoiceId);
    }

    @Override
    public List<AgentOrder> findAgentOrderByOrderTime(AgentOrderStatus status, Date startDate, Date endDate) {
        return agentOrderPersistence.findByOrderTime(status, startDate, endDate);
    }

    @Override
    public AgentOrder findByWorkflowId(Long workflowId) {
        AgentOrder retOrder = agentOrderPersistence.findByWorkflowId(workflowId);
        if (retOrder != null) {
            retOrder.setOrderProductList(agentOrderProductPersistence.findByOrderId(retOrder.getId()));
        }
        return retOrder;

    }

    @Override
    public List<AgentOrder> loads(Date startDate, Date endDate, Collection<Long> userIds, Long orderId, ApplyStatus applyStatus) {
        return agentOrderPersistence.loads(startDate,endDate,userIds,orderId,applyStatus);
    }
}
