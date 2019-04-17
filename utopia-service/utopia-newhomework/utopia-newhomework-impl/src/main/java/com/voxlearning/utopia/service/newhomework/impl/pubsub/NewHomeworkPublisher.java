package com.voxlearning.utopia.service.newhomework.impl.pubsub;

import com.voxlearning.alps.spi.pubsub.AlpsPubsubPublisher;
import com.voxlearning.alps.spi.pubsub.MessagePublisher;
import com.voxlearning.alps.spi.queue.MessageEncodeMode;
import lombok.Getter;

import javax.inject.Named;

/**
 * @author guoqiang.li
 * @since 2017/8/9
 */
@Named
@Getter
public class NewHomeworkPublisher {
    @AlpsPubsubPublisher(topic = "utopia.homework.teacher.topic")
    private MessagePublisher teacherPublisher;

    @AlpsPubsubPublisher(topic = "utopia.homework.student.topic", messageEncodeMode = MessageEncodeMode.PLAIN_TXT)
    private MessagePublisher studentPublisher;

    @AlpsPubsubPublisher(topic = "utopia.homework.parent.topic")
    private MessagePublisher parentPublisher;

    @AlpsPubsubPublisher(topic = "utopia.vacation.homework.student.topic")
    private MessagePublisher studentVacationPublisher;

    @AlpsPubsubPublisher(topic = "utopia.homework.parent.reward.topic")
    private MessagePublisher parentRewardPublisher;
}
