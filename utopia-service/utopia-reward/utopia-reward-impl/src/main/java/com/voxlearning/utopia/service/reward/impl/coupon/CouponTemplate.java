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

package com.voxlearning.utopia.service.reward.impl.coupon;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.util.AnnotationUtils;
import com.voxlearning.utopia.api.constant.CouponProductionName;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.integral.api.mapper.UserIntegral;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.reward.base.support.RewardProductDetailGenerator;
import com.voxlearning.utopia.service.reward.constant.IdentificationCouponName;
import com.voxlearning.utopia.service.reward.constant.RewardOrderSaleGroup;
import com.voxlearning.utopia.service.reward.entity.RewardCouponDetail;
import com.voxlearning.utopia.service.reward.entity.RewardProduct;
import com.voxlearning.utopia.service.reward.impl.dao.RewardCouponDetailPersistence;
import com.voxlearning.utopia.service.reward.impl.dao.RewardProductDao;
import com.voxlearning.utopia.service.reward.impl.internal.RewardHelpers;
import com.voxlearning.utopia.service.reward.impl.loader.RewardLoaderImpl;
import com.voxlearning.utopia.service.reward.mapper.RewardProductDetail;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.sms.consumer.SmsServiceClient;
import com.voxlearning.utopia.service.user.api.UserIntegralService;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.consumer.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Inject;
import java.lang.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author XiaoPeng.Yang
 * @since 14-7-30
 */
@Slf4j
abstract public class CouponTemplate implements InitializingBean {

    @Getter private final CouponProductionName productionName;

    @Inject RewardCouponDetailPersistence rewardCouponDetailPersistence;
    @Inject private CouponTemplateManager couponTemplateManager;
    @Inject private RewardHelpers rewardHelpers;
    @Inject protected StudentLoaderClient studentLoaderClient;
    @Inject protected TeacherLoaderClient teacherLoaderClient;
    @Inject protected TeacherServiceClient teacherServiceClient;
    @Inject protected DeprecatedGroupLoaderClient groupLoaderClient;
    @Inject private RewardLoaderImpl rewardLoader;

    @Inject private MessageCommandServiceClient messageCommandServiceClient;
    @Inject private SmsServiceClient smsServiceClient;
    @Inject private RewardProductDao rewardProductDao;
    @Inject private ParentLoaderClient parentLoaderClient;

    @ImportService(interfaceClass = UserIntegralService.class) private UserIntegralService userIntegralService;

    protected CouponTemplate() {
        IdentificationCouponName annotation = getClass().getAnnotation(IdentificationCouponName.class);
        productionName = annotation.value();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        couponTemplateManager.register(this);
    }

    public RewardProductDetail loadProductDetailByProductIdAndUserInfo(Long productId, User user) {
        return null;
    }

    final public MapMessage rebate(final CouponContext context) {
        if (null == context) {
            log.error("CouponContext must not be null");
            return MapMessage.errorMessage("操作失败");
        }

        // 获取CouponDetail
        List<RewardCouponDetail> details = rewardHelpers.getRewardCouponDetailLoader().loadUserRewardCouponDetails(context.getUserId())
                .stream()
                .filter(source -> Objects.equals(source.getId(), context.getCouponDetailId()))
                .collect(Collectors.toList());
        if (details.isEmpty()) {
            return MapMessage.errorMessage("操作失败");
        }
        final RewardCouponDetail detail = MiscUtils.firstElement(details);
        if (detail.getRebated()) {
            return MapMessage.errorMessage("请不要重复申请返利");
        }
        if (!detail.getUsed()) {
            return MapMessage.errorMessage("您的使用信息未查询到，请联系客服400-160-1717咨询！");
        }
        final RewardProduct product = rewardLoader.loadProductByProductName(context.getCouponProductionName());
        if (product == null || !product.getRebated()) {
            return MapMessage.errorMessage("该产品不能返利");
        }
        // 返利
        try {
            // 更新已获取返利并发钱
            if (rewardCouponDetailPersistence.couponRebated(detail)) {
                IntegralHistory integralHistory = new IntegralHistory(detail.getUserId(), IntegralType.虚拟产品返利, getRebateAmount());
                integralHistory.setComment(StringUtils.formatMessage("{}返利成功，获得学豆", product.getProductName()));
                if (!userIntegralService.changeIntegral(integralHistory).isSuccess()) {
                    throw new RuntimeException();
                }
            } else {
                throw new RuntimeException();
            }
        } catch (Exception ex) {
            log.error("FAILED TO REBATE FOR STUDENT '{}' ON COUPONDETAILID '{}'", context.getUserId(), context.getCouponDetailId(), ex);
            return MapMessage.errorMessage("操作失败");
        }
        return MapMessage.successMessage("恭喜！奖励已经发放到你的账户，请查收！");
    }

    final public MapMessage exchangeForStudent(final CouponContext context) {
        return null;
    }

    final public MapMessage exchangeForTeahcer(final CouponContext context) {
        return null;
    }

    final public MapMessage exchangeForRstaff(final CouponContext context) {
        return null;
    }

    protected int getRebateAmount() {
        return 0;
    }

    protected boolean sendSmsFlag() {
        return false;
    }

    protected String getIntegralComment() {
        return "";
    }

    protected String getSmsMessage(RewardCouponDetail couponDetail) {
        return "";
    }

    protected String getSmsMessageForTeacher(RewardCouponDetail couponDetail) {
        return "";
    }

    protected String getSystemMessage(RewardCouponDetail couponDetail) {
        return "";
    }

    protected String getSystemMessageForTeacher(RewardCouponDetail couponDetail) {
        return "";
    }

    protected void sendJournal(StudentDetail studentDetail) {
    }

    @Documented
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface NeedSendClazzJournal {
    }
}
