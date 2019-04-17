package com.voxlearning.washington.cache;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.washington.mapper.wechat.WechatOAuthUserInfo;

import static com.voxlearning.alps.repackaged.org.apache.commons.lang3.StringUtils.isBlank;

public class WechatCacheManager {

    private static final String OAUTH_REFRESH_TOKEN_PREFIX = "TEACHER_WECHAT:OAUTH_REFRESH_TOKEN_";
    private static final String OAUTH_ACCESS_TOKEN_PREFIX = "TEACHER_WECHAT:OAUTH_ACCESS_TOKEN_";
    private static final String OAUTH_WECHAT_USER_INFO_PREFIX = "TEACHER_WECHAT:OAUTH_WECHAT_USER_INFO_";

    public static final WechatCacheManager INSTANCE = new WechatCacheManager();

    private static final UtopiaCache UTOPIA_CACHE_PERSISTENCE;

    static {
        UTOPIA_CACHE_PERSISTENCE = CacheSystem.CBS.getCache("persistence");
    }

    private WechatCacheManager() {
    }

    public UtopiaCache getCache() {
        return UTOPIA_CACHE_PERSISTENCE;
    }

    public WechatOAuthUserInfo getOAuthUserInfo(String openId) {
        CacheObject<Object> cacheObject = INSTANCE.getCache().get(OAUTH_WECHAT_USER_INFO_PREFIX + openId);
        if (null == cacheObject || null == cacheObject.getValue()) {
            return null;
        }

        if (!(cacheObject.getValue() instanceof WechatOAuthUserInfo)) {
            return null;
        }

        return (WechatOAuthUserInfo) cacheObject.getValue();
    }

    public String getOAuthRefreshToken(String openId) {
        CacheObject<Object> cacheObject = INSTANCE.getCache().get(OAUTH_REFRESH_TOKEN_PREFIX + openId);
        if (null == cacheObject || null == cacheObject.getValue()) {
            return null;
        }

        return String.valueOf(cacheObject.getValue());
    }

    public String getOAuthAccessToken(String openId) {
        CacheObject<Object> cacheObject = INSTANCE.getCache().get(OAUTH_ACCESS_TOKEN_PREFIX + openId);
        if (null == cacheObject || null == cacheObject.getValue()) {
            return null;
        }

        return String.valueOf(cacheObject.getValue());
    }

    public void setOAuthAccessToken(String openId, String accessToken) {
        INSTANCE.getCache().set(OAUTH_ACCESS_TOKEN_PREFIX + openId, 7000, accessToken);
    }

    public void setOAuthRefreshToken(String openId, String refreshToken) {
        INSTANCE.getCache().set(OAUTH_REFRESH_TOKEN_PREFIX + openId, 29 * 24 * 60 * 60, refreshToken);
    }

    public void setOAuthUserInfo(String openId, WechatOAuthUserInfo info) {
        if (null == info || isBlank(openId)) {
            return;
        }

        INSTANCE.getCache().set(OAUTH_WECHAT_USER_INFO_PREFIX + openId, 29 * 24 * 60 * 60, info);
    }
}
