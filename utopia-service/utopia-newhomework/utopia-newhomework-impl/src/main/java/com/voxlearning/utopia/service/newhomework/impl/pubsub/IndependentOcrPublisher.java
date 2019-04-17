package com.voxlearning.utopia.service.newhomework.impl.pubsub;

import com.voxlearning.alps.spi.pubsub.AlpsPubsubPublisher;
import com.voxlearning.alps.spi.pubsub.MessagePublisher;
import com.voxlearning.alps.spi.queue.MessageEncodeMode;
import lombok.Getter;

import javax.inject.Named;

/**
 * 独立拍照Publisher
 *
 * @author majianxin
 * @date 2019/4/3
 */
@Named
@Getter
public class IndependentOcrPublisher {

    /**
     * 老师报名古诗活动
     */
    @AlpsPubsubPublisher(topic = "utopia.independent.ocr.delete.topic", messageEncodeMode = MessageEncodeMode.PLAIN_TXT)
    private MessagePublisher independentOcrPublisher;

}
