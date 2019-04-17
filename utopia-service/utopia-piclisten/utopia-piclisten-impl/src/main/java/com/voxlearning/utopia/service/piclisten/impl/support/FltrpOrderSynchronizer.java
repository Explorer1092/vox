package com.voxlearning.utopia.service.piclisten.impl.support;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.order.api.util.AfentiOrderUtil;
import com.voxlearning.utopia.service.piclisten.impl.service.ParentSelfStudyServiceImpl;
import com.voxlearning.utopia.service.piclisten.impl.service.VendorQueueService;
import com.voxlearning.utopia.service.piclisten.support.BaseOrderSynchronizer;
import com.voxlearning.utopia.service.vendor.api.mapper.OrderSynchronizeBookInfo;
import com.voxlearning.utopia.service.vendor.api.mapper.OrderSynchronizeContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xinxin
 * @since 7/11/17.
 * 外研社订单同步器
 */
@Named
@Slf4j
public class FltrpOrderSynchronizer extends BaseOrderSynchronizer {
    @Inject
    private ParentSelfStudyServiceImpl parentSelfStudyService;
    @Inject
    private VendorQueueService vendorService;
    @Inject
    private CommonConfigServiceClient commonConfigServiceClient;

    private boolean isValid(UserOrder order) {
        if (null == order
                || (OrderProductServiceType.safeParse(order.getOrderProductServiceType()) != OrderProductServiceType.PicListenBook && !AfentiOrderUtil.isAfentiImpovedOrder(OrderProductServiceType.safeParse(order.getOrderProductServiceType())))
                || StringUtils.isBlank(order.getExtAttributes())) {
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
            if (info.getSource().equals(PUBLISHER_FLTRP)) {
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

        Map<String, Object> params = generateNotifyParam(order);
        vendorService.sendHttpNotify(OrderProductServiceType.PicListenBook.name(), commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.getType(), CONFIG_ORDER_SYNCHRONIZE_TARGET_FLTRP), params);
    }

    private Map<String, Object> generateNotifyParam(UserOrder order) {
        FltrpOrderInfo info = new FltrpOrderInfo();
        info.setAppId(ProductConfig.get(CONFIG_PICLISTEN_WAIYANSHE_APPID));
        info.setOrderSn(order.getId());
        info.setMobile(SafeConverter.toString(getRealNotifyUserId(order)));

        //解析bookId
        info.setBookEditionCode(getSDKBookId(order));
        info.setEffectivePeriod(getPeriod(order));
        //解析手机号
        MapMessage message = parentSelfStudyService.getFltrpMobile(getRealNotifyUserId(order));
        if (message.isSuccess()) {
            String mobile = message.get("mobile").toString();
            info.setPhone(mobile);
        }
        info.setSign(FltrpSignatureGenerator.sign(info.toMap(), ProductConfig.get(CONFIG_PICLISTEN_WAIYANSHE_SECRET)));

        return new HashMap<>(info.toMap());
    }

    private String getPeriod(UserOrder order) {
        //这里有个约定，所有子产品都是相同period的
        OrderSynchronizeContext context = JsonUtils.fromJson(order.getExtAttributes(), OrderSynchronizeContext.class);
        int period = SafeConverter.toInt(context.getBooks().get(0).getPeriod());
        return String.valueOf(period / 30);
    }

    private String getSDKBookId(UserOrder order) {
        String waiyanBookId;

        OrderSynchronizeContext context = JsonUtils.fromJson(order.getExtAttributes(), OrderSynchronizeContext.class);
        if (StringUtils.isNotBlank(context.getPackageId())) {
            //打包卖的把包id做为bookId传给外研社
            waiyanBookId = context.getPackageId();
        } else {
            //如果不是打包卖的，里面只能有一本教材
            waiyanBookId = context.getBooks().get(0).getBookId();
        }

        return waiyanBookId;
    }
}
