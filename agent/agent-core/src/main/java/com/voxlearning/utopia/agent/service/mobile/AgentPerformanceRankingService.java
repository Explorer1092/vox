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

package com.voxlearning.utopia.agent.service.mobile;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.agent.bean.PerformanceRankingData;
import com.voxlearning.utopia.agent.dao.mongo.AgentPerformanceRankingDao;
import com.voxlearning.utopia.agent.persist.entity.AgentPerformanceRanking;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import lombok.Getter;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * AgentPerformanceRankingService
 *
 * @author song.wang
 * @date 2016/7/18
 */
@Named
public class AgentPerformanceRankingService extends AbstractAgentService {

    private static final String CONFIG_AGENT_PERFORMANCE_RANKING_DAY = "AGENT_PERFORMANCE_RANKING_DAY";

    @Inject private CommonConfigServiceClient commonConfigServiceClient;

    @Inject
    private AgentPerformanceRankingDao agentPerformanceRankingDao;
    @Inject
    private BaseOrgService baseOrgService;
    @Inject
    private PerformanceService performanceService;

    @Getter
    private Map<Integer, List<PerformanceRankingData>> rankingMap = new HashMap<>();
    private Integer rankingDataDay = 0;
    private Object lock = new Object();

    public PerformanceRankingData getRankingDataByUserId(Long userId, Integer type, Integer day) {
        // 根据配置判断是否锁定排行榜
        Integer fixedDay = SafeConverter.toInt(commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.getType(), CONFIG_AGENT_PERFORMANCE_RANKING_DAY));
        if (fixedDay > 0) {
            day = fixedDay;
        }

        AgentPerformanceRanking performanceRanking = agentPerformanceRankingDao.findByUserId(userId, type, day);

        if (performanceRanking == null) {
            return null;
        }

