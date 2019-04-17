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

package com.voxlearning.utopia.business.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Deprecated
@ServiceVersion(version = "1.1")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
@CyclopsMonitor("utopia")
public interface BusinessHomeworkService extends IPingable {

    /**
     * 组卷页面获取课本系列、课本、单元信息
     */
    List<Map<String, String>> getBookInfo(String type, String queryStr);

    MapMessage getReadingDraftByReadingId(Long readingId);

}
