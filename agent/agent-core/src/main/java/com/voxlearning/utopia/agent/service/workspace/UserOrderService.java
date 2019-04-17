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

package com.voxlearning.utopia.agent.service.workspace;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilder;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.mockexam.integration.StringUtil;
import com.voxlearning.utopia.agent.persist.AgentProductPersistence;
import com.voxlearning.utopia.agent.persist.entity.AgentProduct;
import com.voxlearning.utopia.agent.persist.entity.material.AgentMaterialBudget;
import com.voxlearning.utopia.agent.persist.entity.material.AgentMaterialCost;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.service.common.BaseOrderService;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.common.BaseUserService;
import com.voxlearning.utopia.agent.service.material.AgentMaterialBudgetService;
import com.voxlearning.utopia.agent.service.sysconfig.ProductConfigService;
import com.voxlearning.utopia.agent.utils.MathUtils;
import com.voxlearning.utopia.service.crm.api.constants.ApplyStatus;
import com.voxlearning.utopia.service.crm.api.constants.ApplyType;
import com.voxlearning.utopia.service.crm.api.constants.SystemPlatformType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentOrderPaymentMode;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentOrderStatus;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentOrderType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.*;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentOrderLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.service.agent.AgentOrderServiceClient;
import com.voxlearning.utopia.service.workflow.api.bean.WorkFlowProcessUser;
import com.voxlearning.utopia.service.workflow.api.constants.WorkFlowType;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowRecord;
import com.voxlearning.utopia.service.workflow.consumer.WorkFlowDataServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用户商品购买用Service
 * <p>
 * Created by Alex on 14-8-14.
 */
@Named
public class UserOrderService extends AbstractAgentService {

    @Inject private BaseOrderService baseOrderService;
    @Inject private BaseUserService baseUserService;
    @Inject private AgentProductPersistence agentProductPersistence;
    @Inject private ProductConfigService productConfigService;
    @Inject private BaseOrgService baseOrgService;
    @Inject private AgentOrderServiceClient agentOrderServiceClient;
    @Inject private WorkFlowDataServiceClient workFlowDataServiceClient;
    @Inject private AgentOrderLoaderClient agentOrderLoaderClient;
    @Inject private AgentMaterialBudgetService agentMaterialBudgetService;

    public MapMessage repairOrderData() {
        List<AgentGroupUser> agentUsers = baseOrgService.getAllMarketDepartmentUsers();
        if (CollectionUtils.isEmpty(agentUsers)) {
            return MapMessage.errorMessage("未找到用户所管理的用户!");
        }
        List<Long> draftOrderIds = new ArrayList<>();
        agentUsers.forEach(p -> {
            if (p == null) {
                return;
            }
            List<AgentOrder> orders = baseOrderService.findAgentOrderByCreator(p.getUserId());
            //orders = orders.stream().filter(p1 -> p1.getOrderStatus() == AgentOrderStatus.DRAFT.getStatus()).collect(Collectors.toList());
            Set<Long> orderIds = orders.stream().map(AgentOrder::getId).collect(Collectors.toSet());
            if (CollectionUtils.isNotEmpty(orderIds)) {
                draftOrderIds.addAll(orderIds);
            }
        });
        if (CollectionUtils.isEmpty(draftOrderIds)) {
            return MapMessage.errorMessage("未找到相关的订单");
        }
        Map<Long, AgentOrder> orderMap = agentOrderLoaderClient.loadAgentOrderByIds(draftOrderIds);
        if (MapUtils.isEmpty(orderMap)) {
            return MapMessage.errorMessage("未找到相关的订单");
        }
        List<AgentOrderProduct> allOrderProduct = new ArrayList<>();
        orderMap.values().forEach(p -> {
            List<AgentOrderProduct> agentOrderProducts = agentOrderLoaderClient.findAgentOrderProductByOrderId(p.getId());
            if (CollectionUtils.isNotEmpty(agentOrderProducts)) {
                allOrderProduct.addAll(agentOrderProducts);
            }
        });
        if (CollectionUtils.isEmpty(allOrderProduct)) {
            return MapMessage.errorMessage("订单和产品的关系出现错误");
        }
        Set<Long> productIds = allOrderProduct.stream().map(AgentOrderProduct::getProductId).collect(Collectors.toSet());
        Map<Long, AgentProduct> allProduct = agentProductPersistence.loads(productIds);
        allOrderProduct.forEach(p -> {
            if (StringUtils.isBlank(p.getProductName()) || p.getPrice() == null) {
                Long agentProductId = p.getProductId();
                if (allProduct.containsKey(agentProductId)) {
                    AgentProduct product = allProduct.get(p.getProductId());
                    p.setProductName(product.getProductName());
                    p.setPrice(product.getPrice());
                    agentOrderServiceClient.replaceAgentOrderProduct(p);
                }
            }
        });
        //draftOrderIds
        return MapMessage.successMessage();
    }

