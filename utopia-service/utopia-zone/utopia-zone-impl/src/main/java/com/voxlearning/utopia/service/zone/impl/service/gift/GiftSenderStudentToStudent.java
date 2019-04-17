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

package com.voxlearning.utopia.service.zone.impl.service.gift;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.integral.api.mapper.UserIntegral;
import com.voxlearning.utopia.service.integral.client.IntegralLoaderClient;
import com.voxlearning.utopia.service.user.api.UserIntegralService;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.zone.api.constant.GiftHistoryType;
import com.voxlearning.utopia.service.zone.api.entity.Gift;
import com.voxlearning.utopia.service.zone.api.entity.GiftHistory;
import com.voxlearning.utopia.service.zone.api.mapper.SendGiftContext;
import com.voxlearning.utopia.service.zone.impl.persistence.GiftHistoryPersistence;
import com.voxlearning.utopia.service.zone.impl.support.SendFreeGiftCountCacheManager;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.Date;

/**
 * Student -> Student.
 *
 * @author Rui Bao
 * @author Xiaohai Zhang
 * @since 13-9-10
 */
@Named
@GiftSenderType(GiftHistoryType.STUDENT_TO_STUDENT)
@InsufficientCoinErrorMessage("您的学豆不足！")
public class GiftSenderStudentToStudent extends AbstractGiftSender {

    @Inject private IntegralLoaderClient integralLoaderClient;

    @Inject private GiftHistoryPersistence giftHistoryPersistence;

    @Inject private SendFreeGiftCountCacheManager sendFreeGiftCountCacheManager;

    @ImportService(interfaceClass = UserIntegralService.class) protected UserIntegralService userIntegralService;

    @Override
    boolean isFreeGift(Gift gift) {
        return gift.getSilver() == 0;
    }

    @Override
    MapMessage canSendFreeGift(User sender, boolean finishedQuizOrHomeworkWithinToday) {
        // 今天没有发送过免费礼物并且完成了作业或者测验
        if (sendFreeGiftCountCacheManager.currentCount(sender.getId()) >= 1) {
            return MapMessage.errorMessage("免费礼物每天只能赠送一次哦");
        }
        boolean springFestival = SendGiftConstants.springFestivalDateRange.contains(new Date());
        if (!springFestival && !finishedQuizOrHomeworkWithinToday) {
            return MapMessage.errorMessage("做作业后才可以赠送这个礼物哦");
        }
        return MapMessage.successMessage();
    }

    @Override
    boolean hasSufficientBalance(User sender, Gift gift, int count) {
        UserIntegral integral = integralLoaderClient.getIntegralLoader().loadStudentIntegral(sender.getId());
        return null != integral && integral.getUsable() >= gift.getSilver() * count;
    }

    @Override
    MapMessage buyAndSendGift(User sender, Collection<User> receivers, Gift gift, SendGiftContext context) {
        String postscript = StringUtils.defaultString(context.getPostscript());
        GiftHistoryType type = context.getType();
        if (gift.getSilver() == 0) {
            for (User receiver : receivers) {
                giftHistoryPersistence.persist(GiftHistory.of(sender.getId(), receiver.getId(), gift.getId(), null, 0, postscript, type));
            }
            return MapMessage.successMessage().add("gift", gift);
        } else {
            IntegralHistory integralHistory = new IntegralHistory(sender.getId(), IntegralType.赠送礼物, -gift.getSilver() * receivers.size());
            integralHistory.setComment("赠送礼物扣除学豆");
            MapMessage message = userIntegralService.changeIntegral(sender, integralHistory);
            if (!message.isSuccess()) {
                return MapMessage.errorMessage();
            }
            for (User receiver : receivers) {
                giftHistoryPersistence.persist(GiftHistory.of(sender.getId(), receiver.getId(), gift.getId(), null, gift.getSilver(), postscript, type));
            }
            return MapMessage.successMessage().add("gift", gift);
        }
    }

    @Override
    void postProcess(User sender, Collection<User> receivers, Gift gift, SendGiftContext context) {
        // 学生成功发送了免费礼物，计数+1
        if (gift.getSilver() == 0) {
            sendFreeGiftCountCacheManager.increase(sender.getId());
        }
    }
}
