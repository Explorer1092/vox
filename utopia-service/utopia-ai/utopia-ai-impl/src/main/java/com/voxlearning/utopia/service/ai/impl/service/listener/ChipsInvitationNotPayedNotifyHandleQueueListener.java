package com.voxlearning.utopia.service.ai.impl.service.listener;

import com.voxlearning.alps.core.util.StringUtils;
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
                @QueueDestination(system = QueueSystem.KFK, config = "primary", queue = "utopia.chips.invitation.not.payed.notify.message.queue"),
                @QueueDestination(system = QueueSystem.KFK, config = "main-backup", queue = "utopia.chips.invitation.not.payed.notify.message.queue")
        },
        maxPermits = 4
)
public class ChipsInvitationNotPayedNotifyHandleQueueListener implements MessageListener {
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
            logger.error("ChipsInvitationNotPayedNotifyHandleQueueListener no message");
            return;
        }
        Object body = message.decodeBody();

        if (body != null && body instanceof String) {
            String json = (String) body;
            Map<String, Object> param = JsonUtils.fromJson(json);
            if (param == null) {
                logger.error("ChipsInvitationNotPayedNotifyHandleQueueListener error. message:{}", body);
                return;
            }

            long userId = SafeConverter.toLong(param.get("U"));
            String myName = chipsWechatUserPersistence.loadByUserId(userId).stream().filter(e -> StringUtils.isNotBlank(e.getNickName())).findFirst().map(ChipsWechatUserEntity::getNickName).orElse("**");
            long invitee = SafeConverter.toLong(param.get("T"));
            String nickName = chipsWechatUserPersistence.loadByUserId(invitee).stream().filter(e -> StringUtils.isNotBlank(e.getNickName())).findFirst().map(ChipsWechatUserEntity::getNickName).orElse("**");

            Map<String, WechatTemplateData> templateDataMap = new HashMap<>();
            templateDataMap.put("first", new WechatTemplateData(myName + ",您的好友已经参加薯条英语邀请活动下单成功，还未支付哦", "#FF6551"));
            templateDataMap.put("keyword1", new WechatTemplateData("薯条英语体验课", null));
            templateDataMap.put("keyword2", new WechatTemplateData(nickName, null));
            templateDataMap.put("remark", new WechatTemplateData( "点击查看详情", "#FF6551"));

            String productId = Optional.ofNullable(chipsContentService.loadActivityConfig("invite")).map(e -> e.get("productId")).map(SafeConverter::toString).orElse("");
            Map<String, Object> map = new HashMap<>();
            map.put("url", WechatConfig.getBaseSiteUrl() + "/chips/center/invite_personal_center.vpage?activity=" + productId);
            try {
                wechatServiceClient.getWechatService()
                        .processWechatTemplateMessageNotice(userId, WechatTemplateMessageType.CHIPS_INVITATION_COM_FAIL_NOTIFY.name(), templateDataMap, map);
            } catch (Exception e) {
                logger.warn("send WechatTemplateMessage error. user:{}, dataMap:{} ", userId, templateDataMap, e);
            }
        }
    }
}
