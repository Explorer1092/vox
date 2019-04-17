package com.voxlearning.utopia.service.ai.internal;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishProductTimetable;
import com.voxlearning.utopia.service.ai.impl.persistence.ChipsEnglishProductTimetableDao;
import com.voxlearning.utopia.service.ai.impl.support.WechatConfig;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.email.api.constants.EmailTemplate;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.order.api.entity.UserOrderProductRef;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.wechat.api.constants.WechatTemplateMessageType;
import com.voxlearning.utopia.service.wechat.api.data.WechatTemplateData;
import com.voxlearning.utopia.service.wechat.client.WechatServiceClient;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Named
public class ChipsMessageService {

    @Inject
    private EmailServiceClient emailServiceClient;

    @Inject
    private CommonConfigServiceClient commonConfigServiceClient;

    @Inject
    private UserOrderLoaderClient userOrderLoaderClient;

    @Inject
    private WechatServiceClient wechatServiceClient;

    @Inject
    private ChipsEnglishProductTimetableDao chipsEnglishProductTimetableDao;

    private static final Logger logger = LoggerFactory.getLogger(ChipsMessageService.class);

    private static String CHIPS_ORDER_REMIND_EMAIL = "CHIPS_ORDER_REMIND_EMAIL";

    public void notifyNoClass(String productName, Long userId) {
        if (RuntimeMode.isProduction()) {
            // 发邮件提醒
            Map<String, Object> content = new HashMap<>();
            content.put("info", "用户ID:" + userId
                    + " 产品名称:" + productName);
            String emailList = commonConfigServiceClient.getCommonConfigBuffer()
                    .loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.getType(), CHIPS_ORDER_REMIND_EMAIL);
            if (StringUtils.isNotBlank(emailList)) {
                emailServiceClient.createTemplateEmail(EmailTemplate.office)
                        .to(emailList)
                        .subject("【薯条英语】产品班级配置为空提醒")
                        .content(content)
                        .send();
            }
        }
    }

    public void notifyUserBuyInfoEmail(Map<String, Object> map) {
        try {
            // 发邮件提醒
            Map<String, Object> content = new HashMap<>();
            content.put("info", "用户ID:" + SafeConverter.toString(map.get("userId"))
                    + " 产品名称:" + SafeConverter.toString(map.get("productName"))
                    + " 订单号:" + SafeConverter.toString(map.get("orderId"))
                    + " 金额:" + SafeConverter.toString(map.get("payAmount"))
                    + " 购买时间:" + DateUtils.dateToString(new Date(SafeConverter.toLong(map.get("payTime")))));
            String emailList = commonConfigServiceClient.getCommonConfigBuffer()
                    .loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.getType(), CHIPS_ORDER_REMIND_EMAIL);
            if (StringUtils.isNotBlank(emailList)) {
                emailServiceClient.createTemplateEmail(EmailTemplate.office)
                        .to(emailList)
                        .subject("【薯条英语】购买成功提醒")
                        .content(content)
                        .send();
            }
        } catch (Exception e) {

        }
    }

    public void notifyUserGroupShoppingSuccess(UserOrder userOrder) {
        try {
            if (userOrder == null) {
                logger.error("send wechat template message order not exist error. order:{}", userOrder);
                return;
            }

            Map<String, WechatTemplateData> templateDataMap = new HashMap<>();
            String firstData = "太棒了，恭喜您拼团成功！";
            templateDataMap.put("first", new WechatTemplateData(firstData, "#FF6551"));
            templateDataMap.put("keyword1", new WechatTemplateData(userOrder.getProductName(), null));

            List<UserOrderProductRef> orderProductRefList = userOrderLoaderClient.loadOrderProducts(userOrder.getUserId(), userOrder.getId());
            Date beginDate;
            if (CollectionUtils.isEmpty(orderProductRefList)) {
                beginDate = Optional.ofNullable(chipsEnglishProductTimetableDao.load(userOrder.getProductId()))
                        .map(ChipsEnglishProductTimetable::getBeginDate)
                        .orElse(DateUtils.calculateDateDay(new Date(), 10));
            } else {
                Set<String> productIds = orderProductRefList.stream().map(UserOrderProductRef::getProductId).collect(Collectors.toSet());
                beginDate = chipsEnglishProductTimetableDao.loads(productIds).values().stream()
                        .sorted(Comparator.comparing(ChipsEnglishProductTimetable::getBeginDate))
                        .findFirst()
                        .map(ChipsEnglishProductTimetable::getBeginDate)
                        .orElse(DateUtils.calculateDateDay(new Date(), 10));
            }

            templateDataMap.put("keyword2", new WechatTemplateData(DateUtils.dateToString(beginDate, DateUtils.FORMAT_SQL_DATE), null));
            templateDataMap.put("remark", new WechatTemplateData("\n\r点击这里，添加老师微信二维码", "#FF6551"));
            Map<String, Object> map = new HashMap<>();
            map.put("url", WechatConfig.getBaseSiteUrl() + "/chips/order/" + userOrder.genUserOrderId() + "/paymentsuccess.vpage");
            wechatServiceClient.getWechatService().processWechatTemplateMessageNotice(userOrder.getUserId(), WechatTemplateMessageType.CHIPS_GROUP_SHOPPING_SUCCESS.name(), templateDataMap, map);
        } catch (Exception e) {
            logger.error("send wechat template message error. order:{}", userOrder, e);
        }
    }
}
