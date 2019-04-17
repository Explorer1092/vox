/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.agent.service.common;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.agent.persist.AgentOrderProcessHistoryPersistence;
import com.voxlearning.utopia.agent.persist.AgentOrderProcessPersistence;
import com.voxlearning.utopia.agent.persist.AgentProductPersistence;
import com.voxlearning.utopia.agent.persist.entity.AgentOrderProcess;
import com.voxlearning.utopia.agent.persist.entity.AgentOrderProcessHistory;
import com.voxlearning.utopia.agent.persist.entity.AgentProduct;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.utils.MathUtils;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentOrderStatus;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentOrderType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentOrder;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentOrderProduct;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentOrderLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.service.agent.AgentOrderServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * 订单服务
 * Created by Shuai.Huan on 2014/8/8.
 */
@Named
public class BaseOrderService extends AbstractAgentService {

    @Inject private AgentOrderLoaderClient agentOrderLoaderClient;
    @Inject private AgentOrderServiceClient agentOrderServiceClient;
    @Inject private AgentOrderProcessPersistence agentOrderProcessPersistence;
    @Inject private AgentOrderProcessHistoryPersistence agentOrderProcessHistoryPersistence;
    @Inject private AgentProductPersistence agentProductPersistence;
    @Inject private BaseUserService baseUserService;
    @Inject private BaseOrgService baseOrgService;

    private Map<Integer, AgentOrderStatus> orderStatusMap = AgentOrderStatus.toMap();
    private Map<Integer, AgentOrderType> orderTypeMap = AgentOrderType.toMap();

    //AgentOrder
    public AgentOrder getOrderById(Long id) {
        return agentOrderLoaderClient.getOrderById(id);
    }


    public boolean updateCrmUserInfo(Long orderId, String creator, String latestProcessor) {
        return agentOrderServiceClient.updateCrmUserInfo(orderId, creator, latestProcessor);
    }

    //AgentOrderProcess
    public AgentOrderProcess getOrderProcessById(Long id) {
        return agentOrderProcessPersistence.loadFromDatabase(id);
    }

    public AgentOrderProcess getOrderProcessByOrderId(Long orderId) {
        return agentOrderProcessPersistence.findByOrderId(orderId);
    }

    public void saveOrderProcess(AgentOrderProcess agentOrderProcess) {
        agentOrderProcessPersistence.persist(agentOrderProcess);
    }

    public void deleteProcessByOrderId(Long orderId) {
        agentOrderProcessPersistence.deleteByOrderId(orderId);
    }

    public void deleteOrderProductById(Long orderProductId) {
        agentOrderServiceClient.deleteAgentOrderProduct(orderProductId);
    }

    public void saveOrderHistory(AgentOrderProcessHistory agentOrderProcessHistory) {
        agentOrderProcessHistoryPersistence.insert(agentOrderProcessHistory);
    }

    // 更改这个方法，直接传入AgentOrder，减少多余的查询 By Wyc 2016-05-27
    public Map<String, Object> generateOrderMap(AgentOrder agentOrder) {
//        AgentOrder agentOrder = agentOrderPersistence.load(orderId);
        AgentOrderType orderType = AgentOrderType.of(agentOrder.getOrderType());
        switch (orderType) {
            case REFUND:
                return generateRefundOrderMap(agentOrder);
            case CASH_WITHDRAW:
                return generateCashWithdrawOrderMap(agentOrder);
            default:
                return generateDefaultOrderMap(agentOrder);
        }
    }

    public List<AgentOrder> findAgentOrderByCreator(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return agentOrderLoaderClient.findAgentOrderByCreator(userId);
    }


    public List<Map<String, Object>> loadUserOrders(Long userId) {
        List<AgentOrder> orderList = findAgentOrderByCreator(userId);
        if (orderList == null || orderList.size() == 0) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> retOrders = new ArrayList<>();
        for (AgentOrder order : orderList) {
            if (order.getOrderStatus() != AgentOrderStatus.DRAFT.getStatus()) {
                retOrders.add(generateOrderMap(order));
            }
        }

        return retOrders;
    }

