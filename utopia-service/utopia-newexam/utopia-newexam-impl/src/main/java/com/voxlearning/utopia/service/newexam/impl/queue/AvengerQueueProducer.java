package com.voxlearning.utopia.service.newexam.impl.queue;

import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.MessageEncodeMode;
import com.voxlearning.alps.spi.queue.MessageProducer;
import lombok.Getter;

import javax.inject.Named;

/**
 * @Description: 单元检测数据上报
 * @author: Mr_VanGogh
 * @date: 2019/4/10 下午4:21
 */
@Named("com.voxlearning.utopia.service.newexam.impl.queue.AvengerQueueProducer")
public class AvengerQueueProducer {

    /**
     * 布置考试上报
     */
    @Getter
    @AlpsQueueProducer(config = "Deadpool", queue = "utopia.avenger.homework", encodeMode = MessageEncodeMode.PLAIN_TXT)
    private MessageProducer newExamProducer;

    /**
     * 这个好像没有用到？？？
     * 做题明细上报
     */
    @Getter
    @AlpsQueueProducer(config = "Deadpool", queue = "utopia.avenger.homeworkprocessresult", encodeMode = MessageEncodeMode.PLAIN_TXT)
    private MessageProducer processResultProducer;

    /**
     * 完成作业上报
     */
    @Getter
    @AlpsQueueProducer(config = "Deadpool", queue = "utopia.avenger.homework.student.finish", encodeMode = MessageEncodeMode.PLAIN_TXT)
    private MessageProducer studentNewExamFinishProducer;

    /**
     * 各种做题数据上报，从aurora改过来的
     */
    @Getter
    @AlpsQueueProducer(config = "Deadpool", queue = "utopia.avenger.bigdata.processresult", encodeMode = MessageEncodeMode.PLAIN_TXT)
    private MessageProducer journalNewExamProcessResultProducer;
}
