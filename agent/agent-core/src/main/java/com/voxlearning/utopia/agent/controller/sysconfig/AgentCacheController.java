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

package com.voxlearning.utopia.agent.controller.sysconfig;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.agent.bean.DailyPerformanceInfo;
import com.voxlearning.utopia.agent.bean.indicator.sum.SumOfflineIndicator;
import com.voxlearning.utopia.agent.bean.indicator.sum.SumOfflineIndicatorWithBudget;
import com.voxlearning.utopia.agent.bean.indicator.sum.SumOnlineIndicator;
import com.voxlearning.utopia.agent.bean.indicator.sum.SumOnlineIndicatorWithBudget;
import com.voxlearning.utopia.agent.constants.AgentConstants;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.persist.entity.SchoolDayIncreaseData;
import com.voxlearning.utopia.agent.service.common.BaseDictService;
import com.voxlearning.utopia.agent.service.mobile.PerformanceService;
import com.voxlearning.utopia.agent.service.sysconfig.AgentDictSchoolService;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentDictSchool;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentDictSchoolLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentGroupLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentGroupUserLoaderClient;
import com.voxlearning.utopia.service.region.api.constant.RegionType;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AgentCacheController Agent缓存处理类
 *
 * @author song.wang
 * @date 2017/5/18
 */

@Controller
@RequestMapping("/sysconfig/cache")
public class AgentCacheController extends AbstractAgentController {

    @Inject private RaikouSystem raikouSystem;

    @Inject
    private AgentDictSchoolLoaderClient agentDictSchoolLoaderClient;
    @Inject
    private AgentGroupUserLoaderClient agentGroupUserLoaderClient;
    @Inject
    private AgentGroupLoaderClient agentGroupLoaderClient;
    @Inject
    private AgentDictSchoolService agentDictSchoolService;
    @Inject
    private PerformanceService performanceService;
    @Inject
    private BaseDictService baseDictService;

//    @Inject private SchoolMauIncreaseStatisticsService schoolMauIncreaseStatisticsService;

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String indexPage() {
        return "/sysconfig/cache/index";
    }

    @RequestMapping(value = "clear_performance_cache.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage clearPerformanceCache() {

        int day = getRequestInt("day", performanceService.lastSuccessDataDay());
        if (day < 20170501) {
            return MapMessage.errorMessage("输入日期不能小于20170501");
        }
////        clearSchoolDayIncreaseDataCache(Collections.singletonList(day));
////        clearDailyPerformanceInfoCache(Collections.singletonList(day));
        clearPerformanceDataCache(Collections.singletonList(day));
//        schoolMauIncreaseStatisticsService.clearSchoolMauIncreaseStatisticsCache();
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "clear_dict_school_cache.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage clearDictSchoolCache() {
        return clearAgentDictSchoolCache();
    }

    // 清除字典表缓存
    private MapMessage clearAgentDictSchoolCache() {
        // 清除区域维度的缓存
        List<ExRegion> exRegionList = raikouSystem.getRegionBuffer().loadRegions();
        Set<Integer> allCountyCodes = exRegionList.stream().filter(p -> p.fetchRegionType() == RegionType.COUNTY).map(ExRegion::getId).collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(allCountyCodes)) {
            List<String> keyList = new ArrayList<>();
            for (Integer countyCode : allCountyCodes) {
                keyList.add(AgentDictSchool.ck_county_code(countyCode));
                if (keyList.size() % 500 == 0) {
                    agentCacheSystem.CBS.flushable.delete(keyList);
                    keyList.clear();
                }
            }
            if (keyList.size() > 0) {
                agentCacheSystem.CBS.flushable.delete(keyList);
                keyList.clear();
            }
        }

