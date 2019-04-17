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
 * unless prior written permission is obtained from Shanghai Sunny Education,  Inc.
 *
 */

package com.voxlearning.washington.controller.open.exception;

import lombok.Getter;

public class IllegalVendorUserException extends IllegalArgumentException {

    private static final long serialVersionUID = -8810365725889634471L;

    @Getter private String code;

    public IllegalVendorUserException(String code, String message) {
        super(message);
        this.code = code;
    }

    public IllegalVendorUserException(String code, Throwable cause) {
        super(cause);
        this.code = code;
    }
}
