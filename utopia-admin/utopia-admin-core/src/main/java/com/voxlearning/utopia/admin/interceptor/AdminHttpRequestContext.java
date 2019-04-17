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

package com.voxlearning.utopia.admin.interceptor;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.webmvc.support.AlertMessageManager;
import com.voxlearning.alps.webmvc.support.context.UtopiaHttpRequestContext;
import com.voxlearning.utopia.admin.auth.AuthCurrentAdminUser;
import lombok.Getter;
import lombok.Setter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AdminHttpRequestContext extends UtopiaHttpRequestContext {

    @Setter @Getter private AuthCurrentAdminUser currentAdminUser;
    @Setter @Getter private String relativeUriPath;
    @Setter @Getter private String webAppContextPath;
    @Setter @Getter private AlertMessageManager alertMessageManager;

    public AdminHttpRequestContext(HttpServletRequest request, HttpServletResponse response) {
        super(request, response);
    }

    public boolean isLoggedIn() {
        return currentAdminUser != null && !StringUtils.isBlank(currentAdminUser.getAdminUserName());
    }

    public void initAlertMessageManager(AlertMessageManager amm) {
        alertMessageManager = amm;
    }
//
    public void saveAlertMessagesToSession(String sessionKey) {

    }

}
