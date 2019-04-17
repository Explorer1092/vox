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

package com.voxlearning.utopia.service.reward.impl;

import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.MobileRule;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.StringUtils;
import com.voxlearning.utopia.api.constant.CouponProductionName;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.integral.api.mapper.UserIntegral;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.reward.api.mapper.newversion.teacher.entity.TeacherCouponEntity;
import com.voxlearning.utopia.service.reward.base.support.RewardProductDetailGenerator;
import com.voxlearning.utopia.service.reward.constant.RewardOrderSaleGroup;
import com.voxlearning.utopia.service.reward.entity.RewardCoupon;
import com.voxlearning.utopia.service.reward.entity.RewardCouponDetail;
import com.voxlearning.utopia.service.reward.entity.RewardOrder;
import com.voxlearning.utopia.service.reward.entity.RewardProduct;
import com.voxlearning.utopia.service.reward.impl.coupon.CouponContext;
import com.voxlearning.utopia.service.reward.impl.coupon.CouponTemplate;
import com.voxlearning.utopia.service.reward.impl.coupon.CouponTemplateManager;
import com.voxlearning.utopia.service.reward.impl.dao.RewardCouponDao;
import com.voxlearning.utopia.service.reward.impl.dao.RewardCouponDetailPersistence;
import com.voxlearning.utopia.service.reward.impl.dao.RewardProductDao;
import com.voxlearning.utopia.service.reward.impl.internal.RewardHelpers;
import com.voxlearning.utopia.service.reward.impl.loader.RewardLoaderImpl;
import com.voxlearning.utopia.service.reward.impl.service.RewardServiceImpl;
import com.voxlearning.utopia.service.reward.impl.service.newversion.NewRewardLoaderImpl;
import com.voxlearning.utopia.service.reward.impl.service.newversion.NewRewardServiceImpl;
import com.voxlearning.utopia.service.reward.mapper.RewardProductDetail;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.sms.consumer.SmsServiceClient;
import com.voxlearning.utopia.service.user.api.UserIntegralService;
import com.voxlearning.utopia.service.user.api.entities.TeacherExtAttribute;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.ResearchStaffDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * Created by XiaoPeng.Yang on 14-7-30.
 */
@Named
public class CouponService {

    private static final Logger logger = LoggerFactory.getLogger(CouponService.class);

    @Inject private RewardCouponDetailPersistence rewardCouponDetailPersistence;
    @Inject private CouponTemplateManager couponTemplateManager;
    @Inject private RewardHelpers rewardHelpers;
    @Inject private StudentLoaderClient studentLoaderClient;
    @Inject private TeacherLoaderClient teacherLoaderClient;
    @Inject private RewardLoaderImpl rewardLoader;
    @Inject private UserOrderLoaderClient userOrderLoaderClient;

    @Inject private MessageCommandServiceClient messageCommandServiceClient;
    @Inject private SmsServiceClient smsServiceClient;
    @Inject private RewardProductDao rewardProductDao;
    @Inject private RewardCouponDao rewardCouponDao;
    @Inject private RewardServiceImpl rewardService;
    @Inject private NewRewardLoaderImpl newRewardLoader;
    @Inject private NewRewardServiceImpl newRewardService;

    @ImportService(interfaceClass = UserIntegralService.class)
    private UserIntegralService userIntegralService;

