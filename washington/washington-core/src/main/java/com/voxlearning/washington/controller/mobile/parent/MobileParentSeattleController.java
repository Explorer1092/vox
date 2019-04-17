/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.washington.controller.mobile.parent;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MobileRule;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.entity.o2o.TrusteeOrderRecord;
import com.voxlearning.utopia.entity.o2o.TrusteeReserveRecord;
import com.voxlearning.utopia.service.config.client.BusinessActivityManagerClient;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.order.api.constants.OrderStatus;
import com.voxlearning.utopia.service.order.api.constants.OrderType;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.order.consumer.TrusteeOrderServiceClient;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.user.api.entities.BusinessActivity;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.client.AsyncUserCacheServiceClient;
import com.voxlearning.utopia.service.vendor.api.constant.ParentAppPushType;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageTag;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageType;
import com.voxlearning.washington.controller.mobile.AbstractMobileController;
import com.voxlearning.washington.controller.open.ApiConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Summer Yang on 2016/6/27.
 * 通用支付 加群 报名活动 入口 家长端
 */
@Controller
@RequestMapping(value = "/seattle")
@Slf4j
public class MobileParentSeattleController extends AbstractMobileController {

    @Inject private RaikouSystem raikouSystem;

    @Inject private AsyncUserCacheServiceClient asyncUserCacheServiceClient;
    @Inject private MessageCommandServiceClient messageCommandServiceClient;
    @Inject private BusinessActivityManagerClient businessActivityManagerClient;
    @Inject private TrusteeOrderServiceClient trusteeOrderServiceClient;

    // 线上1分钱测试家长ID账号
    private static final List<Long> freeParentIds = Arrays.asList(27398018L, 27398020L, 27398022L, 27398025L, 27398027L, 27398030L,
            27398035L, 27398036L, 27398040L, 27398042L, 27398041L, 27398038L, 27398029L, 27398032L, 27398047L, 27398049L,
            27398054L, 27398055L);

    /**
     * 通用入口
     */
    @RequestMapping(value = "/index.vpage", method = {RequestMethod.GET})
    public String index(Model model) {
        // 获取活动ID
        Long activityId = getRequestLong("id");
        MapMessage message = validate(activityId);
        if (!message.isSuccess()) {
            model.addAttribute("result", message);
            return "seattle/errorinfo";
        }
        // 都过了 返回页面
        model.addAttribute("activity", message.get("activity"));
        return "seattle/index";
    }

    @RequestMapping(value = "/loadActivity.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage loadActivity(){
        // 获取活动ID
        Long activityId = getRequestLong("activityId");
        MapMessage message = validate(activityId);
        return message;
    }

