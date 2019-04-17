package com.voxlearning.utopia.admin.service.legacy;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.admin.auth.AuthCurrentAdminUser;
import com.voxlearning.utopia.admin.service.AbstractAdminService;
import com.voxlearning.utopia.payment.PaymentGateway;
import com.voxlearning.utopia.payment.PaymentGatewayManager;
import com.voxlearning.utopia.payment.RefundRequest;
import com.voxlearning.utopia.service.config.api.CRMConfigService;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.order.api.constants.PaymentStatus;
import com.voxlearning.utopia.service.order.api.constants.RefundHistoryStatus;
import com.voxlearning.utopia.service.order.api.entity.OrderRefundHistory;
import com.voxlearning.utopia.service.order.api.entity.UserOrderPaymentHistory;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.order.consumer.UserOrderServiceClient;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.user.api.entities.StudentParent;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.client.UserSmsServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Summer on 2017/11/2.
 */
@Named
public class CrmRefundService extends AbstractAdminService {
    @Inject UserOrderLoaderClient userOrderLoaderClient;
    @Inject UserOrderServiceClient userOrderServiceClient;
    @Inject private PaymentGatewayManager paymentGatewayManager;
    @Inject private EmailServiceClient emailServiceClient;
    @Inject private UserSmsServiceClient userSmsServiceClient;
    @ImportService(interfaceClass = CRMConfigService.class)
    private CRMConfigService crmConfigService;


    public MapMessage batchRefundUserOrder(AuthCurrentAdminUser currentAdminUser, String ids, String comment) {
        if (StringUtils.isBlank(ids)) {
            return MapMessage.errorMessage("请选择一条记录");
        }
        List<String> refundIds = Arrays.asList(StringUtils.split(ids, ","));
        Map<String, OrderRefundHistory> historyMap = userOrderLoaderClient.loadOrderRefundHistoryByIds(refundIds);
        if (MapUtils.isEmpty(historyMap)) {
            return MapMessage.errorMessage("请选择一条记录");
        }
        OrderRefundHistory successHis = historyMap.values().stream().filter(h -> h.getStatus() == RefundHistoryStatus.SUCCESS).findFirst().orElse(null);
        if (successHis != null) {
            return MapMessage.errorMessage("订单已成功退款，请重新选择");
        }
        // 判断是否是一种方式
        List<OrderRefundHistory> other = new ArrayList<>();
        List<OrderRefundHistory> wechatHis = new ArrayList<>();
        List<OrderRefundHistory> aliHis = new ArrayList<>();
        List<OrderRefundHistory> aliWapHis = new ArrayList<>();
        for (OrderRefundHistory history : historyMap.values()) {
            if (StringUtils.isNotBlank(history.getPayMethod()) && history.getPayMethod().contains("alipay")) {
                if(history.getPayMethod().contains("alipay_wap")){
                    aliWapHis.add(history);
                }else{
                    aliHis.add(history);
                }
            } else if (StringUtils.isNotBlank(history.getPayMethod()) && history.getPayMethod().contains("wechat")) {
                wechatHis.add(history);
            } else {
                other.add(history);
            }
        }
        if (CollectionUtils.isNotEmpty(other)) {
            return MapMessage.errorMessage("不支持的支付方式");
        }
        if (CollectionUtils.isNotEmpty(wechatHis) && CollectionUtils.isNotEmpty(aliHis)) {
            return MapMessage.errorMessage("请选择一种支付方式进行批量退款");
        }
        // 如果是微信  修改状态
        if (CollectionUtils.isNotEmpty(wechatHis)) {
            for (OrderRefundHistory history : wechatHis) {
                history.setUpdateDatetime(new Date());
                history.setAgentUserName(currentAdminUser.getAdminUserName());
                history.setStatus(RefundHistoryStatus.REFUNDING);
                history.setComment(comment);
            }
            userOrderServiceClient.updateRefundHistory(wechatHis);
            return MapMessage.successMessage("提交成功");
        }
        //alipaywap支付采用的接口去退款
        if (CollectionUtils.isNotEmpty(aliWapHis)) {
            for (OrderRefundHistory history : aliWapHis) {
                history.setUpdateDatetime(new Date());
                history.setAgentUserName(currentAdminUser.getAdminUserName());
                history.setStatus(RefundHistoryStatus.REFUNDING);
                history.setComment(comment);
            }
            userOrderServiceClient.updateRefundHistory(aliWapHis);
            return MapMessage.successMessage("提交成功");
        }

        if (CollectionUtils.isNotEmpty(aliHis)) {
            // 支付宝操作
            Set<String> payMethods = aliHis.stream().map(OrderRefundHistory::getPayMethod).collect(Collectors.toSet());
            if (payMethods.size() > 1) {
                return MapMessage.errorMessage("请选择相同的支付方式进行退款");
            }
            RefundRequest refundRequest = new RefundRequest();
            refundRequest.setPayMethod(payMethods.stream().findFirst().orElse(""));
            refundRequest.setBatchNum(SafeConverter.toString(aliHis.size()));
            // 拼装支付宝退款详情
            StringBuilder stringBuilder = new StringBuilder();
            for (OrderRefundHistory history : aliHis) {
                stringBuilder.append(history.getId()).append("^").append(history.getRefundFee().doubleValue()).append("^")
                        .append("协商退款").append("#");
                // 修改状态
                history.setUpdateDatetime(new Date());
                history.setAgentUserName(currentAdminUser.getAdminUserName());
                history.setStatus(RefundHistoryStatus.REFUNDING);
                history.setComment(comment);
                userOrderServiceClient.saveOrUpdateRefundHistory(history);
            }
            String refundDetail = StringUtils.substring(stringBuilder.toString(), 0, stringBuilder.toString().length() - 1);
            refundRequest.setDetailData(refundDetail);
            PaymentGateway paymentGateway = paymentGatewayManager.getPaymentGateway(refundRequest.getPayMethod());
            String alipayRefundFormStr = paymentGateway.getRefundRequestForm(refundRequest).generateHtml("refundForm");
            return MapMessage.successMessage().add("alipayForm", alipayRefundFormStr);
        }
        return MapMessage.errorMessage("请选择正确的数据");
    }

