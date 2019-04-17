package com.voxlearning.utopia.service.piclisten.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.order.api.entity.*;
import com.voxlearning.utopia.service.order.api.util.AfentiOrderUtil;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.piclisten.api.PicListenBookOrderService;
import com.voxlearning.utopia.service.piclisten.impl.loader.TextBookManagementLoaderImpl;
import com.voxlearning.utopia.service.piclisten.impl.support.*;
import com.voxlearning.utopia.service.piclisten.support.OrderSynchronizer;
import com.voxlearning.utopia.service.vendor.api.entity.TextBookManagement;
import com.voxlearning.utopia.service.vendor.api.mapper.OrderSynchronizeBookInfo;
import com.voxlearning.utopia.service.vendor.api.mapper.OrderSynchronizeContext;
import org.springframework.beans.factory.BeanFactoryUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.alps.repackaged.org.apache.commons.lang3.StringUtils.isBlank;

/**
 * @author xinxin
 * @since 10/17/17.
 */
@Named
@ExposeService(interfaceClass = PicListenBookOrderService.class)
public class PicListenBookOrderServiceImpl extends SpringContainerSupport implements PicListenBookOrderService {
    @Inject
    private UserOrderLoaderClient userOrderLoaderClient;
    @Inject
    private CommonConfigServiceClient commonConfigServiceClient;
    @Inject
    private VendorQueueService vendorService;

    @Inject
    private TextBookManagementLoaderImpl textBookManagementLoader;


    @Override
    public MapMessage synchronizePicListenOrder(UserOrder order) {
        if (order == null)
            return MapMessage.errorMessage("订单是 null");
        Map<String, OrderSynchronizer> synchronizerMap = BeanFactoryUtils.beansOfTypeIncludingAncestors(getApplicationContext(), OrderSynchronizer.class);
        if (MapUtils.isNotEmpty(synchronizerMap)) {
            fillBookInfoIfNecessary(order);
            for (OrderSynchronizer synchronizer : synchronizerMap.values()) {
                synchronizer.accept(order);
            }
        }
        return MapMessage.successMessage();
    }


