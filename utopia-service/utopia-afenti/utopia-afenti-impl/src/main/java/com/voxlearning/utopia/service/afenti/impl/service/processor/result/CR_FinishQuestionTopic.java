package com.voxlearning.utopia.service.afenti.impl.service.processor.result;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.pubsub.AlpsPubsubPublisher;
import com.voxlearning.alps.spi.pubsub.MessagePublisher;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.afenti.api.context.CastleResultContext;
import com.voxlearning.utopia.service.afenti.impl.util.AfentiUtils;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Named;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Named
public class CR_FinishQuestionTopic extends SpringContainerSupport implements IAfentiTask<CastleResultContext> {
    @AlpsPubsubPublisher(topic = "utopia.afenti.finish.question.topic") private MessagePublisher messagePublisher;

    @Override
    public void execute(CastleResultContext context) {
        //发送广播
        OrderProductServiceType serviceType = AfentiUtils.getOrderProductServiceType(context.getSubject());
        Map<String, Object> event = new HashMap<>();
        event.put("userId", context.getStudent().getId());
        event.put("appKey", serviceType != null ? serviceType.name() : "");
        event.put("right", context.getMaster());
        event.put("finishTime", new Date().getTime());
        messagePublisher.publish(Message.newMessage().writeObject(event));
    }
}
