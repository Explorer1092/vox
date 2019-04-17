package com.voxlearning.utopia.service.mizar.consumer.manager;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCachePrefix;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Yuechen Wang on 2016/10/08.
 */
@UtopiaCachePrefix(prefix = "MIZAR:USER:SESSION")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class MizarUserSessionManager extends PojoCacheObject<String, MizarUserSessionManager.MizarUserSession> {
    public MizarUserSessionManager(UtopiaCache cache) {
        super(cache);
    }

    public synchronized void addUserSessionAttribute(String userId, String attrKey, Object attrValue) {
        if (StringUtils.isBlank(userId) || StringUtils.isBlank(attrKey) || attrValue == null) return;

        MizarUserSession userSession = getUserSession(userId);
        userSession.getAttributes().put(attrKey, attrValue);

        updateUserSession(userId, userSession);
    }

    public synchronized Object getUserSessionAttribute(String userId, String attrKey) {
        if (StringUtils.isBlank(userId) || StringUtils.isBlank(attrKey)) return null;

        MizarUserSession userSession = getUserSession(userId);
        return userSession.getAttributes().get(attrKey);
    }

    public synchronized void removeUserSession(String userId) {
        if (StringUtils.isBlank(userId)) return;
        cache.delete(cacheKey(userId));
    }

    private MizarUserSession getUserSession(String userId) {
        MizarUserSession cacheObject = load(userId);
        if (cacheObject != null) {
            return cacheObject;
        } else {
            MizarUserSession session = new MizarUserSession();
            session.setAttributes(new HashMap<>());
            updateUserSession(userId, session);
            return session;
        }
    }

    private void updateUserSession(String userId, MizarUserSession userSession) {
        set(userId, userSession);
    }

    @NoArgsConstructor
    @Getter
    @Setter
    public static class MizarUserSession implements Serializable {
        private Map<String, Object> attributes = new HashMap<>();
    }
}
