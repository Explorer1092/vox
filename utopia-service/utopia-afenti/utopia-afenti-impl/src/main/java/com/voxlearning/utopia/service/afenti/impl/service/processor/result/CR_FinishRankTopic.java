package com.voxlearning.utopia.service.afenti.impl.service.processor.result;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.pubsub.AlpsPubsubPublisher;
import com.voxlearning.alps.spi.pubsub.MessagePublisher;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.afenti.api.context.CastleResultContext;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLearningPlanPushExamHistory;
import com.voxlearning.utopia.service.afenti.impl.util.AfentiUtils;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Named;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Summer on 2017/11/1.
 */
@Named
public class CR_FinishRankTopic extends SpringContainerSupport implements IAfentiTask<CastleResultContext> {
    @AlpsPubsubPublisher(topic = "utopia.afenti.finish.rank.topic") private MessagePublisher messagePublisher;

    @Override
    public void execute(CastleResultContext context) {
        //发送广播
        OrderProductServiceType serviceType = AfentiUtils.getOrderProductServiceType(context.getSubject());
        Map<String, Object> event = new HashMap<>();
        event.put("userId", context.getStudent().getId());
        event.put("appKey", serviceType != null ? serviceType.name() : "");
        event.put("star", context.getStar());
        event.put("finishTime", new Date().getTime());
        // 获取推题时间
        AfentiLearningPlanPushExamHistory history = context.getHistories().stream().findAny().orElse(null);
        if (history != null) {
            event.put("pushTime", history.getCreatetime().getTime());
        }
        messagePublisher.publish(Message.newMessage().writeObject(event));
    }
}
