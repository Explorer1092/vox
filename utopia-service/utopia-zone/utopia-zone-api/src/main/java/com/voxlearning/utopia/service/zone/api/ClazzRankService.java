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

package com.voxlearning.utopia.service.zone.api;

import com.voxlearning.alps.annotation.remote.Async;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.utopia.entity.tempactivity.ClazzRankHistory;
import com.voxlearning.utopia.entity.tempactivity.ClazzRankRewardHistory;

import java.util.List;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "2017.07.12")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface ClazzRankService {

    @Async
    AlpsFuture<List<ClazzRankHistory>> findClazzRankHistoriesByClazzId(Long clazzId);

    @Async
    AlpsFuture<List<ClazzRankHistory>> findClazzRankHistoriesByMonth(String month);

    @Async
    AlpsFuture<ClazzRankRewardHistory> insertClazzRankRewardHistory(ClazzRankRewardHistory history);

    @Async
    AlpsFuture<List<ClazzRankRewardHistory>> findClazzRankRewardHistoriesByUserId(Long userId);
}