    public RewardProductDetail loadProductDetailByProductIdAndUserInfo(Long productId, User user) {
        RewardProduct product = rewardLoader.loadRewardProduct(productId);
        if (product == null) {
            return null;
        }
        RewardProductDetailGenerator generator = rewardHelpers.getRewardProductDetailGenerator();
        switch (user.fetchUserType()) {
            case STUDENT:
                StudentDetail studentDetail;
                if (user instanceof StudentDetail) {
                    studentDetail = (StudentDetail) user;
                } else {
                    studentDetail = studentLoaderClient.loadStudentDetail(user.getId());
                }
                //增加权限判断  看这个奖品当前角色可不可以购买
                if (!product.getStudentVisible()) {
                    return null;
                }
                return generator.generateStudentRewardProductDetail(product, studentDetail);
            case TEACHER:
                TeacherDetail teacherDetail;
                if (user instanceof TeacherDetail) {
                    teacherDetail = (TeacherDetail) user;
                } else {
                    teacherDetail = teacherLoaderClient.loadTeacherDetail(user.getId());
                }

                return generator.generateTeacherRewardProductDetail(product, teacherDetail);
            case RESEARCH_STAFF:
                //增加权限判断  看这个奖品当前角色可不可以购买
                if (!product.getTeacherVisible()) {
                    return null;
                }
                return generator.generateResearchStaffRewardProductDetail(product);
            default:
                return null;
        }
    }

    public MapMessage exchangedCouponForStudent(Long productId, StudentDetail student, String mobile) {

        final Long studentId = student.getId();
        final RewardProduct product = rewardLoader.loadRewardProduct(productId);
        if (product == null) {
            return MapMessage.errorMessage("该产品不存在");
        }

        RewardCoupon coupon = rewardCouponDao.loadRewardCouponByPID(productId);
        if (coupon == null)
            return MapMessage.errorMessage("配置错误!");

        if (!product.getStudentVisible()) {
            return MapMessage.errorMessage("对不起，你不能兑换这个奖品，请重新登录兑换");
        }
        if (Boolean.FALSE.equals(product.getRepeatExchanged())) {
            // 判断用户是否已经兑换过该优惠券
            List<RewardCouponDetail> details = rewardHelpers.getRewardCouponDetailLoader().loadUserRewardCouponDetails(studentId)
                    .stream()
                    .filter(source -> Objects.equals(source.getProductId(), product.getId()))
                    .collect(Collectors.toList());
            RewardCouponDetail myCoupon = MiscUtils.firstElement(details);
            if (myCoupon != null) {
                return MapMessage.errorMessage("你已经兑换过了");
            }
        }

        // 查找剩余优惠券
        List<RewardCouponDetail> couponDetails = rewardCouponDetailPersistence.loadByProductId(product.getId())
                .stream()
                .filter(source -> !Boolean.TRUE.equals(source.getExchanged()))
                .collect(Collectors.toList());
        if (couponDetails.isEmpty()) {
            return MapMessage.errorMessage("库存告急，你来晚了");
        }
        final RewardProductDetail detail = loadProductDetailByProductIdAndUserInfo(product.getId(), student);
        // 判断余额
        UserIntegral integral = student.getUserIntegral();
        if (integral == null || integral.getUsable() < detail.getDiscountPrice()) {
            return MapMessage.errorMessage("你的学豆数量不足，请检查一下价格和数量吧！");
        }
        //判断权限
        if (RewardOrderSaleGroup.VIP.name().equals(detail.getSaleGroup()) && !userOrderLoaderClient.isVipUser(student.getId())) {
            return MapMessage.errorMessage("这个奖品是VIP专享的，你还不能兑换。");
        }
        // 随机选取一张优惠券
        final RewardCouponDetail couponDetail = couponDetails.get(RandomUtils.nextInt(couponDetails.size()));
        String notifyMsg = null;

        // 兑换扣钱
        try {
            MapMessage orderMsg =  newRewardService.createRewardOrder(student, detail, null, 1, null, RewardOrder.Source.app, null);
            if (orderMsg.isSuccess()) {
                if (!rewardCouponDetailPersistence.couponExchanged(couponDetail, studentId, mobile, (Long)orderMsg.get("orderId"))) {
                    return MapMessage.errorMessage("兑换失败，请重试");
                }
            } else {
                return orderMsg;
            }
        } catch (Exception ex) {
            logger.error("FAILED TO EXCHANGE COUPON, STUDENT '{}', PRODUCT '{}'", studentId, product.getProductName(), ex);
            return MapMessage.errorMessage("兑换失败，请重试");
        }

        if (StringUtils.isNotEmpty(mobile) && coupon.getSendSms()) {
            notifyMsg = getSmsMessage(coupon.getSmsTpl(), rewardCouponDetailPersistence.load(couponDetail.getId()),product);
            // 发短信
            smsServiceClient.createSmsMessage(mobile)
                    .content(notifyMsg)
                    .type(SmsType.EXCHANGE_COUPON_STUDENT.name())
                    .send();
        }

        String systemMsg = getSystemMessage(coupon.getMsgTpl(), couponDetail, product);
        // 系统消息
        messageCommandServiceClient.getMessageCommandService().sendUserMessage(studentId, systemMsg);

        return MapMessage.successMessage().add("productName", product.getProductName())
                .add("couponNo", couponDetail.getCouponNo())
                .add("productUrl", product.getUsedUrl());
    }

