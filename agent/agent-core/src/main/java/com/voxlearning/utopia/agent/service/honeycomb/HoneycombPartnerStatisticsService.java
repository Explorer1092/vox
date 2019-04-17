package com.voxlearning.utopia.agent.service.honeycomb;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.agent.DayUtils;
import com.voxlearning.utopia.agent.bean.honeycomb.HoneycombPartnerStatisticsData;
import com.voxlearning.utopia.agent.constants.AgentConstants;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.partner.AgentPartnerService;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Named
public class HoneycombPartnerStatisticsService {

    @Inject
    private BaseOrgService baseOrgService;
    @Inject
    private AgentPartnerService agentPartnerService;

    public List<HoneycombPartnerStatisticsData> getGroupStatisticsData(Collection<Long> groupIds, Collection<Integer> days){
        List<HoneycombPartnerStatisticsData> resultList = new ArrayList<>();
        if(CollectionUtils.isEmpty(groupIds)){
            return resultList;
        }

        List<Future<HoneycombPartnerStatisticsData>> futureList = new ArrayList<>();
        for (Long groupId : groupIds) {
            futureList.add(AlpsThreadPool.getInstance().submit(() -> getGroupStatisticsData(groupId, days)));
        }

        for(Future<HoneycombPartnerStatisticsData> future : futureList) {
            try {
                HoneycombPartnerStatisticsData item = future.get();
                if(item != null){
                    resultList.add(item);
                }
            } catch (Exception e) {
            }
        }
        return resultList;
    }


    public HoneycombPartnerStatisticsData getGroupStatisticsData(Long groupId, Collection<Integer> days){
        AgentGroup group = baseOrgService.getGroupById(groupId);
        if(group == null){
            return null;
        }

        int totalCount = 0;
        if(CollectionUtils.isNotEmpty(days)){

            List<AgentGroupUser> groupUsers = baseOrgService.getAllGroupUsersByGroupId(group.getId());
            List<Long> userIds = groupUsers.stream().map(AgentGroupUser::getUserId).collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(userIds)) {
                Map<Long, Integer> dataMap = agentPartnerService.findPartnerNumByUserIds(userIds, DayUtils.getMinDate(days), DateUtils.addDays(DayUtils.getMaxDate(days), 1));
                if(MapUtils.isNotEmpty(dataMap)){
                    for(Integer count : dataMap.values()){
                        totalCount += SafeConverter.toInt(count);
                    }
                }
            }
        }

        return generateData(group.getId(), AgentConstants.INDICATOR_TYPE_GROUP, group.getGroupName(), totalCount);
    }



    public List<HoneycombPartnerStatisticsData> getUserStatisticsData(Collection<Long> userIds, Collection<Integer> days){
        List<HoneycombPartnerStatisticsData> resultList = new ArrayList<>();
        if(CollectionUtils.isEmpty(userIds)){
            return resultList;
        }

        List<Future<HoneycombPartnerStatisticsData>> futureList = new ArrayList<>();
        for (Long userId : userIds) {
            futureList.add(AlpsThreadPool.getInstance().submit(() -> getUserStatisticsData(userId, days)));
        }
        for(Future<HoneycombPartnerStatisticsData> future : futureList) {
            try {
                HoneycombPartnerStatisticsData item = future.get();
                if(item != null){
                    resultList.add(item);
                }
            } catch (Exception e) {
            }
        }
        return resultList;
    }

    public HoneycombPartnerStatisticsData getUserStatisticsData(Long userId, Collection<Integer> days){
        AgentUser user = baseOrgService.getUser(userId);
        if(user == null){
            return null;
        }

        int totalCount = 0;

        if(CollectionUtils.isNotEmpty(days)){
            Map<Long, Integer> dataMap = agentPartnerService.findPartnerNumByUserIds(Collections.singleton(userId), DayUtils.getMinDate(days), DateUtils.addDays(DayUtils.getMaxDate(days), 1));
            if(MapUtils.isNotEmpty(dataMap)){
                for(Integer count : dataMap.values()){
                    totalCount += SafeConverter.toInt(count);
                }
            }
        }

        return generateData(user.getId(), AgentConstants.INDICATOR_TYPE_USER, user.getRealName(), totalCount);
    }

    private HoneycombPartnerStatisticsData generateData(Long id, Integer idType, String name, int totalCount){
        HoneycombPartnerStatisticsData data = new HoneycombPartnerStatisticsData();
        data.setId(id);
        data.setIdType(idType);
        data.setName(name);
        data.setTotalCount(totalCount);
        return data;
    }
}
