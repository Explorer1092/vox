package com.voxlearning.utopia.agent.service.honeycomb;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilder;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.utopia.agent.bean.honeycomb.HoneycombOrderStatisticsData;
import com.voxlearning.utopia.agent.constants.AgentConstants;
import com.voxlearning.utopia.agent.persist.entity.activity.ActivityCardStatistics;
import com.voxlearning.utopia.agent.persist.entity.activity.ActivityOrderStatistics;
import com.voxlearning.utopia.agent.persist.entity.honeycomb.HoneycombOrder;
import com.voxlearning.utopia.agent.persist.entity.honeycomb.HoneycombOrderStatistics;
import com.voxlearning.utopia.agent.persist.honeycomb.HoneycombOrderStatisticsDao;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.partner.AgentPartnerService;
import com.voxlearning.utopia.agent.view.activity.ActivityCardStatisticsView;
import com.voxlearning.utopia.agent.view.activity.ActivityOrderStatisticsView;
import com.voxlearning.utopia.core.utils.MQUtils;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

@Named
public class HoneycombOrderStatisticsService {

    @Inject
    private HoneycombOrderStatisticsDao orderStatisticsDao;
    @Inject
    private BaseOrgService baseOrgService;
    @Inject
    private HoneycombUserService honeycombUserService;
    @Inject
    private AgentPartnerService agentPartnerService;


    public void orderStatistics(HoneycombOrder order){
        if(order == null || StringUtils.isBlank(order.getActivityId()) || order.getHoneycombId() == null || order.getPayTime() == null){
            return;
        }
        Integer day = SafeConverter.toInt(DateUtils.dateToString(order.getPayTime(), "yyyyMMdd"));
        AtomicCallbackBuilder<MapMessage> builder = AtomicCallbackBuilderFactory.getInstance().newBuilder();

        builder.keyPrefix(this.getClass().getSimpleName() + "orderStatistics")
            .keys(order.getHoneycombId(), day, order.getActivityId())
            .callback(() -> {

                HoneycombOrderStatistics orderStatistics = orderStatisticsDao.loadByUidAndDay(order.getHoneycombId(), day, order.getActivityId());

                if(orderStatistics == null){
                    orderStatistics = new HoneycombOrderStatistics();
                    orderStatistics.setActivityId(order.getActivityId());
                    orderStatistics.setHoneycombId(order.getHoneycombId());
                    orderStatistics.setDay(day);
                }
                orderStatistics.setCount(SafeConverter.toInt(orderStatistics.getCount()) + 1);
                orderStatisticsDao.upsert(orderStatistics);
                return MapMessage.successMessage();
            })
            .build()
            .execute();

    }





    public List<HoneycombOrderStatisticsData> getGroupStatisticsData(Collection<Long> groupIds, Collection<Integer> days){
        return getGroupStatisticsData(null, groupIds, days);
    }

    public List<HoneycombOrderStatisticsData> getGroupStatisticsData(String activityId, Collection<Long> groupIds, Collection<Integer> days){
        List<HoneycombOrderStatisticsData> resultList = new ArrayList<>();
        if(CollectionUtils.isEmpty(groupIds)){
            return resultList;
        }

        List<Future<HoneycombOrderStatisticsData>> futureList = new ArrayList<>();
        for (Long groupId : groupIds) {
            futureList.add(AlpsThreadPool.getInstance().submit(() -> getGroupStatisticsData(activityId, groupId, days)));
        }

        for(Future<HoneycombOrderStatisticsData> future : futureList) {
            try {
                HoneycombOrderStatisticsData item = future.get();
                if(item != null){
                    resultList.add(item);
                }
            } catch (Exception e) {
            }
        }
        return resultList;
    }

    public HoneycombOrderStatisticsData getGroupStatisticsData(Long groupId, Collection<Integer> days){
        return getGroupStatisticsData(null, groupId, days);
    }

    public HoneycombOrderStatisticsData getGroupStatisticsData(String activityId, Long groupId, Collection<Integer> days){
        AgentGroup group = baseOrgService.getGroupById(groupId);
        if(group == null){
            return null;
        }

        List<HoneycombOrderStatistics> totalDataList = new ArrayList<>();         // 人员订单

        List<HoneycombOrderStatistics> targetDataList = new ArrayList<>();        // 异业订单统计

        if(CollectionUtils.isNotEmpty(days)){

            List<AgentGroupUser> groupUsers = baseOrgService.getAllGroupUsersByGroupId(group.getId());
            List<Long> userIds = groupUsers.stream().map(AgentGroupUser::getUserId).collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(userIds)) {

                // 获取专员对应的蜂巢账号
                Map<Long, List<Long>> userListMap = honeycombUserService.getHoneycombUserIds(userIds);
                if(MapUtils.isNotEmpty(userListMap)){
                    Set<Long> honeycombIds = userListMap.values().stream().flatMap(List::stream).collect(Collectors.toSet());
                    List<HoneycombOrderStatistics> dataList = getStatisticsDataList(activityId, honeycombIds, days);
                    if(CollectionUtils.isNotEmpty(dataList)){
                        totalDataList.addAll(dataList);
                    }
                }

                // 获取异业机构的联系人（专员蜂巢用户ID下的指定粉丝）
                Map<Long, List<Long>> honeycombListMap = agentPartnerService.findHoneycomIdsByUserIds(userIds);
                if(MapUtils.isNotEmpty(honeycombListMap)) {
                    Set<Long> targetHoneycombIds = honeycombListMap.values().stream().flatMap(List::stream).collect(Collectors.toSet());
                    if (CollectionUtils.isNotEmpty(targetHoneycombIds)) {
                        List<HoneycombOrderStatistics> dataList = getStatisticsDataList(activityId, targetHoneycombIds, days);
                        if (CollectionUtils.isNotEmpty(dataList)) {
                            targetDataList.addAll(dataList);
                        }
                    }
                }

            }
        }