        PerformanceRankingData data = new PerformanceRankingData();
        data.setType(performanceRanking.getType());
        data.setUserId(performanceRanking.getUserId());
        data.setUserName(performanceRanking.getUserName());
        List<AgentGroup> groupList = baseOrgService.getUserGroups(performanceRanking.getUserId());
        AgentGroup group = null;
        if (CollectionUtils.isNotEmpty(groupList)) {
            group = groupList.stream().findFirst().get();
        }
        if (group != null) {
            data.setGroupId(group.getId());
            data.setGroupName(group.getGroupName());
        }
        data.setRanking(performanceRanking.getRanking());
        data.setRankingFloat(performanceRanking.getRankingFloat());
        data.setTotalCount(performanceRanking.getTotalCount());
        return data;
    }

    public List<PerformanceRankingData> getRankingDataList(Integer type) {
        int day = performanceService.lastSuccessDataDay();
        // 根据配置判断是否锁定排行榜
        Integer fixedDay = SafeConverter.toInt(commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.getType(), CONFIG_AGENT_PERFORMANCE_RANKING_DAY));
        if (fixedDay > 0) {
            day = fixedDay;
        }
        if (!Objects.equals(day, this.rankingDataDay)) {
            rankingMap.forEach((k, v) -> {
                if (v != null) {
                    v.clear();
                }
            });
            this.rankingDataDay = day;
        }

        List<PerformanceRankingData> rankingDataList = rankingMap.get(type);
        if (CollectionUtils.isNotEmpty(rankingDataList)) {
            return rankingDataList;
        }

        synchronized (lock) {
            rankingDataList = rankingMap.get(type);
            if (CollectionUtils.isNotEmpty(rankingDataList)) {
                return rankingDataList;
            }

            List<AgentPerformanceRanking> rankingList = agentPerformanceRankingDao.findByDay(type, day);
            if (CollectionUtils.isEmpty(rankingList)) {
                return Collections.emptyList();
            }
            Collections.sort(rankingList, (o1, o2) -> o1.getRanking() - o2.getRanking());
            rankingDataList = convertRankingList(rankingList);
            rankingMap.put(type, rankingDataList);
            return rankingDataList;
        }

    }

    private List<PerformanceRankingData> convertRankingList(List<AgentPerformanceRanking> rankingList) {
        if (CollectionUtils.isEmpty(rankingList)) {
            return Collections.emptyList();
        }
        List<PerformanceRankingData> rankingDataList = new ArrayList<>();
        for (AgentPerformanceRanking ranking : rankingList) {
            PerformanceRankingData data = new PerformanceRankingData();
            data.setType(ranking.getType());
            data.setUserId(ranking.getUserId());
            data.setUserName(ranking.getUserName());
            data.setGroupId(ranking.getGroupId());
            data.setGroupName(ranking.getGroupName());
            data.setRanking(ranking.getRanking());
            data.setRankingFloat(ranking.getRankingFloat());
            data.setTotalCount(ranking.getTotalCount());
            rankingDataList.add(data);
        }
        return rankingDataList;
    }


    public List<PerformanceRankingData> getCurrentRegionRankingList(Integer type, Long currentUserId, AgentRoleType roleType) {

        List<Long> groupIdList = baseOrgService.getGroupIdListByUserId(currentUserId);
        if (CollectionUtils.isEmpty(groupIdList)) {
            return Collections.emptyList();
        }
        Long groupId = groupIdList.get(0);
        // 获取当前用户所在的大区级部门
        AgentGroup group = baseOrgService.getParentGroupByRole(groupId, AgentGroupRoleType.Region);
        if (group == null) {
            return Collections.emptyList();
        }
        // 获取同大区下面指定角色的用户
        List<Long> userIdList = baseOrgService.getAllSubGroupUserIdsByGroupIdAndRole(group.getId(), roleType.getId());

        int day = performanceService.lastSuccessDataDay();
        // 根据配置判断是否锁定排行榜
        Integer fixedDay = SafeConverter.toInt(commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.getType(), CONFIG_AGENT_PERFORMANCE_RANKING_DAY));
        if (fixedDay > 0) {
            day = fixedDay;
        }

        Map<Long, AgentPerformanceRanking> rankingMap = agentPerformanceRankingDao.findByUserIds(userIdList, type, day);
        if (MapUtils.isEmpty(rankingMap)) {
            return Collections.emptyList();
        }
        List<AgentPerformanceRanking> rankingList = new ArrayList<>(rankingMap.values());
        Collections.sort(rankingList, (o1, o2) -> o1.getRanking() - o2.getRanking());
        return convertRankingList(rankingList);
    }

    public List<PerformanceRankingData> searchRankingListByUserName(Integer type, String name) {
        int day = performanceService.lastSuccessDataDay();
        // 根据配置判断是否锁定排行榜
        Integer fixedDay = SafeConverter.toInt(commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.getType(), CONFIG_AGENT_PERFORMANCE_RANKING_DAY));
        if (fixedDay > 0) {
            day = fixedDay;
        }

        List<AgentPerformanceRanking> rankingList = agentPerformanceRankingDao.findByUserName(type, name, day);

        if (CollectionUtils.isEmpty(rankingList)) {
            return Collections.emptyList();
        }
        return convertRankingList(rankingList);

    }


    public List<PerformanceRankingData> getManagedUsersRankingDateList(Long userId, Integer day) {

        // 根据配置判断是否锁定排行榜
        Integer fixedDay = SafeConverter.toInt(commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.getType(), CONFIG_AGENT_PERFORMANCE_RANKING_DAY));
        if (fixedDay > 0) {
            day = fixedDay;
        }

        List<AgentUser> userList = baseOrgService.getManagedGroupUsers(userId, false);
        if (CollectionUtils.isEmpty(userList)) {
            return Collections.emptyList();
        }

        List<PerformanceRankingData> retList = new ArrayList<>();
        for (AgentUser user : userList) {
            AgentRoleType role = baseOrgService.getUserRole(user.getId());
            if (role == null) {
                continue;
            }
            int rankingType = getRankingType(role);
            PerformanceRankingData rankingData = getRankingDataByUserId(user.getId(), rankingType, day);
            if (rankingData == null) {
                continue;
            }
            List<PerformanceRankingData> subordinateDataList = getManagedUsersRankingDateList(user.getId(), day);
            if (CollectionUtils.isNotEmpty(subordinateDataList)) {
                rankingData.setSubordinateDataList(subordinateDataList);
            }
            retList.add(rankingData);
        }
        return retList;
    }

    private Integer getRankingType(AgentRoleType roleType) {
        Integer type = 3;
        if (roleType == AgentRoleType.Region) {
            type = 1;
        } else if (roleType == AgentRoleType.CityManager) {
            type = 2;
        } else {
            type = 3;
        }
        return type;
    }


}