    // 通用报名页面
    @RequestMapping(value = "/reserve.vpage", method = {RequestMethod.GET})
    public String reserve(Model model) {
        // 获取活动ID
        Long activityId = getRequestLong("activityId");
        MapMessage message = validate(activityId);
        if (!message.isSuccess()) {
            model.addAttribute("result", message);
            return "seattle/errorinfo";
        }
        BusinessActivity activity = (BusinessActivity) message.get("activity");
        // 是否获取孩子
        if (activity.getNeedLogin() && currentUser().fetchUserType() == UserType.PARENT) {
            // 获取学生ID
            Long studentId = getRequestLong("sid");

            //获取学生列表
            if (studentId == 0) {
                model.addAttribute("result", MapMessage.errorMessage("请先去绑定孩子吧~").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE));
                return "seattle/errorinfo";
            }

            model.addAttribute("studentInfo", getStudentInfo(studentId));
        }
        model.addAttribute("activity", message.get("activity"));
        return "seattle/reserve";
    }

    // 通用支付页面
    @RequestMapping(value = "/paydetail.vpage", method = {RequestMethod.GET})
    public String payDetail(Model model) {
        // 获取活动ID
        Long activityId = getRequestLong("activityId");
        MapMessage message = validate(activityId);
        if (!message.isSuccess()) {
            model.addAttribute("result", message);
            return "seattle/errorinfo";
        }
        BusinessActivity activity = (BusinessActivity) message.get("activity");
        if (StringUtils.isBlank(activity.getProductName()) || activity.getProductPrice() <= 0) {
            model.addAttribute("result", MapMessage.errorMessage("你访问的活动不存在~").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE));
            return "seattle/errorinfo";
        }
        // 是否获取孩子
        if (activity.getNeedLogin() && currentUser().fetchUserType() == UserType.PARENT) {
            // 获取学生ID
            Long studentId = getRequestLong("sid");

            //获取学生列表
            if (studentId == 0) {
                model.addAttribute("result", MapMessage.errorMessage("请先去绑定孩子吧~").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE));
                return "seattle/errorinfo";
            }

            model.addAttribute("studentInfo", getStudentInfo(studentId));
        }
        model.addAttribute("activity", message.get("activity"));
        return "seattle/paydetail";
    }

    /**
     * 获得通用支付前的参数
     * @return
     */
    @RequestMapping(value = "/loadPaydetail.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage loadPaydetail() {
        Long activityId = getRequestLong("activityId");
        MapMessage message = validate(activityId);
        if (!message.isSuccess()) {
            return message;
        }

        BusinessActivity activity = (BusinessActivity) message.get("activity");
        if (StringUtils.isBlank(activity.getProductName()) || activity.getProductPrice() <= 0) {
            return MapMessage.errorMessage("你访问的活动不存在~").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
        }
        // 是否获取孩子
        if (activity.getNeedLogin() && currentUser().fetchUserType() == UserType.PARENT) {
            // 获取学生ID
            Long studentId = getRequestLong("sid");

            //获取学生列表
            if (studentId == 0) {
                return MapMessage.errorMessage("请先去绑定孩子吧~").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
            }

            message.add("studentInfo", getStudentInfo(studentId));
        }
        return message;
    }

    // 获取孩子预约过哪些报名活动
    @RequestMapping(value = "loadstudentreserve.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadStudentReserve() {
        Long activityId = getRequestLong("activityId");
        Long studentId = getRequestLong("studentId");
        // hardcode 378
        if (activityId == 378) {
            return MapMessage.successMessage().add("reserve", false);
        }
        List<TrusteeReserveRecord> reserveRecords = trusteeOrderServiceClient.loadTrusteeReserveByStudentIdAndActivityId(studentId, activityId);
        if (CollectionUtils.isNotEmpty(reserveRecords)) {
            return MapMessage.successMessage().add("reserve", true);
        }
        return MapMessage.successMessage().add("reserve", false);
    }

    // 获取家长预约直播课的记录
    @RequestMapping(value = "loadparentsubscribe.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadPrentSubscribe() {
        User parent = currentParent();
        if (parent == null) {
            return MapMessage.errorMessage("请登录后重试");
        }
        Long activityId = getRequestLong("activityId");
        List<TrusteeReserveRecord> reserveRecords = trusteeOrderServiceClient.loadTrusteeReserveByParentIdAndActivityId(parent.getId(), activityId);
        if (CollectionUtils.isNotEmpty(reserveRecords)) {
            return MapMessage.successMessage().add("subscribe", true);
        }
        return MapMessage.successMessage().add("subscribe", false);
    }

    // 获取学生购买过的信息
    @RequestMapping(value = "loadstudentorders.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadStudentOrders() {
        Long studentId = getRequestLong("studentId");
        Long activityId = getRequestLong("activityId");
        if (studentId <= 0L) {  // 临时应对中学
            studentId = currentUserId();
        }

        List<TrusteeOrderRecord> orderRecords = trusteeOrderServiceClient.loadTrusteeOrderByStudentId(studentId);
        orderRecords = orderRecords.stream().filter(o -> o.getStatus() == TrusteeOrderRecord.Status.Paid)
                .filter(o -> o.getActivityId() != null && Objects.equals(o.getActivityId(), activityId))
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(orderRecords)) {
            return MapMessage.successMessage().add("pay", true);
        }
        return MapMessage.successMessage().add("pay", false);
    }


    // 通用报名页面发送验证码
    @RequestMapping(value = "/sendrcode.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendReserveSmsCode() {
        String mobile = getRequestString("mobile");
        try {
            if (!MobileRule.isMobile(mobile)) {
                return MapMessage.errorMessage("请输入正确的手机号");
            }
            return smsServiceClient.getSmsService().sendUnbindMobileVerificationCode(mobile, SmsType.PARENT_TRUSTEE_SEND_VERIFY_CODE.name(), false);
        } catch (Exception ex) {
            logger.error("send verify code failed", ex);
            return MapMessage.successMessage("发送验证码失败，请稍后再试");
        }
    }

    // 通用报名页面 -- 验证码验证并生成预约记录
    @RequestMapping(value = "verifycode.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage verifyCode() {
        String mobile = getRequestString("mobile");
        String code = getRequestString("code");
        Long studentId = getRequestLong("studentId");
        Long activityId = getRequestLong("activityId");
        String track = getRequestString("track");

        if (StringUtils.isBlank(mobile) || StringUtils.isBlank(code)) {
            return MapMessage.errorMessage("请填写正确的验证码");
        }
        if (!MobileRule.isMobile(mobile)) {
            return MapMessage.errorMessage("手机号不正确");
        }
        BusinessActivity activity = businessActivityManagerClient.getBusinessActivityBuffer()
                .load(activityId);
        if (activity == null) {
            return MapMessage.errorMessage("您访问的活动不存在或已过期~");
        }
        MapMessage message = smsServiceClient.getSmsService().verifyValidateCode(mobile, code, SmsType.PARENT_TRUSTEE_SEND_VERIFY_CODE.name());
        if (!message.isSuccess()) {
            return message;
        }
        // 判断是否超过规定的数量
        if (activity.getLimit() != null && activity.getLimit() > 0
                && asyncUserCacheServiceClient.getAsyncUserCacheService()
                .SeattleSoldCountManager_loadSoldCount(activityId)
                .getUninterruptibly() >= activity.getLimit()) {
            return MapMessage.errorMessage("对不起，已售罄~");
        }
        // 验证通过 生成预约单
        TrusteeReserveRecord reserveRecord = new TrusteeReserveRecord();
        reserveRecord.setSensitiveMobile(mobile);
        reserveRecord.setNeedPay(false);
        reserveRecord.setParentId(currentUserId());
        reserveRecord.setStudentId(studentId);
        reserveRecord.setStatus(TrusteeReserveRecord.Status.Success);
        reserveRecord.setActivityId(activityId);
        reserveRecord.setTrack(track);
        try {
            AtomicLockManager.instance().wrapAtomic(trusteeOrderServiceClient)
                    .keyPrefix("ParentService:saveReserveRecord")
                    .keys(mobile, activity.getId())
                    .proxy().saveReserveRecord(reserveRecord);
        } catch (DuplicatedOperationException ex) {
            return MapMessage.errorMessage("您点击太快了，请重试");
        }
        // 报名成功 返回配置的页面
        return MapMessage.successMessage().add("returnUrl", activity.getReturnUrl()).add("returnContent", activity.getReturnContent());
    }

    // 通用支付处理
    @RequestMapping(value = "/order.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage createOrder() {
        Long activityId = getRequestLong("activityId");
        Long studentId = getRequestLong("sid");
        Long parentId = currentUserId();
        String remark = getRequestString("remark");
        String track = getRequestString("track");
        BusinessActivity activity = businessActivityManagerClient.getBusinessActivityBuffer()
                .load(activityId);
        if (activity == null) {
            return MapMessage.errorMessage("您访问的活动不存在或已过期~");
        }
        if (parentId == 0) {
            return MapMessage.errorMessage("参数错误");
        }

        if (studentId == 0) {
            studentId = parentId;
        }

        // 判断是否超过规定的数量
        if (activity.getLimit() != null && activity.getLimit() > 0
                && asyncUserCacheServiceClient.getAsyncUserCacheService()
                .SeattleSoldCountManager_loadSoldCount(activityId)
                .getUninterruptibly() >= activity.getLimit()) {
            return MapMessage.errorMessage("对不起，已售罄~");
        }
        try {
            final Long sid = studentId;
            List<TrusteeOrderRecord> orderRecordList = trusteeOrderServiceClient.loadTrusteeOrderByParentId(parentId);
            // 过滤是否已经购买过了 如果购买过， 给一个提示
            TrusteeOrderRecord paidRecord = orderRecordList.stream().filter(o -> Objects.equals(o.getActivityId(), activityId))
                    .filter(o -> Objects.equals(o.getStudentId(), sid))
                    .filter(o -> o.getStatus() == TrusteeOrderRecord.Status.Paid).findFirst().orElse(null);
            if (paidRecord != null) {
                return MapMessage.errorMessage("您已成功购买该产品，请去我的订单查看。").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
            }
            // 看看该类型是否有未支付订单， 如果有，直接用现有的订单
            List<UserOrder> userOrderList = userOrderLoaderClient.loadUserOrderList(parentId);
            UserOrder order = null;
            if (CollectionUtils.isNotEmpty(userOrderList)) {
                order = userOrderList.stream().filter(o -> o.getOrderType() == OrderType.seattle)
                        .filter(o -> Objects.equals(o.getProductId(), activityId.toString()))
                        .filter(o -> o.getOrderStatus() == OrderStatus.New)
                        .findFirst().orElse(null);
            }
            if (order != null) {
                return MapMessage.successMessage().add("orderId", order.genUserOrderId());
            } else {
                order = UserOrder.newOrder(OrderType.seattle, parentId);
                order.setUserId(parentId);
                // 1分钱测试
                if (RuntimeMode.ge(Mode.PRODUCTION) && freeParentIds.contains(parentId)) {
                    order.setOrderPrice(new BigDecimal(0.01));
                } else {
                    order.setOrderPrice(new BigDecimal(activity.getProductPrice()));
                }
                order.setProductId(activityId.toString());
                // 扩展属性
                Map<String, Object> extMap = new HashMap<>();
                extMap.put("studentId", studentId);
                extMap.put("remark", remark);
                extMap.put("track", track);
                order.setExtAttributes(JsonUtils.toJson(extMap));
                order.setProductName(activity.getProductName());
                order.setOrderReferer("17parent");
                order.setOrderProductServiceType(OrderProductServiceType.Seattle.name());
                MapMessage message = AtomicLockManager.instance().wrapAtomic(userOrderServiceClient)
                        .keyPrefix("UserOrderService:saveUserOrder")
                        .keys(order.getId())
                        .proxy().saveUserOrder(order);
                if (message.isSuccess()) {
                    return MapMessage.successMessage().add("orderId", order.genUserOrderId());
                }
            }
        } catch (DuplicatedOperationException ex) {
            return MapMessage.errorMessage("您点击太快了，请重试");
        } catch (Exception ex) {
            logger.error("Create seattle order failed,parentId:{},studentId:{},activityId:{}", parentId, studentId, activityId, ex);
        }
        return MapMessage.errorMessage("生成订单失败");
    }

    // 通用预约 点击预约
    @RequestMapping(value = "subscribe.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage subscribe() {
        Long studentId = getRequestLong("studentId");
        Long activityId = getRequestLong("activityId");
        String track = getRequestString("track");
        Long parentId = currentUserId();
        if (parentId == null || parentId == 0) {
            return MapMessage.errorMessage("请登录后重试");
        }
        BusinessActivity activity = businessActivityManagerClient.getBusinessActivityBuffer()
                .load(activityId);
        if (activity == null) {
            return MapMessage.errorMessage("您访问的活动不存在或已过期~");
        }
        // 判断是否超过规定的数量
        if (activity.getLimit() != null && activity.getLimit() > 0
                && asyncUserCacheServiceClient.getAsyncUserCacheService()
                .SeattleSoldCountManager_loadSoldCount(activityId)
                .getUninterruptibly() >= activity.getLimit()) {
            return MapMessage.errorMessage("对不起，已售罄~");
        }
        // 再次做下校验
        List<TrusteeReserveRecord> reserveRecords = trusteeOrderServiceClient.loadTrusteeReserveByParentIdAndActivityId(parentId, activityId);
        if (CollectionUtils.isNotEmpty(reserveRecords)) {
            return MapMessage.successMessage().add("returnUrl", activity.getReturnUrl()).add("returnContent", activity.getReturnContent());
        }

        // 验证通过 生成预约单
        TrusteeReserveRecord reserveRecord = new TrusteeReserveRecord();
        reserveRecord.setNeedPay(false);
        reserveRecord.setParentId(parentId);
        reserveRecord.setStudentId(studentId);
        reserveRecord.setStatus(TrusteeReserveRecord.Status.Success);
        reserveRecord.setActivityId(activityId);
        reserveRecord.setTrack(track);
        try {
            AtomicLockManager.instance().wrapAtomic(trusteeOrderServiceClient)
                    .keyPrefix("ParentService:saveReserveRecord")
                    .keys(parentId, activity.getId())
                    .proxy().saveReserveRecord(reserveRecord);
        } catch (DuplicatedOperationException ex) {
            return MapMessage.errorMessage("您点击太快了，请重试");
        }
        // 报名成功 发送定时push提醒以及小铃铛消息
        if (activity.getSendMsg() != null && activity.getSendMsg()) {
            //新消息中心
            AppMessage message = new AppMessage();
            message.setUserId(parentId);
            message.setMessageType(ParentMessageType.REMINDER.getType());
            message.setTitle(activity.getTitle());
            message.setContent(activity.getMsgContent());
            message.setLinkUrl(activity.getReturnUrl());
            message.setLinkType(0);
            messageCommandServiceClient.getMessageCommandService().createAppMessage(message);
            // jpush定时消息
            Map<String, Object> jpushExtInfo = new HashMap<>();
            jpushExtInfo.put("url", activity.getReturnUrl());
            jpushExtInfo.put("tag", ParentMessageTag.通知.name());
            jpushExtInfo.put("s", ParentAppPushType.NOTICE.name());
            Long sendTime = activity.getSendSubscribeDate() == null ? 0 : activity.getSendSubscribeDate().getTime();
            if (sendTime != 0) {
                appMessageServiceClient.sendAppJpushMessageByIds(activity.getMsgContent(),
                        AppMessageSource.PARENT, Collections.singletonList(parentId), jpushExtInfo, sendTime);
            }
        }
        return MapMessage.successMessage().add("returnUrl", activity.getReturnUrl()).add("returnContent", activity.getReturnContent());
    }

    private MapMessage validate(Long activityId) {
        if (activityId == 0) {
            return MapMessage.errorMessage("您访问的活动不存在~").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
        }
        // 获取活动
        BusinessActivity activity = businessActivityManagerClient.getBusinessActivityBuffer()
                .load(activityId);
        if (activity == null) {
            return MapMessage.errorMessage("您访问的活动不存在~").setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
        }
        if (activity.getStatus() != null && activity.getStatus() == BusinessActivity.Status.Offline) {
            return MapMessage.errorMessage(activity.getDisabledContent()).setErrorCode(ApiConstants.RES_RESULT_BAD_REQUEST_CODE);
        }
        User user = currentUser();
        // 需要登录的情况
        if (activity.getNeedLogin() && user == null) {
            return MapMessage.errorMessage("请重新登录~").setErrorCode(ApiConstants.RES_RESULT_NEED_RELOGIN_CODE);
        }
        return MapMessage.successMessage().add("activity", activity);
    }


    private Map<String, Object> getStudentInfo(Long studentId) {
        User t = raikouSystem.loadUser(studentId);
        Map<String, Object> infoMap = new HashMap<>();
        infoMap.put("id", t.getId());
        infoMap.put("name", t.getProfile().getRealname());
        infoMap.put("img", getUserAvatarImgUrl(t.getProfile().getImgUrl()));
        return infoMap;
    }

}
