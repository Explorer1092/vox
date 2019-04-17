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

package com.voxlearning.utopia.agent.interceptor;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.webmvc.support.AlertMessageManager;
import com.voxlearning.alps.webmvc.support.context.UtopiaHttpRequestContext;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.cache.AgentCacheSystem;
import lombok.Getter;
import lombok.Setter;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Shuai.Huan on 2014/7/3.
 */
public class AgentHttpRequestContext extends UtopiaHttpRequestContext {
    @Inject
    AgentCacheSystem agentCacheSystem;

    @Setter
    @Getter
    private AuthCurrentUser currentUser;
    @Setter
    @Getter
    private String relativeUriPath;
    @Setter
    @Getter
    private String webAppContextPath;
    @Setter
    @Getter
    private AlertMessageManager alertMessageManager;

    @Setter
    @Getter
    @Deprecated
    private Integer unreadNotifyCount;

    @Setter
    @Getter
    private Map<String, String> headerMap = new HashMap<>();  // 将request的header统一成小写并保存与原来的对应关系，主要解决某些网络供应商将header统一成小写的问题

    public AgentHttpRequestContext(HttpServletRequest request, HttpServletResponse response) {
        super(request, response);
    }

    public boolean isLoggedIn() {
        return currentUser != null && !StringUtils.isBlank(currentUser.getUserName());
    }

    public void initAlertMessageManager(AlertMessageManager messageManager) {
        alertMessageManager = messageManager;
    }

//    public void saveAlertMessagesToSession(String sessionKey) {
//        getRequest().getSession().setAttribute(sessionKey, alertMessageManager);
//    }
    public void saveAlertMessagesToCache(String cacheKey){

    }
}
