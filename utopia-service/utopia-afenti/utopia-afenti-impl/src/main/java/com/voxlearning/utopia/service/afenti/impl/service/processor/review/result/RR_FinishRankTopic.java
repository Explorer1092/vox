package com.voxlearning.utopia.service.afenti.impl.service.processor.review.result;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.pubsub.AlpsPubsubPublisher;
import com.voxlearning.alps.spi.pubsub.MessagePublisher;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.service.afenti.api.context.ReviewResultContext;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Named;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author songtao
 * @since 2017/11/29
 */
@Named
public class RR_FinishRankTopic extends SpringContainerSupport implements IAfentiTask<ReviewResultContext> {

    @AlpsPubsubPublisher(topic = "utopia.afenti.review.finish.rank.topic") private MessagePublisher messagePublisher;

    @Override
    public void execute(ReviewResultContext context) {
        if (context.getStat() != null) {
            return;
        }

        Map<String, Object> event = new HashMap<>();
        event.put("userId", context.getStudent().getId());
        event.put("subject", context.getSubject());
        event.put("star", context.getStar());
        event.put("finishTime", new Date().getTime());

        messagePublisher.publish(Message.newMessage().writeObject(event));
    }
}
