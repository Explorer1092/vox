/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.washington.controller.open.v2.order;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.DigestSignUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.api.constant.VendorAppsOrderPayType;
import com.voxlearning.utopia.service.order.api.constants.OrderStatus;
import com.voxlearning.utopia.service.order.api.constants.OrderType;
import com.voxlearning.utopia.service.order.api.constants.PaymentStatus;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.api.entity.OrderProductItem;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.order.client.AsyncOrderCacheServiceClient;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.order.consumer.UserOrderServiceClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.washington.controller.open.AbstractApiController;
import com.voxlearning.washington.support.SessionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * Created by Summer on 2016/12/9.
 */
@Controller
@Slf4j
@RequestMapping(value = "/v2/order/")
public class OrderApiV2Controller extends AbstractApiController {

    @Inject private AsyncOrderCacheServiceClient asyncOrderCacheServiceClient;

    @Inject private UserOrderLoaderClient userOrderLoaderClient;
    @Inject private UserOrderServiceClient userOrderServiceClient;

    // 获取商品信息
    @RequestMapping(value = "/products.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage getProducts() {
        MapMessage resultMap = new MapMessage();

        try {
            validateRequest();
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        StudentDetail studentDetail = getApiRequestStudentDetail();
        VendorApps app = getApiRequestApp();
        List<OrderProduct> allProducts = userOrderLoaderClient.loadAllOrderProductsByModifyPrice(studentDetail);
        allProducts = allProducts.stream()
                .filter(p -> StringUtils.equals(app.getAppKey(), p.getProductType()))
                .collect(Collectors.toList());
        Set<String> productIds = allProducts.stream().map(OrderProduct::getId).collect(Collectors.toSet());
        Map<String, List<OrderProductItem>> itemMap = userOrderLoaderClient.loadProductItemsByProductIds(productIds);
        List<Map<String, Object>> productList = new ArrayList<>();
        for (OrderProduct product : allProducts) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put(RES_PRODUCT_ID, product.getId());
            item.put(RES_PRODUCT_NAME, product.getName());
            item.put(RES_PRODUCT_PRICE, product.getPrice());
            item.put(RES_PRODUCT_ORIGNAL_PRICE, product.getOriginalPrice());
            item.put(RES_PRODUCT_ITEMS, getShowItemMap(itemMap.get(product.getId())));
            productList.add(item);
        }
        // generate response information
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        resultMap.add(RES_PRODUCT_LIST, productList);
        return resultMap;
    }

    // 组装返回的item列表数据
    private List<Map<String, Object>> getShowItemMap(List<OrderProductItem> orderProductItems) {
        if (CollectionUtils.isEmpty(orderProductItems)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> dataList = new ArrayList<>();
        for (OrderProductItem item : orderProductItems) {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("id", item.getId());
            itemMap.put("name", item.getName());
            itemMap.put("desc", item.getDesc());
            itemMap.put("price", item.getOriginalPrice());
            itemMap.put("appId", item.getAppItemId());
            itemMap.put("period", item.getPeriod());
            dataList.add(itemMap);
        }
        return dataList;
    }

    // 注册订单 （生成订单）
    @RequestMapping(value = "/register.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage registerOrder() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_PRODUCT_ID, "商品ID");
            validateRequired(REQ_PRODUCT_NAME, "商品名");
            validateNumber(REQ_HWCOINT, "消费作业币金额");
            validateNumber(REQ_BANK, "消费金额");
            validateRequiredNumber(REQ_ORDER_SEQ, "订单序号");

            if (StringUtils.isNotEmpty(getRequestString(REQ_HWCOINT))) {
                validateRequest(REQ_PRODUCT_ID, REQ_PRODUCT_NAME, REQ_HWCOINT, REQ_ORDER_SEQ);
            } else if (StringUtils.isNotEmpty(getRequestString(REQ_BANK))) {
                validateRequest(REQ_PRODUCT_ID, REQ_PRODUCT_NAME, REQ_BANK, REQ_ORDER_SEQ);
            } else {
                throw new IllegalArgumentException(MessageFormat.format(VALIDATE_ERROR_NUMBER_MSG, "消费金额"));
            }
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        // Save Order Information
        User curUser = getApiRequestUser();
        VendorApps app = getApiRequestApp();
        Long orderSeq = getRequestLong(REQ_ORDER_SEQ);
        // 检查订单是否存在
        List<UserOrder> userOrderList = userOrderLoaderClient.loadUserOrderList(curUser.getId());
        if (CollectionUtils.isNotEmpty(userOrderList)) {
            UserOrder order = userOrderList.stream()
                    .filter(o -> Objects.equals(o.getOrderSeq(), orderSeq))
                    .filter(o -> o.getOrderProductServiceType() != null && Objects.equals(o.getOrderProductServiceType(), app.getAppKey()))
                    .filter(o -> o.getOrderStatus() == OrderStatus.New)
                    .filter(o -> o.getPaymentStatus() == PaymentStatus.Unpaid)
                    .findAny().orElse(null);
            if (order != null) {
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
                resultMap.add(RES_ORDER_ID, order.genUserOrderId());
                resultMap.add(RES_ORDER_TOKEN, order.getOrderToken());
                return resultMap;
            }
        }

        // 检查产品是否存在
        OrderProduct product = userOrderLoaderClient.loadOrderProductById(getRequestString(REQ_PRODUCT_ID));
        // 如果product是null，表示是道具产品，在我们的product中不存在，如果product不是null，检查是不是online的产品
        if (product == null || !product.isOnline()) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, "该VIP周期已经暂停开通，您可以选择其他时长的周期");
            return resultMap;
        }