    public AgentOrder createUserOrder(AuthCurrentUser user, Long productId) {
        AgentOrder draftOrder = new AgentOrder();
        draftOrder.setCreator(user.getUserId());
        draftOrder.setCreatorName(user.getRealName());
        draftOrder.setConsignee(user.getRealName());
        draftOrder.setMobile(user.getUserPhone());
        draftOrder.setCreatorGroup(0L);
        draftOrder.setOrderType(AgentOrderType.BUY_MATERIAL.getType());
        draftOrder.setOrderStatus(AgentOrderStatus.DRAFT.getStatus());
        draftOrder.setOrderAmount(0f);
        draftOrder.setLatestProcessor(user.getUserId());
        draftOrder.setLatestProcessorName(user.getRealName());
        draftOrder.setLatestProcessorGroup(0L);
        // 工作流相关内容
        draftOrder.setAccount(SafeConverter.toString(user.getUserId()));
        draftOrder.setAccountName(user.getRealName());
        draftOrder.setApplyType(ApplyType.AGENT_MATERIAL_APPLY);
        draftOrder.setStatus(ApplyStatus.PENDING);
        draftOrder.setUserPlatform(SystemPlatformType.AGENT);

        if (productId != null) {
            AgentProduct product = agentProductPersistence.load(productId);
            AgentOrderProduct orderProduct = new AgentOrderProduct();
            orderProduct.setProductType(product.getProductType());
            orderProduct.setProductId(productId);
            orderProduct.setProductQuantity(0);
            orderProduct.setRank(1);
            orderProduct.setProductName(product.getProductName());
            orderProduct.setPrice(product.getPrice());
            List<AgentOrderProduct> orderProductList = new ArrayList<>();
            orderProductList.add(orderProduct);
            draftOrder.setOrderProductList(orderProductList);
        }
        Long orderId = agentOrderServiceClient.saveOrder(draftOrder);

        return baseOrderService.getOrderById(orderId);
    }

    public void addOrderProduct(AgentOrder order, Long productId) {
        if (order == null || productId == null) {
            return;
        }

        List<AgentOrderProduct> orderProductList = order.getOrderProductList();
        if (orderProductList == null) {
            orderProductList = new ArrayList<>();
        }

        int maxRank = 0;
        for (AgentOrderProduct orderProduct : orderProductList) {
            if (productId.equals(orderProduct.getProductId())) {
                return;
            }

            if (maxRank < orderProduct.getRank()) {
                maxRank = orderProduct.getRank();
            }

        }

        AgentProduct product = agentProductPersistence.load(productId);

        AgentOrderProduct orderProduct = new AgentOrderProduct();
        orderProduct.setProductType(product.getProductType());
        orderProduct.setOrderId(order.getId());
        orderProduct.setProductId(productId);
        orderProduct.setProductQuantity(0);
        orderProduct.setRank(maxRank + 1);
        orderProduct.setProductName(product.getProductName());
        orderProduct.setPrice(product.getPrice());

        orderProductList.add(orderProduct);
        order.setOrderProductList(orderProductList);

        agentOrderServiceClient.saveOrder(order);
    }

    public void removeOrderProduct(AgentOrder order, Long orderProductId) {
        if (order == null || orderProductId == null) {
            return;
        }

        List<AgentOrderProduct> orderProductList = order.getOrderProductList();
        if (orderProductList == null) {
            return;
        }

        boolean isValidOrderProduct = false;
        for (AgentOrderProduct orderProduct : orderProductList) {
            if (orderProductId.equals(orderProduct.getId())) {
                isValidOrderProduct = true;
                break;
            }
        }

        if (isValidOrderProduct) {
            baseOrderService.deleteOrderProductById(orderProductId);
        }
    }

