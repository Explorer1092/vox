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

/**
 * SendGiftContext validate exception definition.
 *
 * @author Xiaohai Zhang
 * @serial
 * @since Feb 26, 2015
 */
public class SendGiftContextValidateException extends Exception {
    private static final long serialVersionUID = 749026315401429738L;

    public SendGiftContextValidateException() {
    }

    public SendGiftContextValidateException(String message) {
        super(message);
    }
}