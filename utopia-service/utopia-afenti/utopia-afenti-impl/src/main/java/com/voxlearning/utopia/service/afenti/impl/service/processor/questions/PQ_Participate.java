package com.voxlearning.utopia.service.afenti.impl.service.processor.questions;

import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.afenti.api.context.PushQuestionContext;
import com.voxlearning.utopia.service.afenti.impl.pubsub.AfentiPublisher;
import com.voxlearning.utopia.service.afenti.impl.util.AfentiUtils;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ruib
 * @since 2017/3/28
 */
@Named
public class PQ_Participate extends SpringContainerSupport implements IAfentiTask<PushQuestionContext> {
    @Inject private AfentiPublisher publisher;

    @Override
    public void execute(PushQuestionContext context) {
        OrderProductServiceType type = AfentiUtils.getOrderProductServiceType(context.getSubject());
        if (type == null) {
            logger.error("PQ_Participate Subject {} not available", context.getSubject());
            context.errorResponse();
            return;
        }

        try {
            Map<String, Object> map = new HashMap<>();
            map.put("eventType", "app_chuang_participate");
            map.put("studentId", String.valueOf(context.getStudent().getId()));
            map.put("appKey", type.name());
            publisher.getMessagePlainPublisher().publish(Message.newMessage().withPlainTextBody(JsonUtils.toJson(map)));
        } catch (Exception ex) {
            logger.error("CR_FinishChuangTask Error.", ex);
        }
    }
}
