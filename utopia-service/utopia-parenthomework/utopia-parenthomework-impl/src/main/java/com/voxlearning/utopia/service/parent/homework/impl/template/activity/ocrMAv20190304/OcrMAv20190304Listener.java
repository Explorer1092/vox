package com.voxlearning.utopia.service.parent.homework.impl.template.activity.ocrMAv20190304;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.spi.pubsub.PubsubDestination;
import com.voxlearning.alps.spi.pubsub.PubsubSubscriber;
import com.voxlearning.alps.spi.pubsub.PubsubSystem;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageListener;
import com.voxlearning.utopia.core.utils.LoggerUtils;
import com.voxlearning.utopia.service.parent.homework.impl.template.base.ActivityContext;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

import static com.voxlearning.utopia.service.parent.homework.impl.util.Constants.ACTIVITY_ID;

/**
 * 作业拍照识别监听
 *
 * @author Wenlong Meng
 * @since Feb 25, 2019
 */
@Named
@PubsubSubscriber(
        destinations = {
                @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = "parent.ocr.info.notify"),
                @PubsubDestination(system = PubsubSystem.KFK, connection = "main-backup", topic = "parent.ocr.info.notify")
        }
)
public class OcrMAv20190304Listener implements MessageListener {
    //Local variables
    @Inject private OcrMAv20190304Template ocrMAV20190304Template;

    //Logic
    /**
     * 参见活动二维码识别
     *
     * @param message
     */
    @Override
    public void onMessage(Message message) {
        String messageBody = message.getBodyAsString();
        Map<String, Object> messageMap = JsonUtils.fromJson(messageBody);
        LoggerUtils.debug("HomeworkOcrListener.message", messageBody);

        //构建参数
        Long studentId = SafeConverter.toLong(messageMap.get("studentId"));
        Long parentId = SafeConverter.toLong(messageMap.get("parentId"));
        String url = SafeConverter.toString(messageMap.get("url"));
        Integer xCount = SafeConverter.toInt(messageMap.get("errorCount"));
        Integer vCount = SafeConverter.toInt(messageMap.get("rightCount"));
        ActivityContext c = new ActivityContext();
        c.setActivityId(ACTIVITY_ID);
        c.setStudentId(studentId);
        c.setParentId(parentId);
        c.set("url", url);
        c.set("xCount", xCount);
        c.set("vCount", vCount);

        //执行
        ocrMAV20190304Template.process(c);
        LoggerUtils.debug("OcrMAv20190304Listener.result", c.getMapMessage());
    }

}