        //是否可以购买检查
        MapMessage validMessage = validateStudentPay(curUser.getId(), product);
        if (!validMessage.isSuccess()) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, validMessage.getInfo());
            return resultMap;
        }


        String refer = getRequestString("refer");
        if (StringUtils.isBlank(refer)) {
            refer = asyncOrderCacheServiceClient.getAsyncOrderCacheService()
                    .UserOrderReferCacheManager_fetchOrderRefer(curUser.getId())
                    .take();
        }
        // 生成新的订单
        String orderToken = StringUtils.join(System.currentTimeMillis(), RandomUtils.randomNumeric(10));

        UserOrder userOrder = UserOrder.newOrder(OrderType.app, curUser.getId());
        userOrder.setOrderProductServiceType(app.getAppKey());
        userOrder.setUserId(curUser.getId());
        userOrder.setUserName(curUser.fetchRealname());
        userOrder.setOrderSeq(orderSeq);
        userOrder.setProductId(product.getId()); // 使用我们自己的ID
        userOrder.setProductName(product.getName());
        userOrder.setOrderToken(orderToken);
        userOrder.setOrderReferer(refer);
        if (StringUtils.isNotEmpty(getRequestString(REQ_HWCOINT))) {
            userOrder.setPayType(VendorAppsOrderPayType.HWCOIN.name());
            userOrder.setOrderPrice(new BigDecimal(getRequestDouble(REQ_HWCOINT)));
        } else {
            userOrder.setPayType(VendorAppsOrderPayType.BANK.name());
            userOrder.setOrderPrice(new BigDecimal(getRequestDouble(REQ_BANK)));
        }
        MapMessage message;
        try {
            message = userOrderServiceClient.saveUserOrder(userOrder);
        } catch (Exception ex) {
            logger.error("Failed to persist user order", ex);
            message = MapMessage.errorMessage();
        }
        if (!message.isSuccess()) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, message.getInfo());
            return resultMap;
        }
        // generate response information
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        resultMap.add(RES_ORDER_ID, userOrder.genUserOrderId());
        resultMap.add(RES_ORDER_TOKEN, orderToken);

        return resultMap;
    }

    @RequestMapping(value = "/status.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage getUserOrderStatus() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_ORDER_ID, "订单ID");
            validateRequest(REQ_ORDER_ID);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        String orderId = getRequestString(REQ_ORDER_ID);
        String sessionKey = getRequestString(REQ_SESSION_KEY);
        UserOrder userOrder = userOrderLoaderClient.loadUserOrder(orderId);
        if (userOrder == null) {
            resultMap.add(RES_RESULT, RES_RESULT_ORDER_UNKNOWN_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_ORDER_UNKNOW_MSG);
        } else {
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            Map<String, String> sigParams = new HashMap<>();
            sigParams.put(REQ_APP_KEY, userOrder.getOrderProductServiceType());
            sigParams.put(REQ_SESSION_KEY, sessionKey);
            resultMap.add(RES_ORDER_STATUS, userOrder.getPaymentStatus().name());
            resultMap.add(RES_PRODUCT_ID, userOrder.getProductId());
            resultMap.add(RES_PRODUCT_NAME, userOrder.getProductName());
            sigParams.put(RES_ORDER_STATUS, userOrder.getPaymentStatus().name());
            sigParams.put(RES_PRODUCT_ID, userOrder.getProductId());
            sigParams.put(RES_PRODUCT_NAME, userOrder.getProductName());
            if (userOrder.getPayType() != null && Objects.equals(userOrder.getPayType(), VendorAppsOrderPayType.HWCOIN.name())) {
                resultMap.add(RES_HWCOIN, userOrder.getOrderPrice());
                sigParams.put(RES_HWCOIN, String.valueOf(userOrder.getOrderPrice()));
            }
            if (userOrder.getPayType() != null && Objects.equals(userOrder.getPayType(), VendorAppsOrderPayType.BANK.name())) {
                resultMap.add(RES_BANK, userOrder.getOrderPrice());
                sigParams.put(RES_BANK, String.valueOf(userOrder.getOrderPrice()));
            }
            // 加入SIG防止DNS劫持
            resultMap.add(RES_SIG, DigestSignUtils.signMd5(sigParams, getApiRequestApp().getSecretKey()));
        }
        return resultMap;
    }

    @RequestMapping(value = "/query.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage queryUserPaidOrders() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequest();
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        Long userId = getApiRequestUser().getId();
        String appKey = getApiRequestApp().getAppKey();

        List<UserOrder> orders = userOrderLoaderClient.loadUserOrderList(userId);
        orders = orders.stream()
                .filter(o -> o.getOrderProductServiceType() != null && Objects.equals(o.getOrderProductServiceType(), appKey))
                .filter(o -> o.getPaymentStatus() != null && o.getPaymentStatus() == PaymentStatus.Paid)
                .collect(Collectors.toList());

        List<Map<String, Object>> userOrders = new ArrayList<>();
        for (UserOrder order : orders) {
            Map<String, Object> orderInfo = new LinkedHashMap<>();
            orderInfo.put(RES_ORDER_ID, order.getId());
            orderInfo.put(RES_PRODUCT_ID, order.getProductId());
            orderInfo.put(RES_PRODUCT_NAME, order.getProductName());
            orderInfo.put(RES_PAYMENT_AMOUNT, order.getOrderPrice());
            orderInfo.put(RES_PAYMENT_STATUS, order.getPaymentStatus().name());
            userOrders.add(orderInfo);
        }
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        resultMap.add(RES_USER_ORDERS, userOrders);
        return resultMap;
    }

}
