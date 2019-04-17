/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.zone.support;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilder;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.common.JsonStringSerializer;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.utopia.service.zone.api.ClazzZoneService;
import com.voxlearning.utopia.service.zone.api.constant.GiftHistoryType;
import com.voxlearning.utopia.service.zone.api.mapper.SendGiftContext;
import com.voxlearning.utopia.service.zone.api.mapper.SendGiftContextValidateException;
import org.slf4j.Logger;

import java.util.*;

/**
 * Helper for sending gift.
 *
 * @author Xiaohai Zhang
 * @since Feb 26, 2015
 */
public class SendGiftCreator {
    private static final Logger logger = LoggerFactory.getLogger(SendGiftCreator.class);

    private final ClazzZoneService clazzZoneService;
    private final GiftHistoryType type;

    private Long senderId;
    private Collection<Long> receiverIds = new LinkedHashSet<>();
    private Long giftId;
    private String postscript = "";
    private String paymentPassword = "";
    private boolean skipPaymentPasswordValidation = false;
    private boolean finishedQuizOrHomeworkWithinToday = false;  // take effects for student

    public SendGiftCreator(ClazzZoneService clazzZoneService,
                           GiftHistoryType type) {
        this.clazzZoneService = Objects.requireNonNull(clazzZoneService);
        this.type = Objects.requireNonNull(type);
    }

    public SendGiftCreator sender(Long senderId) {
        this.senderId = senderId;
        return this;
    }

    public SendGiftCreator receivers(Long... receiverIds) {
        if (receiverIds != null) {
            this.receiverIds.addAll(Arrays.asList(receiverIds));
        }
        return this;
    }

    public SendGiftCreator receivers(Collection<Long> receiverIds) {
        if (receiverIds != null) {
            this.receiverIds.addAll(receiverIds);
        }
        return this;
    }

    public SendGiftCreator gift(Long giftId) {
        this.giftId = giftId;
        return this;
    }

    public SendGiftCreator postscript(String postscript) {
        this.postscript = postscript;
        return this;
    }

    public SendGiftCreator paymentPassword(String paymentPassword) {
        this.paymentPassword = paymentPassword;
        return this;
    }

    public SendGiftCreator skipPaymentPasswordValidation() {
        this.skipPaymentPasswordValidation = true;
        return this;
    }

    public SendGiftCreator finishedQuizOrHomeworkWithinToday(boolean finishedQuizOrHomeworkWithinToday) {
        this.finishedQuizOrHomeworkWithinToday = finishedQuizOrHomeworkWithinToday;
        return this;
    }

    public MapMessage send() {
        SendGiftContext context = new SendGiftContext();
        context.setType(type);
        context.setSenderId(senderId);
        context.setReceiverIds(receiverIds);
        context.setGiftId(giftId);
        context.setPostscript(postscript);
        context.setPaymentPassword(paymentPassword);
        context.setSkipPaymentPasswordValidation(skipPaymentPasswordValidation);
        context.setExtensions(new LinkedHashMap<>());
        context.getExtensions().put("finishedQuizOrHomeworkWithinToday", finishedQuizOrHomeworkWithinToday);

        try {
            context.validate();
        } catch (SendGiftContextValidateException ex) {
            logger.error("SendGiftContext validate failed: {}", JsonStringSerializer.getInstance().serialize(context));
            if (StringUtils.isNotBlank(ex.getMessage())) {
                return MapMessage.errorMessage(ex.getMessage());
            } else {
                return MapMessage.errorMessage("赠送礼物失败");
            }
        }

        try {
            AtomicCallbackBuilder<MapMessage> builder = AtomicCallbackBuilderFactory.getInstance().newBuilder();
            return builder.keyPrefix("SendGiftCreator_sendGift")
                    .keys(context.getSenderId(), context.getType().getId())
                    .callback(() -> clazzZoneService.sendGift(context))
                    .build()
                    .execute();
        } catch (Exception ex) {
            if (ex instanceof DuplicatedOperationException) {
                return MapMessage.errorMessage("礼物已经赠送");
            }
            logger.error("Failed to send gift", ex);
            return MapMessage.errorMessage("赠送礼物失败");
        }
    }
}
