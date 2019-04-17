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

package com.voxlearning.utopia.service.zone.api;

import com.voxlearning.alps.annotation.remote.Idempotent;
import com.voxlearning.alps.annotation.remote.NoResponseWait;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.user.api.entities.Clazz;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Clazz zone loader interface definitions.
 *
 * @author Xiaohai Zhang
 * @since Feb 25, 2015
 */
@ServiceVersion(version = "20170204")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
@Deprecated
public interface ZoneLoader extends IPingable {

    @Idempotent
    List<Map<String, Object>> likeCountRank(Clazz clazz, Long userId);

    @Idempotent
    List<Map<String, Object>> studyMasterCountRank(Clazz clazz, Long userId);

    @Idempotent
    List<Map<String, Object>> silverRank(Clazz clazz, Long userId);

    @NoResponseWait
    void rankSnapshot(Long clazzId);

    @Idempotent
    List<Map<String, Object>> studyMasterCountRankSnapshot(Clazz clazz, Long userId);

    @Idempotent
    List<Map<String, Object>> silverRankSnapshot(Clazz clazz, Long userId);

}
