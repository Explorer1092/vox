package com.voxlearning.utopia.service.vendor.impl.listener;

import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.spi.queue.*;
import com.voxlearning.utopia.api.constant.MySelfStudyActionEvent;
import com.voxlearning.utopia.service.vendor.impl.service.MySelfStudyServiceImpl;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * 我的自学动态队列监听器
 *
 * @author jiangpeng
 * @since 2016-10-20 上午11:45
 **/
@Named
@QueueConsumer(
        destinations = {
                @QueueDestination(system = QueueSystem.KFK, config = "primary", queue = "utopia.vendor.myselfstudy.queue"),
                @QueueDestination(system = QueueSystem.KFK, config = "main-backup", queue = "utopia.vendor.myselfstudy.queue")
        },
        maxPermits = 64
)
public class MySelfStudyActionQueueListener implements MessageListener {

    private final MySelfStudyServiceImpl mySelfStudyService;

    @Inject
    public MySelfStudyActionQueueListener(MySelfStudyServiceImpl mySelfStudyService) {
        this.mySelfStudyService = mySelfStudyService;
    }

    @Override
    public void onMessage(Message message) {
        String messageText = message.getBodyAsString();
        MySelfStudyActionEvent event = JsonUtils.fromJson(messageText, MySelfStudyActionEvent.class);
        mySelfStudyService.handleEvent(event);
    }
}
