/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.user.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.utopia.service.user.api.ThirdPartyLoader;
import com.voxlearning.utopia.service.user.api.entities.LandingSource;
import com.voxlearning.utopia.service.user.cache.UserCache;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.List;

/**
 * @author changyuan.liu
 * @since 2015.12.10
 */
public class ThirdPartyLoaderClient implements ThirdPartyLoader {
    private static final Logger logger = LoggerFactory.getLogger(ThirdPartyLoaderClient.class);

    @ImportService(interfaceClass = ThirdPartyLoader.class)
    private ThirdPartyLoader remoteReference;

    @Override
    public LandingSource loadLandingSource(String sourceName, String sourceUid) {
        if (StringUtils.isBlank(sourceName) || StringUtils.isBlank(sourceUid)) {
            return null;
        }

        String key = LandingSource.ck_sourceName_sourceUid(sourceName, sourceUid);

        LandingSource landingSource = UserCache.getUserCache().load(key);
        if (landingSource == null) {
            try {
                landingSource = remoteReference.loadLandingSource(sourceName, sourceUid);
            } catch (Exception ex) {
                logger.error("FAILED TO LOAD LANDING SOURCE FROM BE", ex);
            }
        }
        return landingSource;
    }

    @Override
    public List<LandingSource> loadLandingSource(Long userId, String sourceName) {
        if (userId == null || StringUtils.isBlank(sourceName)) {
            return Collections.emptyList();
        }

        String key = LandingSource.ck_userId_sourceName(userId, sourceName);

        List<LandingSource> landingSource = UserCache.getUserCache().load(key);
        if (landingSource == null) {
            try {
                landingSource = remoteReference.loadLandingSource(userId, sourceName);
            } catch (Exception ex) {
                landingSource = Collections.emptyList();
                logger.error("FAILED TO LOAD LANDING SOURCE FROM BE", ex);
            }
        }
        return landingSource;
    }
}