    private Map<String, Object> generateDefaultOrderMap(AgentOrder agentOrder) {
        Map<String, Object> map = new HashMap<>();

        map.put("createDate", agentOrder.getOrderTime() == null ? "" : DateUtils.dateToString(agentOrder.getOrderTime(), "yyyy-MM-dd"));
        map.put("orderId", agentOrder.getId());
        map.put("orderAmount", agentOrder.getOrderAmount() - agentOrder.getPointChargeAmount());
        if (agentOrder.getOrderType().equals(AgentOrderType.DEPOSIT.getType())) {
            String accountName = agentOrder.getOrderNotes();
            String orderNotes = "";
            if (accountName.indexOf("#") > 0) {
                orderNotes = accountName.substring(accountName.indexOf("#") + 1);
                accountName = accountName.substring(0, accountName.indexOf("#"));
            }
            AgentUser user = baseUserService.getByAccountName(accountName);
            if (user != null) {
                String notes = user.getRealName();
                if (StringUtils.isNotEmpty(user.getBankName())) {
                    notes += " " + user.getBankName();
                }
                if (StringUtils.isNotEmpty(user.getBankHostName())) {
                    notes += " " + user.getBankHostName();
                }
                if (StringUtils.isNotEmpty(user.getBankAccount())) {
                    notes += " " + user.getBankAccount();
                }

                if (!StringUtils.isBlank(orderNotes)) {
                    notes += " " + orderNotes;
                }

                map.put("orderNotes", notes);
            }
        } else {
            map.put("orderNotes", agentOrder.getOrderNotes());
        }
        map.put("orderType", agentOrder.getOrderType());
        map.put("orderStatus", agentOrder.getOrderStatus());
        AgentUser agentUser = baseUserService.getById(agentOrder.getCreator());
        if (agentUser != null) {
            map.put("creator", agentUser.getRealName());
            map.put("usableCashAmount", agentUser.getUsableCashAmount());
        } else {
            map.put("usableCashAmount", 0f);
        }
        if (orderTypeMap.get(agentOrder.getOrderType()) != null) {
            map.put("orderTypeStr", orderTypeMap.get(agentOrder.getOrderType()).getDesc());
        }
        if (orderStatusMap.get(agentOrder.getOrderStatus()) != null) {
            map.put("orderStatusStr", orderStatusMap.get(agentOrder.getOrderStatus()).getDesc());
        }
        agentUser = baseUserService.getById(agentOrder.getLatestProcessor());
        if (agentUser != null) {
            map.put("latestProcessor", agentUser.getRealName());
        }

        List<AgentOrderProduct> orderProductList = agentOrderLoaderClient.findAgentOrderProductByOrderId(agentOrder.getId());
        if (orderProductList != null && orderProductList.size() > 0) {
            StringBuilder builder = new StringBuilder();
            for (AgentOrderProduct orderProduct : orderProductList) {
                AgentProduct product = agentProductPersistence.load(orderProduct.getProductId());
                if (product == null) {
                    continue;
                }
                builder.append("商品名:").append(product.getProductName());
                builder.append(" 商品单价:").append(product.getPrice()).append("元");
                builder.append(" 购买数量:").append(orderProduct.getProductQuantity());
                builder.append(" 金额:").append(MathUtils.floatMultiply(orderProduct.getProductQuantity(), product.getPrice())).append("元");
                builder.append("<br/>");
            }
            if (agentOrder.getPointChargeAmount() > 0) {
                builder.append("点数总抵现金额:").append(agentOrder.getPointChargeAmount()).append("元");
            }
            map.put("orderProducts", builder.toString());
        }

        agentOrder.setOrderProductList(orderProductList);

        map.put("hasProductCard", agentOrder.hasProductCard());

        AgentGroup agentGroup = baseOrgService.getGroupById(agentOrder.getLatestProcessorGroup());
        if (agentGroup != null) {
            map.put("latestProcessorGroup", agentGroup.getGroupName());
        }

        map.put("logisticsInfo", agentOrder.getLogisticsInfo());
        map.put("consignee", agentOrder.getConsignee());
        map.put("address", agentOrder.getAddress());
        map.put("mobile", agentOrder.getMobile());
        return map;
    }

