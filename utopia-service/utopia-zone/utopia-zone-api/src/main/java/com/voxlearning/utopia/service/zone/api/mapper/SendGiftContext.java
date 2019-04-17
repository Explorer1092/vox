/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.zone.api.mapper;

import com.voxlearning.utopia.service.zone.api.constant.GiftHistoryType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

/**
 * Context for sending gift.
 *
 * @author Xiaohai Zhang
 * @serial
 * @since Feb 26, 2015
 */
@Getter
@Setter
public class SendGiftContext implements Serializable {
    private static final long serialVersionUID = -7868073082238787405L;

    private GiftHistoryType type;
    private Long senderId;
    private Collection<Long> receiverIds;
    private Long giftId;
    private String postscript;
    private String paymentPassword;
    private boolean skipPaymentPasswordValidation;
    private Map<String, Object> extensions;

    public void validate() throws SendGiftContextValidateException {
        if (getType() == null) {
            throw new SendGiftContextValidateException();
        }
        if (getSenderId() == null) {
            throw new SendGiftContextValidateException();
        }
        if (getReceiverIds() == null || getReceiverIds().isEmpty()) {
            throw new SendGiftContextValidateException();
        }
        if (getReceiverIds().contains(getSenderId())) {
            throw new SendGiftContextValidateException("不能给自己送礼物哦");
        }
        if (getGiftId() == null) {
            throw new SendGiftContextValidateException();
        }
    }
}