        // 清除学校维度的缓存数据
        List<AgentDictSchool> allDictSchools = baseDictService.loadAllSchoolDictData();
        if (CollectionUtils.isNotEmpty(allDictSchools)) {
            List<String> keyList = new ArrayList<>();
            for (AgentDictSchool dictSchool : allDictSchools) {
                keyList.add(AgentDictSchool.ck_sId(dictSchool.getSchoolId()));
                if (keyList.size() % 500 == 0) {
                    agentCacheSystem.CBS.flushable.delete(keyList);
                    keyList.clear();
                }
            }
            if (keyList.size() > 0) {
                agentCacheSystem.CBS.flushable.delete(keyList);
                keyList.clear();
            }
        }
        return MapMessage.successMessage();
    }

    // 清除schoolDayIncreaseData缓存
    private MapMessage clearSchoolDayIncreaseDataCache(List<Integer> dayList) {

        Set<Long> allSchoolIds = new HashSet<>();
        Set<Integer> countyCodeList = new HashSet<>();
        List<AgentDictSchool> allDictSchools = baseDictService.loadAllSchoolDictData();
        if (CollectionUtils.isNotEmpty(allDictSchools)) {
            allDictSchools.forEach(p -> {
                if (p.getSchoolId() != null) {
                    allSchoolIds.add(p.getSchoolId());
                }
                if (p.getCountyCode() != null) {
                    countyCodeList.add(p.getCountyCode());
                }
            });
        }
        //清除学校维度的缓存数据
        List<String> keyList = new ArrayList<>();
        for (Long schoolId : allSchoolIds) {
            for (Integer day : dayList) {
                keyList.add(SchoolDayIncreaseData.ck_sid_day(schoolId, day));
                if (keyList.size() % 500 == 0) {
                    agentCacheSystem.CBS.flushable.delete(keyList);
                    keyList.clear();
                }
            }
        }

        //清除区域维度的缓存数据
        for (Integer countyCode : countyCodeList) {
            for (Integer day : dayList) {
                keyList.add(SchoolDayIncreaseData.ck_region_day(countyCode, day));
                if (keyList.size() % 500 == 0) {
                    agentCacheSystem.CBS.flushable.delete(keyList);
                    keyList.clear();
                }
            }
        }
        if (keyList.size() > 0) {
            agentCacheSystem.CBS.flushable.delete(keyList);
            keyList.clear();
        }

        return MapMessage.successMessage();
    }

    // 清除区域维度的业绩数据
    private MapMessage clearDailyPerformanceInfoCache(List<Integer> dayList) {

        //清除缓存里的 DailyPerformanceInfo 数据
        Set<Integer> allRegionCodeList = raikouSystem.getRegionBuffer().loadAllRegions().keySet().stream().map(Integer::valueOf).filter(p -> p != null).collect(Collectors.toSet());
        List<String> keyList = new ArrayList<>();
        for (Integer regionCode : allRegionCodeList) {
            for (Integer day : dayList) {
                keyList.add(DailyPerformanceInfo.ck_region_day(regionCode, day));
                if (keyList.size() % 500 == 0) {
                    agentCacheSystem.CBS.flushable.delete(keyList);
                    keyList.clear();
                }
            }
        }
        if (keyList.size() > 0) {
            agentCacheSystem.CBS.flushable.delete(keyList);
            keyList.clear();
        }
        return MapMessage.successMessage();
    }

    // 清除业绩数据
    private MapMessage clearPerformanceDataCache(List<Integer> dayList) {

//        // 清除学校业绩数据
        List<String> keyList = new ArrayList<>();

        // 清除用户的业绩数据
        List<AgentGroupUser> agentGroupUsers = agentGroupUserLoaderClient.findAll();
        if (CollectionUtils.isNotEmpty(agentGroupUsers)) {
            for (AgentGroupUser groupUser : agentGroupUsers) {
                for (Integer day : dayList) {
                    keyList.add(SumOnlineIndicator.ck_id_type_day_level(groupUser.getUserId(), AgentConstants.INDICATOR_TYPE_USER, day, 1));
                    keyList.add(SumOnlineIndicator.ck_id_type_day_level(groupUser.getUserId(), AgentConstants.INDICATOR_TYPE_USER, day, 24));
                    keyList.add(SumOnlineIndicatorWithBudget.ck_id_type_day_level(groupUser.getUserId(), AgentConstants.INDICATOR_TYPE_USER, day, 1));
                    keyList.add(SumOnlineIndicatorWithBudget.ck_id_type_day_level(groupUser.getUserId(), AgentConstants.INDICATOR_TYPE_USER, day, 24));

                    keyList.add(SumOfflineIndicator.ck_id_type_day_level(groupUser.getUserId(), AgentConstants.INDICATOR_TYPE_USER, day, 1));
                    keyList.add(SumOfflineIndicator.ck_id_type_day_level(groupUser.getUserId(), AgentConstants.INDICATOR_TYPE_USER, day, 24));
                    keyList.add(SumOfflineIndicatorWithBudget.ck_id_type_day_level(groupUser.getUserId(), AgentConstants.INDICATOR_TYPE_USER, day, 1));
                    keyList.add(SumOfflineIndicatorWithBudget.ck_id_type_day_level(groupUser.getUserId(), AgentConstants.INDICATOR_TYPE_USER, day, 24));
                    if (keyList.size() > 500) {
                        agentCacheSystem.CBS.flushable.delete(keyList);
                        keyList.clear();
                    }
                }
            }
        }

        // 清除部门的业绩数据
        List<AgentGroup> groupList = agentGroupLoaderClient.findAllGroups();
        if (CollectionUtils.isNotEmpty(groupList)) {
            for (AgentGroup group : groupList) {
                for (Integer day : dayList) {
                    keyList.add(SumOnlineIndicator.ck_id_type_day_level(group.getId(), AgentConstants.INDICATOR_TYPE_GROUP, day, 1));
                    keyList.add(SumOnlineIndicator.ck_id_type_day_level(group.getId(), AgentConstants.INDICATOR_TYPE_GROUP, day, 24));
                    keyList.add(SumOnlineIndicator.ck_id_type_day_level(group.getId(), AgentConstants.INDICATOR_TYPE_UNALLOCATED, day, 1));
                    keyList.add(SumOnlineIndicator.ck_id_type_day_level(group.getId(), AgentConstants.INDICATOR_TYPE_UNALLOCATED, day, 24));

                    keyList.add(SumOnlineIndicatorWithBudget.ck_id_type_day_level(group.getId(), AgentConstants.INDICATOR_TYPE_GROUP, day, 1));
                    keyList.add(SumOnlineIndicatorWithBudget.ck_id_type_day_level(group.getId(), AgentConstants.INDICATOR_TYPE_GROUP, day, 24));
                    keyList.add(SumOnlineIndicatorWithBudget.ck_id_type_day_level(group.getId(), AgentConstants.INDICATOR_TYPE_UNALLOCATED, day, 1));
                    keyList.add(SumOnlineIndicatorWithBudget.ck_id_type_day_level(group.getId(), AgentConstants.INDICATOR_TYPE_UNALLOCATED, day, 24));


                    keyList.add(SumOfflineIndicator.ck_id_type_day_level(group.getId(), AgentConstants.INDICATOR_TYPE_GROUP, day, 1));
                    keyList.add(SumOfflineIndicator.ck_id_type_day_level(group.getId(), AgentConstants.INDICATOR_TYPE_GROUP, day, 24));
                    keyList.add(SumOfflineIndicator.ck_id_type_day_level(group.getId(), AgentConstants.INDICATOR_TYPE_UNALLOCATED, day, 1));
                    keyList.add(SumOfflineIndicator.ck_id_type_day_level(group.getId(), AgentConstants.INDICATOR_TYPE_UNALLOCATED, day, 24));

                    keyList.add(SumOfflineIndicatorWithBudget.ck_id_type_day_level(group.getId(), AgentConstants.INDICATOR_TYPE_GROUP, day, 1));
                    keyList.add(SumOfflineIndicatorWithBudget.ck_id_type_day_level(group.getId(), AgentConstants.INDICATOR_TYPE_GROUP, day, 24));
                    keyList.add(SumOfflineIndicatorWithBudget.ck_id_type_day_level(group.getId(), AgentConstants.INDICATOR_TYPE_UNALLOCATED, day, 1));
                    keyList.add(SumOfflineIndicatorWithBudget.ck_id_type_day_level(group.getId(), AgentConstants.INDICATOR_TYPE_UNALLOCATED, day, 24));
                    if (keyList.size() > 500) {
                        agentCacheSystem.CBS.flushable.delete(keyList);
                        keyList.clear();
                    }
                }
            }
        }

        if (keyList.size() > 0) {
            agentCacheSystem.CBS.flushable.delete(keyList);
            keyList.clear();
        }

        return MapMessage.successMessage();
    }


}
