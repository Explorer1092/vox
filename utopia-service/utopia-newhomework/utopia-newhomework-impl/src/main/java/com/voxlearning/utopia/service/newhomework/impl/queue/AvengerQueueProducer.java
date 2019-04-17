package com.voxlearning.utopia.service.newhomework.impl.queue;

import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.MessageEncodeMode;
import com.voxlearning.alps.spi.queue.MessageProducer;
import lombok.Getter;

import javax.inject.Named;

/**
 * @author xuesong.zhang
 * @since 2017/6/15
 */
@Named("com.voxlearning.utopia.service.newhomework.impl.queue.AvengerQueueProducer")
public class AvengerQueueProducer {

    /**
     * 做题明细上报
     */
    @Getter
    @AlpsQueueProducer(config = "Deadpool", queue = "utopia.avenger.homeworkprocessresult", encodeMode = MessageEncodeMode.PLAIN_TXT)
    private MessageProducer processResultProducer;

    /**
     * 布置作业上报
     */
    @Getter
    @AlpsQueueProducer(config = "Deadpool", queue = "utopia.avenger.homework", encodeMode = MessageEncodeMode.PLAIN_TXT)
    private MessageProducer homeworkProducer;

    /**
     * 完成作业上报
     */
    @Getter
    @AlpsQueueProducer(config = "Deadpool", queue = "utopia.avenger.homework.student.finish", encodeMode = MessageEncodeMode.PLAIN_TXT)
    private MessageProducer studentHomeworkFinishProducer;

    /**
     * 各种做题数据上报，从aurora改过来的
     */
    @Getter
    @AlpsQueueProducer(config = "Deadpool", queue = "utopia.avenger.bigdata.processresult", encodeMode = MessageEncodeMode.PLAIN_TXT)
    private MessageProducer journalHomeworkProcessResultProducer;
}