    public MapMessage exchangedCouponForTeacher(Long productId, TeacherDetail teacherDetail, String mobile, TeacherCouponEntity teacherCoupon) {

        if(teacherDetail.getAuthenticationState() != AuthenticationState.SUCCESS.getState()){
            return MapMessage.errorMessage("对不起，未认证老师不允许兑换!");
        }

        Long teacherId = teacherDetail.getId();
        final RewardProduct product = rewardLoader.loadRewardProduct(productId);
        if (product == null) {
            return MapMessage.errorMessage("该产品不存在");
        }

        if(teacherLoaderClient.isFakeTeacher(teacherId)){
            return MapMessage.errorMessage("对不起，你不能兑换这个奖品!");
        }

        String unit = "学豆";
        if(teacherDetail.isPrimarySchool()){
            unit = "园丁豆";
        }

        RewardCoupon coupon = rewardCouponDao.loadRewardCouponByPID(productId);
        if (coupon == null)
            return MapMessage.errorMessage("配置错误!");

        if (!product.getTeacherVisible()) {
            return MapMessage.errorMessage("对不起，你不能兑换这个奖品，请重新登录兑换");
        }

        TeacherExtAttribute extAttribute = teacherLoaderClient.loadTeacherExtAttribute(teacherDetail.getId());
        int teacherRal = extAttribute == null ? 0 : SafeConverter.toInt(extAttribute.getRewardActiveLevel());

        if (teacherDetail.isSchoolAmbassador() && teacherRal == 0) {
            // 判断是否是包班制
            Set<Long> allRelIds = teacherLoaderClient.loadRelTeacherIds(teacherId);
            Map<Long, TeacherExtAttribute> details = teacherLoaderClient.loadTeacherExtAttributes(allRelIds);
            boolean freeze = true;
            for (TeacherExtAttribute detail : details.values()) {
                if (SafeConverter.toInt(detail.getRewardActiveLevel()) > 0) {
                    freeze = false;
                    break;
                }
            }

            if (freeze) {
                return MapMessage.errorMessage("由于您超过30天未布置作业，{}已经暂时冻结，检查作业且有学生完成即可解冻。",unit);
            }
            //return MapMessage.errorMessage("由于您超过30天未布置作业，园丁豆已经暂时冻结，检查作业且有学生完成即可解冻。");
        }
        if (Boolean.FALSE.equals(product.getRepeatExchanged()) || newRewardLoader.isTeachingResources(product.getOneLevelCategoryId())) {
            // 判断用户是否已经兑换过该优惠券
            List<RewardCouponDetail> details = rewardHelpers.getRewardCouponDetailLoader().loadUserRewardCouponDetails(teacherId)
                    .stream()
                    .filter(source -> Objects.equals(source.getProductId(), productId))
                    .collect(Collectors.toList());

            RewardCouponDetail myCoupon = MiscUtils.firstElement(details);
            if (myCoupon != null) {
                return MapMessage.errorMessage("你已经兑换过了");
            }

        }

        // 查找剩余优惠券
        List<RewardCouponDetail> couponDetails = rewardCouponDetailPersistence.loadByProductId(product.getId())
                .stream()
                .filter(source -> !Boolean.TRUE.equals(source.getExchanged()))
                .collect(Collectors.toList());
        if (couponDetails.isEmpty()) {
            return MapMessage.errorMessage("库存告急，你来晚了");
        }
        final RewardProductDetail detail = loadProductDetailByProductIdAndUserInfo(product.getId(), teacherDetail);

        // 判断余额
        UserIntegral integral = teacherDetail.getUserIntegral();
        Double price = detail.getDiscountPrice();
        if (teacherCoupon != null) {
            price = detail.getDiscountPrice() * teacherCoupon.getDiscount();
        }
        if (integral == null || integral.getUsable() < price) {
            return MapMessage.errorMessage("你的{}数量不足，请检查一下价格和数量吧！",unit);
        }

        //判断权限
        if (RewardOrderSaleGroup.VIP.name().equals(detail.getSaleGroup()) && !teacherDetail.isSchoolAmbassador()) {
            return MapMessage.errorMessage("这个奖品是VIP专享的，你还不能兑换。");
        }

        // 随机选取一张优惠券
        final RewardCouponDetail couponDetail = couponDetails.get(RandomUtils.nextInt(couponDetails.size()));
        // 兑换扣钱
        try {
            MapMessage orderMsg =  newRewardService.createRewardOrder(teacherDetail, detail, null, 1, null, RewardOrder.Source.app, teacherCoupon);
            if (orderMsg.isSuccess()) {
                if (!rewardCouponDetailPersistence.couponExchanged(couponDetail, teacherId, mobile, (Long)orderMsg.get("orderId"))) {
                    return MapMessage.errorMessage("兑换失败，请重试");
                }
            } else {
                return orderMsg;
            }
        } catch (Exception ex) {
            logger.error("FAILED TO EXCHANGE COUPON, STUDENT '{}', PRODUCT '{}'", teacherId, product.getProductName(), ex);
            return MapMessage.errorMessage("兑换失败，请重试");
        }

        if (coupon.getSendSms() && MobileRule.isMobile(mobile) && StringUtils.isNotBlank(coupon.getSmsTpl())) {
            // 发短信
            smsServiceClient.createSmsMessage(mobile)
                    .content(getSmsMessage(coupon.getSmsTpl(), couponDetail,product))
                    .type(SmsType.EXCHANGE_COUPON_TEACHER.name())
                    .send();
        }

        if (coupon.getSendMsg()) {
            // 系统消息
            teacherLoaderClient.sendTeacherMessage(teacherId, getSystemMessage(coupon.getMsgTpl(), couponDetail, product));
        }

        return MapMessage.successMessage()
                .add("productName", product.getProductName())
                .add("couponNo", couponDetail.getCouponNo())
                .add("productUrl", product.getUsedUrl());
    }