        return generateData(group.getId(), AgentConstants.INDICATOR_TYPE_GROUP, group.getGroupName(), totalDataList, targetDataList);
    }

    public List<HoneycombOrderStatisticsData> getUserStatisticsData(Collection<Long> userIds, Collection<Integer> days){
        return getUserStatisticsData(null, userIds, days);
    }

    public List<HoneycombOrderStatisticsData> getUserStatisticsData(String activityId, Collection<Long> userIds, Collection<Integer> days){
        List<HoneycombOrderStatisticsData> resultList = new ArrayList<>();
        if(CollectionUtils.isEmpty(userIds)){
            return resultList;
        }

        List<Future<HoneycombOrderStatisticsData>> futureList = new ArrayList<>();
        for (Long userId : userIds) {
            futureList.add(AlpsThreadPool.getInstance().submit(() -> getUserStatisticsData(activityId, userId, days)));
        }
        for(Future<HoneycombOrderStatisticsData> future : futureList) {
            try {
                HoneycombOrderStatisticsData item = future.get();
                if(item != null){
                    resultList.add(item);
                }
            } catch (Exception e) {
            }
        }
        return resultList;
    }

    public HoneycombOrderStatisticsData getUserStatisticsData(Long userId, Collection<Integer> days){
        return getUserStatisticsData(null, userId, days);
    }

    public HoneycombOrderStatisticsData getUserStatisticsData(String activityId, Long userId, Collection<Integer> days){

        AgentUser user = baseOrgService.getUser(userId);
        if(user == null){
            return null;
        }

        List<HoneycombOrderStatistics> totalDataList = new ArrayList<>();
        List<HoneycombOrderStatistics> targetDataList = new ArrayList<>();

        if(CollectionUtils.isNotEmpty(days)){

            List<Long> honeycombIds = honeycombUserService.getHoneycombUserIds(userId);
            if(CollectionUtils.isNotEmpty(honeycombIds)){
                List<HoneycombOrderStatistics> dataList = getStatisticsDataList(activityId, honeycombIds, days);
                if(CollectionUtils.isNotEmpty(dataList)){
                    totalDataList.addAll(dataList);
                }
            }

            // 获取异业机构的联系人（专员蜂巢用户ID下的指定粉丝）
            Map<Long, List<Long>> honeycombListMap = agentPartnerService.findHoneycomIdsByUserIds(Collections.singleton(userId));
            if(MapUtils.isNotEmpty(honeycombListMap)) {
                Set<Long> targetHoneycombIds = honeycombListMap.values().stream().flatMap(List::stream).collect(Collectors.toSet());
                if (CollectionUtils.isNotEmpty(targetHoneycombIds)) {
                    List<HoneycombOrderStatistics> dataList = getStatisticsDataList(activityId, targetHoneycombIds, days);
                    if (CollectionUtils.isNotEmpty(dataList)) {
                        targetDataList.addAll(dataList);
                    }
                }
            }
        }

        return generateData(user.getId(), AgentConstants.INDICATOR_TYPE_USER, user.getRealName(), totalDataList, targetDataList);
    }

    private List<HoneycombOrderStatistics> getStatisticsDataList(String activityId, Collection<Long> honeycombIds, Collection<Integer> days){
        if(CollectionUtils.isEmpty(honeycombIds) || CollectionUtils.isEmpty(days)){
            return Collections.emptyList();
        }
        if(StringUtils.isNotBlank(activityId)){
            return orderStatisticsDao.loadByUsersAndDays(activityId, honeycombIds, days);
        }else {
            return orderStatisticsDao.loadByUsersAndDays(honeycombIds, days);
        }
    }


    private HoneycombOrderStatisticsData generateData(Long id, Integer idType, String name, List<HoneycombOrderStatistics> totalDataList, List<HoneycombOrderStatistics> targetDataList){
        HoneycombOrderStatisticsData data = new HoneycombOrderStatisticsData();
        data.setId(id);
        data.setIdType(idType);
        data.setName(name);

        int totalOrderCount = 0;
        for(HoneycombOrderStatistics statistics : totalDataList){
            totalOrderCount += SafeConverter.toInt(statistics.getCount());
        }

        data.setTotalCount(totalOrderCount);

        int targetOrderCount = 0;
        for(HoneycombOrderStatistics statistics : targetDataList){
            targetOrderCount += SafeConverter.toInt(statistics.getCount());
        }
        data.setTargetCount(targetOrderCount);
        return data;
    }



    public List<HoneycombOrderStatistics> getOrderStatistics(Collection<Long> userIds, Collection<Integer> days){
        return getOrderStatistics(null, userIds, days);
    }

    public List<HoneycombOrderStatistics> getOrderStatistics(String activityId, Collection<Long> userIds, Collection<Integer> days){
        Map<Long, List<Long>> userListMap = honeycombUserService.getHoneycombUserIds(userIds);
        if(MapUtils.isEmpty(userListMap)){
            return Collections.emptyList();
        }
        Set<Long> honeycombIds = userListMap.values().stream().flatMap(List::stream).collect(Collectors.toSet());
        return getStatisticsDataList(activityId, honeycombIds, days);
    }
}
