package com.voxlearning.utopia.service.vendor.impl.listener;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.pubsub.PubsubDestination;
import com.voxlearning.alps.spi.pubsub.PubsubSubscriber;
import com.voxlearning.alps.spi.pubsub.PubsubSystem;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageListener;
import org.slf4j.Logger;

import javax.inject.Named;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jiangpeng
 * @since 2017-12-04 下午4:27
 **/
@Named
@PubsubSubscriber(destinations = {
        @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = "utopia.afenti.review.finish.rank.topic"),
        @PubsubDestination(system = PubsubSystem.KFK, connection = "main-backup", topic = "utopia.afenti.review.finish.rank.topic")
})
public class AfentiReviewFinishRankSubscriberListner implements MessageListener {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void onMessage(Message message) {
        Map<String, Object> msgMap = null;
        Object object = message.decodeBody();
        if (!RuntimeMode.isProduction()){
            logger.info("just recieve msg : "+ JsonUtils.toJson(object));
        }
        if (object instanceof HashMap) {
            msgMap = HashMap.class.cast(object);
        }
        if (msgMap == null) {
            return;
        }

        long studentId = SafeConverter.toLong(msgMap.get("userId"));
        if (studentId == 0)
            return;
        long finishTime = SafeConverter.toLong(msgMap.get("finishTime"));
        Date finishDate = new Date(finishTime);
    }
}
