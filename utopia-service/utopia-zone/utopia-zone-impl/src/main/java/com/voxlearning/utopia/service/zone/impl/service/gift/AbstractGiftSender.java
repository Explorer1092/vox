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

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.Password;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import com.voxlearning.utopia.service.zone.api.entity.Gift;
import com.voxlearning.utopia.service.zone.api.mapper.SendGiftContext;
import com.voxlearning.utopia.service.zone.api.mapper.SendGiftContextValidateException;
import com.voxlearning.utopia.service.zone.impl.service.ZoneGiftServiceImpl;

import javax.inject.Inject;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;

/**
 * Abstract gift sender implementation.
 */
abstract class AbstractGiftSender extends GiftSenderManagerSupport {

    @Inject private UserLoaderClient userLoaderClient;
    @Inject private ZoneGiftServiceImpl zoneGiftService;

    @Override
    final public MapMessage sendGift(SendGiftContext context) {
        if (context == null) {
            return MapMessage.errorMessage();
        }
        try {
            context.validate();
        } catch (SendGiftContextValidateException ex) {
            logger.error("SendGiftContext validation failed: {}", JsonUtils.toJson(context));
            return MapMessage.errorMessage();
        }

        Collection<Long> userIds = new LinkedHashSet<>();
        userIds.add(context.getSenderId());
        userIds.addAll(context.getReceiverIds());
        Map<Long, User> users = userLoaderClient.loadUsers(userIds);

        User sender = users.get(context.getSenderId());
        if (sender == null) {
            logger.error("Gift sender {} not found", context.getSenderId());
            return MapMessage.errorMessage();
        }

        Collection<User> receivers = new LinkedList<>();
        for (Long receiverId : context.getReceiverIds()) {
            User receiver = users.get(receiverId);
            if (receiver == null) {
                logger.error("Gift receiver {} not found", receiverId);
                return MapMessage.errorMessage();
            }
            receivers.add(receiver);
        }

        Gift gift = zoneGiftService.getGiftBuffer().loadAll().get(context.getGiftId());
        if (gift != null && gift.isDisabledTrue()) {
            gift = null;
        }
        if (gift == null) {
            logger.error("Gift {} not found", context.getGiftId());
            return MapMessage.errorMessage();
        }

        return sendGift(sender, receivers, gift, context);
    }

    private MapMessage sendGift(User sender, Collection<User> receivers, Gift gift, SendGiftContext context) {
        // 判断sender是否能够发送gift
        if (!match(sender, gift)) {
            logger.warn("Sender {} cannot send gift {}", sender.getId(), gift.getId());
            return MapMessage.errorMessage();
        }

        // 判断是否可以赠送免费礼物
        boolean freeGift = isFreeGift(gift);
        if (freeGift) {
            boolean finishedQuizOrHomeworkWithinToday = false;
            Map<String, Object> extensions = context.getExtensions();
            if (extensions != null) {
                finishedQuizOrHomeworkWithinToday = SafeConverter.toBoolean(extensions.get("finishedQuizOrHomeworkWithinToday"));
            }
            MapMessage message;
            try {
                message = canSendFreeGift(sender, finishedQuizOrHomeworkWithinToday);
            } catch (Exception ex) {
                logger.error("Failed when checking if can send free gift", ex);
                message = MapMessage.errorMessage();
            }
            if (!message.isSuccess()) {
                return message;
            }
        }

        // 需要付费的礼物，检查支付密码（如果有）
        if (!freeGift && !context.isSkipPaymentPasswordValidation()) {
            String paymentPassword = context.getPaymentPassword();
            UserAuthentication ua = userLoaderClient.loadUserAuthentication(sender.getId());
            if (StringUtils.isNotBlank(ua.getPaymentPassword()) && StringUtils.isBlank(paymentPassword)) {
                return MapMessage.errorMessage("请输入支付密码");
            }
            if (StringUtils.isNotBlank(paymentPassword)) {
                Password password = Password.of(ua.getPaymentPassword());
                if (password == null || !StringUtils.equals(Password.obscurePassword(paymentPassword, password.getSalt()), password.getPassword())) {
                    return MapMessage.errorMessage("支付密码错误");
                }
            }
        }

        // 判断余额是否足够
        if (!hasSufficientBalance(sender, gift, receivers.size())) {
            InsufficientCoinErrorMessage errorMessage = getClass().getAnnotation(InsufficientCoinErrorMessage.class);
            return MapMessage.errorMessage(errorMessage.value());
        }

        MapMessage message = buyAndSendGift(sender, receivers, gift, context);
        // 如果赠送礼物成功，增加未查看礼物数量
        if (message.isSuccess()) {
            postProcess(sender, receivers, gift, context);
        }
        return message;
    }

    abstract boolean isFreeGift(Gift gift);

    abstract MapMessage canSendFreeGift(User sender, boolean finishedQuizOrHomeworkWithinToday);

    abstract boolean hasSufficientBalance(User sender, Gift gift, int count);

    abstract MapMessage buyAndSendGift(User sender, Collection<User> receivers, Gift gift, SendGiftContext context);

    abstract void postProcess(User sender, Collection<User> receivers, Gift gift, SendGiftContext context);

    final boolean match(User sender, Gift gift) {
        switch (sender.fetchUserType()) {
            case STUDENT:
                return gift.getStudentAvailable() && gift.getSilver() != null;
            case TEACHER:
                return gift.getTeacherAvailable() && gift.getGold() != null;
            default:
                return false;      // 其他类型不允许发送礼物
        }
    }
}
