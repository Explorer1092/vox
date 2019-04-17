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

package com.voxlearning.utopia.admin.service.legacy;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.DigestSignUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.exception.UtopiaRuntimeException;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.admin.service.AbstractAdminService;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.entity.afenti.AfentiOrder;
import com.voxlearning.utopia.service.finance.client.FinanceServiceClient;
import com.voxlearning.utopia.service.order.api.constants.OrderActiveType;
import com.voxlearning.utopia.service.order.api.constants.OrderProductSalesType;
import com.voxlearning.utopia.service.order.api.constants.PaymentStatus;
import com.voxlearning.utopia.service.order.api.entity.*;
import com.voxlearning.utopia.service.order.client.AfentiOrderServiceClient;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.order.consumer.UserOrderServiceClient;
import com.voxlearning.utopia.service.user.api.constants.FinanceFlowRefer;
import com.voxlearning.utopia.service.user.api.service.financial.FinanceFlow;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.utopia.service.wonderland.api.entity.WonderlandTimesCard;
import com.voxlearning.utopia.service.wonderland.api.entity.growingworld.UserProp;
import com.voxlearning.utopia.service.wonderland.client.GrowingWorldLoaderClient;
import com.voxlearning.utopia.service.wonderland.client.GrowingWorldServiceClient;
import com.voxlearning.utopia.service.wonderland.client.WonderlandLoaderClient;
import com.voxlearning.utopia.service.wonderland.client.WonderlandServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * @author xin.xin
 * @since 2014-1-17
 */
@Named
public class AfentiAdminService extends AbstractAdminService {

    @Inject private FinanceServiceClient financeServiceClient;
    @Inject private UserOrderLoaderClient userOrderLoaderClient;
    @Inject private UserOrderServiceClient userOrderServiceClient;
    @Inject private WonderlandLoaderClient wonderlandLoaderClient;
    @Inject private WonderlandServiceClient wonderlandServiceClient;
    @Inject private AfentiOrderServiceClient afentiOrderServiceClient;
    @Inject private GrowingWorldServiceClient growingWorldServiceClient;
    @Inject private GrowingWorldLoaderClient growingWorldLoaderClient;

    /**
     * 激活历史延期
     *
     * @param activateId
     * @param delayDays
     */
    public UserActivatedProduct delayActivationHistory(final String activateId, final int delayDays) {
        UserActivatedProduct product = userOrderLoaderClient.loadUserActivatedProductById(activateId);
        if (product == null || product.getServiceEndTime() == null || product.getServiceStartTime() == null) {
            throw new UtopiaRuntimeException("激活历史不支持延期");
        }
        if (product.getServiceEndTime().compareTo(new Date()) <= 0) {
            throw new UtopiaRuntimeException("激活记录已过期，不能延期！");
        }
        if (delayDays <= -365 || delayDays > 365) {
            throw new UtopiaRuntimeException("延期天数必须是-365~365天");
        }
        Date delayEndTime = DateUtils.nextDay(product.getServiceEndTime(), delayDays);
        if (delayEndTime.compareTo(product.getServiceStartTime()) <= 0) {
            throw new UtopiaRuntimeException("延期后时间不能小于服务开始时间！");
        }
        product.setServiceEndTime(delayEndTime);
        product.setUpdateDatetime(new Date());
        userOrderServiceClient.updateUserActivatedProduct(product);
        return product;
    }