    public MapMessage updateOrder(AgentOrder order, List productQuantity, Boolean needAlert) {
        if (order == null) {
            return MapMessage.errorMessage("订单不存在");
        }
        if (null != order.getConsignee() && order.getConsignee().length() > 50){
            return MapMessage.errorMessage("收货人姓名长度不能超过50");
        }
        if (null != order.getMobile() && order.getMobile().length() > 15){
            return MapMessage.errorMessage("收货人电话格式不正确");
        }
        if (null != order.getAddress() && order.getAddress().length() > 100){
            return MapMessage.errorMessage("收货地址长度不能超过100");
        }

        if (null != order.getOrderNotes() && order.getOrderNotes().length() > 1000){
            return MapMessage.errorMessage("备注长度不能超过1000");
        }
        // 更新Notes
        // 更新商品数量
        if (CollectionUtils.isEmpty(productQuantity) && needAlert) {
            return MapMessage.errorMessage("请从商品列表中选择所需商品");
        }
        List<AgentOrderProduct> orderProductList = order.getOrderProductList();
        float orderAmount = 0;


        for (Object productQuantityInfo : productQuantity) {
            if (productQuantityInfo == null || !(productQuantityInfo instanceof Map)) {
                continue;
            }
            Map<String, Object> info = (Map<String, Object>) productQuantityInfo;
            if (MapUtils.isEmpty(info)) {
                continue;
            }
            Long orderProductId = SafeConverter.toLong(info.get("id"));
            Integer orderProductQuantity = SafeConverter.toInt(info.get("size"));
            if (orderProductQuantity >= 20000){
                return MapMessage.errorMessage("单项购买数量不能大于20000");
            }

            boolean productFlag = true;
            List<String> productNameList = new ArrayList<>();
            for (AgentOrderProduct orderProduct : orderProductList) {
                if (orderProductId.equals(orderProduct.getId())) {
                    AgentProduct product = agentProductPersistence.load(orderProduct.getProductId());
                    if(product == null){
                        return MapMessage.errorMessage("选择的商品不存在");
                    }
                    if (needAlert) {
                        if (product.getStatus() != 2) {
                            return MapMessage.errorMessage(product.getProductName() + "已下架");
                        }
                        if (product.getInventoryQuantity() == null || product.getInventoryQuantity() < orderProductQuantity) {
                            productFlag = false;
                            productNameList.add(product.getProductName() + "库存不足，仅剩" + (product.getInventoryQuantity() == null ? 0 : product.getInventoryQuantity()));

                        }
                    }
                    orderAmount += MathUtils.floatMultiply(product.getPrice(), orderProductQuantity);
                    orderProduct.setProductQuantity(orderProductQuantity);
                    break;
                }
            }
            if(!productFlag){
                productNameList = productNameList.stream().filter(StringUtils::isNoneBlank).collect(Collectors.toList());
                return MapMessage.errorMessage(StringUtils.join(productNameList, "。"));
            }

        }
        order.setOrderAmount(orderAmount);
        agentOrderServiceClient.saveOrder(order);
        return MapMessage.successMessage();
    }
//    public MapMessage submitOrder(AgentOrder order, List productQuantity, String orderNotes,
//                                               String consignee, String mobile, String province, String city, String county, String address, String paymentVoucher, Integer paymentMode,
//                                               Integer costRegionCode, AuthCurrentUser user, List<String> materialBudgetIdList) {
//        try {
//            return AtomicCallbackBuilderFactory.getInstance()
//                    .<MapMessage>newBuilder()
//                    .keyPrefix("AgentSubmitOrder")
//                    .keys(order.getCreator())
//                    .callback(() -> doSubmitOrder(order, productQuantity, orderNotes,
//                            consignee, mobile, province, city, county, address, paymentVoucher, paymentMode,
//                            costRegionCode, user, materialBudgetIdList))
//                    .build()
//                    .execute();
//        }catch (CannotAcquireLockException e) {
//            return MapMessage.errorMessage("该部门城市费用订单请求过多，请稍后再试");
//        }
//
//    }


