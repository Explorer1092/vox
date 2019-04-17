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

package com.voxlearning.utopia.service.vendor.impl.support;

/**
 * Created by Alex on 14-10-22.
 */

import com.voxlearning.alps.cipher.AesUtils;
import com.voxlearning.alps.spi.exception.CryptoException;

public class OpenApiUtils {

    public static String generateSessionKey(String key, Long userId) {
        StringBuilder buffer = new StringBuilder();
        buffer.append(userId).append(",");
        buffer.append(System.currentTimeMillis() % 50); // 模50主要是限制生成的Key的长度不要太大

        try {
            return AesUtils.encryptHexString(key, buffer.toString());
        } catch (CryptoException e) {
            // e.printStackTrace();
            return null;
        }
    }

    public static Long decodeUserIdFromSessionkey(String key, String sessionKey) {
        try {
            String data = AesUtils.decryptHexString(key, sessionKey);
            return Long.valueOf(data.split(",")[0]);
        } catch (RuntimeException e) {
            //e.printStackTrace();
            return -1L;
        }
    }

    public static String generateOrderToken(String key, String appKey, Long userId, Long orderSeq) {
        StringBuilder buffer = new StringBuilder();
        buffer.append(appKey).append(",");
        buffer.append(userId).append(",");
        buffer.append(orderSeq);

        try {
            return AesUtils.encryptHexString(key, buffer.toString());
        } catch (CryptoException e) {
            return null;
        }
    }
}