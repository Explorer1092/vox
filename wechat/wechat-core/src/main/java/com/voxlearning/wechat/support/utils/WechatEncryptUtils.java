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

package com.voxlearning.wechat.support.utils;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.cipher.AesUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.runtime.RuntimeMode;

import java.nio.charset.Charset;
import java.time.Instant;

/**
 * @author Xin Xin
 * @since 10/29/15
 */
public class WechatEncryptUtils {
    private static final String AES_ENCRYPT_KEY = "kas82kja&fi2kd*1";
    private static final Charset WECHAT_ENCRYPT_DEFAULT_CHARSET = Charset.forName("UTF-8");

    public static final String encryptOpenId(String value) {
        if (RuntimeMode.lt(Mode.STAGING)) {
            return (StringUtils.isEmpty(value) ? "openid_test_" + Instant.now().getNano() : value);
        }

        if (StringUtils.isBlank(value)) {
            return null;
        }

        return AesUtils.encryptBase64String(AES_ENCRYPT_KEY, value);
    }

    public static final String decryptOpenId(String value) {
        if (RuntimeMode.lt(Mode.STAGING)) {
            return (StringUtils.isEmpty(value) ? "openid_test_" + Instant.now().getNano() : value);
        }

        if (StringUtils.isBlank(value)) {
            return null;
        }

        try {
            return AesUtils.decryptBase64String(AES_ENCRYPT_KEY, value);
        } catch (Exception ex) {
            return null;
        }
    }

    public static String encryptToAES(String value) {
        return AesUtils.encryptHexString(AES_ENCRYPT_KEY, value);
    }

    public static String decryptFromAES(String value) {
        return AesUtils.decryptHexString(AES_ENCRYPT_KEY, value);
    }
}