    @Override
    public MapMessage notifyThirdPartyCancelOrder(String fixOrderId) {
        UserOrder order = userOrderLoaderClient.loadUserOrderIncludeCanceled(fixOrderId);
        if (null == order) {
            return MapMessage.errorMessage("未查询到订单");
        }

        if (OrderProductServiceType.safeParse(order.getOrderProductServiceType()) != OrderProductServiceType.PicListenBook && !AfentiOrderUtil.isAfentiImpovedOrder(OrderProductServiceType.safeParse(order.getOrderProductServiceType()))) {
            return MapMessage.errorMessage("不支持的订单类型");
        }
        fillBookInfoIfNecessary(order);

        String extAttributes = order.getExtAttributes();
        if (isBlank(extAttributes)) {
            return MapMessage.errorMessage("订单扩展信息不支持退款操作");
        }
        OrderSynchronizeContext context = JsonUtils.fromJson(extAttributes, OrderSynchronizeContext.class);
        if (null == context) {
            return MapMessage.errorMessage("订单同步信息缺失");
        }
        if (isPepOrder(context)) {
            if (isBlank(context.getPackageId())) {
                Map<String, Object> params = generatePepCancelOrderParams(order);
                if (MapUtils.isEmpty(params)) {
                    return MapMessage.errorMessage("订单同步信息缺失");
                }

                vendorService.sendHttpNotify(order.getOrderProductServiceType(), commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.getType(), OrderSynchronizer.CONFIG_ORDER_SYNCHRONIZE_REFUND_TARGET_PEP), params);
            } else {
                Map<String, Object> params = generatePepPackageCancelOrderParams(order);
                if (MapUtils.isEmpty(params)) {
                    return MapMessage.errorMessage("订单同步信息缺失");
                }

                vendorService.sendHttpNotify(order.getOrderProductServiceType(), commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.getType(), OrderSynchronizer.CONFIG_ORDER_SYNC_REFUND_PEP_PACKAGE), params);
            }
            return MapMessage.successMessage();
        } else if (isSephOrder(context)) {
            //加个沪教的订单退款处理
            Map<String, Object> params = generateSephCancelOrderParams(order);
            if (MapUtils.isEmpty(params)) {
                return MapMessage.errorMessage("订单同步信息缺失");
            }
            vendorService.sendHttpNotify(order.getOrderProductServiceType(), commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.getType(), OrderSynchronizer.CONFIG_ORDER_SYNCHRONIZE_REFUND_TARGET_SEPH), params);
            return MapMessage.successMessage();
        }

        return MapMessage.errorMessage("不支持退款同步的订单类型");
    }

    @Override
    public MapMessage notifyThirdPartyChangeOrderBook(String fixedOrderId, String newBookId) {
        UserOrder userOrder = userOrderLoaderClient.loadUserOrder(fixedOrderId);
        if (null == userOrder) {
            return MapMessage.errorMessage("未查询到订单信息");
        }
        if (OrderProductServiceType.safeParse(userOrder.getOrderProductServiceType()) != OrderProductServiceType.PicListenBook && !AfentiOrderUtil.isAfentiImpovedOrder(OrderProductServiceType.safeParse(userOrder.getOrderProductServiceType()))) {
            return MapMessage.errorMessage("订单类型不支持换购");
        }
        fillBookInfoIfNecessary(userOrder);

        String extAttributes = userOrder.getExtAttributes();
        if (isBlank(extAttributes)) {
            return MapMessage.errorMessage("订单扩展信息不支持换购物操作");
        }
        OrderSynchronizeContext context = JsonUtils.fromJson(extAttributes, OrderSynchronizeContext.class);
        if (null == context) {
            return MapMessage.errorMessage("订单同步信息缺失");
        }

        if (isPepOrder(context)) {
            Map<String, Object> params = generatePepChangeBookParameters(newBookId, userOrder);
            if (MapUtils.isEmpty(params)) {
                return MapMessage.errorMessage("同步信息缺失");
            }

            vendorService.sendHttpNotify(userOrder.getOrderProductServiceType(), commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.getType(), OrderSynchronizer.CONFIG_ORDER_CHANGE_BOOK_TARGET_PEP), params);

            return MapMessage.successMessage();
        } else if (isFltrpOrder(context)) {
            List<UserOrderPaymentHistory> userOrderPaymentHistories = userOrderLoaderClient.loadUserOrderPaymentHistoryList(userOrder.getUserId());
            if (CollectionUtils.isEmpty(userOrderPaymentHistories)) {
                return MapMessage.errorMessage("未找到支付记录");
            }
            UserOrderPaymentHistory userOrderPaymentHistory = userOrderPaymentHistories.stream().filter(his -> his.getOrderId().equals(userOrder.getId())).findFirst().orElse(null);
            if (null == userOrderPaymentHistory) {
                return MapMessage.errorMessage("未找到此订单的支付记录");
            }
            if (userOrderPaymentHistory.getPayDatetime().toInstant().isBefore(Instant.now().minusSeconds(72 * 60 * 60))) {
                return MapMessage.errorMessage("外研社订单只能在72小时之内换购，此订单已超72小时");
            }

            Map<String, Object> params = generateFltrpChangeBookParameters(newBookId, userOrder);
            if (MapUtils.isEmpty(params)) {
                return MapMessage.errorMessage("同步信息缺失");
            }

            vendorService.sendHttpNotify(userOrder.getOrderProductServiceType(), commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.getType(), OrderSynchronizer.CONFIG_ORDER_CHANGE_BOOK_TARGET_FLTRP), params);
            return MapMessage.successMessage();
        } else if (isSephOrder(context)) {
            Map<String, Object> params = generateSephChangeBookParams(newBookId, userOrder);
            if (MapUtils.isEmpty(params)) {
                return MapMessage.errorMessage("同步信息缺失");
            }

            vendorService.sendHttpNotify(userOrder.getOrderProductServiceType(), commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.getType(), OrderSynchronizer.CONFIG_ORDER_CHANGE_BOOK_TARGET_SEPH), params);
            return MapMessage.successMessage();
        }
        return MapMessage.errorMessage("不支持换购的出版社");
    }


    private void fillBookInfoIfNecessary(UserOrder order) {
        if (OrderProductServiceType.safeParse(order.getOrderProductServiceType()) != OrderProductServiceType.PicListenBook && !AfentiOrderUtil.isAfentiImpovedOrder(OrderProductServiceType.safeParse(order.getOrderProductServiceType()))) {
            return;
        }
        // 处理阿分题提高版订单
        if (AfentiOrderUtil.isAfentiImpovedOrder(OrderProductServiceType.safeParse(order.getOrderProductServiceType()))) {
            List<UserOrderProductRef> productRefs = userOrderLoaderClient.loadOrderProducts(order.getUserId(), order.getId()).stream()
                    .filter(e -> OrderProductServiceType.safeParse(e.getOrderProductServiceType()) == OrderProductServiceType.PicListenBook).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(productRefs)) {
                return;
            }

            UserOrderProductRef picOrderProductRef = productRefs.get(0);
            OrderProduct picProduct = userOrderLoaderClient.loadOrderProductById(picOrderProductRef.getProductId());
            if (picProduct == null) {
                return;
            }

            List<OrderProductItem> picProItems = userOrderLoaderClient.loadProductItemsByProductId(picProduct.getId())
                    .stream().filter(e -> OrderProductServiceType.safeParse(e.getProductType()) == OrderProductServiceType.PicListenBook).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(picProItems)) {
                return;
            }

            OrderSynchronizeContext ctx = new OrderSynchronizeContext();
            Map<String, Object> productAttr = JsonUtils.fromJson(picProduct.getAttributes());
            if (MapUtils.isNotEmpty(productAttr) && productAttr.containsKey(OrderSynchronizer.FIELD_PACKAGE_ID)) {
                ctx.setPackageId(productAttr.get(OrderSynchronizer.FIELD_PACKAGE_ID).toString());
            }
            for (OrderProductItem item : picProItems) {
                TextBookManagement.SdkInfo sdkInfo = textBookManagementLoader.picListenSdkInfo(item.getAppItemId());
                if (null == sdkInfo || isBlank(sdkInfo.getSdkBookIdV2())) {
                    continue;
                }
                ctx.addBook(sdkInfo.getSdkType().name(), sdkInfo.getSdkBookIdV2(), item.getPeriod(), BigDecimal.valueOf(picOrderProductRef.getProductPrice().longValue()).multiply(BigDecimal.valueOf(100)).intValue());
            }
            if (CollectionUtils.isNotEmpty(ctx.getBooks())) {
                order.setExtAttributes(JsonUtils.toJson(ctx));
            }
            return;
        }

        OrderSynchronizeContext ctx = new OrderSynchronizeContext();
        OrderProduct orderProduct = userOrderLoaderClient.loadOrderProductById(order.getProductId());
        if (null == orderProduct) {
            return;
        }
        Map<String, Object> productAttr = JsonUtils.fromJson(orderProduct.getAttributes());
        if (MapUtils.isNotEmpty(productAttr) && productAttr.containsKey(OrderSynchronizer.FIELD_PACKAGE_ID)) {
            ctx.setPackageId(productAttr.get(OrderSynchronizer.FIELD_PACKAGE_ID).toString());
        }

        List<OrderProductItem> orderProductItems = userOrderLoaderClient.loadProductItemsByProductId(order.getProductId());
        if (CollectionUtils.isEmpty(orderProductItems)) {
            return;
        }
        for (OrderProductItem item : orderProductItems) {
            if (OrderProductServiceType.safeParse(item.getProductType()) != OrderProductServiceType.PicListenBook) {
                continue;
            }
            TextBookManagement.SdkInfo sdkInfo = textBookManagementLoader.picListenSdkInfo(item.getAppItemId());
            if (null == sdkInfo || isBlank(sdkInfo.getSdkBookIdV2())) {
                continue;
            }
            ctx.addBook(sdkInfo.getSdkType().name(), sdkInfo.getSdkBookIdV2(), item.getPeriod(), BigDecimal.valueOf(item.getOriginalPrice().longValue()).multiply(BigDecimal.valueOf(100)).intValue());
        }
        if (CollectionUtils.isNotEmpty(ctx.getBooks())) {
            order.setExtAttributes(JsonUtils.toJson(ctx));
        }
    }

    private boolean isPepOrder(OrderSynchronizeContext context) {
        if (null == context || CollectionUtils.isEmpty(context.getBooks())) return false;

        for (OrderSynchronizeBookInfo info : context.getBooks()) {
            if (StringUtils.isNotBlank(info.getSource()) && info.getSource().equals(OrderSynchronizer.PUBLISHER_PEP)) {
                return true;
            }
        }
        return false;
    }

    private boolean isFltrpOrder(OrderSynchronizeContext context) {
        if (null == context || CollectionUtils.isEmpty(context.getBooks())) return false;

        for (OrderSynchronizeBookInfo info : context.getBooks()) {
            if (StringUtils.isNotBlank(info.getSource()) && info.getSource().equals(OrderSynchronizer.PUBLISHER_FLTRP)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSephOrder(OrderSynchronizeContext context) {
        if (null == context || CollectionUtils.isEmpty(context.getBooks())) return false;

        for (OrderSynchronizeBookInfo info : context.getBooks()) {
            if (StringUtils.isNotBlank(info.getSource()) && info.getSource().equals(OrderSynchronizer.PUBLISHER_SEPH)) {
                return true;
            }
        }
        return false;
    }

    private Map<String, Object> generateFltrpChangeBookParameters(String newBookId, UserOrder userOrder) {
        Map<String, String> params = new HashMap<>();
        params.put(OrderSynchronizer.FIELD_APP_ID, ProductConfig.get(OrderSynchronizer.CONFIG_PICLISTEN_WAIYANSHE_APPID)); //外研社是区分ios和andriod两个不同的appid的，但是订单只能同步一次，所以跟外研社约定了只传andriod的appid
        params.put(OrderSynchronizer.FIELD_NEW_BOOK_CODE, newBookId);
        params.put(OrderSynchronizer.FIELD_ORDER_NO, userOrder.getId());
        params.put(OrderSynchronizer.FIELD_MOBILE, SafeConverter.toString(getRealNotifyUserId(userOrder)));
        String sign = FltrpSignatureGenerator.sign(params, ProductConfig.get(OrderSynchronizer.CONFIG_PICLISTEN_WAIYANSHE_SECRET));
        params.put(OrderSynchronizer.FIELD_SIGN, sign);

        Map<String, Object> map = new HashMap<>();
        map.putAll(params);
        map.put("is_piclisten", true);
        return map;
    }

    private Long getRealNotifyUserId(UserOrder order) {
        List<UserOrderProductRef> orderProducts = userOrderLoaderClient.loadOrderProducts(order.getUserId(), order.getId());
        if (CollectionUtils.isNotEmpty(orderProducts)) {
            UserOrderProductRef orderProduct = orderProducts.stream().filter(p -> OrderProductServiceType.safeParse(p.getOrderProductServiceType()) == OrderProductServiceType.PicListenBook).findFirst().orElse(null);
            if (orderProduct != null) {
                return orderProduct.getRelatedUserId();
            }
        }

        return order.getUserId();
    }

    private Map<String, Object> generatePepChangeBookParameters(String newBookId, UserOrder order) {
        OrderSynchronizeContext context = JsonUtils.fromJson(order.getExtAttributes(), OrderSynchronizeContext.class);
        if (null == context) {
            return null;
        }

        PepOrderInfo orderInfo = new PepOrderInfo();
        orderInfo.setPlatform_key(ProductConfig.get(OrderSynchronizer.CONFIG_PICLISTEN_RENJIAO_APPID));
        orderInfo.setUser_id(SafeConverter.toString(getRealNotifyUserId(order)));

        List<PepOrderInfo.Info> infos = new ArrayList<>();
        for (OrderSynchronizeBookInfo bookInfo : context.getBooks()) {
            if (!bookInfo.getSource().equals(OrderSynchronizer.PUBLISHER_PEP) || isBlank(bookInfo.getBookId()))
                continue;

            PepOrderInfo.ChangeBookInfo info = orderInfo.new ChangeBookInfo();
            info.setBook_id(bookInfo.getBookId());
            info.setNew_book_id(newBookId);
            info.setPay_time(order.getUpdateDatetime().toInstant().toEpochMilli() / 1000);
            info.setPay_tradeno(order.getId());
            info.setReal_price(bookInfo.getPrice());
            infos.add(info);
        }
        orderInfo.setOrder_info(infos);
        orderInfo.setIs_piclisten(true);

        try {
            String sign = PepSignatureGenerator.getSignature(URLEncoder.encode("platform_key=" + orderInfo.getPlatform_key() + "&user_id=" + orderInfo.getUser_id(), "UTF-8"), ProductConfig.get(OrderSynchronizer.CONFIG_PICLISTEN_RENJIAO_SECRET));
            orderInfo.setSign(sign);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        return JsonUtils.safeConvertObjectToMap(orderInfo);
    }

    private Map<String, Object> generatePepPackageCancelOrderParams(UserOrder order) {
        OrderSynchronizeContext context = JsonUtils.fromJson(order.getExtAttributes(), OrderSynchronizeContext.class);
        if (null == context || CollectionUtils.isEmpty(context.getBooks())) {
            return null;
        }

        PepOrderInfo orderInfo = new PepOrderInfo();
        orderInfo.setPlatform_key(ProductConfig.get(OrderSynchronizer.CONFIG_PICLISTEN_RENJIAO_APPID));
        orderInfo.setUser_id(SafeConverter.toString(getRealNotifyUserId(order)));

        List<PepOrderInfo.Info> infos = new ArrayList<>();
        PepOrderInfo.Info info = orderInfo.new Info();
        info.setPay_tradeno(order.getId());
        infos.add(info);
        orderInfo.setOrder_info(infos);
        try {
            String sign = PepSignatureGenerator.getSignature(URLEncoder.encode("platform_key=" + orderInfo.getPlatform_key() + "&user_id=" + orderInfo.getUser_id(), "UTF-8"), ProductConfig.get(OrderSynchronizer.CONFIG_PICLISTEN_RENJIAO_SECRET));
            orderInfo.setSign(sign);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return JsonUtils.safeConvertObjectToMap(orderInfo);
    }

    private Map<String, Object> generatePepCancelOrderParams(UserOrder order) {
        OrderSynchronizeContext context = JsonUtils.fromJson(order.getExtAttributes(), OrderSynchronizeContext.class);
        if (null == context || CollectionUtils.isEmpty(context.getBooks())) {
            return null;
        }

        PepOrderInfo orderInfo = new PepOrderInfo();
        orderInfo.setPlatform_key(ProductConfig.get(OrderSynchronizer.CONFIG_PICLISTEN_RENJIAO_APPID));
        orderInfo.setUser_id(SafeConverter.toString(getRealNotifyUserId(order)));

        List<PepOrderInfo.Info> infos = new ArrayList<>();
        for (OrderSynchronizeBookInfo bookInfo : context.getBooks()) {
            //非人教子产品忽略
            if (isBlank(bookInfo.getSource()) || !bookInfo.getSource().equals(OrderSynchronizer.PUBLISHER_PEP)) {
                continue;
            }

            PepOrderInfo.Info info = orderInfo.new Info();
            info.setBook_id(bookInfo.getBookId());
            info.setPay_time(order.getUpdateDatetime().toInstant().toEpochMilli() / 1000);
            info.setPay_tradeno(order.getId());
            info.setReal_price(bookInfo.getPrice());
            infos.add(info);
        }
        orderInfo.setOrder_info(infos);
        try {
            String sign = PepSignatureGenerator.getSignature(URLEncoder.encode("platform_key=" + orderInfo.getPlatform_key() + "&user_id=" + orderInfo.getUser_id(), "UTF-8"), ProductConfig.get(OrderSynchronizer.CONFIG_PICLISTEN_RENJIAO_SECRET));
            orderInfo.setSign(sign);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return JsonUtils.safeConvertObjectToMap(orderInfo);
    }

    private Map<String, Object> generateSephChangeBookParams(String newBookId, UserOrder order) {
        OrderSynchronizeContext context = JsonUtils.fromJson(order.getExtAttributes(), OrderSynchronizeContext.class);
        if (null == context) {
            return null;
        }
        Map<String, Object> returnMap = new HashMap<>();
        SephOrderInfo sephOrderInfo = new SephOrderInfo();
        sephOrderInfo.setUserId(SafeConverter.toString(order.getUserId()));
        sephOrderInfo.setRTime(DateUtils.dateToString(new Date()));
        sephOrderInfo.setTextBookId(context.getBooks().get(0).getBookId());
        SephOrderInfo.ChangeBookInfo changeBookInfo = sephOrderInfo.new ChangeBookInfo();
        changeBookInfo.setNewCourseId(newBookId);

        Map<String, Object> changeBookMap = changeBookInfo.toMap();
        String changeBookJson = JsonUtils.toJson(changeBookMap);
        String sephSecret = SephOrderSynchronizer.generateSephSecret(changeBookJson);
        returnMap.put("Info", sephSecret);
        return returnMap;
    }

    private Map<String, Object> generateSephCancelOrderParams(UserOrder userOrder) {
        OrderSynchronizeContext context = JsonUtils.fromJson(userOrder.getExtAttributes(), OrderSynchronizeContext.class);
        if (null == context || CollectionUtils.isEmpty(context.getBooks())) {
            return null;
        }
        Map<String, Object> returnMap = new HashMap<>();
        String sephSecret = "";
        for (OrderSynchronizeBookInfo bookInfo : context.getBooks()) {
            SephOrderInfo sephOrderInfo = new SephOrderInfo();
            sephOrderInfo.setUserId(SafeConverter.toString(userOrder.getUserId()));
            sephOrderInfo.setRTime(DateUtils.dateToString(new Date()));
            sephOrderInfo.setTextBookId(bookInfo.getBookId());
            Map<String, Object> cancelMap = sephOrderInfo.toCancelMap();
            String cancelJson = JsonUtils.toJson(cancelMap);
            sephSecret = SephOrderSynchronizer.generateSephSecret(cancelJson);
        }
        returnMap.put("Info", sephSecret);

        return returnMap;
    }
}
