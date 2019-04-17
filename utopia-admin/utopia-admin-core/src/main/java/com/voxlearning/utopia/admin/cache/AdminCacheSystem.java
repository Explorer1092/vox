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

package com.voxlearning.utopia.admin.cache;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.admin.auth.AuthCurrentAdminUser;

import javax.inject.Named;

@Named("com.voxlearning.utopia.admin.cache.AdminCacheSystem")
public class AdminCacheSystem extends SpringContainerSupport {

    private static final String ADMIN_SESSION_KEY_PREFIX = "ADMIN_SESSION_";

    public CBS_Container CBS;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();

        CBS = new CBS_Container();
        CBS.flushable = CacheSystem.CBS.getCacheBuilder().getCache("flushable");
        CBS.unflushable = CacheSystem.CBS.getCacheBuilder().getCache("unflushable");
        CBS.persistence = CacheSystem.CBS.getCacheBuilder().getCache("persistence");
        CBS.storage = CacheSystem.CBS.getCacheBuilder().getCache("storage");
    }

    public static class CBS_Container {
        public UtopiaCache flushable;
        public UtopiaCache unflushable;
        public UtopiaCache persistence;
        public UtopiaCache storage;
    }

    public AuthCurrentAdminUser loadAuthUser(Long adminUserId) {
        String sessionKey = generateAdminSessionKey(adminUserId);
        AuthCurrentAdminUser cacheObject = CBS.storage.load(sessionKey);
        if (cacheObject != null) {
            CBS.storage.set(sessionKey, 1800, cacheObject);
        }
        return cacheObject;
    }

    public void removeAuthUser(Long adminUserId) {
        String sessionKey = generateAdminSessionKey(adminUserId);
        CBS.storage.delete(sessionKey);
    }

    public void saveAuthUser(AuthCurrentAdminUser adminUser) {
        String sessionKey = generateAdminSessionKey(adminUser.getFakeUserId());
        CBS.storage.set(sessionKey, 1800, adminUser);
    }

    public static String generateAdminSessionKey(Long adminUserId) {
        if (adminUserId != null) {
            return ADMIN_SESSION_KEY_PREFIX + adminUserId;
        } else {
            return ADMIN_SESSION_KEY_PREFIX;
        }
    }

    public void incViewUserPhoneCount(Long adminUserId) {
        if (adminUserId == null) {
            return;
        }

        String ck = ckViewUserPhone(adminUserId);
        CBS.flushable.incr(ck, 1, 1, DateUtils.getCurrentToDayEndSecond());
    }

    public long loadViewUserPhoneCount(Long adminUserId) {
        if (adminUserId == null) {
            return 0;
        }

        String ck = ckViewUserPhone(adminUserId);
        return SafeConverter.toLong(StringUtils.trim(CBS.flushable.load(ck)));
    }

    private static String ckViewUserPhone(Long adminUserId) {
        return "ADMIN_VIEW_USER_PHONE:" + adminUserId;
    }

    public void incTempPassword(Long adminUserId) {
        if (adminUserId == null) {
            return;
        }

        String ck = ckTempPassword(adminUserId);
        CBS.flushable.incr(ck, 1, 1, DateUtils.getCurrentToDayEndSecond());
    }

    public long loadTempPasswordCount(Long adminUserId) {
        if (adminUserId == null) {
            return 0;
        }

        String ck = ckTempPassword(adminUserId);
        return SafeConverter.toLong(StringUtils.trim(CBS.flushable.load(ck)));
    }

    private static String ckTempPassword(Long adminUserId) {
        return "ADMIN_TEMP_PASSWORD:" + adminUserId;
    }

}
