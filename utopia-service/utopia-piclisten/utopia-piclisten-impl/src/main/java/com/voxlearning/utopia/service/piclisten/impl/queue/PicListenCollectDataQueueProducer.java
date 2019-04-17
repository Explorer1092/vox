package com.voxlearning.utopia.service.piclisten.impl.queue;

import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageProducer;
import com.voxlearning.utopia.service.vendor.api.entity.PicListenCollectData;
import lombok.Getter;

import javax.inject.Named;

/**
 * @author jiangpeng
 * @since 2017-03-15 下午5:58
 **/
@Named("com.voxlearning.utopia.service.vendor.impl.queue.PicListenCollectDataQueueProducer")
public class PicListenCollectDataQueueProducer {


    @Getter
    @AlpsQueueProducer(queue = "utopia.vendor.collect.queue")
    private MessageProducer producer;


    public void processCollect(PicListenCollectData data){
        Message message = Message.newMessage();
        message = message.writeObject(data);
        producer.produce(message);
    }
}
