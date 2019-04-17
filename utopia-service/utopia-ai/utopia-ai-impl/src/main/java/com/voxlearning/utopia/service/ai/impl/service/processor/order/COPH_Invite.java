package com.voxlearning.utopia.service.ai.impl.service.processor.order;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.ai.cache.manager.ChipsInvitionRankCacheManager;
import com.voxlearning.utopia.service.ai.entity.ChipsUserOrderExt;
import com.voxlearning.utopia.service.ai.entity.ChipsWechatUserEntity;
import com.voxlearning.utopia.service.ai.impl.AbstractAiSupport;
import com.voxlearning.utopia.service.ai.impl.context.ChipsOrderPostContext;
import com.voxlearning.utopia.service.ai.impl.persistence.ChipsActivityInvitationPersistence;
import com.voxlearning.utopia.service.ai.impl.persistence.ChipsWechatUserPersistence;
import com.voxlearning.utopia.service.ai.impl.service.processor.IAITask;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.user.api.entities.ParentExtAttribute;
import com.voxlearning.utopia.service.user.consumer.ParentLoaderClient;
import com.voxlearning.utopia.service.wechat.api.constants.WechatTemplateMessageType;
import com.voxlearning.utopia.service.wechat.api.data.WechatTemplateData;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Named
public class COPH_Invite extends AbstractAiSupport implements IAITask<ChipsOrderPostContext> {

    @Inject
    private ParentLoaderClient parentLoaderClient;

    @Inject
    private ChipsWechatUserPersistence chipsWechatUserPersistence;

    @Inject
    private ChipsActivityInvitationPersistence activityInvitationPersistence;

    @Inject
    private ChipsInvitionRankCacheManager chipsInvitionRankCacheManager;

    @Override
    public void execute(ChipsOrderPostContext context) {
        ChipsUserOrderExt chipsUserOrderExt = context.getOrderExt();
        if (chipsUserOrderExt == null) {
            return;
        }
        //处理邀请
        Long inviter = chipsUserOrderExt.getInviter();
        if (inviter == null || Long.compare(inviter, 0L) <= 0) {
            return;
        }

        OrderProduct orderProduct = userOrderLoaderClient.loadOrderProductById(context.getUserOrder().getProductId());
        if (orderProduct == null) {
            return;
        }

        processUserInvition(inviter, chipsUserOrderExt.getUserId(), orderProduct, context.getUserWechatName());
    }

    private void processUserInvition(Long inviter, Long invitee, OrderProduct orderProduct, String userWechatName) {
        chipsActivityInvitationPersistence.inserOrUpdate(inviter, invitee, orderProduct.getId(), 2);
        Map<String, Object> map = chipsContentService.loadActivityConfig("invite");
        String productId = Optional.ofNullable(map)
                .map(e -> map.get("productId"))
                .map(SafeConverter::toString)
                .orElse("");
        chipsInvitionRankCacheManager.updateRank(productId, inviter, 1);
        //发送邀请成功微信模板消息
        try {
            String inviterWechatName = Optional.ofNullable(inviter)
                    .map(user -> {
                        ChipsWechatUserEntity userEntity = chipsWechatUserPersistence.loadByUserId(user).stream().findFirst().orElse(null);
                        if (userEntity != null) {
                            return userEntity.getNickName();
                        }
                        ParentExtAttribute inviterExt = parentLoaderClient.loadParentExtAttribute(user);
                        if (inviterExt != null) {
                            return inviterExt.getWechatNick();
                        }
                        return "";
                    })
                    .filter(StringUtils::isNotBlank)
                    .orElse("**");

            Map<String, WechatTemplateData> templateDataMap = new HashMap<>();
            String title = inviterWechatName + "，有朋友通过你的邀请成功报名" + orderProduct.getName() + "\n\r";
            templateDataMap.put("first", new WechatTemplateData(title, "#FF6551"));
            templateDataMap.put("keyword1", new WechatTemplateData(userWechatName, null));
            templateDataMap.put("keyword2", new WechatTemplateData(DateUtils.dateToString(new Date()), null));
            templateDataMap.put("remark", new WechatTemplateData("\n\r→点击继续邀请，专属大奖拿不停~", "#FF6551"));
            wechatServiceClient.getWechatService().processWechatTemplateMessageNotice(inviter, WechatTemplateMessageType.CHIPS_INVITATION_SUCCESS.name(), templateDataMap, null);

            int count = activityInvitationPersistence.loadByActivityTypeAndInviter(orderProduct.getId(), inviter).stream().filter(e -> e.getStatus() == 2).collect(Collectors.toList()).size();
            BigDecimal total = new BigDecimal(2.97).multiply(new BigDecimal(count));
            Map<String, WechatTemplateData> templateDataMap2 = new HashMap<>();
            templateDataMap2.put("first", new WechatTemplateData("你有新的分红到账，可在菜单栏“我的收入”查看分红", "#FF6551"));
            templateDataMap2.put("keyword1", new WechatTemplateData("2.97", null));
            templateDataMap2.put("keyword2", new WechatTemplateData(total.setScale(2, RoundingMode.DOWN).toString(), null));
            templateDataMap2.put("remark", new WechatTemplateData("\n参与薯条英语【邀请赢取重磅好礼】活动，还可以免费领取《薯条英语礼盒套装》图书哦～\n点击查看活动详情", "#FF6551"));
            wechatServiceClient.getWechatService().processWechatTemplateMessageNotice(inviter, WechatTemplateMessageType.CHIPS_INVITATION_ORDER_NOTIFY.name(), templateDataMap2, null);
        } catch (Exception e) {
            logger.error("send wechat template message error.", e);
        }
    }
}
