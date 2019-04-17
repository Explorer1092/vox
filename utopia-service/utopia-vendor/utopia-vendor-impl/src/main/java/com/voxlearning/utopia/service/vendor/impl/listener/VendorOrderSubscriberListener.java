package com.voxlearning.utopia.service.vendor.impl.listener;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.spi.pubsub.PubsubDestination;
import com.voxlearning.alps.spi.pubsub.PubsubSubscriber;
import com.voxlearning.alps.spi.pubsub.PubsubSystem;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageListener;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.vendor.impl.service.ClazzExpandSellServiceImpl;
import com.voxlearning.utopia.service.vendor.impl.service.XiaoUOrderInfoServiceImpl;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.voxlearning.utopia.api.constant.OrderProductServiceType.*;

/**
 * @author jiangpeng
 * @since 2018-01-15 下午4:10
 **/
@Named
@PubsubSubscriber(destinations = {
        @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = "utopia.order.success.topic"),
        @PubsubDestination(system = PubsubSystem.KFK, connection = "main-backup", topic = "utopia.order.success.topic")
})
public class VendorOrderSubscriberListener implements MessageListener {

    @Inject
    private XiaoUOrderInfoServiceImpl xiaoUOrderInfoService;
    @Inject
    private ClazzExpandSellServiceImpl clazzExpandSellService;

    private static final List<OrderProductServiceType> afentiList = Arrays.asList(AfentiExam, AfentiMath, AfentiChinese, AfentiChineseImproved, AfentiExamImproved, AfentiMathImproved);

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
        dealXiaoUOrderInfo(msgMap);
        dealExpandClazzOrderInfo(msgMap);
    }


    private void dealXiaoUOrderInfo(Map<String, Object> msgMap) {
        Long studentId = MapUtils.getLong(msgMap, "userId");
        OrderProductServiceType type = OrderProductServiceType.safeParse(SafeConverter.toString(msgMap.get("serviceType")));
        if (afentiList.contains(type)) {
            xiaoUOrderInfoService.addXiaoUOrderInfo(studentId, type.name());
        }
    }

    private void dealExpandClazzOrderInfo(Map<String, Object> msgMap) {
        Long studentId = MapUtils.getLong(msgMap, "userId");
        OrderProductServiceType type = OrderProductServiceType.safeParse(SafeConverter.toString(msgMap.get("serviceType")));
        if (clazzExpandSellService.getExpandClazzList().contains(type)) {
            clazzExpandSellService.addUsedStudentOrderInfo(studentId, type.name());
        }
    }
}