    // 计算一个订单是否是最后一个订单
    public MapMessage isLatestOrder(UserOrder order) {
        // 打包产品不限制
        if (order.getOrderProductServiceType() != null && OrderProductServiceType.safeParse(order.getOrderProductServiceType()) == OrderProductServiceType.GroupProduct) {
            return MapMessage.successMessage();
        }
        // 按ITEM激活的产品从数量倒叙限制
        if (order.getOrderProductServiceType() != null && (OrderProductServiceType.safeParse(order.getOrderProductServiceType()) == OrderProductServiceType.ValueAddedLiveTimesCard
                || OrderProductServiceType.safeParse(order.getOrderProductServiceType()) == OrderProductServiceType.EnglishStoryBook
                || OrderProductServiceType.safeParse(order.getOrderProductServiceType()) == OrderProductServiceType.GrowingWorldProp)) {
            // 道具类限制
            List<UserOrder> paidList = userOrderLoaderClient.loadUserPaidOrders(order.getOrderProductServiceType(), order.getUserId())
                    .stream()
                    .sorted((o1, o2) -> {
                        OrderProductItem item1 = userOrderLoaderClient.loadProductItemsByProductId(o1.getProductId()).stream().findFirst().orElse(null);
                        OrderProductItem item2 = userOrderLoaderClient.loadProductItemsByProductId(o2.getProductId()).stream().findFirst().orElse(null);
                        int count1 = item1 == null ? 999 : item1.getPeriod();
                        int count2 = item2 == null ? 999 : item2.getPeriod();
                        if (count1 == count2) {
                            return o1.getCreateDatetime().compareTo(o2.getCreateDatetime());
                        }
                        return Integer.compare(count1, count2);
                    })
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(paidList) || StringUtils.equals(order.getId(), paidList.get(0).getId())) {
                return MapMessage.successMessage();
            } else {
                return MapMessage.errorMessage("消耗品需要从最低价格订单开始退");
            }
        } else {
            OrderProduct product = userOrderLoaderClient.loadOrderProductById(order.getProductId());
            if (product == null) {
                return MapMessage.successMessage();
            }
            List<OrderProductItem> itemList = userOrderLoaderClient.loadProductItemsByProductId(product.getId());
            if (CollectionUtils.isEmpty(itemList)) {
                return MapMessage.successMessage();
            }
            OrderProductItem item = itemList.stream().filter(i -> i.getActiveType() == OrderActiveType.ITEM)
                    .findFirst().orElse(null);
            if (item != null) {
                return MapMessage.successMessage();
            }
            // 找到最后的一个，看看是否是当前订单 （过滤道具类订单）
            List<UserOrder> paidList = userOrderLoaderClient.loadUserPaidOrders(order.getOrderProductServiceType(), order.getUserId())
                    .stream()
                    .sorted((o1, o2) -> Long.compare(o2.getUpdateDatetime().getTime(), o1.getUpdateDatetime().getTime()))
                    .collect(Collectors.toList());
            UserOrder latest = null;
            for (UserOrder o : paidList) {
                OrderProduct p = userOrderLoaderClient.loadOrderProductById(o.getProductId());
                if (p == null || p.getSalesType() == OrderProductSalesType.ITEM_BASED) {
                    continue;
                }
                latest = o;
                break;
            }
            if (latest == null || StringUtils.equals(order.getId(), latest.getId())) {
                return MapMessage.successMessage();
            } else {
                return MapMessage.errorMessage("请从最后一个订单开始退款");
            }
        }

    }

    /**
     * 计算订单可退最大金额
     *
     * @param order
     * @return Double
     */


    private double growingWorldPropCal(UserOrderPaymentHistory history, UserOrder order) {
        List<OrderProductItem> items = userOrderLoaderClient.loadProductItemsByProductId(order.getProductId());
        if (CollectionUtils.isEmpty(items)) {
            return 0;
        }
        OrderProductItem item = items.get(0);
        UserProp prop = growingWorldLoaderClient.getRemoteReference().loadUserPropInfoByPropId(order.getUserId(), item.getAppItemId());
        if (prop == null) {
            return 0;
        }
        // 总剩余次数
        int card = prop.getQuantity();
        if (card <= 0) {
            return 0d;
        }
        // 剩余次数大于订单次数 全退
        if (item.getPeriod() <= card) {
            return history.getPayAmount().doubleValue();
        }
        return history.getPayAmount()
                .multiply(new BigDecimal(card).divide(new BigDecimal(item.getPeriod()), 4, BigDecimal.ROUND_HALF_UP))
                .setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    private double englishStoryBookCal(UserOrderPaymentHistory history, UserOrder order) {
        // 获取剩余次数
        Map<String, Object> notifyMap = new HashMap<>();
        notifyMap.put("user_id", order.getUserId());
        String sig = getEnglishStoryBookSign(order.getUserId(), null);
        notifyMap.put("sign", sig);
        String url = "http://storybook.test.17zuoye.net/info/cards";
        if (RuntimeMode.isStaging()) {
            url = "http://storybook.staging.17zuoye.net/info/cards";
        } else if (RuntimeMode.isProduction()) {
            url = "http://storybook.17zuoye.com/info/cards";
        }
        String URL = UrlUtils.buildUrlQuery(url, notifyMap);
        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().get(URL).execute();
        if (response.hasHttpClientException()) {
            logger.error("Crm user order cal EnglishStoryBook cards ex.user {}", order.getUserId());
            return 0;
        } else {
            Map<String, Object> result = JsonUtils.convertJsonObjectToMap(response.getResponseString());
            if (!SafeConverter.toBoolean(result.get("success"))) {
                logger.error("Crm user order cal EnglishStoryBook cards return error, user {}, response {}", order.getUserId(), response.getResponseString());
                return 0;
            } else {
                int card = SafeConverter.toInt(result.get("cards"));
                // 总剩余次数
                if (card <= 0) {
                    return 0d;
                }
                List<OrderProductItem> items = userOrderLoaderClient.loadProductItemsByProductId(order.getProductId());
                if (CollectionUtils.isEmpty(items)) {
                    return 0d;
                }
                OrderProductItem item = items.get(0);
                // 剩余次数大于订单次数 全退
                if (item.getPeriod() <= card) {
                    return history.getPayAmount().doubleValue();
                }
                return history.getPayAmount()
                        .multiply(new BigDecimal(card).divide(new BigDecimal(item.getPeriod()), 4, BigDecimal.ROUND_HALF_UP))
                        .setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            }
        }
    }

    private String getEnglishStoryBookSign(Long userId, Integer num) {
        Map<String, String> sigParams = new HashMap<>();
        sigParams.put("user_id", userId.toString());
        sigParams.put("appKey", OrderProductServiceType.EnglishStoryBook.name());
        if (num != null && num > 0) {
            sigParams.put("num", num.toString());
        }
        VendorApps apps = vendorLoaderClient.loadVendor(OrderProductServiceType.EnglishStoryBook.name());
        return DigestSignUtils.signMd5(sigParams, apps.getSecretKey());
    }

    private double valueAddedLiveTimesCardCal(UserOrderPaymentHistory history, UserOrder order) {
        // 按次数计算
        WonderlandTimesCard card = wonderlandLoaderClient.getWonderlandLoader()
                .loadWonderlandTimesCard(order.getUserId(), OrderProductServiceType.ValueAddedLiveTimesCard.name())
                .take();
        // 总剩余次数
        if (card == null || card.getTimes() == null || card.getTimes() <= 0) {
            return 0d;
        }
        List<OrderProductItem> items = userOrderLoaderClient.loadProductItemsByProductId(order.getProductId());
        if (CollectionUtils.isEmpty(items)) {
            return 0d;
        }
        OrderProductItem item = items.get(0);
        // 剩余次数大于订单次数 全退
        if (item.getPeriod() <= card.getTimes()) {
            return history.getPayAmount().doubleValue();
        }
        return history.getPayAmount()
                .multiply(new BigDecimal(card.getTimes()).divide(new BigDecimal(item.getPeriod()), 4, BigDecimal.ROUND_HALF_UP))
                .setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    private double defaultCal(UserOrderPaymentHistory history, UserOrder order) {
        if (history.getServiceStartTime() == null || history.getServiceEndTime() == null || history.getPayAmount().compareTo(new BigDecimal(0)) <= 0) {
            return 0d;
        }
        // 如果已过期 只能全额退款
        if (history.getServiceEndTime().toInstant().isBefore(Instant.now())) {
            return history.getPayAmount().doubleValue();
        }
        // 如果服务期还没开始,直接可退金额为订单支付金额
        if (history.getServiceStartTime().toInstant().isAfter(Instant.now())) {
            return history.getPayAmount().doubleValue();
        }
        //如果正在服务期,根据剩余天数所占服务期天数的比例计算退款金额(四舍五入取整)
        // 可退的天数
        long refundDays = DateUtils.dayDiff(history.getServiceEndTime(), new Date());
        // 总的有效期
        double serviceDays = DateUtils.dayDiff(history.getServiceEndTime(), history.getServiceStartTime());
        if (serviceDays == 0) {
            return 0d;
        }
        return history.getPayAmount()
                .multiply(new BigDecimal(refundDays).divide(new BigDecimal(serviceDays), 4, BigDecimal.ROUND_HALF_UP))
                .setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * 计算可退金额根据天数
     *
     * @param history
     * @param refundDays
     * @param order
     * @return Double
     */
    public Double calculateRefundAmount(UserOrderPaymentHistory history, long refundDays, UserOrder order) {
        // 历史订单退全额
        if (history == null) {
            if (StringUtils.isNotBlank(order.getOldOrderId())) {
                // 查找历史订单
                AfentiOrder afentiOrder = afentiOrderServiceClient.getAfentiOrderService()
                        .loadAfentiOrder(order.getOldOrderId())
                        .getUninterruptibly();
                if (afentiOrder != null) {
                    return afentiOrder.getPayAmount();
                } else {
                    return 0d;
                }
            } else {
                return 0d;
            }
        }
        // 道具类的根据使用次数计算
        switch (OrderProductServiceType.safeParse(order.getOrderProductServiceType())) {
            case ValueAddedLiveTimesCard:
                return valueAddedLiveTimesCardCal(history, order);
            case EnglishStoryBook:
                return englishStoryBookCal(history, order);
            case GrowingWorldProp:
                return growingWorldPropCal(history, order);
            default:
                if (history.getServiceStartTime() == null || history.getServiceEndTime() == null) {
                    return 0d;
                }
                // 全额退
                if (refundDays == 0 || history.getServiceEndTime().toInstant().isBefore(Instant.now())) {
                    return history.getPayAmount().doubleValue();
                }
                // 总共可退天数
                long totalDays = DateUtils.dayDiff(history.getServiceEndTime(), history.getServiceStartTime());
                if (refundDays > totalDays) {
                    return 0d;
                }
                //如果正在服务期,根据剩余天数所占服务期天数的比例计算退款金额
                return history.getPayAmount()
                        .multiply(new BigDecimal(refundDays).divide(new BigDecimal(totalDays), 4, BigDecimal.ROUND_HALF_UP))
                        .setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        }
    }


    public Double calculateRefundAmountForMulti(Integer refundDays, UserOrder order, String productId) {
        List<UserOrderAmortizeHistory> paidAmortizeList = userOrderLoaderClient.loadOrderAmortizeHistory(order.genUserOrderId())
                .stream().filter(h -> h.getPaymentStatus() == PaymentStatus.Paid)
                .filter(h -> StringUtils.equals(h.getProductId(), productId))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(paidAmortizeList)) {
            return 0d;
        }
        // 计算总付费金额
        BigDecimal totalPaidPrice = new BigDecimal(0);
        for (UserOrderAmortizeHistory history : paidAmortizeList) {
            totalPaidPrice = totalPaidPrice.add(history.getPayAmount());
        }
        UserOrderAmortizeHistory first = paidAmortizeList.get(0);
        if (first.getServiceStartTime() == null || first.getServiceEndTime() == null) {
            return totalPaidPrice.doubleValue();
        }
        // 已经过期的全额退
        if (refundDays == 0 || first.getServiceEndTime().toInstant().isBefore(Instant.now())) {
            return totalPaidPrice.doubleValue();
        }
        // 总共可退天数
        long totalDays = DateUtils.dayDiff(first.getServiceEndTime(), first.getServiceStartTime());
        if (refundDays > totalDays) {
            return 0d;
        }
        //如果正在服务期,根据剩余天数所占服务期天数的比例计算退款金额
        return totalPaidPrice.multiply(new BigDecimal(refundDays).divide(new BigDecimal(totalDays), 4, BigDecimal.ROUND_HALF_UP))
                .setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * 道具类退款扣减道具
     *
     * @param order
     */
    public void changeCardTimes(UserOrder order) {
        if (order == null || order.getOrderProductServiceType() == null) {
            return;
        }
        switch (OrderProductServiceType.safeParse(order.getOrderProductServiceType())) {
            case ValueAddedLiveTimesCard:
                changeValueAddedLiveTimesCard(order);
                break;
            case EnglishStoryBook:
                changeEnglishStoryBook(order);
                break;
            case GrowingWorldProp:
                changeGrowingWorldProp(order);
                break;
            default:
        }
    }

    private void changeGrowingWorldProp(UserOrder order) {
        int refundTimes;
        // 订单可退次数
        List<OrderProductItem> items = userOrderLoaderClient.loadProductItemsByProductId(order.getProductId());
        if (CollectionUtils.isEmpty(items)) {
            logger.error("Crm load GrowingWorldProp productItem is null, product {}", order.getProductId());
            return;
        }
        OrderProductItem item = items.get(0);
        // 查询剩余总次数
        UserProp prop = growingWorldLoaderClient.getRemoteReference().loadUserPropInfoByPropId(order.getUserId(), item.getAppItemId());
        // 总剩余次数
        if (prop == null || prop.getQuantity() == null || prop.getQuantity() <= 0) {
            logger.error("Crm decr GrowingWorldProp load total error, order " + order.genUserOrderId());
            return;
        }
        int times = item.getPeriod();
        if (prop.getQuantity() >= times) {
            refundTimes = times;
        } else {
            refundTimes = prop.getQuantity();
        }
        MapMessage message = growingWorldServiceClient.getRemoteReference()
                .consumeUserProp(order.getUserId(), item.getAppItemId(), -refundTimes);
        if (!message.isSuccess()) {
            logger.error("ValueAddedLiveTimesCard change times fail, order is " + order.genUserOrderId());
        }
    }

    private void changeEnglishStoryBook(UserOrder order) {
        // 获取剩余次数
        int refundTimes;
        Map<String, Object> notifyMap = new HashMap<>();
        notifyMap.put("user_id", order.getUserId());
        String sig = getEnglishStoryBookSign(order.getUserId(), null);
        notifyMap.put("sign", sig);
        String url = "http://storybook.test.17zuoye.net/info/cards";
        if (RuntimeMode.isStaging()) {
            url = "http://storybook.staging.17zuoye.net/info/cards";
        } else if (RuntimeMode.isProduction()) {
            url = "http://storybook.17zuoye.com/info/cards";
        }
        String URL = UrlUtils.buildUrlQuery(url, notifyMap);
        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().get(URL).execute();
        if (response.hasHttpClientException()) {
            logger.error("Crm change user EnglishStoryBook cards ex.user {}", order.getUserId());
        } else {
            Map<String, Object> result = JsonUtils.convertJsonObjectToMap(response.getResponseString());
            if (!SafeConverter.toBoolean(result.get("success"))) {
                logger.error("Crm user order cal EnglishStoryBook cards return error, user {}, response {}", order.getUserId(), response.getResponseString());
            } else {
                int card = SafeConverter.toInt(result.get("cards"));
                if (card <= 0) {
                    logger.error("EnglishStoryBook cardData error, userId " + order.getUserId());
                    return;
                }
                // 订单可退次数
                List<OrderProductItem> items = userOrderLoaderClient.loadProductItemsByProductId(order.getProductId());
                if (CollectionUtils.isEmpty(items)) {
                    logger.error("EnglishStoryBook productItem data error, orderId " + order.genUserOrderId());
                    return;
                }
                int times = items.get(0).getPeriod();
                if (card >= times) {
                    refundTimes = times;
                } else {
                    refundTimes = card;
                }
                // 扣减听课卡
                decrEnglishStoryBookCard(order, refundTimes);
            }
        }
    }

    private void decrEnglishStoryBookCard(UserOrder order, int refundTimes) {
        Map<String, Object> notifyMap = new HashMap<>();
        notifyMap.put("user_id", order.getUserId());
        notifyMap.put("num", refundTimes);
        String sig = getEnglishStoryBookSign(order.getUserId(), refundTimes);
        notifyMap.put("sign", sig);
        String url = "http://storybook.test.17zuoye.net/info/decrement";
        if (RuntimeMode.isStaging()) {
            url = "http://storybook.staging.17zuoye.net/info/decrement";
        } else if (RuntimeMode.isProduction()) {
            url = "http://storybook.17zuoye.com/info/decrement";
        }
        String URL = UrlUtils.buildUrlQuery(url, notifyMap);
        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().get(URL).execute();
        if (response.hasHttpClientException()) {
            logger.error("Crm decr user EnglishStoryBook cards ex.user {}", order.getUserId());
        } else {
            Map<String, Object> result = JsonUtils.convertJsonObjectToMap(response.getResponseString());
            if (!SafeConverter.toBoolean(result.get("success"))) {
                logger.error("Crm decr user EnglishStoryBook cards return error, user {}, response {}", order.getUserId(), response.getResponseString());
            }
        }
    }

    private void changeValueAddedLiveTimesCard(UserOrder order) {
        int refundTimes;
        // 查询剩余总次数
        WonderlandTimesCard card = wonderlandLoaderClient.getWonderlandLoader()
                .loadWonderlandTimesCard(order.getUserId(), OrderProductServiceType.ValueAddedLiveTimesCard.name())
                .take();
        // 总剩余次数
        if (card == null || card.getTimes() == null || card.getTimes() <= 0) {
            logger.error("ValueAddedLiveTimesCard cardData error, userId " + order.getUserId());
            return;
        }
        // 订单可退次数
        List<OrderProductItem> items = userOrderLoaderClient.loadProductItemsByProductId(order.getProductId());
        if (CollectionUtils.isEmpty(items)) {
            logger.error("ValueAddedLiveTimesCard productItem data error, orderId " + order.genUserOrderId());
            return;
        }
        int times = items.get(0).getPeriod();
        if (card.getTimes() >= times) {
            refundTimes = times;
        } else {
            refundTimes = card.getTimes().intValue();
        }
        MapMessage message = wonderlandServiceClient.getWonderlandService()
                .changeWonderlandTimesCard(order.getUserId(), OrderProductServiceType.ValueAddedLiveTimesCard.name(), -refundTimes);
        if (!message.isSuccess()) {
            logger.error("ValueAddedLiveTimesCard change times fail, order is " + order.genUserOrderId());
        }
    }

    // 查询用户的纯充值流水
    public List<FinanceFlow> getDepositFinanceFlow(Long userId) {
        List<FinanceFlow> flows = financeServiceClient.getFinanceService()
                .findUserFinanceFlows(userId)
                .getUninterruptibly();
        flows = flows.stream().filter(f -> StringUtils.isNotBlank(f.getRefer()) && f.getRefer().equals(FinanceFlowRefer.Deposit.name())).collect(Collectors.toList());
        return flows;
    }

    public static void main(String[] args) {
        long diff = DateUtils.dayDiff(DateUtils.stringToDate("2018-04-24 18:00:00"), new Date());
        System.out.println(diff);
    }

}
