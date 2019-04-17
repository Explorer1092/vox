package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.finish.callback;

import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.business.BusinessEvent;
import com.voxlearning.utopia.business.BusinessEventType;
import com.voxlearning.utopia.service.newhomework.api.client.callback.PostFinishHomework;
import com.voxlearning.utopia.service.newhomework.api.context.FinishHomeworkContext;
import com.voxlearning.utopia.service.newhomework.impl.queue.BusinessQueueProducer;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * 删了又捞回来
 */
@Named
public class PostFinishNewHomeworkMagicCastle extends SpringContainerSupport implements PostFinishHomework {

    @Inject
    private BusinessQueueProducer businessQueueProducer;

    @Override
    public void afterHomeworkFinished(FinishHomeworkContext context) {
        BusinessEvent event = new BusinessEvent();
        event.setType(BusinessEventType.STUDENT_ACTIVE_MAGICIAN_AND_ADD_VALUE);
        event.getAttributes().put("studentId", context.getUserId());

        Message message = Message.newMessage().withStringBody(JsonUtils.toJson(event));
        businessQueueProducer.getProducer().produce(message);
    }

}