    public MapMessage doSubmitOrder(AgentOrder order, List productQuantity, String orderNotes,
                                  String consignee, String mobile, String province, String city, String county, String address, String paymentVoucher, Integer paymentMode,
                                  Integer costRegionCode, AuthCurrentUser user, List<String> materialBudgetIdList) {
        // 更新订单
        MapMessage msg = updateOrder(order, productQuantity, true);
        if (!msg.isSuccess()) {
            return msg;
        }
        order.setOrderNotes(orderNotes);
        order.setConsignee(consignee);
        order.setMobile(mobile);
        order.setProvince(province);
        order.setCity(city);
        order.setCounty(county);
        order.setAddress(address);
        if (paymentMode == 0) {
            return MapMessage.errorMessage("请选择支付方式");
        }
        if (paymentMode == 2) {
            if (null == costRegionCode || costRegionCode <= 0){
                return MapMessage.errorMessage("请选择费用城市。");
            }
            List<AgentGroupUser> groupUserList = baseOrgService.getGroupUserByUser(order.getCreator());
            if (CollectionUtils.isNotEmpty(groupUserList)){
                List<AgentMaterialBudget> latest6MonthCityBudget = agentMaterialBudgetService.getLatest6MonthCityBudget(groupUserList.get(0).getGroupId(), costRegionCode);
                if (CollectionUtils.isNotEmpty(latest6MonthCityBudget)){
                    Double balanceSum = 0d;
                    if (CollectionUtils.isNotEmpty(latest6MonthCityBudget)){
                        for (int i = 0; i < latest6MonthCityBudget.size(); i++) {
                            AgentMaterialBudget item = latest6MonthCityBudget.get(i);
                            balanceSum = MathUtils.doubleAdd(balanceSum,item.getBalance());
                        }
                    }
                    if (SafeConverter.toDouble(order.getOrderAmount()) > balanceSum) {
                        return MapMessage.errorMessage("余额不足");
                    }
                } else {
                    return MapMessage.errorMessage("暂无该城市近六个月预算。");
                }
            }else {
                return MapMessage.errorMessage("用户机构对应不正确。");
            }
        }
        if (paymentMode == 3 && StringUtils.isBlank(paymentVoucher)) {
            return MapMessage.errorMessage("所选支付方式为自付时，必须上传支付凭证。");
        }
        order.setPaymentMode(paymentMode);
        if(paymentMode == 3){
            order.setPaymentVoucher(paymentVoucher);
        }
        if (paymentMode == 1) {
            AgentMaterialCost userMaterialCost = agentMaterialBudgetService.getUserMaterialCostByUserId(order.getCreator());
            if (userMaterialCost == null) {
                return MapMessage.errorMessage("未设置余额");
            }
            if (SafeConverter.toDouble(order.getOrderAmount()) > userMaterialCost.getBalance()) {
                return MapMessage.errorMessage("余额不足");
            }
//            Float budget = userMaterialCost.getBudget().floatValue();
//            if (budget == null || Objects.equals(budget, 0f)) {
//                return MapMessage.errorMessage("预算不够");
//            }
        }
        List<AgentOrderProduct> orderProductList = order.getOrderProductList();
        if (CollectionUtils.isEmpty(orderProductList)) {
            return MapMessage.errorMessage("请选择商品！");
        }
        order.setOrderProductList(orderProductList);
        order.setOrderStatus(AgentOrderStatus.UNCHECKED.getStatus());
        if (StringUtils.isBlank(order.getCreatorName())) {
            order.setCreatorName(user.getRealName());
        }
        AgentGroup group = baseOrgService.getGroupFirstOne(order.getCreator(), null);
        if (group == null) {
            return MapMessage.errorMessage("用户部门未找到");
        }
        String quantityChangeDesc = "【购买商品】订单号：" + order.getId() + "(" + (StringUtils.isBlank(group.getLogo()) ? "" : group.getGroupName() + " - ") + user.getRealName() + ")";
        AtomicCallbackBuilder<MapMessage> builder = AtomicCallbackBuilderFactory.getInstance().newBuilder();
        MapMessage message = builder.keyPrefix("AgentProductQuantity")
                .keys(order.getOrderProductList().stream().map(AgentOrderProduct::getProductId).collect(Collectors.toList()))
                .callback(() -> {
                    try {
                        List<String> productNameList = new ArrayList<>();
                        // 变更库存量
                        order.getOrderProductList().forEach(p -> {
                            AgentProduct product = productConfigService.getById(p.getProductId());
                            if (product == null) {
                                productNameList.add(p.getProductName() + ",ID:" + p.getProductId() + " 已被删除");
                                return;
                            }
                            if (product.getStatus() != 2) {
                                productNameList.add(product.getProductName() + "已下架");
                                return;
                            }
                            if (product.getInventoryQuantity() == null || product.getInventoryQuantity() < p.getProductQuantity()) {
                                productNameList.add(product.getProductName() + "库存不足，仅剩" + (product.getInventoryQuantity() == null ? 0 : product.getInventoryQuantity()));
                            }
                        });
                        if (CollectionUtils.isNotEmpty(productNameList)) {
                            return MapMessage.errorMessage(StringUtils.join(productNameList, "。"));
                        }
                        order.getOrderProductList().forEach(p -> {
                            AgentProduct product = productConfigService.getById(p.getProductId());
                            Integer preQuantity = product.getInventoryQuantity() == null ? 0 : product.getInventoryQuantity();
                            product.setInventoryQuantity(product.getInventoryQuantity() - p.getProductQuantity());
                            productConfigService.updateProduct(product);
                            // 添加库存变更记录
                            productConfigService.addAgentProductInventoryRecord(user.getUserId(), product.getId(), preQuantity, product.getInventoryQuantity(), -p.getProductQuantity(), quantityChangeDesc);
                        });
                        return MapMessage.successMessage();
                    } catch (CannotAcquireLockException e) {
                        return MapMessage.errorMessage("订单提交失败，请稍后重试！").withDuplicatedException();
                    }
                })
                .build()
                .execute();
        if (!message.isSuccess()) {
            return message;
        }


        order.setOrderTime(new Date());
        Long id = agentOrderServiceClient.saveOrder(order);
        order.setId(id);

        // 扣除余额
        if (AgentOrderPaymentMode.MATERIAL_COST.getPayId().equals(paymentMode)) {
            agentMaterialBudgetService.deductMaterialBalance(order);
        } else if (AgentOrderPaymentMode.CITY_COST.getPayId().equals(paymentMode)){
            MapMessage mapMessage = agentMaterialBudgetService.deductCityBalance(order,costRegionCode,materialBudgetIdList);
            if(mapMessage.isSuccess()){
                String str = SafeConverter.toString(mapMessage.get("costMonthStr"));
                order.setCostMonthStr(str);
                agentOrderServiceClient.upsertAgentOrder(order);
            }
        }

        AlpsThreadPool.getInstance().submit(() -> addWorkflowRecord(id));
        return MapMessage.successMessage();
    }