    private Map<String, Object> generateRefundOrderMap(AgentOrder agentOrder) {
        Map<String, Object> map = new HashMap<>();
        map.put("createDate", DateUtils.dateToString(agentOrder.getCreateDatetime(), "yyyy-MM-dd"));
        map.put("orderId", agentOrder.getId());
        map.put("orderAmount", agentOrder.getOrderAmount() - agentOrder.getPointChargeAmount());
        map.put("orderType", agentOrder.getOrderType());
        map.put("orderStatus", agentOrder.getOrderStatus());
        map.put("creator", agentOrder.getLatestProcessorName());
        if (orderTypeMap.get(agentOrder.getOrderType()) != null) {
            map.put("orderTypeStr", orderTypeMap.get(agentOrder.getOrderType()).getDesc());
        }
        if (orderStatusMap.get(agentOrder.getOrderStatus()) != null) {
            map.put("orderStatusStr", "等待财务确认");
        }
        map.put("latestProcessor", agentOrder.getLatestProcessorName());

        Map<String, Object> refundInfoMap = JsonUtils.convertJsonObjectToMap(agentOrder.getOrderNotes());
        String afentiOrderId = SafeConverter.toString(refundInfoMap.get("orderId"));
        String productInfo = StringUtils.join(
                "外部订单号:", afentiOrderId, ",",
                "产品名称:", refundInfoMap.get("productName"), ",",
                "金额:", refundInfoMap.get("amount")
        );
        String memo = StringUtils.isNotBlank(SafeConverter.toString(refundInfoMap.get("memo"))) ? SafeConverter.toString(refundInfoMap.get("memo")) : "(无)";
        String payMethod = SafeConverter.toString(refundInfoMap.get("payMethod"));
        String notes = StringUtils.join(
                "用户信息:", agentOrder.getCreatorName(), "(" + agentOrder.getCreator() + "),",
                "支付流水号:", refundInfoMap.get("transactionId"), ",",
                "支付方式:", payMethod, ",",
                "备注:", memo
        );

        map.put("orderProducts", productInfo);
        map.put("hasProductCard", false);
        map.put("orderNotes", notes);
        map.put("latestProcessorGroup", "17Admin");
        map.put("logisticsInfo", agentOrder.getLogisticsInfo());
        return map;
    }

    private Map<String, Object> generateCashWithdrawOrderMap(AgentOrder agentOrder) {
        Map<String, Object> map = new HashMap<>();
        map.put("createDate", DateUtils.dateToString(agentOrder.getCreateDatetime(), "yyyy-MM-dd"));
        map.put("orderId", agentOrder.getId());
        map.put("orderAmount", agentOrder.getOrderAmount() - agentOrder.getPointChargeAmount());
        map.put("orderType", agentOrder.getOrderType());
        map.put("orderStatus", agentOrder.getOrderStatus());
        map.put("creator", agentOrder.getLatestProcessorName());
        if (orderTypeMap.get(agentOrder.getOrderType()) != null) {
            map.put("orderTypeStr", orderTypeMap.get(agentOrder.getOrderType()).getDesc());
        }
        if (orderStatusMap.get(agentOrder.getOrderStatus()) != null) {
            map.put("orderStatusStr", "等待财务确认");
        }
        map.put("latestProcessor", agentOrder.getLatestProcessorName());
        map.put("orderProducts", "-");
        map.put("hasProductCard", false);
        String notes = StringUtils.join(
                "用户信息:", agentOrder.getCreatorName(), "(" + agentOrder.getCreator() + "),",
                "支付流水号:", JsonUtils.fromJsonToList(agentOrder.getOrderNotes(), String.class)
        );
        map.put("orderNotes", notes);
        map.put("latestProcessorGroup", "17Admin");
        map.put("logisticsInfo", agentOrder.getLogisticsInfo());
        return map;
    }

    public void removeOrderById(Long orderId) {
        agentOrderServiceClient.removeOrderById(orderId);
    }
}
