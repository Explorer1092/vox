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

package com.voxlearning.utopia.agent.service;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.RoleType;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.DigestSignUtils;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.webmvc.support.authentication.utils.AuthSessionMappingInfo;
import com.voxlearning.alps.webmvc.support.authentication.utils.SessionKeyUtils;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.agent.cache.AgentCacheSystem;
import com.voxlearning.utopia.agent.interceptor.AgentHttpRequestContext;
import com.voxlearning.utopia.core.config.CommonConfiguration;
import org.apache.commons.codec.digest.DigestUtils;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author shiwei.liao
 * @since 2015/12/1.
 */
@Named
public class AgentApiAuth {

    public static final String APP_KEY = "17Agent";
    public static final String PLATFORM_SECRET_KEY_TEST = "YtfaPhAO0#95";    // 调用平台接口使用的 secretKey
    public static final String PLATFORM_SECRET_KEY = "LtaO3Y9oENjw";    // 调用平台接口使用的 secretKey

    // md5加密掩码
    public static final String APP_SECRET_KEY_TEST = "i7k11MzIQOvc";    // 天玑专用，测试环境
    public static final String APP_SECRET_KEY = "tk58Ychbmsit";            // 天玑专用，线上环境


    public static final String PARAM_SESSION_KEY = "session_key";
    public static final String PARAM_SIG = "sig";

    private static final String AGENT_USER_TICKET_PREFIX = "agent_user_ticket_";
    private static final int USER_TICKET_EXPIRE_IN_SECONDS = (int) TimeUnit.DAYS.toSeconds(30);

    @Inject
    private AgentCacheSystem agentCacheSystem;

    public String getUserSign(Long userId) {
        String encryptionCode = touchTicket(userId);
        return generateSign(userId, encryptionCode);
    }

    public boolean isSignValid(String sign, Long userId) {
        int index = sign.indexOf(".");
        if (index < 0) {
            return false;
        }
        String cookieSign = sign.substring(0, index);
        String signTime = sign.substring(index + 1);
        String encryptionCode = touchTicket(userId);
        String newSign = DigestUtils.sha1Hex(userId + "." + signTime + encryptionCode);
        //校验签名
        if (!cookieSign.equals(newSign)) {
            return false;
        }
        //校验时间
        Date signDate = new Date(SafeConverter.toLong(signTime));
        if (DateUtils.dayDiff(new Date(), signDate) > 30) {
            return false;
        }
        return true;
    }

    private String generateSign(Long userId, String code) {
        long time = System.currentTimeMillis();
        String userString = userId + "." + time + code;
        String sign = DigestUtils.sha1Hex(userString) + "." + time;
        return sign;
    }

    public String touchTicket(Long userId) {
        String ticket = loadTicket(userId);
        if (StringUtils.isNotBlank(ticket)) {
            return ticket;
        }
        ticket = RandomUtils.randomString(8);
        saveTicket(userId, ticket);
        return ticket;
    }

    private void saveTicket(Long userId, String ticket) {
        agentCacheSystem.CBS.unflushable.set(AGENT_USER_TICKET_PREFIX + userId, USER_TICKET_EXPIRE_IN_SECONDS, ticket);
    }

    private String loadTicket(Long userId) {
        return agentCacheSystem.CBS.unflushable.load(AGENT_USER_TICKET_PREFIX + userId);
    }

    public void clearTicket(Long userId){
        agentCacheSystem.CBS.unflushable.delete(AGENT_USER_TICKET_PREFIX + userId);
    }

    public String generateSessionKey(Long userId){
        return generateSessionKey(userId, getUserSign(userId));
    }

    public static String generateSessionKey(Long userId, String password){
        AuthSessionMappingInfo authSessionMappingInfo = new AuthSessionMappingInfo(userId, password, Collections.singletonList(RoleType.ROLE_MARKETER));
        return SessionKeyUtils.generateSessionKey(CommonConfiguration.getInstance().getSessionEncryptKey(), authSessionMappingInfo);
    }

    public boolean isSessionKeyValid(String sessionKey){
        AuthSessionMappingInfo authSessionMappingInfo = null;
        try {
            authSessionMappingInfo = SessionKeyUtils.decodeFromSessionKey(CommonConfiguration.getInstance().getSessionEncryptKey(), sessionKey);
        }catch (Exception ex){
        }
        if(authSessionMappingInfo == null || authSessionMappingInfo.getUserId() == null || StringUtils.isBlank(authSessionMappingInfo.getPassword())){
            return false;
        }
        return isSignValid(authSessionMappingInfo.getPassword(), authSessionMappingInfo.getUserId());
    }

    public Long fetchUserIdBySessionKey(String sessionKey){
        AuthSessionMappingInfo authSessionMappingInfo = SessionKeyUtils.decodeFromSessionKey(CommonConfiguration.getInstance().getSessionEncryptKey(), sessionKey);
        try{
            authSessionMappingInfo = SessionKeyUtils.decodeFromSessionKey(CommonConfiguration.getInstance().getSessionEncryptKey(), sessionKey);
        }catch (Exception ex){
        }
        if(authSessionMappingInfo == null || authSessionMappingInfo.getUserId() == null){
            return null;
        }
        return authSessionMappingInfo.getUserId();
    }

    public static String generateAppKeySig(Map<String, String> paramMap, String secretKey){
        if(paramMap == null){
            paramMap = new HashMap<>();
        }
        if(!paramMap.containsKey("app_key")){
            paramMap.put("app_key", APP_KEY);
        }
        return DigestSignUtils.signMd5(paramMap, secretKey);
    }

    public static String generateAppKeySig(Map<String, String> paramMap){
        String secretKey;
        if(RuntimeMode.lt(Mode.STAGING)){
            secretKey = AgentApiAuth.PLATFORM_SECRET_KEY_TEST;
        }else {
            secretKey = AgentApiAuth.PLATFORM_SECRET_KEY;
        }
        return generateAppKeySig(paramMap, secretKey);
    }
}