    private String getSystemMessage(String tpl, RewardCouponDetail couponDetail, RewardProduct product) {
        if (StringUtils.isEmpty(tpl)) {
            return null;
        } else {
            return tpl.replace("$couponNo", couponDetail.getCouponNo())
                    .replace("$productName", product.getProductName());
        }
    }

    private String getSmsMessage(String tpl, RewardCouponDetail couponDetail, RewardProduct product) {
        if (StringUtils.isEmpty(tpl)) {
            return null;
        } else {
            return tpl.replace("$couponNo", couponDetail.getCouponNo())
                    .replace("$productName", product.getProductName());
        }
    }

    public MapMessage couponRebate(Long userId, Long couponDetailId, CouponProductionName couponProductionName) {
        CouponContext context = CouponContext.of(couponProductionName).withUserId(userId).withCouponDetailId(couponDetailId);
        CouponTemplate template = couponTemplateManager.get(couponProductionName);
        if (null == template) {
            logger.error("Unrecognized template" + couponProductionName.name());
            return MapMessage.errorMessage("操作失败");
        }
        return template.rebate(context);
    }

    public MapMessage exchangedCouponForRstaff(Long productId, ResearchStaffDetail researchStaffDetail, String mobile) {

        final Long userId = researchStaffDetail.getId();
        final RewardProduct product = rewardLoader.loadRewardProduct(productId);
        if (product == null) {
            return MapMessage.errorMessage("该产品不存在");
        }

        RewardCoupon coupon = rewardCouponDao.loadRewardCouponByPID(productId);
        if (coupon == null)
            return MapMessage.errorMessage("配置错误!");

        if (!product.getTeacherVisible()) {
            return MapMessage.errorMessage("对不起，你不能兑换这个奖品，请重新登录兑换");
        }
        if (Boolean.FALSE.equals(product.getRepeatExchanged())) {
            // 判断用户是否已经兑换过该优惠券
            List<RewardCouponDetail> details = rewardHelpers.getRewardCouponDetailLoader().loadUserRewardCouponDetails(userId)
                    .stream()
                    .filter(source -> Objects.equals(source.getProductId(), product.getId()))
                    .collect(Collectors.toList());

            RewardCouponDetail myCoupon = MiscUtils.firstElement(details);
            if (myCoupon != null) {
                return MapMessage.errorMessage("你已经兑换过了");
            }
        }

        // 查找剩余优惠券
        List<RewardCouponDetail> couponDetails = rewardHelpers.getRewardCouponDetailLoader().loadProductRewardCouponDetails(product.getId())
                .stream()
                .filter(source -> !Boolean.TRUE.equals(source.getExchanged()))
                .collect(Collectors.toList());
        if (couponDetails.isEmpty()) {
            return MapMessage.errorMessage("库存告急，你来晚了");
        }

        final RewardProductDetail detail = loadProductDetailByProductIdAndUserInfo(product.getId(), researchStaffDetail);
        // 判断余额
        UserIntegral integral = researchStaffDetail.getUserIntegral();
        if (integral == null || integral.getUsable() < detail.getDiscountPrice()) {
            return MapMessage.errorMessage("你的园丁豆数量不足，请检查一下价格和数量吧！");
        }
        // 随机选取一张优惠券
        final RewardCouponDetail couponDetail = couponDetails.get(RandomUtils.nextInt(couponDetails.size()));
        // 兑换扣钱
        try {
            if (rewardCouponDetailPersistence.couponExchanged(couponDetail, userId, mobile)) {
                IntegralHistory integralHistory = new IntegralHistory(userId, newRewardService.fetchIntegralByExchangProduct(detail.getOneLevelCategoryId()), -detail.getDiscountPrice().intValue() * 10);
                integralHistory.setComment(newRewardService.fetchRewardIntegralComment(integralHistory.toIntegralType()));

                if (!userIntegralService.changeIntegral(integralHistory).isSuccess()) {
                    throw new RuntimeException();
                }
                couponDetail.setUserId(userId);
            } else {
                throw new RuntimeException();
            }
        } catch (Exception ex) {
            logger.error("FAILED TO EXCHANGE COUPON, STUDENT '{}', PRODUCT '{}'", userId, product.getProductName(), ex);
            return MapMessage.errorMessage("兑换失败，请重试");
        }

        if (coupon.getSendSms() && MobileRule.isMobile(mobile) && StringUtils.isNotBlank(coupon.getSmsTpl())) {
            // 发短信
            smsServiceClient.createSmsMessage(mobile)
                    .content(getSmsMessage(coupon.getSmsTpl(), couponDetail,product))
                    .type(SmsType.EXCHANGE_COUPON_TEACHER.name())
                    .send();
        }
        // 系统消息
        teacherLoaderClient.sendTeacherMessage(researchStaffDetail.getId(), getSystemMessage(coupon.getMsgTpl(), couponDetail, product));
        return MapMessage.successMessage().add("productName", product.getProductName()).add("couponNo", couponDetail.getCouponNo())
                .add("productUrl", product.getUsedUrl());
    }
}