    public MapMessage addWorkflowRecord(Long id){
        AgentOrder order = agentOrderLoaderClient.getOrderById(id);
        if(order == null || order.getWorkflowId() != null){
            return MapMessage.errorMessage();
        }
        if(order.getOrderStatus() == null || order.getOrderStatus() == AgentOrderStatus.DRAFT.getStatus() || order.getWorkflowId() != null){
            return MapMessage.errorMessage();
        }
        Integer paymentMode = order.getPaymentMode();
        AgentUser user = baseOrgService.getUser(order.getCreator());
        // 创建工作流
        WorkFlowRecord workFlowRecord = new WorkFlowRecord();
        workFlowRecord.setStatus("init");
        workFlowRecord.setSourceApp("agent");
        workFlowRecord.setTaskName("物料申请");
        String taskContent = order.generateSummary();
        if(StringUtils.isNotBlank(taskContent) && taskContent.length() > 500){ // 数据库字段长度512
            taskContent = taskContent.substring(0, 500);
        }
        workFlowRecord.setTaskContent(taskContent);
        workFlowRecord.setLatestProcessorName(user.getRealName());
        workFlowRecord.setCreatorName(user.getRealName());
        workFlowRecord.setCreatorAccount(String.valueOf(user.getId()));
        workFlowRecord.setWorkFlowType(WorkFlowType.AGENT_MATERIAL_APPLY);

        AgentUser targetUser = null;
        AgentRoleType userRole = baseOrgService.getUserRole(user.getId());
        if (AgentRoleType.BusinessDeveloper == userRole) {
            if (paymentMode == 3) {// 城市费用，现金支付 ， 转至财务审核
                workFlowRecord.setStatus("lv1");
            } else if (paymentMode == 2){
                AgentUser userManager = baseOrgService.getUserRealManager(user.getId());
                if (null != userManager) {
                    targetUser = userManager;
                    workFlowRecord.setStatus("init2");
                } else {
                    workFlowRecord.setStatus("lv1");
                }
            }else { // 物料经费 ， 需上级审核
                AgentUser userManager = baseOrgService.getUserRealManager(user.getId());
                if (null != userManager) {
                    targetUser = userManager;
                } else {
                    workFlowRecord.setStatus("lv2");
                }
            }
        } else if (AgentRoleType.CityManager == userRole) {  // 市经理提交的物料申请
            if (paymentMode == 3 || paymentMode == 2) { // 城市费用，现金支付 ， 转至财务审核
                workFlowRecord.setStatus("lv1");
            } else { // 物料经费 ， 需审核员审核
                workFlowRecord.setStatus("lv2");
            }
        }else if (AgentRoleType.Country == userRole){
            workFlowRecord.setStatus("lv3");
            agentOrderServiceClient.updateApplyStatus(order.getId(),ApplyStatus.APPROVED);
            agentOrderServiceClient.updateOrderStatus(order.getId(),AgentOrderStatus.APPROVED);
        }

        WorkFlowProcessUser processUser = null;
        if (targetUser != null) {
            processUser = new WorkFlowProcessUser();
            processUser.setUserPlatform("agent");
            processUser.setAccount(String.valueOf(targetUser.getId()));
            processUser.setAccountName(targetUser.getRealName());
        }

        MapMessage mapMessage = workFlowDataServiceClient.addWorkFlowRecord(workFlowRecord, processUser);
        Long workflowId = fetchWorkflowId(mapMessage);
        if (workflowId == null) {
            return MapMessage.errorMessage();
        }
        agentOrderServiceClient.updateWorkflowId(id, workflowId);
        return MapMessage.successMessage();
    }

