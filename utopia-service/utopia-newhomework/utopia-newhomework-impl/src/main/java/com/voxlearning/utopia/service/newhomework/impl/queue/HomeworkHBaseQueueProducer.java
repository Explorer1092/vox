package com.voxlearning.utopia.service.newhomework.impl.queue;

import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.MessageEncodeMode;
import com.voxlearning.alps.spi.queue.MessageProducer;
import lombok.Getter;

import javax.inject.Named;

/**
 * @author xuesong.zhang
 * @since 2017/8/15
 */
@Named("com.voxlearning.utopia.service.newhomework.impl.queue.HomeworkHBaseQueueProducer")
public class HomeworkHBaseQueueProducer {
//    @Getter
//    @AlpsQueueProducer(queue = "utopia.homework.hbase.queue")

    @Getter
    @AlpsQueueProducer(config = "Deadpool", queue = "utopia.homework.hbase.queue.homeworkResult", encodeMode = MessageEncodeMode.PLAIN_TXT)
    private MessageProducer homeworkResultHbaseProducer;

    @Getter
    @AlpsQueueProducer(config = "Deadpool", queue = "utopia.homework.hbase.queue.homeworkResultAnswer", encodeMode = MessageEncodeMode.PLAIN_TXT)
    private MessageProducer homeworkResultAnswerHbaseProducer;


    @Getter
    @AlpsQueueProducer(config = "Deadpool", queue = "utopia.homework.hbase.queue.homeworkProcessResult", encodeMode = MessageEncodeMode.PLAIN_TXT)
    private MessageProducer homeworkProcessResultHbaseProducer;

}
