package com.voxlearning.utopia.service.newhomework.impl.pubsub;

import com.voxlearning.alps.spi.pubsub.AlpsPubsubPublisher;
import com.voxlearning.alps.spi.pubsub.MessagePublisher;
import lombok.Getter;

import javax.inject.Named;

/**
 * 古诗活动Publisher
 *
 * @author majianxin
 * @date 2019/3/7
 */
@Named
@Getter
public class AncientPoetryActivityPublisher {

    /**
     * 老师报名古诗活动
     */
    @AlpsPubsubPublisher(topic = "utopia.poetry.assign.topic")
    private MessagePublisher poetryAssignPublisher;

}
