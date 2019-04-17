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

package com.voxlearning.ucenter.support;

import com.voxlearning.alps.webmvc.cookie.CookieManager;

public class LastUserNameCookieManager {
    public final static String COOKIE_NAME_LAST_USER_NAME = "voxlastname";

    private final CookieManager cookieManager;

    public LastUserNameCookieManager(CookieManager cookieManager) {
        this.cookieManager = cookieManager;
    }

    public void cleanupLastUserNameCookie() {
        if (cookieManager.getCookie(COOKIE_NAME_LAST_USER_NAME, null) != null)
            cookieManager.deleteCookieTLD(COOKIE_NAME_LAST_USER_NAME);
    }

    public void setLastUserNameCookie(int expire, String lastname) {
        cookieManager.setCookieTLD(COOKIE_NAME_LAST_USER_NAME, lastname, expire);
    }

    public String getLastUserNameCookie() {
        return cookieManager.getCookie(COOKIE_NAME_LAST_USER_NAME, null);
    }
}
