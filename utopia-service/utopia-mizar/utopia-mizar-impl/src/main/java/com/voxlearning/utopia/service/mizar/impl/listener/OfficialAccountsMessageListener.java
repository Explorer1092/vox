package com.voxlearning.utopia.service.mizar.impl.listener;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.queue.*;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.mizar.api.entity.oa.OfficialAccounts;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageShareType;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageTag;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 *
 * Created by alex on 2017/8/4.
 */
@Named
@QueueConsumer(
        destinations = {
                @QueueDestination(system = QueueSystem.KFK, config = "primary", queue = "utopia.officialaccount.message.queue"),
                @QueueDestination(system = QueueSystem.KFK, config = "main-backup", queue = "utopia.officialaccount.message.queue")
        },
        maxPermits = 8
)
public class OfficialAccountsMessageListener extends SpringContainerSupport implements MessageListener {

    @Inject private MessageCommandServiceClient messageCommandServiceClient;
    @Inject private AppMessageServiceClient appMessageServiceClient;

    @Override
    public void onMessage(Message message) {

        String messageText = message.getBodyAsString();
        if (logger.isDebugEnabled()) {
            logger.debug("Message received: {}", messageText);
        }

        Map<String, Object> map = JsonUtils.fromJson(messageText);
        if (map == null) {
            logger.error("Failed to parse JSON text: {}", messageText);
            return;
        }

        AppMessage userMessage = JsonUtils.fromJson(JsonUtils.toJson(map.get("userMessage")), AppMessage.class);
        if (userMessage == null) {
            logger.error("Unknown official accounts message received: {}", messageText);
            return;
        }

        messageCommandServiceClient.getMessageCommandService().createAppMessage(userMessage);

        boolean sendPush = SafeConverter.toBoolean(map.get("sendPush"));
        if (!sendPush) {
            return;
        }

        Long parentId = userMessage.getUserId();
        OfficialAccounts accounts = JsonUtils.fromJson(JsonUtils.toJson(map.get("officialAccounts")), OfficialAccounts.class);
        String jpushContent = SafeConverter.toString(map.get("jpushContent"));

        Map<String, Object> jpushExtInfo = new HashMap<>();
        jpushExtInfo.put("studentId", "");
        jpushExtInfo.put("url", "");
        jpushExtInfo.put("tag", ParentMessageTag.公众号.name());
        jpushExtInfo.put("shareType", ParentMessageShareType.NO_SHARE_VIEW.name());
        jpushExtInfo.put("shareContent", "");
        jpushExtInfo.put("shareUrl", "");
        jpushExtInfo.put("ext_tab_message_type", accounts.getId());
        jpushExtInfo.put("officialAccountName", accounts.getName());
        jpushExtInfo.put("officialAccountID", accounts.getId());
        jpushExtInfo.put("timestamp", userMessage.getCreateTime());

        appMessageServiceClient.sendAppJpushMessageByIds(jpushContent, AppMessageSource.PARENT, Collections.singletonList(parentId), jpushExtInfo);
    }
}