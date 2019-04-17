package com.voxlearning.utopia.agent.listener;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.queue.*;
import com.voxlearning.utopia.agent.service.activity.LiveEnrollmentPositiveService;
import com.voxlearning.utopia.agent.service.activity.LiveEnrollmentService;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.Map;

/**
 * AgentWorkFlowQueueListener
 *
 * @author song.wang
 * @date 2016/12/27
 */
@Named
@QueueConsumer(
        destinations = {
                @QueueDestination( system = QueueSystem.KFK,config = "primary",queue = "live-cast-enrollment-activity-order-refund"),
                @QueueDestination(system = QueueSystem.KFK,config = "main-backup", queue = "live-cast-enrollment-activity-order-refund")
        }
)
public class AgentLiveEnrollmentRefundListener extends SpringContainerSupport implements MessageListener {

    @Inject
    private LiveEnrollmentPositiveService liveEnrollmentPositiveService;

    @Override
    public void onMessage(Message message) {
        Map map = null;
        Object decoded = message.decodeBody();
        if (decoded instanceof String) {
            String messageText = (String) decoded;
            map = JsonUtils.fromJson(messageText);
        }else if (decoded instanceof Map)
            map = (Map) decoded;

        if (map == null) {
            logger.error("AgentLiveEnrollmentRefundListener error message {}", JsonUtils.toJson(message.decodeBody()));
            return;
        }

        String payOrderId = SafeConverter.toString(map.get("payOrderId"));
        String refundOrderId = SafeConverter.toString(map.get("refundOrderId"));
        Long refundPrice = SafeConverter.toLong(map.get("refundPrice"));

        liveEnrollmentPositiveService.saveLiveEnrollmentRefundOrder(payOrderId, refundPrice,refundOrderId);

    }
}
