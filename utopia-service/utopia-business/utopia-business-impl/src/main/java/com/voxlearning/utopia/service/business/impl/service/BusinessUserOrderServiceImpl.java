package com.voxlearning.utopia.service.business.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.util.CacheKeyGenerator;
import com.voxlearning.utopia.business.api.BusinessUserOrderService;
import com.voxlearning.utopia.entity.payment.PaymentCallbackContext;
import com.voxlearning.utopia.entity.payment.PaymentVerifiedData;
import com.voxlearning.utopia.payment.PaymentGateway;
import com.voxlearning.utopia.service.business.impl.support.BusinessCacheSystem;
import com.voxlearning.utopia.service.business.impl.support.BusinessServiceSpringBean;
import com.voxlearning.utopia.service.business.impl.support.order.userOrder.UserOrderFilterChain;
import com.voxlearning.utopia.service.business.impl.support.order.userOrder.UserOrderFilterContext;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.mizar.consumer.service.OfficialAccountsServiceClient;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.client.UserSmsServiceClient;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.vendor.api.constant.ParentAppPushType;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageShareType;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageTag;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageType;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Summer on 2016/12/9.
 */

@Named
@Service(interfaceClass = BusinessUserOrderService.class)
@ExposeService(interfaceClass = BusinessUserOrderService.class)
@Slf4j
public class BusinessUserOrderServiceImpl extends BusinessServiceSpringBean implements BusinessUserOrderService {

    @Inject private MessageCommandServiceClient messageCommandServiceClient;
    @Inject private UserSmsServiceClient userSmsServiceClient;

    @Inject private UserOrderLoaderClient userOrderLoaderClient;
    @Inject private BusinessCacheSystem businessCacheSystem;
    @Inject private OfficialAccountsServiceClient officialAccountsServiceClient;

    @Override
    public UserOrder processUserOrderPayment(PaymentCallbackContext context) {
        final PaymentVerifiedData paymentVerifiedData = context.getVerifiedPaymentData();
        final String originalOrderId = paymentVerifiedData.getTradeNumber();
        // 支付的订单包含已经取消的
        final UserOrder userOrder = userOrderLoaderClient.loadUserOrderIncludeCanceled(originalOrderId);
        if (null == userOrder) return null;

        UserOrderFilterContext filterContext = new UserOrderFilterContext();
        filterContext.setOrder(userOrder);
        filterContext.setCallbackContext(context);

        UserOrderFilterChain filterChain = new UserOrderFilterChain(getApplicationContext());
        filterChain.doFilter(filterContext);

        return filterContext.getOrder();
    }

    @Override
    public MapMessage processUserOrderPayment(String userOrderId, BigDecimal payAmount, String externalTradeNumber, String externalUserId){
        UserOrder userOrder = userOrderLoaderClient.loadUserOrderIncludeCanceled(userOrderId);
        if (null == userOrder){
            return MapMessage.errorMessage().setInfo("订单号错误");
        }

        if(payAmount == null || payAmount.compareTo(BigDecimal.ZERO) < 0){
            return MapMessage.errorMessage().setInfo("支付金额错误");
        }

        PaymentCallbackContext context = new PaymentCallbackContext("cardpay", PaymentGateway.CallbackAction_Notify);
        context.setVerifiedPaymentData(new PaymentVerifiedData());
        context.getVerifiedPaymentData().setExternalTradeNumber(externalTradeNumber);
        context.getVerifiedPaymentData().setExternalUserId(externalUserId);
        context.getVerifiedPaymentData().setPayAmount(payAmount);
        context.getVerifiedPaymentData().setTradeNumber(userOrderId);

        UserOrderFilterContext filterContext = new UserOrderFilterContext();
        filterContext.setOrder(userOrder);
        filterContext.setCallbackContext(context);

        UserOrderFilterChain filterChain = new UserOrderFilterChain(getApplicationContext());
        filterChain.doFilter(filterContext);

        return MapMessage.successMessage().setInfo("订单支付成功");
    }

    // 提醒家长订单
    @Override
    public MapMessage remindParentForOrder(UserOrder order, List<Long> parentIdList) {
        if (order == null || CollectionUtils.isEmpty(parentIdList)) {
            return MapMessage.errorMessage("参数错误");
        }
        for (Long parentId : parentIdList) {
            String cacheKey = CacheKeyGenerator.generateCacheKey("AFENTI_ORDER_REMIND_DAY_FP", null, new Object[]{parentId, order.getOrderProductServiceType()});
            String sendStr = businessCacheSystem.CBS.unflushable.load(cacheKey);
            if (StringUtils.isNotBlank(sendStr)) {
                continue;
            }
            // 发送短信
            doSendRemindBySMS(parentId, order);
            // 判断家长是否关注了增值公众号
            if (!officialAccountsServiceClient.isFollow(12L, parentId)) {
                String content = "你的孩子" + order.getUserName() + "想要通过《［" + order.getProductName() + "］》进行自学提升，希望你能帮他开通学习并投来殷切的眼神～【点击为孩子开通】";
                // 发送小铃铛消息
                AppMessage message = new AppMessage();
                message.setUserId(parentId);
                message.setMessageType(ParentMessageType.REMINDER.getType());
                message.setTitle("订单提醒");
                message.setContent(content);
                message.setLinkUrl("/parentMobile/ucenter/orderlistForInterest.vpage?isPaid=0");
                message.setLinkType(1);
                Map<String, Object> extInfo = new HashMap<>();
                extInfo.put("tag", ParentMessageTag.订单.name());
                message.setExtInfo(extInfo);
                messageCommandServiceClient.getMessageCommandService().createAppMessage(message);

                // 发送push
                Map<String, Object> jpushExtInfo = new HashMap<>();
                jpushExtInfo.put("studentId", "");
                jpushExtInfo.put("url", "");
                jpushExtInfo.put("tag", ParentMessageTag.订单.name());
                jpushExtInfo.put("shareType", ParentMessageShareType.NO_SHARE_VIEW.name());
                jpushExtInfo.put("shareContent", "");
                jpushExtInfo.put("shareUrl", "");
                //新的push字段
                jpushExtInfo.put("s", ParentAppPushType.ORDER_CENTER.name());
                appMessageServiceClient.sendAppJpushMessageByIds(content, AppMessageSource.PARENT, Collections.singletonList(parentId), jpushExtInfo);
            }
            // 记录缓存
            businessCacheSystem.CBS.unflushable.set(cacheKey, DateUtils.getCurrentToDayEndSecond(), "sended");
        }
        return MapMessage.successMessage();
    }

    //发送短信通知
    private void doSendRemindBySMS(Long parentId, UserOrder order) {
        UserAuthentication ua = userLoaderClient.loadUserAuthentication(parentId);
        if (ua.isMobileAuthenticated()) {
            String url = "http://www.17zyw.cn/RnUBji";
            if (RuntimeMode.current().ge(Mode.STAGING)) {
                url = "http://www.17zyw.cn/QnU32y";
            }
            String content = "你的孩子" + order.getUserName() + "想要通过［" + order.getProductName() + "］进行自学提升，" +
                    "希望你能帮他开通学习【点链接为孩子开通［" + url + "］】" +
                    "或登录“家长通App”—“我的”—“我的订单”查看";
            userSmsServiceClient.buildSms().to(ua)
                    .content(content)
                    .type(SmsType.STUDENT_REMIND_PARENT_FOR_AFENTI_ORDER)
                    .send();
        }
    }
}
