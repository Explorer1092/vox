package com.voxlearning.utopia.service.afenti.impl.service.processor.result;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.pubsub.AlpsPubsubPublisher;
import com.voxlearning.alps.spi.pubsub.MessagePublisher;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.service.afenti.api.constant.UtopiaAfentiConstants;
import com.voxlearning.utopia.service.afenti.api.context.ElfResultContext;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Summer on 2017/7/20.
 */
@Named
public class SER_GrowthWorldTopic extends SpringContainerSupport implements IAfentiTask<ElfResultContext> {
    @AlpsPubsubPublisher(topic = "utopia.afenti.growth.topic") private MessagePublisher messagePublisher;

    @Override
    public void execute(ElfResultContext context) {
        if (!StringUtils.equals(context.getQuestionId(), UtopiaAfentiConstants.NO_SIMILAR_QUESTION)) {
            // 发送广播  阿凡提错题城堡答题正确数
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", "answer_correct");
            event.put("studentId", context.getStudent().getId());
            event.put("num", 1);
            event.put("source", "afenti_elf");
            messagePublisher.publish(Message.newMessage().writeObject(event));
        }
    }
}
