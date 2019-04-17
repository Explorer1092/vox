package com.voxlearning.utopia.service.zone.impl.listener;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.queue.*;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.order.api.entity.OrderProductItem;
import com.voxlearning.utopia.service.order.api.loader.UserOrderLoader;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.zone.api.ClazzActivityService;
import com.voxlearning.utopia.service.zone.api.entity.ClazzActivity;
import com.voxlearning.utopia.service.zone.api.entity.ClazzActivityRecord;
import com.voxlearning.utopia.service.zone.api.entity.ZoneClazzRewardNotice;
import com.voxlearning.utopia.service.zone.api.entity.plot.PlotActivityBizObject;
import com.voxlearning.utopia.service.zone.api.plot.PlotActivityService;
import com.voxlearning.utopia.service.zone.impl.persistence.ClazzActivityPersistence;
import com.voxlearning.utopia.service.zone.impl.persistence.ClazzActivityRecordPersistence;
import com.voxlearning.utopia.service.zone.impl.persistence.ZoneClazzRewardPersistence;

import javax.annotation.Resource;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 订单消费
 *
 * @author chensn
 * @since 2018.11.10
 */
@Named("com.voxlearning.utopia.service.zone.impl.listener.ClazzActivityOrderQueueListener")
@QueueConsumer(
        destinations = {
                @QueueDestination(system = QueueSystem.KFK, config = "main", queue = "utopia.order.payment.topic")
        },
        maxPermits = 8
)
public class ClazzActivityOrderQueueListener extends SpringContainerSupport implements MessageListener {
    @Resource
    private StudentLoaderClient studentLoaderClient;
    @Resource
    private UserOrderLoaderClient userOrderLoaderClient;
    @Resource
    private ClazzActivityService clazzActivityService;
    @Resource
    private ClazzActivityRecordPersistence clazzActivityRecordPersistence;
    @ImportService(interfaceClass = UserOrderLoader.class)
    protected UserOrderLoader userOrderLoader;
    @Resource
    PlotActivityService plotActivityService;
    @Resource
    private ZoneClazzRewardPersistence zoneClazzRewardPersistence;
    @Resource
    private ClazzActivityPersistence clazzActivityPersistence;


    @Override
    public void onMessage(Message message) {
        try {
            if (message == null) {
                logger.error("clazz order  queue no message");
                return;
            }
            Object body = message.decodeBody();

            if (body != null && body instanceof String) {
                String json = (String) body;
                Map<String, Object> event = JsonUtils.fromJson(json, Map.class);
                if (event == null || event.get("userId") == null || event.get("orderId") == null || event.get("itemId") == null) {
                    return;
                }
                Long userId = SafeConverter.toLong(event.get("userId"));
                StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(userId);
                Clazz clazz = studentDetail.getClazz();
                List<ClazzActivity> list = clazzActivityPersistence.findUsedActivity();
                if (CollectionUtils.isNotEmpty(list)) {
                    ClazzActivity activity = list.get(0);
                    if (new Date().before(activity.getEndDate())) {
                        //当前时间在截止日期之前，订单都拦截
                        List<OrderProductItem> orderProductItems = userOrderLoaderClient.loadProductItemsByProductType(OrderProductServiceType.AfentiMath);
                        orderProductItems.addAll(userOrderLoaderClient.loadProductItemsByProductType(OrderProductServiceType.AfentiChinese));
                        orderProductItems.addAll(userOrderLoaderClient.loadProductItemsByProductType(OrderProductServiceType.AfentiExam));
                        Map<String, OrderProductItem> products = orderProductItems
                                .stream()
                                .filter(e ->e.getPeriod() == 90)
                                .filter(p -> !p.isDisabledTrue())
                                .collect(Collectors.toMap(OrderProductItem::getId, Function.identity()));
                        String itemId = (String) event.get("itemId");
                        if (products.containsKey(itemId)) {
                            OrderProductItem orderProductItem = products.get(itemId);
                            ClazzActivityRecord userRecord = clazzActivityService.findUserRecord(userId, studentDetail.getClazz().getSchoolId(), studentDetail.getClazz().getId(), 3);
                            if (userRecord == null) {
                                //没参加活动不处理
                                return;
                            }
//                            List<String> orderIds = userRecord.getOrderIds();
                            String orderMsgId = (String) event.get("orderId");
                            Map<String, Boolean> orderPay = userRecord.getOrderPay();
                            if (orderPay == null) {
                                orderPay = new LinkedHashMap<>();
                            }
                            orderPay.put(orderMsgId, true);
                            userRecord.setOrderPay(orderPay);
                            Integer plotGroupDateId = plotActivityService.getPlotGroupDateId(3, new Date().getTime());
                            if (plotGroupDateId == null) {
                                //当前剧情不存在
                                return;
                            }
                            if (userRecord.getBizObject() != null) {
                                PlotActivityBizObject bizObject = JsonUtils.fromJson(JsonUtils.toJson(userRecord.getBizObject()), PlotActivityBizObject.class);
                                bizObject.setVip(true);
                                bizObject.setAppkey(orderProductItem.getProductType());
                                userRecord.setBizObject(bizObject);
                            }
                            List<ZoneClazzRewardNotice> clazzList = zoneClazzRewardPersistence.findByClazzIdAndReward(3, clazz.getId(), plotGroupDateId);
                            ZoneClazzRewardNotice zoneClazzRewardNotice = new ZoneClazzRewardNotice();
                            zoneClazzRewardNotice.setActivityId(3);
                            zoneClazzRewardNotice.setClazzId(clazz.getId());
                            zoneClazzRewardNotice.setUserId(userId);
                            zoneClazzRewardNotice.setPrice(new BigDecimal(SafeConverter.toDouble(event.get("payAmount"))));
                            zoneClazzRewardNotice.setRewardType(plotGroupDateId);
                            zoneClazzRewardNotice.setIsReceived(false);
                            zoneClazzRewardNotice.generateId();
                            zoneClazzRewardNotice.setPeriod(orderProductItem.getPeriod());
                            String productType = orderProductItem.getProductType();
                            if (StringUtils.isNoneBlank(productType)) {
                                if (productType.contains("Improved")) {
                                    zoneClazzRewardNotice.setIsImproved(true);
                                } else {
                                    zoneClazzRewardNotice.setIsImproved(false);
                                }
                                if (productType.contains("Math")) {
                                    zoneClazzRewardNotice.setSubject(2);
                                } else if (productType.contains("Chinese")) {
                                    zoneClazzRewardNotice.setSubject(1);
                                } else {
                                    zoneClazzRewardNotice.setSubject(3);
                                }
                            } else {
                                zoneClazzRewardNotice.setSubject(3);
                                zoneClazzRewardNotice.setIsImproved(false);
                            }
                            zoneClazzRewardNotice.setIsFirst(CollectionUtils.isEmpty(clazzList));
                            zoneClazzRewardPersistence.insert(zoneClazzRewardNotice);
                            clazzActivityRecordPersistence.upsert(userRecord);

                        }

                    }
                }



            }
        } catch (Exception ex) {
            return;
        }
    }
}
