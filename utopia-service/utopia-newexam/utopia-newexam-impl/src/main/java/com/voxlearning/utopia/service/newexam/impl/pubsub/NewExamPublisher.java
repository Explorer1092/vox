package com.voxlearning.utopia.service.newexam.impl.pubsub;

import com.voxlearning.alps.spi.pubsub.AlpsPubsubPublisher;
import com.voxlearning.alps.spi.pubsub.MessagePublisher;
import com.voxlearning.alps.spi.queue.MessageEncodeMode;
import lombok.Getter;

import javax.inject.Named;

/**
 * @author guohong.tan
 * @since 2019/2/21
 */
@Named
@Getter
public class NewExamPublisher {
    @AlpsPubsubPublisher(topic = "utopia.newexam.teacher.topic", messageEncodeMode = MessageEncodeMode.PLAIN_TXT)
    private MessagePublisher teacherPublisher;

    @AlpsPubsubPublisher(topic = "utopia.newexam.student.topic", messageEncodeMode = MessageEncodeMode.PLAIN_TXT)
    private MessagePublisher studentPublisher;

}
