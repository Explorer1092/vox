package com.voxlearning.utopia.service.crm.api.loader.agent;

import com.voxlearning.alps.annotation.remote.Idempotent;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.crm.api.constants.ApplyStatus;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentOrderStatus;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentOrder;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentOrderProduct;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * AgentOrderLoader
 *
 * @author song.wang
 * @date 2017/1/11
 */

@ServiceVersion(version = "2018.05.25")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
public interface AgentOrderLoader extends IPingable {

    /**
     * 根据订单Id返回订单包含产品
     *
     * @param orderId 订单ID
     * @return 包含订单产品的AgentOrder
     */
    AgentOrder getOrderById(Long orderId);
    /**
     * 获取用户未提交的订单（草稿状态， 购物车）
     * @param userId
     * @return
     */
    AgentOrder loadUserDraftOrder(Long userId);

    /**
     * 根据订单状态查询订单
     *
     * @param status 订单状态
     * @return 对应状态的订单
     */
    List<AgentOrder> findAgentOrderByStatus(AgentOrderStatus status);

    /**
     * 查询指定订单ID的所有产品
     *
     * @param orderId 订单Id
     * @return
     */
    List<AgentOrderProduct> findAgentOrderProductByOrderId(Long orderId);

    /**
     * 查询指定订单ID的所有产品
     *
     * @param orderIds 订单Id
     * @return
     */
    Map<Long,List<AgentOrderProduct>> findAgentOrderProductByOrderIds(List<Long> orderIds);

    /**
     * 查询指定Id下的订单
     *
     * @param orderId 订单Id
     * @return AgentOrder
     */
    AgentOrder loadAgentOrderById(Long orderId);

    /**
     * 查询一堆Id下的订单
     *
     * @param orderIds 一堆ID
     * @return ID 与 订单的映射
     */
    Map<Long, AgentOrder> loadAgentOrderByIds(Collection<Long> orderIds);

    /**
     * 查询指定ID下的产品
     *
     * @param productId 产品Id
     * @return AgentOrderProduct
     */
    AgentOrderProduct loadAgentOrderProductById(Long productId);

    /**
     * 根据创建人
     *
     * @param creator 创建人id
     * @return 订单列表
     */
    List<AgentOrder> findAgentOrderByCreator(Long creator);

    /**
     * 根据发货单ID 查询 订单
     *
     * @param invoiceId 发货单ID
     * @return 订单列表
     */
    List<AgentOrder> findAgentOrderByInvoiceId(Long invoiceId);

    /**
     *  根据订单创建时间和订单状态查询订单
     * @param status 订单状态
     * @param startDate 创建的开始时间
     * @param endDate 创建的结束时间
     * @return 订单列表
     */
    List<AgentOrder> findAgentOrderByOrderTime(AgentOrderStatus status, Date startDate, Date endDate);

    /**
     * 根据工作流Id 查找申请记录
     */
    @Idempotent
    AgentOrder findByWorkflowId(Long workflowId);

    /**
     * 按照条件查询
     * @param startDate
     * @param endDate
     * @param userIds
     * @param orderId
     * @param applyStatus
     * @return
     */
    List<AgentOrder> loads(Date startDate, Date endDate,Collection<Long> userIds,Long orderId,ApplyStatus applyStatus);

}