    private Long fetchWorkflowId(MapMessage mapMessage){
        if(!mapMessage.isSuccess()){
            return null;
        }
        WorkFlowRecord workFlowRecord = (WorkFlowRecord) mapMessage.get("workFlowRecord");
        if(workFlowRecord == null){
            return null;
        }
        return workFlowRecord.getId();
    }

    private Boolean checkCityCostMonth(Integer cityCostMonth) {
        Integer yearMonth = SafeConverter.toInt(DateUtils.dateToString(new Date(), "yyyyMM"));
        return cityCostMonth <= yearMonth;
    }

    public MapMessage beforeSubmitOrder(List<AgentOrderProduct> orderProductList) {
        Map<String,Object> dataMap = new HashMap<>();
        //购物车中的商品
        List<Long> productIds = orderProductList.stream().map(AgentOrderProduct::getProductId).collect(Collectors.toList());
        Map<Long, AgentOrderProduct> orderProductMap = orderProductList.stream().collect(Collectors.toMap(AgentOrderProduct::getProductId, Function.identity(), (o1, o2) -> o1));
        //商品信息
        Map<Long, AgentProduct> productMap = agentProductPersistence.loads(productIds);
        if (MapUtils.isEmpty(productMap) || MapUtils.isEmpty(orderProductMap)) {
            return MapMessage.errorMessage("商品信息不存在！");
        }
        //判断是否库存不足
        List<String> productIdList = new ArrayList<>();
        List<String> productNameList = new ArrayList<>();
        productIds.forEach(item -> {
            AgentOrderProduct orderProduct = orderProductMap.get(item);
            AgentProduct product = productMap.get(item);
            if (null != orderProduct && null != product){
                if (product.getInventoryQuantity() <= 0 || orderProduct.getProductQuantity() > product.getInventoryQuantity()){
                    productIdList.add(ConversionUtils.toString(product.getId()));
                    productNameList.add(product.getProductName());
                }
            }
        });
        //如果订单中的商品全部库存不足
        if (productIds.size() == productIdList.size()){
            return MapMessage.errorMessage().add("errorInfo","购物车中的商品库存不足，请返回修改！").add("inventoryFlag","allNotEnough");
        }
        if (CollectionUtils.isNotEmpty(productIdList) && CollectionUtils.isNotEmpty(productNameList)){
            return MapMessage.errorMessage().add("errorInfo",StringUtils.formatMessage("{}库存不足，是否仅购买有货商品？",StringUtils.join(productNameList,"、"))).add("productIdList",productIdList);
        }else {
            return MapMessage.successMessage();
        }
    }

}
