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

package com.voxlearning.utopia.service.business.consumer;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.annotation.remote.ServiceMethod;
import com.voxlearning.utopia.service.business.api.AgentManager;
import com.voxlearning.utopia.service.business.api.entity.DailyIncreasementData;
import com.voxlearning.utopia.service.business.api.entity.DailyIncreasementRegionData;
import com.voxlearning.utopia.service.business.api.entity.DayIncreaseDataStatus;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class AgentManagerClient implements AgentManager {

    @ImportService(interfaceClass = AgentManager.class)
    private AgentManager remoteReference;

    @Override
    @ServiceMethod(timeout = 30, unit = TimeUnit.SECONDS, retries = 0)
    public List<DailyIncreasementData> findDailyIncreasementDataListBySchool1(Integer start, Integer end, Set<Long> schoolSet) {
        return remoteReference.findDailyIncreasementDataListBySchool1(start, end, schoolSet);
    }

    @Override
    @ServiceMethod(timeout = 30, unit = TimeUnit.SECONDS, retries = 0)
    public List<DailyIncreasementData> findDailyIncreasementDataListBySchool2(Integer start, Integer end, Set<Long> schoolSet, SchoolLevel schoolLevel) {
        return remoteReference.findDailyIncreasementDataListBySchool2(start, end, schoolSet, schoolLevel);
    }

    @Override
    @ServiceMethod(timeout = 30, unit = TimeUnit.SECONDS, retries = 0)
    public List<DailyIncreasementData> findDailyIncreasementDataListByRegionCode1(Integer start, Integer end, Set<Integer> regionCodeSet) {
        return remoteReference.findDailyIncreasementDataListByRegionCode1(start, end, regionCodeSet);
    }

    @Override
    @ServiceMethod(timeout = 30, unit = TimeUnit.SECONDS, retries = 0)
    public List<DailyIncreasementData> findDailyIncreasementDataListByRegionCode2(Integer start, Integer end, Set<Integer> regionCodeSet, SchoolLevel schoolLevel) {
        return remoteReference.findDailyIncreasementDataListByRegionCode2(start, end, regionCodeSet, schoolLevel);
    }

    @Override
    @ServiceMethod(timeout = 1, unit = TimeUnit.MINUTES, retries = 0)
    public List<DailyIncreasementData> findDailyIncreasementDataList(Integer start, Integer end, Integer regionCode) {
        return remoteReference.findDailyIncreasementDataList(start, end, regionCode);
    }

    @Override
    @ServiceMethod(timeout = 30, unit = TimeUnit.SECONDS, retries = 0)
    public List<DailyIncreasementRegionData> findDailyIncreasementRegionDataListByRegionCode1(Integer start, Integer end, Collection<Integer> regionCodeSet) {
        return remoteReference.findDailyIncreasementRegionDataListByRegionCode1(start, end, regionCodeSet);
    }

    @Override
    @ServiceMethod(timeout = 30, unit = TimeUnit.SECONDS, retries = 0)
    public List<DailyIncreasementRegionData> findDailyIncreasementRegionDataListByRegionCode2(Integer start, Integer end, Collection<Integer> regionCodeSet, SchoolLevel schoolLevel) {
        return remoteReference.findDailyIncreasementRegionDataListByRegionCode2(start, end, regionCodeSet, schoolLevel);
    }

    @Override
    @ServiceMethod(timeout = 30, unit = TimeUnit.SECONDS, retries = 0)
    public List<DailyIncreasementRegionData> findDailyIncreasementRegionDataListSortWithStudentAuth(Integer date, Set<Integer> regionSet) {
        return remoteReference.findDailyIncreasementRegionDataListSortWithStudentAuth(date, regionSet);
    }

    @Override
    @ServiceMethod(timeout = 1, unit = TimeUnit.MINUTES, retries = 0)
    public List<DailyIncreasementRegionData> findDailyIncreasementRegionDataList(Integer start, Integer end, Set<Integer> regionSet) {
        return remoteReference.findDailyIncreasementRegionDataList(start, end, regionSet);
    }

    @Override
    @ServiceMethod(timeout = 1, unit = TimeUnit.MINUTES, retries = 0)
    public List<DailyIncreasementData> findByAreaAndSchools(Integer start, Integer end, Integer county, Collection<Long> schools) {
        return remoteReference.findByAreaAndSchools(start, end, county, schools);
    }

    @Override
    @ServiceMethod(timeout = 1, unit = TimeUnit.MINUTES, retries = 0)
    public List<DayIncreaseDataStatus> findLastSuccess() {
        return remoteReference.findLastSuccess();
    }
}