    public MapMessage manualRefund(AuthCurrentAdminUser currentAdminUser, String id, String comment,String oaOrderId) {
        if (StringUtils.isBlank(id) || StringUtils.isBlank(comment)) {
            return MapMessage.errorMessage("参数错误");
        }
        OrderRefundHistory history = userOrderLoaderClient.loadOrderRefundHistoryById(id);
        if (history == null) {
            return MapMessage.errorMessage("错误的ID");
        }
        UserOrderPaymentHistory paymentHistory = userOrderLoaderClient.loadUserOrderPaymentHistoryList(history.getUserId()).stream()
                .filter(h -> h.getPaymentStatus() == PaymentStatus.Refunding
                        || h.getPaymentStatus() == PaymentStatus.RefundFail
                        || h.getPaymentStatus() == PaymentStatus.Refund)
                .filter(h -> Objects.equals(h.getOuterTradeId(), id))
                .findFirst().orElse(null);
        if (paymentHistory == null) {
            return MapMessage.errorMessage("找不到支付流水");
        }
        // 修改状态
        history.setUpdateDatetime(new Date());
        history.setAgentUserName(currentAdminUser.getAdminUserName());
        history.setStatus(RefundHistoryStatus.SUCCESS);
        history.setComment(comment);
        if(StringUtils.isNotBlank(oaOrderId)){
            history.setOaOrderId(oaOrderId);
        }
        userOrderServiceClient.saveOrUpdateRefundHistory(history);

        userOrderServiceClient.updatePaymentHistoryStatus(paymentHistory, PaymentStatus.Refund);
        sendRefundNotify("订单手动退款成功", StringUtils.formatMessage("用户{}的订单{}已退款成功。", history.getUserId(), history.getOrderId()));
        // 发送短信
        //对于手动关闭的订单客服统一进行手动发送退款通知短信，不在使用此发送短信功能
        //doSendRemindBySMS(history.getUserId(), history.getOrderId(), history.getRefundFee().doubleValue());
        return MapMessage.successMessage("处理成功");
    }

    public UserAuthentication getUserRemindUA(Long userId){
        UserAuthentication ua = null;
        User user = userLoaderClient.loadUser(userId);
        if (user.fetchUserType() == UserType.PARENT) {
            UserAuthentication parentUa = userLoaderClient.loadUserAuthentication(userId);
            if (parentUa != null && parentUa.isMobileAuthenticated()) {
                ua = parentUa;
            }
        } else if (user.fetchUserType() == UserType.STUDENT) {
            StudentParent keyParent = parentLoaderClient.loadStudentKeyParent(userId);
            if (keyParent != null) {
                UserAuthentication parentUa = userLoaderClient.loadUserAuthentication(keyParent.getParentUser().getId());
                if (parentUa != null && parentUa.isMobileAuthenticated()) {
                    ua = parentUa;
                }
            }
            if (ua == null) {
                UserAuthentication studentUa = userLoaderClient.loadUserAuthentication(userId);
                if (studentUa != null && studentUa.isMobileAuthenticated()) {
                    ua = studentUa;
                }
            }
        }

        return ua;
    }

    //发送短信通知
    private void doSendRemindBySMS(Long userId, String orderId, Double refundFee) {
        UserAuthentication ua = getUserRemindUA(userId);
        if (ua != null) {
            String content = "您好，尾号" + StringUtils.substring(orderId, orderId.length() - 6, orderId.length())
                    + "的订单已成功受理退款" + refundFee + "元，3-7个工作日将返还至您申请退款账户，请注意查询，如有问题请拨打：4001601717";
            userSmsServiceClient.buildSms().to(ua)
                    .content(content)
                    .type(SmsType.USER_ORDER_REFUND_REMIND)
                    .send();
        }
    }

    private void sendRefundNotify(String subject, String content) {
        emailServiceClient.createPlainEmail()
                .to(crmConfigService.$loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.name(), "ORDER_REFUND_EMAIL_KEFU"))
                .cc(crmConfigService.$loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.name(), "ORDER_REFUND_EMAIL_CC"))
                .subject(subject)
                .body(content)
                .send();
    }
}
