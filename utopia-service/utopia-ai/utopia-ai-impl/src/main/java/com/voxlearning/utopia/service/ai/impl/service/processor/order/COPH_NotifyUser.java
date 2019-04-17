package com.voxlearning.utopia.service.ai.impl.service.processor.order;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishProductTimetable;
import com.voxlearning.utopia.service.ai.entity.ChipsWechatUserEntity;
import com.voxlearning.utopia.service.ai.impl.AbstractAiSupport;
import com.voxlearning.utopia.service.ai.impl.context.ChipsOrderPostContext;
import com.voxlearning.utopia.service.ai.impl.persistence.ChipsWechatUserPersistence;
import com.voxlearning.utopia.service.ai.impl.service.processor.IAITask;
import com.voxlearning.utopia.service.ai.impl.support.WechatConfig;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.sms.consumer.SmsServiceClient;
import com.voxlearning.utopia.service.user.api.entities.ParentExtAttribute;
import com.voxlearning.utopia.service.user.consumer.ParentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.SensitiveUserDataServiceClient;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;
import com.voxlearning.utopia.service.wechat.api.constants.WechatTemplateMessageType;
import com.voxlearning.utopia.service.wechat.api.data.WechatTemplateData;
import com.voxlearning.utopia.service.wechat.client.WechatServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

@Named
public class COPH_NotifyUser extends AbstractAiSupport implements IAITask<ChipsOrderPostContext> {

    @Inject
    private AppMessageServiceClient appMessageClient;

    @Inject
    private SmsServiceClient smsServiceClient;

    @Inject
    private SensitiveUserDataServiceClient sensitiveUserDataServiceClient;

    @Inject
    private WechatServiceClient wechatServiceClient;

    @Inject
    private ParentLoaderClient parentLoaderClient;

    @Inject
    private ChipsWechatUserPersistence chipsWechatUserPersistence;
    @Override
    public void execute(ChipsOrderPostContext context) {
        //push和短信提醒
        processPushAndSmsNotify(context.getUserId());
        String userWechatName = Optional.ofNullable(context.getUserId())
                .map(user -> {
                    ChipsWechatUserEntity wechatUserEntity = chipsWechatUserPersistence.loadByUserId(user).stream()
                            .filter(e -> StringUtils.isNotBlank(e.getNickName()))
                            .findFirst().orElse(null);
                    if (wechatUserEntity != null) {
                        return wechatUserEntity.getNickName();
                    }
                    ParentExtAttribute parentExtAttribute = parentLoaderClient.loadParentExtAttribute(context.getUserId());
                    if (parentExtAttribute != null) {
                        return parentExtAttribute.getWechatNick();
                    }
                    return "";
                })
                .filter(StringUtils::isNotBlank)
                .orElse("家长");

        //微信模板消息提醒
        processWechatNotify(SafeConverter.toString(context.getParam().get("productName")), context.getUserOrder().getProductId(), userWechatName, context.getUserId());
        context.setUserWechatName(userWechatName);
    }

    private void processPushAndSmsNotify(Long userId) {
        //发送push
        String content = "【重要提示】报名成功记得添加老师微信（薯条英语），添加微信时，请备注您的家长通手机号";
        try {
            appMessageClient.sendAppJpushMessageByIds(content, AppMessageSource.PARENT, Arrays.asList(userId), Collections.emptyMap(), 0L);
        } catch (Exception e) {
            logger.error("send app push error.", e);
        }

        // 发短信
        try {
            String am = sensitiveUserDataServiceClient.loadUserMobile(userId);
            if (StringUtils.isNoneBlank(am)) {
                smsServiceClient.createSmsMessage(am).content(content).type(SmsType.PAY_SUCCESS_SMS_NOTIFY.name()).send();
            }
        } catch (Exception e) {
            logger.error("send sms error.", e);
        }

    }

    private void processWechatNotify(String orderProductName, String orderProductId, String userWechatName, Long userId) {
        //发送购买通知微信模板
        try {
            Map<String, WechatTemplateData> templateDataMap = new HashMap<>();
            String firstData = userWechatName + "，恭喜您报名成功！还差一步即可完成报名，赶快添加老师微信吧。\n\r";
            templateDataMap.put("first", new WechatTemplateData(firstData, "#FF6551"));
            templateDataMap.put("keyword1", new WechatTemplateData(orderProductName, null));
            Date beginDate = Optional.ofNullable(chipsEnglishProductTimetableDao.load(orderProductId)).map(ChipsEnglishProductTimetable::getBeginDate).orElse(DateUtils.calculateDateDay(new Date(), 10));
            templateDataMap.put("keyword2", new WechatTemplateData(DateUtils.dateToString(beginDate, DateUtils.FORMAT_SQL_DATE), null));
            templateDataMap.put("remark", new WechatTemplateData("\n\r点击这里，添加老师微信二维码", "#FF6551"));
            Map<String, Object> map = new HashMap<>();
            map.put("url", WechatConfig.getBaseSiteUrl() + "/chips/center/myteacherV2.vpage?product=" + orderProductId);
            wechatServiceClient.getWechatService().processWechatTemplateMessageNotice(userId, WechatTemplateMessageType.CHIPS_BUY_SUCCESS.name(), templateDataMap, map);
        } catch (Exception e) {
            logger.error("send wechat template message error. productId:{}, userId:{}", orderProductId, userId, e);
        }
    }

}
