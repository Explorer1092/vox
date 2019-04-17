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

package com.voxlearning.utopia.service.vendor.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.remote.core.support.ValueWrapperFuture;
import com.voxlearning.utopia.service.vendor.api.AsyncVendorSessionService;
import com.voxlearning.utopia.service.vendor.impl.support.OpenApiUtils;

import javax.inject.Named;

@Named
@ExposeService(interfaceClass = AsyncVendorSessionService.class)
@Deprecated
public class AsyncVendorSessionServiceImpl extends SpringContainerSupport implements AsyncVendorSessionService {

    @Override
    public AlpsFuture<String> generateSessionKey(String key, Long userId) {
        String s = OpenApiUtils.generateSessionKey(key, userId);
        return new ValueWrapperFuture<>(s);
    }

    @Override
    public AlpsFuture<Long> decodeUserIdFromSessionKey(String key, String sessionKey) {
        Long l = OpenApiUtils.decodeUserIdFromSessionkey(key, sessionKey);
        return new ValueWrapperFuture<>(l);
    }

    @Override
    public AlpsFuture<String> generateOrderToken(String key, String appKey, Long userId, Long orderSeq) {
        String s = OpenApiUtils.generateOrderToken(key, appKey, userId, orderSeq);
        return new ValueWrapperFuture<>(s);
    }
}
