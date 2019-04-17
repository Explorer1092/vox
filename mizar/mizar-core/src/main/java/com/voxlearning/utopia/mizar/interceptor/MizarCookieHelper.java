package com.voxlearning.utopia.mizar.interceptor;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.cipher.CipherUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.service.mizar.client.AsyncMizarCacheServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Cookie related helper class
 * Created by wangshichao on 16/9/7.
 */
@Named
public class MizarCookieHelper {

    private static final String APP_NAME = "MIZAR_CODEC";

    @Inject private AsyncMizarCacheServiceClient asyncMizarCacheServiceClient;

    private Map<String, Object> parseCookie(String cookSign) {
        try {
            String cookie = CipherUtils.DES().decryptHexString(asyncMizarCacheServiceClient.getAsyncMizarCacheService()
                    .CodecSaltCacheManager_getCode(APP_NAME).getUninterruptibly(), cookSign);
            if (cookie == null) {
                return Collections.emptyMap();
            }
            return JsonUtils.fromJson(cookie);
        } catch (Exception ignored) {
            return Collections.emptyMap();
        }
    }

    public String genCookie(String userId) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("time", new Date());
        String code = asyncMizarCacheServiceClient.getAsyncMizarCacheService()
                .CodecSaltCacheManager_getCode(APP_NAME)
                .getUninterruptibly();
        return CipherUtils.DES().encryptHexString(code, JsonUtils.toJson(map));
    }

    public Map<String, Object> getCookMapFromCookie(HttpServletRequest request) {
        String cookieSign = "";
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("userId")) {
                cookieSign = cookie.getValue();
                break;
            }
        }
        if (StringUtils.isEmpty(cookieSign)) {
            return null;
        }
        Map<String, Object> map = parseCookie(cookieSign);
        if (map == null || map.isEmpty()) {
            return null;
        }
        return map;
    }


}
