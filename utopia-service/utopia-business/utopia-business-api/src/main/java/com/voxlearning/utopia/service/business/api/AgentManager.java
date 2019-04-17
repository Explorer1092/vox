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

package com.voxlearning.utopia.service.business.api;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.remote.ServiceMethod;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.business.api.entity.DailyIncreasementData;
import com.voxlearning.utopia.service.business.api.entity.DailyIncreasementRegionData;
import com.voxlearning.utopia.service.business.api.entity.DayIncreaseDataStatus;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20160310")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 0)
@CyclopsMonitor("utopia")
public interface AgentManager extends IPingable {

    @ServiceMethod(timeout = 30, unit = TimeUnit.SECONDS, retries = 0)
    List<DailyIncreasementData> findDailyIncreasementDataListBySchool1(Integer start, Integer end, Set<Long> schoolSet);

    @ServiceMethod(timeout = 30, unit = TimeUnit.SECONDS, retries = 0)
    List<DailyIncreasementData> findDailyIncreasementDataListBySchool2(Integer start, Integer end, Set<Long> schoolSet, SchoolLevel schoolLevel);

    @ServiceMethod(timeout = 30, unit = TimeUnit.SECONDS, retries = 0)
    List<DailyIncreasementData> findDailyIncreasementDataListByRegionCode1(Integer start, Integer end, Set<Integer> regionCodeSet);

    @ServiceMethod(timeout = 30, unit = TimeUnit.SECONDS, retries = 0)
    List<DailyIncreasementData> findDailyIncreasementDataListByRegionCode2(Integer start, Integer end, Set<Integer> regionCodeSet, SchoolLevel schoolLevel);

    @ServiceMethod(timeout = 1, unit = TimeUnit.MINUTES, retries = 0)
    List<DailyIncreasementData> findDailyIncreasementDataList(Integer start, Integer end, Integer regionCode);

    @ServiceMethod(timeout = 30, unit = TimeUnit.SECONDS, retries = 0)
    List<DailyIncreasementRegionData> findDailyIncreasementRegionDataListByRegionCode1(Integer start, Integer end, Collection<Integer> regionCodeSet);

    @ServiceMethod(timeout = 30, unit = TimeUnit.SECONDS, retries = 0)
    List<DailyIncreasementRegionData> findDailyIncreasementRegionDataListByRegionCode2(Integer start, Integer end, Collection<Integer> regionCodeSet, SchoolLevel schoolLevel);

    @ServiceMethod(timeout = 30, unit = TimeUnit.SECONDS, retries = 0)
    List<DailyIncreasementRegionData> findDailyIncreasementRegionDataListSortWithStudentAuth(Integer date, Set<Integer> regionSet);

    @ServiceMethod(timeout = 1, unit = TimeUnit.MINUTES, retries = 0)
    List<DailyIncreasementRegionData> findDailyIncreasementRegionDataList(Integer start, Integer end, Set<Integer> regionSet);

    @ServiceMethod(timeout = 1, unit = TimeUnit.MINUTES, retries = 0)
    List<DailyIncreasementData> findByAreaAndSchools(Integer start, Integer end, Integer county, Collection<Long> schools);

    @ServiceMethod(timeout = 1, unit = TimeUnit.MINUTES, retries = 0)
    List<DayIncreaseDataStatus> findLastSuccess();

}
