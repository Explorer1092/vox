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

package com.voxlearning.washington.controller.connect;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.webmvc.cookie.CookieManager;
import com.voxlearning.utopia.service.user.api.constants.SsoConnections;
import com.voxlearning.utopia.service.user.api.entities.LandingSource;
import org.slf4j.Logger;

/**
 * Created by Alex on 14-10-15.
 */
@Deprecated
public abstract class AbstractSsoConnector {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public abstract MapMessage validateToken(SsoConnections connectionInfo, String token);

    public String processUserBinding(LandingSource landingSource, String sourceName, MapMessage validateResult, CookieManager cookieManager) {
        return null;
    }

}
