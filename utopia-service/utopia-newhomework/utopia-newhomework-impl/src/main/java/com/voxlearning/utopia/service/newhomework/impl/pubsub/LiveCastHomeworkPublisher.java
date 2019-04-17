package com.voxlearning.utopia.service.newhomework.impl.pubsub;

import com.voxlearning.alps.spi.pubsub.AlpsPubsubPublisher;
import com.voxlearning.alps.spi.pubsub.MessagePublisher;
import lombok.Getter;

import javax.inject.Named;

/**
 * @author guoqiang.li
 * @since 2017/8/9
 */
@Named
@Getter
public class LiveCastHomeworkPublisher {
    @AlpsPubsubPublisher(topic = "utopia.17xue.homework.teacher.topic")
    private MessagePublisher teacherPublisher;
}
