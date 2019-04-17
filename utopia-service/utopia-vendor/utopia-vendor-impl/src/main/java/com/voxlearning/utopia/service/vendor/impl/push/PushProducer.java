package com.voxlearning.utopia.service.vendor.impl.push;

import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.MessageProducer;
import com.voxlearning.utopia.service.push.api.support.PushContext;
import com.voxlearning.utopia.service.vendor.impl.push.umeng.UmengPushRateController;
import lombok.Getter;

import javax.inject.Named;

/**
 * @author xinxin
 * @since 10/11/2016
 */
@Named
public class PushProducer {

    @Getter
    @AlpsQueueProducer(queue = "utopia.umeng.queue")
    private MessageProducer umengProducer;
    @Getter
    @AlpsQueueProducer(queue = "utopia.mi.queue")
    private MessageProducer miProducer;
    @Getter
    @AlpsQueueProducer(queue = "utopia.hw.queue")
    private MessageProducer hwProducer;
    @Getter
    @AlpsQueueProducer(queue = "utopia.jpush.queue")
    private MessageProducer jpushProducer;

    @Getter
    @AlpsQueueProducer(queue = "utopia.umeng.retry.queue")
    private MessageProducer umengRetryProducer;
    @Getter
    @AlpsQueueProducer(queue = "utopia.pushRetry.queue")
    private MessageProducer jpushRetryProducer;

    @Getter
    @AlpsQueueProducer(queue = "yiqixue.jpush.queue")
    private MessageProducer yiQiXueProducer;
    @Getter
    @AlpsQueueProducer(queue = "yiqixue.pushRetry.queue")
    private MessageProducer yiQiXueRetryProducer;

    public void produce(PushContext context) {
        if (UmengPushRateController.isProduceAvailable(context)) {
//            umengProducer.produce(Message.newMessage().writeObject(context));
        }
    }
}
