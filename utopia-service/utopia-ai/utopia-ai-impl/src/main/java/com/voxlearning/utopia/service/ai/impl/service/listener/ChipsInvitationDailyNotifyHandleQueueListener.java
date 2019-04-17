package com.voxlearning.utopia.service.ai.impl.service.listener;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.spi.queue.*;
import com.voxlearning.utopia.service.ai.internal.ChipsContentService;
import com.voxlearning.utopia.service.wechat.api.constants.WechatTemplateMessageType;
import com.voxlearning.utopia.service.wechat.api.data.WechatTemplateData;
import com.voxlearning.utopia.service.wechat.client.WechatServiceClient;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@Named
@QueueConsumer(
        destinations = {
                @QueueDestination(system = QueueSystem.KFK, config = "primary", queue = "utopia.chips.invitation.notify.message.queue"),
                @QueueDestination(system = QueueSystem.KFK, config = "main-backup", queue = "utopia.chips.invitation.notify.message.queue")
        },
        maxPermits = 4
)
public class ChipsInvitationDailyNotifyHandleQueueListener implements MessageListener {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Inject
    private WechatServiceClient wechatServiceClient;

    @Inject
    private ChipsContentService chipsContentService;

    @Override
    public void onMessage(Message message) {
        if (message == null) {
            logger.error("ChipsInvitationDailyNotifyHandleQueueListener no message");
            return;
        }
        Object body = message.decodeBody();

        if (body != null && body instanceof String) {
            String json = (String) body;
            Map<String, Object> param = JsonUtils.fromJson(json);
            if (param == null) {
                logger.error("ChipsInvitationDailyNotifyHandleQueueListener error. message:{}", body);
                return;
            }

            long userId = SafeConverter.toLong(param.get("U"));

            Map<String, Object> map = chipsContentService.loadActivityConfig("invite");

            String beginDate = Optional.ofNullable(map)
                    .map(ma -> ma.get("acBeginDate"))
                    .map(SafeConverter::toString)
                    .map(DateUtils::stringToDate)
                    .map(e -> DateUtils.dateToString(e, "MM月dd日"))
                    .orElse("");
            String endDate = Optional.ofNullable(map)
                    .map(ma -> ma.get("acEndDate"))
                    .map(SafeConverter::toString)
                    .map(DateUtils::stringToDate)
                    .map(e -> DateUtils.dateToString(e, "MM月dd日"))
                    .orElse("");
            Map<String, WechatTemplateData> templateDataMap = new HashMap<>();
            templateDataMap.put("first", new WechatTemplateData("每邀请一人返30%\n\r", "#FF6551"));
            templateDataMap.put("keyword1", new WechatTemplateData("薯条英语邀请返利活动", null));
            templateDataMap.put("keyword2", new WechatTemplateData(beginDate + "-" + endDate, null));
            templateDataMap.put("remark", new WechatTemplateData("\n\r邀请人数达到10人，免费领取《薯条英语礼盒套装》，价值299元哦～", "#FF6551"));
            try {
                wechatServiceClient.getWechatService()
                        .processWechatTemplateMessageNotice(userId, WechatTemplateMessageType.CHIPS_INVITATION_BEGIN_NOTIFY.name(), templateDataMap, Collections.emptyMap());
            } catch (Exception e) {
                logger.warn("send WechatTemplateMessage error. user:{}, dataMap:{} ", userId, templateDataMap, e);
            }
        }
    }
}
