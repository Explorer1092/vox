package com.voxlearning.utopia.service.parent.homework.impl.template.activity.ocrMAv20190304.dnf;

import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.spi.pubsub.PubsubDestination;
import com.voxlearning.alps.spi.pubsub.PubsubSubscriber;
import com.voxlearning.alps.spi.pubsub.PubsubSystem;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageListener;
import com.voxlearning.utopia.core.utils.LoggerUtils;
import com.voxlearning.utopia.core.utils.ObjectUtils;
import com.voxlearning.utopia.service.parent.homework.impl.template.base.ActivityContext;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

import static com.voxlearning.utopia.service.parent.homework.impl.util.Constants.ACTIVITY_ID;

/**
 * 未完成活动提醒
 *
 * @author Wenlong Meng
 * @since Feb 25, 2019
 */
@Named("activity.ocrMAv20190304.dnf.OcrMAv20190304Listener")
@PubsubSubscriber(
        destinations = {
                @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = "utopia.parent.platform.homework.activity.dnf"),
                @PubsubDestination(system = PubsubSystem.KFK, connection = "main-backup", topic = "utopia.parent.platform.homework.activity.dnf")
        }
)
public class DNFOcrMAv20190304Listener implements MessageListener {
    //Local variables
    @Inject private DNFOcrMAv20190304Template ocrMAV20190304Template;

    //Logic
    /**
     * 活动二维码识别
     *
     * @param message
     */
    @Override
    public void onMessage(Message message) {
        String messageBody = message.getBodyAsString();
        Map<String, Object> messageMap = JsonUtils.fromJson(messageBody);
        LoggerUtils.debug("dnf.OcrMAv20190304Listener.message", messageBody);

        //构建参数
        String activityId = ObjectUtils.get(()->(String)messageMap.get("activityId"), ACTIVITY_ID);
        ActivityContext c = new ActivityContext();
        c.setActivityId(activityId);
        c.setExtInfos(messageMap);

        //执行
        ocrMAV20190304Template.process(c);
        LoggerUtils.debug("dnf.OcrMAv20190304Listener.result", c.getMapMessage());
    }

}
