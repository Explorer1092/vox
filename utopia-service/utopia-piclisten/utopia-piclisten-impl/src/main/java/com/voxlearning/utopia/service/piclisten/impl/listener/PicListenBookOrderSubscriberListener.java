package com.voxlearning.utopia.service.piclisten.impl.listener;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.spi.pubsub.PubsubDestination;
import com.voxlearning.alps.spi.pubsub.PubsubSubscriber;
import com.voxlearning.alps.spi.pubsub.PubsubSystem;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageListener;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.order.api.entity.OrderProductItem;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.piclisten.impl.loader.TextBookManagementLoaderImpl;
import com.voxlearning.utopia.service.piclisten.impl.service.PicListenCommonServiceImpl;
import com.voxlearning.utopia.service.piclisten.impl.service.VendorQueueService;
import com.voxlearning.utopia.service.piclisten.impl.support.PepOrderInfo;
import com.voxlearning.utopia.service.piclisten.impl.support.PepSignatureGenerator;
import com.voxlearning.utopia.service.vendor.api.entity.TextBookManagement;
import com.voxlearning.utopia.service.vendor.api.mapper.OrderSynchronizeBookInfo;
import com.voxlearning.utopia.service.vendor.api.mapper.OrderSynchronizeContext;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.voxlearning.utopia.service.piclisten.support.OrderSynchronizer.*;

/**
 * Created by Summer on 2017/9/26.
 * 点读机订单广播后处理
 * 还有阿分题活动的订单后处理，
 */
@Named
@PubsubSubscriber(destinations = {
        @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = "utopia.order.success.topic"),
        @PubsubDestination(system = PubsubSystem.KFK, connection = "main-backup", topic = "utopia.order.success.topic")
})
public class PicListenBookOrderSubscriberListener implements MessageListener {
    @Inject private UserOrderLoaderClient userOrderLoaderClient;
    @Inject private TextBookManagementLoaderImpl textBookManagementLoader;
    @Inject private VendorQueueService vendorService;
    @Inject private CommonConfigServiceClient commonConfigServiceClient;
    @Inject private PicListenCommonServiceImpl picListenCommonService;


    @Override
    public void onMessage(Message message) {
        Map<String, Object> msgMap = null;
        Object object = message.decodeBody();
        if (object instanceof String) {
            msgMap = JsonUtils.fromJson((String) object);
        }
        if (msgMap == null) {
            return;
        }
        Long orderPayTime = SafeConverter.toLong(msgMap.get("payTime"), System.currentTimeMillis()) / 1000; //秒
        OrderProductServiceType serviceType = OrderProductServiceType.safeParse((String) msgMap.get("serviceType"));

        if (serviceType == OrderProductServiceType.GroupProduct) {
            String productId = SafeConverter.toString(msgMap.get("productId"), "");
            Long parentId = SafeConverter.toLong(msgMap.get("extAttr"));
            String orderId = SafeConverter.toString(msgMap.get("orderId"), "");
            // 处理捆绑的同步
            OrderSynchronizeContext context = new OrderSynchronizeContext();
            List<OrderProductItem> orderProductItems = userOrderLoaderClient.loadProductItemsByProductId(productId);
            if (CollectionUtils.isNotEmpty(orderProductItems)) {
                for (OrderProductItem item : orderProductItems) {
                    if (OrderProductServiceType.safeParse(item.getProductType()) != OrderProductServiceType.PicListenBook)
                        continue;
                    TextBookManagement.SdkInfo sdkInfo = textBookManagementLoader.picListenSdkInfo(item.getAppItemId());
                    if (null == sdkInfo || StringUtils.isBlank(sdkInfo.getSdkBookIdV2())) continue;

                    context.addBook(sdkInfo.getSdkType().name(),
                            sdkInfo.getSdkBookIdV2(),
                            item.getPeriod(),
                            BigDecimal.valueOf(item.getOriginalPrice().longValue()).multiply(BigDecimal.valueOf(100)).intValue());
                }
            }
            Map<String, Object> params = generateNotifyParam(context, parentId, orderId, orderPayTime);
            vendorService.sendHttpNotify(OrderProductServiceType.PicListenBook.name(),
                    commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.getType(), CONFIG_ORDER_SYNCHRONIZE_TARGET_PEP),
                    params);
            picListenCommonService.addPiclistenPurchaseCount(1L);
        }else if (serviceType == OrderProductServiceType.PicListenBook){
            String productId = SafeConverter.toString(msgMap.get("productId"), "");
            if (StringUtils.isEmpty(productId)) {
                return;
            }
            List<OrderProductItem> orderProductItems = userOrderLoaderClient.loadProductItemsByProductId(productId);
            if (CollectionUtils.isNotEmpty(orderProductItems)) {
                //FIXME 特别恶心：这里不能按照子产品数量是多少就加几个，因为有的子产品不是点读机，也按照PicListenBook配置了，比如赠送的双语故事会
                //FIXME 打包售卖活动目前都是两册合并打包，先这么处理
                if (orderProductItems.size() > 1) {
                    picListenCommonService.addPiclistenPurchaseCount(2L);
                } else {
                    picListenCommonService.addPiclistenPurchaseCount(1L);
                }
            }
        }
    }

    private Map<String, Object> generateNotifyParam(OrderSynchronizeContext context, Long userId, String orderId, Long payTime) {
        PepOrderInfo orderInfo = new PepOrderInfo();
        List<PepOrderInfo.Info> infos = new ArrayList<>();
        for (OrderSynchronizeBookInfo bookInfo : context.getBooks()) {
            PepOrderInfo.Info info = orderInfo.new Info();
            info.setBook_id(bookInfo.getBookId());
            info.setPay_time(payTime);
            info.setPay_tradeno(orderId);
            info.setReal_price(bookInfo.getPrice());
            infos.add(info);
        }
        orderInfo.setOrder_info(infos);

        orderInfo.setPlatform_key(ProductConfig.get(CONFIG_PICLISTEN_RENJIAO_APPID));
        orderInfo.setUser_id(userId.toString());

        try {
            String sign = PepSignatureGenerator.getSignature(URLEncoder.encode("platform_key=" + orderInfo.getPlatform_key() + "&user_id=" + orderInfo.getUser_id(), "UTF-8"), ProductConfig.get(CONFIG_PICLISTEN_RENJIAO_SECRET));
            orderInfo.setSign(sign);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        return JsonUtils.safeConvertObjectToMap(orderInfo);
    }
}
