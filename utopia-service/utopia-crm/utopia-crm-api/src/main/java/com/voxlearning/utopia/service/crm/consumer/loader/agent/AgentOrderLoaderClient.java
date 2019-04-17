package com.voxlearning.utopia.service.crm.consumer.loader.agent;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.service.crm.api.constants.ApplyStatus;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentOrderStatus;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentOrder;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentOrderProduct;
import com.voxlearning.utopia.service.crm.api.loader.agent.AgentOrderLoader;

import java.util.*;

/**
 * AgentOrderLoaderClient
 *
 * @author song.wang
 * @date 2017/1/11
 */
public class AgentOrderLoaderClient implements AgentOrderLoader {

    @ImportService(interfaceClass = AgentOrderLoader.class)
    private AgentOrderLoader remoteReference;

    @Override
    public AgentOrder getOrderById(Long orderId) {
        return remoteReference.getOrderById(orderId);
    }

    @Override
    public AgentOrder loadUserDraftOrder(Long userId) {
        return remoteReference.loadUserDraftOrder(userId);
    }

    @Override
    public List<AgentOrder> findAgentOrderByStatus(AgentOrderStatus status) {
        return remoteReference.findAgentOrderByStatus(status);
    }

    @Override
    public List<AgentOrderProduct> findAgentOrderProductByOrderId(Long orderId) {
        if (orderId == null) {
            return Collections.emptyList();
        }
        return remoteReference.findAgentOrderProductByOrderId(orderId);
    }

    @Override
    public Map<Long,List<AgentOrderProduct>> findAgentOrderProductByOrderIds(List<Long> orderIds) {
        if (CollectionUtils.isEmpty(orderIds)) {
            return new HashMap<>();
        }
        return remoteReference.findAgentOrderProductByOrderIds(orderIds);
    }

    @Override
    public AgentOrder loadAgentOrderById(Long orderId) {
        if (orderId == null) {
            return null;
        }
        return remoteReference.loadAgentOrderById(orderId);
    }

    @Override
    public Map<Long, AgentOrder> loadAgentOrderByIds(Collection<Long> orderIds) {
        if (CollectionUtils.isEmpty(orderIds)) {
            return Collections.emptyMap();
        }
        return remoteReference.loadAgentOrderByIds(orderIds);
    }

    @Override
    public AgentOrderProduct loadAgentOrderProductById(Long productId) {
        if (productId == null) {
            return null;
        }
        return remoteReference.loadAgentOrderProductById(productId);
    }

    @Override
    public List<AgentOrder> findAgentOrderByCreator(Long creator) {
        return remoteReference.findAgentOrderByCreator(creator);
    }

    @Override
    public List<AgentOrder> findAgentOrderByInvoiceId(Long invoiceId) {
        return remoteReference.findAgentOrderByInvoiceId(invoiceId);
    }

    @Override
    public List<AgentOrder> findAgentOrderByOrderTime(AgentOrderStatus status, Date startDate, Date endDate) {
        return remoteReference.findAgentOrderByOrderTime(status, startDate, endDate);
    }

    @Override
    public AgentOrder findByWorkflowId(Long workflowId) {
        return remoteReference.findByWorkflowId(workflowId);
    }

    @Override
    public List<AgentOrder> loads(Date startDate, Date endDate, Collection<Long> userIds, Long orderId, ApplyStatus applyStatus) {
        return remoteReference.loads(startDate,endDate,userIds,orderId,applyStatus);
    }

}
