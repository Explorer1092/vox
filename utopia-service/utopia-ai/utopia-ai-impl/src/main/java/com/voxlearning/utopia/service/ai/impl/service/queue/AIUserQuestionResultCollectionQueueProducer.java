package com.voxlearning.utopia.service.ai.impl.service.queue;

import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageProducer;
import com.voxlearning.utopia.service.ai.context.AbstractAIContext;
import lombok.Getter;

import javax.inject.Named;


@Named("com.voxlearning.utopia.service.ai.impl.service.queue.AIUserQuestionResultCollectionQueueProducer")
public class AIUserQuestionResultCollectionQueueProducer {


    @Getter
    @AlpsQueueProducer(queue = "utopia.chips.question.result.collect.queue")
    private MessageProducer producer;


    public void processCollect(AbstractAIContext data){
        Message message = Message.newMessage();
        message = message.writeObject(data);
        producer.produce(message);
    }
}
