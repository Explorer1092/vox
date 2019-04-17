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

package com.voxlearning.washington.controller.open.v1;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.DigestSignUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.api.constant.VendorAppsOrderPayType;
import com.voxlearning.utopia.service.order.api.constants.OrderStatus;
import com.voxlearning.utopia.service.order.api.constants.OrderType;
import com.voxlearning.utopia.service.order.api.constants.PaymentStatus;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.api.entity.OrderProductItem;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.order.client.AsyncOrderCacheServiceClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.washington.controller.open.AbstractApiController;
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

import static com.voxlearning.utopia.api.constant.OrderProductServiceType.*;
import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * Order related API controller class.
 *
 * @author Zhilong Hu
 * @since 2014-06-9
 */
@Controller
@RequestMapping(value = "/v1/order")
@Slf4j
public class OrderApiController extends AbstractApiController {

    @Inject private RaikouSystem raikouSystem;

    @Inject private AsyncOrderCacheServiceClient asyncOrderCacheServiceClient;

    @RequestMapping(value = "/register.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage registerOrder() {
        MapMessage resultMap = new MapMessage();

        try {
            validateRequiredNumber(REQ_PRODUCT_ID, "商品ID");
            validateRequired(REQ_PRODUCT_NAME, "商品名");
            validateDigitNumber(REQ_INTEGRAL, "消费学豆数");
            validateNumber(REQ_HWCOINT, "消费作业币金额");
            validateNumber(REQ_BANK, "消费金额");
            validateRequiredAny(REQ_HWCOINT, REQ_BANK, "作业币金额或者实际金额");
            validateRequiredNumber(REQ_ORDER_SEQ, "订单序号");

            if (StringUtils.isNotEmpty(getRequestString(REQ_HWCOINT))) {
                validateRequest(REQ_PRODUCT_ID, REQ_PRODUCT_NAME, REQ_HWCOINT, REQ_ORDER_SEQ);
            } else if (StringUtils.isNotEmpty(getRequestString(REQ_INTEGRAL))) {
                validateRequest(REQ_PRODUCT_ID, REQ_PRODUCT_NAME, REQ_INTEGRAL, REQ_ORDER_SEQ);
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
        if (StringUtils.equals(app.getAppKey(), SanguoDmz.name())
                || StringUtils.equals(app.getAppKey(), A17ZYSPG.name())
                || StringUtils.equals(app.getAppKey(), TravelAmerica.name())
                || StringUtils.equals(app.getAppKey(), PetsWar.name())) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, "即日起，本产品的开通功能将永久性关闭。在包月有效期限内，您仍可继续使用。");
            return resultMap;
        }

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
        OrderProduct product = getOrderProduct(app.getAppKey(), getRequestString(REQ_PRODUCT_ID));
        // 如果product是null，表示是道具产品，在我们的product中不存在，如果product不是null，检查是不是online的产品
        if (product == null || !product.isOnline()) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, "该VIP周期已经暂停开通，您可以选择其他时长的周期");
            return resultMap;
        }

        Long pid = getRequestLong(REQ_PARENT_ID);
        User parent = raikouSystem.loadUser(pid);
        if(Objects.nonNull(parent) && parent.isParent()){
            if(userBlacklistServiceClient.isInUserBlackList(curUser)){
                return MapMessage.errorMessage("该产品无法购买");
            }
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

        UserOrder newOrder = UserOrder.newOrder(OrderType.app, curUser.getId());
        newOrder.setOrderProductServiceType(OrderProductServiceType.valueOf(app.getAppKey()) == null ? null : OrderProductServiceType.valueOf(app.getAppKey()).name());
        newOrder.setUserId(curUser.getId());
        newOrder.setUserName(curUser.fetchRealname());
        newOrder.setOrderSeq(orderSeq);
        newOrder.setProductId(product.getId());
        newOrder.setProductName(product.getName());
        newOrder.setOrderToken(orderToken);
        newOrder.setOrderReferer(refer);

        if (StringUtils.isNotEmpty(getRequestString(REQ_HWCOINT))) {
            newOrder.setPayType(VendorAppsOrderPayType.HWCOIN.name());
            newOrder.setOrderPrice(new BigDecimal(getRequestDouble(REQ_HWCOINT)));
        } else {
            newOrder.setPayType(VendorAppsOrderPayType.BANK.name());
            newOrder.setOrderPrice(new BigDecimal(getRequestDouble(REQ_BANK)));
        }
        MapMessage message;
        try {
            message = userOrderServiceClient.saveUserOrder(newOrder);
        } catch (Exception ex) {
            logger.error("Failed to persist app user order", ex);
            message = MapMessage.errorMessage();
        }
        if (!message.isSuccess()) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, message.getInfo());
            return resultMap;
        }
        // generate response information
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        resultMap.add(RES_ORDER_ID, newOrder.genUserOrderId());
        resultMap.add(RES_ORDER_TOKEN, orderToken);

        return resultMap;
    }

    private OrderProduct getOrderProduct(String appKey, String productId) {
        List<OrderProduct> allProducts = userOrderLoaderClient.loadAvailableProduct();
        OrderProduct product = allProducts.stream()
                .filter(p -> StringUtils.equals(p.getProductType(), appKey))
                .filter(p -> Objects.equals(p.getId(), productId))
                .findFirst()
                .orElse(null);

        if (product != null) {
            return product;
        }

        // 兼容老版本，走美等传过来的是AppItemId,需要根据AppItemId去改
        Set<String> productIds = allProducts.stream()
                .filter(p -> StringUtils.equals(p.getProductType(), appKey))
                .map(OrderProduct::getId).collect(Collectors.toSet());
        Map<String, List<OrderProductItem>> itemList = userOrderLoaderClient.loadProductItemsByProductIds(productIds);
        for (String productKey : productIds) {
            List<OrderProductItem> items = itemList.get(productKey);
            if (items == null) continue;

            for (OrderProductItem item : items) {
                if (Objects.equals(productId, item.getAppItemId())) {
                    return allProducts.stream()
                            .filter(p -> StringUtils.equals(p.getProductType(), appKey))
                            .filter(p -> Objects.equals(p.getId(), productKey))
                            .findFirst()
                            .orElse(null);
                }
            }
        }

        return null;
    }


    // 据阿包说此接口是第三方在接收回调失败后， 主动发起的验证接口
    @RequestMapping(value = "/status.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage getAppsOrderStatus() {
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
        UserOrder order = userOrderLoaderClient.loadUserOrder(orderId);
        if (order == null) {
            resultMap.add(RES_RESULT, RES_RESULT_ORDER_UNKNOWN_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_ORDER_UNKNOW_MSG);
        } else {
            // 获取第三方产品ID
            String productId = order.getProductId();
            List<OrderProductItem> items = userOrderLoaderClient.loadProductItemsByProductId(productId);
            if (CollectionUtils.isNotEmpty(items) && StringUtils.isNoneBlank(items.get(0).getAppItemId())) {
                productId = items.get(0).getAppItemId();
            }
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            Map<String, String> sigParams = new HashMap<>();
            sigParams.put(REQ_APP_KEY, order.getOrderProductServiceType());
            sigParams.put(REQ_SESSION_KEY, sessionKey);
            resultMap.add(RES_ORDER_STATUS, order.getPaymentStatus().name());
            resultMap.add(RES_PRODUCT_ID, productId);
            resultMap.add(RES_PRODUCT_NAME, order.getProductName());
            sigParams.put(RES_ORDER_STATUS, order.getPaymentStatus().name());
            sigParams.put(RES_PRODUCT_ID, productId);
            sigParams.put(RES_PRODUCT_NAME, order.getProductName());
            if (order.getPayType() != null && Objects.equals(order.getPayType(), VendorAppsOrderPayType.HWCOIN.name())) {
                resultMap.add(RES_HWCOIN, order.getOrderPrice());
                sigParams.put(RES_HWCOIN, String.valueOf(order.getOrderPrice()));
            }
            if (order.getPayType() != null && Objects.equals(order.getPayType(), VendorAppsOrderPayType.BANK.name())) {
                resultMap.add(RES_BANK, order.getOrderPrice());
                sigParams.put(RES_BANK, String.valueOf(order.getOrderPrice()));
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
        orders = orders.stream().filter(o -> o.getOrderProductServiceType() != null && Objects.equals(o.getOrderProductServiceType(), appKey))
                .filter(o -> o.getPaymentStatus() != null && o.getPaymentStatus() == PaymentStatus.Paid)
                .collect(Collectors.toList());

        List<Map<String, Object>> userOrders = new ArrayList<>();
        for (UserOrder order : orders) {
            Map<String, Object> orderInfo = new LinkedHashMap<>();
            orderInfo.put(RES_ORDER_ID, order.genUserOrderId());
            orderInfo.put(RES_PRODUCT_ID, order.getProductId());
            orderInfo.put(RES_PRODUCT_NAME, order.getProductName());
            orderInfo.put(RES_PAYMENT_AMOUNT, order.getOrderPrice());
            orderInfo.put(RES_PAYMENT_STATUS, StringUtils.lowerCase(order.getPaymentStatus().name()));
            userOrders.add(orderInfo);
        }
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        resultMap.add(RES_USER_ORDERS, userOrders);

        return resultMap;
    }

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

        VendorApps app = getApiRequestApp();
        OrderProductServiceType type = OrderProductServiceType.safeParse(app.getAppKey());

        List<OrderProduct> allProducts = userOrderLoaderClient.loadAvailableProduct();
        allProducts = allProducts.stream()
                .filter(p -> StringUtils.equals(app.getAppKey(), p.getProductType()))
                // FIXME: 2017/1/9 暂时用价格排序吧。 以前是根据有效期
                .sorted((o1, o2) -> Double.compare(o1.getPrice().doubleValue(), o2.getPrice().doubleValue()))
                .collect(Collectors.toList());

        // 获取商品tips
        Map<String, List<String>> product_tips_map = fetchProductTips(type);

        List<Map<String, Object>> productList = new ArrayList<>();
        List<String> productIds = allProducts.stream().map(OrderProduct::getId).collect(Collectors.toList());
        Map<String, List<OrderProductItem>> itemMap = userOrderLoaderClient.loadProductItemsByProductIds(productIds);
        for (OrderProduct product : allProducts) {
            Map<String, Object> item = new LinkedHashMap<>();

            // product id
            String productId = product.getId();
            List<OrderProductItem> items = itemMap.get(productId);
            if (CollectionUtils.isNotEmpty(items) && StringUtils.isNoneBlank(items.get(0).getAppItemId())) {
                productId = items.get(0).getAppItemId();
            }

            item.put(RES_PRODUCT_ID, productId);
            item.put(RES_PRODUCT_NAME, product.getName());
            item.put(RES_PRODUCT_PRICE, product.getPrice().floatValue());
            item.put(RES_PRODUCT_ORIGNAL_PRICE, product.getOriginalPrice().floatValue());
            item.put(RES_PRODUCT_ATTRS, product.getAttributes());
            // 非捆绑模式给period
            if (CollectionUtils.isNotEmpty(items)) {
                item.put(RES_PRODUCT_PERIOD, items.get(0).getPeriod());
                List<String> tips = new ArrayList<>();
                String key = type.name() + "-" + items.get(0).getPeriod();
                if (product_tips_map.containsKey(key)) tips.addAll(product_tips_map.get(key));
                item.put(RES_TIPS, tips);
                item.put(RES_SALES_TYPE, items.get(0).getSalesType().name());
            }

            productList.add(item);
        }

        // generate response information
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        resultMap.add(RES_PRODUCT_LIST, productList);

        return resultMap;
    }
}
