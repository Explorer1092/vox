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

package com.voxlearning.utopia.service.business.impl.manager;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.common.Spring;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.business.api.AgentManager;
import com.voxlearning.utopia.service.business.api.entity.DailyIncreasementData;
import com.voxlearning.utopia.service.business.api.entity.DailyIncreasementRegionData;
import com.voxlearning.utopia.service.business.api.entity.DayIncreaseDataStatus;
import com.voxlearning.utopia.service.business.impl.dao.DailyIncreasementDataDao;
import com.voxlearning.utopia.service.business.impl.dao.DailyIncreasementRegionDataDao;
import com.voxlearning.utopia.service.business.impl.persistence.DayIncreaseDataStatusPersistence;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Spring
@Named
@Service(interfaceClass = AgentManager.class)
@ExposeService(interfaceClass = AgentManager.class)
public class AgentManagerImpl extends SpringContainerSupport implements AgentManager {

    @Inject private DailyIncreasementDataDao dailyIncreasementDataDao;
    @Inject private DailyIncreasementRegionDataDao dailyIncreasementRegionDataDao;
    @Inject private DayIncreaseDataStatusPersistence dayIncreaseDataStatusPersistence;

    @Override
    public List<DailyIncreasementData> findDailyIncreasementDataListBySchool1(Integer start, Integer end, Set<Long> schoolSet) {
        return dailyIncreasementDataDao.findDailyIncreasementBySchool(start, end, schoolSet);
    }

    @Override
    public List<DailyIncreasementData> findDailyIncreasementDataListBySchool2(Integer start, Integer end, Set<Long> schoolSet, SchoolLevel schoolLevel) {
        return dailyIncreasementDataDao.findDailyIncreasementBySchool(start, end, schoolSet, schoolLevel);
    }

    @Override
    public List<DailyIncreasementData> findDailyIncreasementDataListByRegionCode1(Integer start, Integer end, Set<Integer> regionCodeSet) {
        return dailyIncreasementDataDao.findDailyIncreasementByRegionCode(start, end, regionCodeSet);
    }

    @Override
    public List<DailyIncreasementData> findDailyIncreasementDataListByRegionCode2(Integer start, Integer end, Set<Integer> regionCodeSet, SchoolLevel schoolLevel) {
        return dailyIncreasementDataDao.findDailyIncreasementByRegionCode(start, end, regionCodeSet, schoolLevel);
    }

    @Override
    public List<DailyIncreasementData> findDailyIncreasementDataList(Integer start, Integer end, Integer regionCode) {
        return dailyIncreasementDataDao.findDailyIncreasementData(start, end, regionCode);
    }

    @Override
    public List<DailyIncreasementRegionData> findDailyIncreasementRegionDataListByRegionCode1(Integer start, Integer end, Collection<Integer> regionCodeSet) {
        return dailyIncreasementRegionDataDao.findDailyIncreasementByRegionCode(start, end, regionCodeSet);
    }

    @Override
    public List<DailyIncreasementRegionData> findDailyIncreasementRegionDataListByRegionCode2(Integer start, Integer end, Collection<Integer> regionCodeSet, SchoolLevel schoolLevel) {
        return dailyIncreasementRegionDataDao.findDailyIncreasementByRegionCode(start, end, regionCodeSet, schoolLevel);
    }

    @Override
    public List<DailyIncreasementRegionData> findDailyIncreasementRegionDataListSortWithStudentAuth(Integer date, Set<Integer> regionSet) {
        return dailyIncreasementRegionDataDao.findDataSortWithStudentAuth(date, regionSet);
    }

    @Override
    public List<DailyIncreasementRegionData> findDailyIncreasementRegionDataList(Integer start, Integer end, Set<Integer> regionSet) {
        return dailyIncreasementRegionDataDao.findData(start, end, regionSet);
    }

    @Override
    public List<DailyIncreasementData> findByAreaAndSchools(Integer start, Integer end, Integer county, Collection<Long> schools) {
        return dailyIncreasementDataDao.findByAreaAndSchools(start, end, county, schools);
    }

    @Override
    public List<DayIncreaseDataStatus> findLastSuccess() {
        return dayIncreaseDataStatusPersistence.findLastSuccess();
    }

}
