package com.voxlearning.utopia.service.ai.impl.service.listener;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.spi.queue.*;
import com.voxlearning.utopia.service.ai.entity.ChipsWechatUserEntity;
import com.voxlearning.utopia.service.ai.impl.persistence.ChipsWechatUserPersistence;
import com.voxlearning.utopia.service.ai.impl.support.WechatConfig;
import com.voxlearning.utopia.service.ai.internal.ChipsContentService;
import com.voxlearning.utopia.service.wechat.api.constants.WechatTemplateMessageType;
import com.voxlearning.utopia.service.wechat.api.data.WechatTemplateData;
import com.voxlearning.utopia.service.wechat.client.WechatServiceClient;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@Named
@QueueConsumer(
        destinations = {
                @QueueDestination(system = QueueSystem.KFK, config = "primary", queue = "utopia.chips.invitation.visit.notify.message.queue"),
                @QueueDestination(system = QueueSystem.KFK, config = "main-backup", queue = "utopia.chips.invitation.visit.notify.message.queue")
        },
        maxPermits = 4
)
public class ChipsInvitationVisitDailyNotifyHandleQueueListener implements MessageListener {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Inject
    private WechatServiceClient wechatServiceClient;

    @Inject
    private ChipsContentService chipsContentService;

    @Inject
    private ChipsWechatUserPersistence chipsWechatUserPersistence;

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
            String nickName = chipsWechatUserPersistence.loadByUserId(userId).stream().findFirst().map(ChipsWechatUserEntity::getNickName).orElse("**");

            Map<String, WechatTemplateData> templateDataMap = new HashMap<>();
            templateDataMap.put("first", new WechatTemplateData(nickName + "，有朋友浏览了你的邀请页面\n", "#FF6551"));
            templateDataMap.put("keyword1", new WechatTemplateData("朋友浏览了活动页面", null));
            templateDataMap.put("keyword2", new WechatTemplateData("你参加的薯条英语邀请活动状态更新了，赶紧去看看吧！", null));
            templateDataMap.put("remark", new WechatTemplateData( "\n点击查看详情", "#FF6551"));
            String productId = Optional.ofNullable(chipsContentService.loadActivityConfig("invite")).map(e -> e.get("productId")).map(SafeConverter::toString).orElse("");
            Map<String, Object> map = new HashMap<>();
            map.put("url", WechatConfig.getBaseSiteUrl() + "/chips/center/invite_personal_center.vpage?activity=" + productId);
            try {
                wechatServiceClient.getWechatService()
                        .processWechatTemplateMessageNotice(userId, WechatTemplateMessageType.CHIPS_INVITATION_TEAM_COM_NOTIFY.name(), templateDataMap, map);
            } catch (Exception e) {
                logger.warn("send WechatTemplateMessage error. user:{}, dataMap:{} ", userId, templateDataMap, e);
            }
        }
    }
}
