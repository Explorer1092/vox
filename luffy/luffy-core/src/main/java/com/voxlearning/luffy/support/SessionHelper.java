package com.voxlearning.luffy.support;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.luffy.cache.LuffyWebCacheSystem;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class SessionHelper {
    @Inject
    private LuffyWebCacheSystem luffyWebCacheSystem;

    private final static Integer SESSIONKEY_EXPIRATION_IN_SECONDS = 86400 * 25;

    private final static String CACHE_PRE = "CHIPS_MINI_PROGRAM_";

    public boolean validToken(String token) {
        String val = luffyWebCacheSystem.CBS.persistence.load(CACHE_PRE + token);
        return StringUtils.isNotBlank(val);
    }

    public void cacheToken(String token, String sessionKey) {
        luffyWebCacheSystem.CBS.persistence.set(CACHE_PRE + token, SESSIONKEY_EXPIRATION_IN_SECONDS, sessionKey);
    }
}
