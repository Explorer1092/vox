package com.voxlearning.utopia.service.crm.api.service.agent;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.crm.api.constants.ApplyStatus;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentOrderStatus;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentOrder;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentOrderProduct;

import java.util.concurrent.TimeUnit;

/**
 * AgentOrderService
 *
 * @author song.wang
 * @date 2017/1/11
 */

@ServiceVersion(version = "2017.01.12")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface AgentOrderService extends IPingable {

    void removeOrderById(Long orderId);

    AgentOrder replaceAgentOrder(AgentOrder agentOrder);

    Long insertAgentOrder(AgentOrder agentOrder);

    AgentOrderProduct replaceAgentOrderProduct(AgentOrderProduct orderProduct);

    Long insertAgentOrderProduct(AgentOrderProduct orderProduct);

    Boolean updateCrmUserInfo(final Long orderId, String creator, String latestProcessor);

    Integer deleteAgentOrderProduct(Long productId);

    Boolean removeAgentOrder(Long orderId);

    AgentOrder upsertAgentOrder(AgentOrder agentOrder);

    Long saveOrder(AgentOrder agentOrder);

    Boolean updateWorkflowId(Long id, Long workflowId);

    /**
     * 更新申请状态
     * @param id id
     * @param status status
     * @return boolean
     */
    Boolean updateApplyStatus(Long id, ApplyStatus status);

    /**
     * 更新申请状态
     * @param id id
     * @param orderStatus orderStatus
     * @return boolean
     */
    Boolean updateOrderStatus(Long id, AgentOrderStatus orderStatus);

    int updateAgentOrderInvoiceId(Long id);
}
