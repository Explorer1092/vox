package com.voxlearning.washington.controller;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.utopia.core.utils.LoggerUtils;
import com.voxlearning.utopia.service.coupon.api.mapper.CouponShowMapper;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.washington.support.AbstractController;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Summer
 * @since 2017/3/16
 */

@Controller
@Slf4j
@NoArgsConstructor
@RequestMapping("/coupon")
public class CouponController extends AbstractController {

    // 获取可用的优惠劵
    @RequestMapping(value = "loadcoupons.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadCoupons() {
        Long userId = currentUserId();
        if (userId == null || userId == 0) {
            return MapMessage.errorMessage("用户不存在");
        }
        String orderId = getRequestString("orderId");
        UserOrder userOrder = userOrderLoaderClient.loadUserOrder(orderId);
        if (userOrder == null) {
            LoggerUtils.error("orderIsNull", null,orderId);
            return MapMessage.errorMessage("订单不存在");
        }
        // 如果是特定类型 打折  直减 需要计算金额的， 把计算后的金额返回展示
        List<CouponShowMapper> mappers = userOrderLoaderClient.loadOrderUsableCoupons(userOrder, userId);
        return MapMessage.successMessage().add("coupons", mappers);
    }

    // 关联订单与使用的优惠劵
    @RequestMapping(value = "relatedcouponorder.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage relatedCouponOrder() {
        Long userId = currentUserId();
        if (userId == null || userId == 0) {
            return MapMessage.errorMessage("用户不存在");
        }
        String orderId = getRequestString("orderId");
        String refId = getRequestString("refId");
        String couponId = getRequestString("couponId");
        UserOrder userOrder = userOrderLoaderClient.loadUserOrder(orderId);
        if (userOrder == null) {
            LoggerUtils.error("orderIsNull", null,orderId);
            return MapMessage.errorMessage("无效的订单，请重新创建订单");
        }
        if (StringUtils.isBlank(refId) || StringUtils.isBlank(couponId)) {
            return MapMessage.errorMessage("请选择优惠劵");
        }
        // 如果是已经关联的 直接返回成功
        if (StringUtils.isNotBlank(userOrder.getCouponRefId()) && Objects.equals(userOrder.getCouponRefId(), refId)) {
            return MapMessage.successMessage();
        }

        try {
            return atomicLockManager.wrapAtomic(userOrderServiceClient)
                    .expirationInSeconds(30)
                    .keyPrefix("ORDER_COUPON_RELATED")
                    .keys(orderId)
                    .proxy()
                    .relatedCouponOrder(userOrder, refId, couponId);
        } catch (DuplicatedOperationException ignore) {
            return MapMessage.errorMessage("您点击太快了，请重试");
        }
    }

    // 获取是否赠送优惠劵 支付成功后
    @RequestMapping(value = "loadsendcouponinfo.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadSendCouponInfo() {
        // 活动下线
        return MapMessage.errorMessage();
//        WonderlandActivity activity = wonderlandActivityServiceClient.getWonderlandActivityBuffer()
//                .load(WonderlandActivityIdType.PAY_SEND_COUPON);
//        if (!activity.validateActivity()) {
//            return MapMessage.errorMessage();
//        }
//        User user = currentUser();
//        if (user == null) {
//            return MapMessage.errorMessage("用户不存在");
//        }
//        if (user.fetchUserType() != UserType.STUDENT) {
//            return MapMessage.errorMessage("用户不存在");
//        }
//        String orderId = getRequestString("orderId");
//        UserOrder userOrder = userOrderLoaderClient.loadUserOrder(orderId);
//        if (userOrder == null) {
//            return MapMessage.errorMessage("无效的订单，请重新创建订单");
//        }
//        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(user.getId());
//        // 获取学生和购买的产品是否会送优惠劵以及优惠劵信息
//        boolean inGray = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "Pay", "SendCoupon");
//        if (inGray) {
//            return userOrderLoaderClient.loadSendCouponInfo(studentDetail, userOrder);
//        } else {
//            return MapMessage.errorMessage();
//        }
    }
}
