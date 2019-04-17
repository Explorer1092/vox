package com.voxlearning.ucenter.controller.connect;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.webmvc.cookie.CookieManager;
import com.voxlearning.utopia.service.user.api.constants.SsoConnections;
import com.voxlearning.utopia.service.user.api.entities.LandingSource;
import org.slf4j.Logger;

/**
 * @author Alex
 * @since 14-10-15.
 */
public abstract class AbstractSsoConnector {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public abstract MapMessage validateToken(SsoConnections connectionInfo, String token);

    public String processUserBinding(LandingSource landingSource, String sourceName, MapMessage validateResult, CookieManager cookieManager) {
        return null;
    }

}
