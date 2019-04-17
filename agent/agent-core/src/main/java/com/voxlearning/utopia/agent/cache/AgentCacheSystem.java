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

package com.voxlearning.utopia.agent.cache;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.inject.Named;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Named("com.voxlearning.utopia.agent.cache.AgentCacheSystem")
public class AgentCacheSystem extends SpringContainerSupport {

    public CBS_Container CBS;

    @Getter private AlertMessageCache alertMessageCache;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();

        CBS = new CBS_Container();
        CBS.flushable = CacheSystem.CBS.getCacheBuilder().getCache("flushable");
        CBS.unflushable = CacheSystem.CBS.getCacheBuilder().getCache("unflushable");
//        CBS.persistence = CacheSystem.CBS.getCacheBuilder().getCache("persistence");

        alertMessageCache = new AlertMessageCache(CBS.unflushable);
    }

    public static class CBS_Container {
        public UtopiaCache flushable;
        public UtopiaCache unflushable;
//        public UtopiaCache persistence;
    }

    public synchronized void addUserSessionAttribte(Long userId, String attrKey, Object attrValue) {
        if (userId == null || StringUtils.isBlank(attrKey) || attrValue == null) return;

        AgentUserSession userSession = getUserSession(userId);
        userSession.getAttributes().put(attrKey, attrValue);

        updateUserSession(userId, userSession);
    }

    public synchronized Object getUserSessionAttribte(Long userId, String attrKey) {
        if (userId == null || StringUtils.isBlank(attrKey)) return null;

        AgentUserSession userSession = getUserSession(userId);
        return userSession.getAttributes().get(attrKey);
    }

    public synchronized void setAuthCurrentUser(Long userId, AuthCurrentUser currentUser) {
        if (userId == null || currentUser == null) return;

        AgentUserSession userSession = getUserSession(userId);
        userSession.setAuthCurrentUser(currentUser);

        updateUserSession(userId, userSession);
    }

    public synchronized AuthCurrentUser getAuthCurrentUser(Long userId) {
        if (userId == null) return null;

        AgentUserSession userSession = getUserSession(userId);
        return userSession.getAuthCurrentUser();
    }

    private AgentUserSession getUserSession(Long userId) {
        CacheObject<AgentUserSession> cacheObject = CBS.unflushable.get(getUserSessionKey(userId));
        if (cacheObject != null && cacheObject.getValue() != null) {
            return cacheObject.getValue();
        } else {
            AgentUserSession session = new AgentUserSession();
            session.setAttributes(new HashMap<>());
            updateUserSession(userId, session);
            return session;
        }
    }

    private void updateUserSession(Long userId, AgentUserSession userSession) {
        CBS.unflushable.set(getUserSessionKey(userId), SafeConverter.toInt(DateUtils.addDays(new Date(), 30).getTime() / 1000), userSession);
    }

    public synchronized void removeUserSession(Long userId) {
        if (userId == null) return;
        CBS.unflushable.delete(getUserSessionKey(userId));
    }

    private String getUserSessionKey(Long userId) {
        return "MARKETING_SESSION_20170329_" + userId;
    }

    @NoArgsConstructor
    @Getter
    @Setter
    public static class AgentUserSession implements Serializable {
        private Integer defaultSchoolLevel;
        private AuthCurrentUser authCurrentUser;
        private Map<String, Object> attributes = new HashMap<>();
    }

    public boolean needRefreshUserAuth(Long userId){
        Long expireTime = CBS.unflushable.load(getUserAuthRefreshTimeKey(userId));
        if(expireTime == null || expireTime < (new Date()).getTime()){
            return true;
        }
        return false;
    }

    // 设置用户权限刷新时间为30分钟
    public void updateUserAuthRefreshTime(Long userId) {
        long time = DateUtils.addMinutes(new Date(), 30).getTime();
        CBS.unflushable.set(getUserAuthRefreshTimeKey(userId), SafeConverter.toInt(time / 1000), time);
    }

    private String getUserAuthRefreshTimeKey(Long userId){
        return "USER_AUTH_REFRESH_TIME_" + userId;
    }


    private String getUserUnreadNotifyCountKey(Long userId){
        return "USER_UNREAD_NOTIFY_COUNT_" + userId;
    }

    public void updateUserUnreadNotifyCount(Long userId, Integer unreadCount){
        CBS.unflushable.set(getUserUnreadNotifyCountKey(userId), SafeConverter.toInt(DateUtils.addMinutes(new Date(), 30).getTime() / 1000), unreadCount);
    }

    public void incrUserUnreadNotifyCount(Long userId){
        Integer unreadCount = CBS.unflushable.load(getUserUnreadNotifyCountKey(userId));
        if(unreadCount != null){
            updateUserUnreadNotifyCount(userId, unreadCount + 1);
        }
    }

    public void decrUserUnreadNotifyCount(Long userId){
        Integer unreadCount = CBS.unflushable.load(getUserUnreadNotifyCountKey(userId));
        if(unreadCount != null && unreadCount > 0){
            updateUserUnreadNotifyCount(userId, unreadCount - 1);
        }
    }

    public Integer getUserUnreadNotifyCount(Long userId){
        return CBS.unflushable.load(getUserUnreadNotifyCountKey(userId));
    }


    public void saveSignInFlag(String type, Long userId, Long schoolId, Integer expirationInSeconds){
        if(StringUtils.isBlank(type) || userId == null){
            return;
        }
        String key = "SIGN_" + type + "_" + userId;
        if(schoolId != null){
            key += "_" + schoolId;
        }
        CBS.flushable.set(key, expirationInSeconds == null ? DateUtils.getCurrentToDayEndSecond() : expirationInSeconds, true);
    }

    public boolean loadSignInFlag(String type, Long userId, Long schoolId){
        if(StringUtils.isBlank(type) || userId == null){
            return false;
        }
        String key = "SIGN_" + type + "_" + userId;
        if(schoolId != null){
            key += "_" + schoolId;
        }
        return SafeConverter.toBoolean(CBS.flushable.load(key));
    }

    public void deleteSignInFlag(String type, Long userId, Long schoolId){
        if(StringUtils.isBlank(type) || userId == null){
            return;
        }
        String key = "SIGN_" + type + "_" + userId;
        if(schoolId != null){
            key += "_" + schoolId;
        }
        CBS.flushable.delete(key);
    }

}
