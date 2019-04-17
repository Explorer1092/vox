package com.voxlearning.utopia.service.piclisten.impl.support;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.order.api.util.AfentiOrderUtil;
import com.voxlearning.utopia.service.piclisten.impl.service.VendorQueueService;
import com.voxlearning.utopia.service.piclisten.support.BaseOrderSynchronizer;
import com.voxlearning.utopia.service.vendor.api.mapper.OrderSynchronizeBookInfo;
import com.voxlearning.utopia.service.vendor.api.mapper.OrderSynchronizeContext;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.voxlearning.alps.repackaged.org.apache.commons.lang3.StringUtils.isBlank;

/**
 * @author xinxin
 * @since 7/11/17.
 * 人教订单同步器
 */
@Named
@Slf4j
public class PepOrderSynchronizer extends BaseOrderSynchronizer {
    @Inject
    private VendorQueueService vendorService;
    @Inject
    private CommonConfigServiceClient commonConfigServiceClient;

    private boolean isValid(UserOrder order) {
        if (OrderProductServiceType.safeParse(order.getOrderProductServiceType()) != OrderProductServiceType.PicListenBook && !AfentiOrderUtil.isAfentiImpovedOrder(OrderProductServiceType.safeParse(order.getOrderProductServiceType()))) {
            return false;
        }

        Map<String, Object> attrs = JsonUtils.fromJson(order.getExtAttributes());
        if (MapUtils.isEmpty(attrs)) {
            return false;
        }

        OrderSynchronizeContext context = JsonUtils.fromJson(order.getExtAttributes(), OrderSynchronizeContext.class);
        if (null == context || CollectionUtils.isEmpty(context.getBooks())) {
            return false;
        }
        for (OrderSynchronizeBookInfo info : context.getBooks()) {
            if (StringUtils.isNotBlank(info.getSource()) && info.getSource().equals(PUBLISHER_PEP)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void accept(UserOrder order) {
        if (!isValid(order)) {
            return;
        }

        OrderSynchronizeContext context = JsonUtils.fromJson(order.getExtAttributes(), OrderSynchronizeContext.class);
        if (isBlank(context.getPackageId())) {
            Map<String, Object> params = generateNotifyParam(order);
            vendorService.sendHttpNotify(OrderProductServiceType.PicListenBook.name(), commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.getType(), CONFIG_ORDER_SYNCHRONIZE_TARGET_PEP), params);
        } else {
            //打包产品订单同步
            Map<String, Object> params = generatePackageNotifyParam(order);
            vendorService.sendHttpNotify(OrderProductServiceType.PicListenBook.name(), commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.getType(), CONFIG_ORDER_SYNC_TARGET_PEP_PACKAGE), params);
        }
    }

    private Map<String, Object> generatePackageNotifyParam(UserOrder order) {
        OrderSynchronizeContext context = JsonUtils.fromJson(order.getExtAttributes(), OrderSynchronizeContext.class);
        if (isBlank(context.getPackageId())) {
            return null;
        }

        PepOrderInfo orderInfo = new PepOrderInfo();
        orderInfo.setPlatform_key(ProductConfig.get(CONFIG_PICLISTEN_RENJIAO_APPID));
        orderInfo.setUser_id(SafeConverter.toString(getRealNotifyUserId(order)));
        orderInfo.setPay_time(order.getUpdateDatetime().toInstant().toEpochMilli() / 1000);
        orderInfo.setReal_price(BigDecimal.valueOf(order.getOrderPrice().longValue()).multiply(BigDecimal.valueOf(100)).intValue());
        orderInfo.setPay_tradeno(order.getId());

        List<PepOrderInfo.Info> infos = new ArrayList<>();
        for (OrderSynchronizeBookInfo bookInfo : context.getBooks()) {
            PepOrderInfo.Info info = orderInfo.new Info();
            info.setBook_id(bookInfo.getBookId());
            info.setReal_price(bookInfo.getPrice());
            infos.add(info);
        }
        orderInfo.setOrder_info(infos);

        try {
            String sign = PepSignatureGenerator.getSignature(URLEncoder.encode("platform_key=" + orderInfo.getPlatform_key() + "&user_id=" + orderInfo.getUser_id(), "UTF-8"), ProductConfig.get(CONFIG_PICLISTEN_RENJIAO_SECRET));
            orderInfo.setSign(sign);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        return JsonUtils.safeConvertObjectToMap(orderInfo);
    }

    private Map<String, Object> generateNotifyParam(UserOrder order) {
        OrderSynchronizeContext context = JsonUtils.fromJson(order.getExtAttributes(), OrderSynchronizeContext.class);
        if (!isBlank(context.getPackageId())) {
            return null;
        }

        PepOrderInfo orderInfo = new PepOrderInfo();
        List<PepOrderInfo.Info> infos = new ArrayList<>();
        for (OrderSynchronizeBookInfo bookInfo : context.getBooks()) {
            PepOrderInfo.Info info = orderInfo.new Info();
            info.setBook_id(bookInfo.getBookId());
            info.setPay_time(order.getUpdateDatetime().toInstant().toEpochMilli() / 1000);
            info.setPay_tradeno(order.getId());
            info.setReal_price(bookInfo.getPrice());
            infos.add(info);
        }
        orderInfo.setOrder_info(infos);

        orderInfo.setPlatform_key(ProductConfig.get(CONFIG_PICLISTEN_RENJIAO_APPID));
        orderInfo.setUser_id(SafeConverter.toString(getRealNotifyUserId(order)));

        try {
            String sign = PepSignatureGenerator.getSignature(URLEncoder.encode("platform_key=" + orderInfo.getPlatform_key() + "&user_id=" + orderInfo.getUser_id(), "UTF-8"), ProductConfig.get(CONFIG_PICLISTEN_RENJIAO_SECRET));
            orderInfo.setSign(sign);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        return JsonUtils.safeConvertObjectToMap(orderInfo);
    }

}
