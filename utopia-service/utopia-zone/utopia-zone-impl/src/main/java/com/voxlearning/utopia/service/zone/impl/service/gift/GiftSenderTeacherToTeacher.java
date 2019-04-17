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
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.integral.api.mapper.UserIntegral;
import com.voxlearning.utopia.service.user.api.UserIntegralService;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.zone.api.constant.GiftHistoryType;
import com.voxlearning.utopia.service.zone.api.entity.Gift;
import com.voxlearning.utopia.service.zone.api.entity.GiftHistory;
import com.voxlearning.utopia.service.zone.api.mapper.SendGiftContext;
import com.voxlearning.utopia.service.zone.impl.persistence.GiftHistoryPersistence;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;

/**
 * Teacher -> Teacher.
 *
 * @author Rui Bao
 * @author Xiaohai Zhang
 * @since 13-9-16
 */
@Named
@GiftSenderType(GiftHistoryType.TEACHER_TO_TEACHER)
@InsufficientCoinErrorMessage("您的园丁豆不足！")
public class GiftSenderTeacherToTeacher extends AbstractGiftSender {

    @Inject private GiftHistoryPersistence giftHistoryPersistence;
    @Inject private TeacherLoaderClient teacherLoaderClient;

    @ImportService(interfaceClass = UserIntegralService.class) protected UserIntegralService userIntegralService;

    @Override
    boolean isFreeGift(Gift gift) {
        return gift.getGold() == 0;
    }

    @Override
    MapMessage canSendFreeGift(User sender, boolean finishedQuizOrHomeworkWithinToday) {
        return MapMessage.successMessage();
    }

    @Override
    boolean hasSufficientBalance(User sender, Gift gift, int count) {
        UserIntegral integral = teacherLoaderClient.loadMainSubTeacherUserIntegral(sender.getId(), null);
        return null != integral && integral.getUsable() >= gift.getGold() * count;
    }

    @Override
    MapMessage buyAndSendGift(User sender, Collection<User> receivers, Gift gift, SendGiftContext context) {
        String postscript = context.getPostscript();
        GiftHistoryType type = context.getType();
        if (gift.getGold() == 0) {
            for (User receiver : receivers) {
                giftHistoryPersistence.persist(GiftHistory.of(sender.getId(), receiver.getId(), gift.getId(), 0, null, postscript, type));
            }
            return MapMessage.successMessage().add("gift", gift);
        } else {
            IntegralHistory integralHistory = new IntegralHistory(sender.getId(), IntegralType.赠送礼物, -gift.getGold() * receivers.size() * 10);
            integralHistory.setComment("赠送礼物扣除园丁豆");
            MapMessage message = userIntegralService.changeIntegral(sender, integralHistory);
            if (!message.isSuccess()) {
                return MapMessage.errorMessage();
            }
            for (User receiver : receivers) {
                giftHistoryPersistence.persist(GiftHistory.of(sender.getId(), receiver.getId(), gift.getId(), gift.getGold(), null, postscript, type));
            }
            return MapMessage.successMessage().add("gift", gift);
        }
    }

    @Override
    void postProcess(User sender, Collection<User> receivers, Gift gift, SendGiftContext context) {
    }
}
