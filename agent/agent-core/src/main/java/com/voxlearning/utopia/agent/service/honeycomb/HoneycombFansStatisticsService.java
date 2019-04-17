package com.voxlearning.utopia.agent.service.honeycomb;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilder;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.utopia.agent.bean.honeycomb.HoneycombFansStatisticsData;
import com.voxlearning.utopia.agent.constants.AgentConstants;
import com.voxlearning.utopia.agent.persist.entity.honeycomb.HoneycombFans;
import com.voxlearning.utopia.agent.persist.entity.honeycomb.HoneycombFansStatistics;
import com.voxlearning.utopia.agent.persist.honeycomb.HoneycombFansStatisticsDao;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.core.utils.MQUtils;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Named
public class HoneycombFansStatisticsService {

    @Inject
    private HoneycombFansStatisticsDao fansStatisticsDao;
    @Inject
    private BaseOrgService baseOrgService;
    @Inject
    private HoneycombUserService honeycombUserService;

    public void fansStatistics(HoneycombFans fans){
        if(fans == null ||  fans.getHoneycombId() == null || fans.getFansTime() == null){
            return ;
        }

        Integer day = SafeConverter.toInt(DateUtils.dateToString(fans.getFansTime(), "yyyyMMdd"));
        AtomicCallbackBuilder<MapMessage> builder = AtomicCallbackBuilderFactory.getInstance().newBuilder();
        builder.keyPrefix(this.getClass().getSimpleName() + "fansStatistics")
            .keys(fans.getHoneycombId(), day)
            .callback(() -> {
                HoneycombFansStatistics statistics = fansStatisticsDao.loadByUidAndDay(fans.getHoneycombId(), day);

                if (statistics == null) {
                    statistics = new HoneycombFansStatistics();
                    statistics.setHoneycombId(fans.getHoneycombId());
                    statistics.setDay(day);
                }
                statistics.setCount(SafeConverter.toInt(statistics.getCount()) + 1);
                fansStatisticsDao.upsert(statistics);
                return MapMessage.successMessage();
            })
            .build()
            .execute();

    }


    public List<HoneycombFansStatisticsData> getGroupStatisticsData(Collection<Long> groupIds, Collection<Integer> days){
        List<HoneycombFansStatisticsData> resultList = new ArrayList<>();
        if(CollectionUtils.isEmpty(groupIds)){
            return resultList;
        }

        List<Future<HoneycombFansStatisticsData>> futureList = new ArrayList<>();
        for (Long groupId : groupIds) {
            futureList.add(AlpsThreadPool.getInstance().submit(() -> getGroupStatisticsData(groupId, days)));
        }

        for(Future<HoneycombFansStatisticsData> future : futureList) {
            try {
                HoneycombFansStatisticsData item = future.get();
                if(item != null){
                    resultList.add(item);
                }
            } catch (Exception e) {
            }
        }
        return resultList;
    }


    public HoneycombFansStatisticsData getGroupStatisticsData(Long groupId, Collection<Integer> days){
        AgentGroup group = baseOrgService.getGroupById(groupId);
        if(group == null){
            return null;
        }

        List<HoneycombFansStatistics> totalDataList = new ArrayList<>();         //

        if(CollectionUtils.isNotEmpty(days)){

            List<AgentGroupUser> groupUsers = baseOrgService.getAllGroupUsersByGroupId(group.getId());
            List<Long> userIds = groupUsers.stream().map(AgentGroupUser::getUserId).collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(userIds)) {

                // 获取专员对应的蜂巢账号
                Map<Long, List<Long>> userListMap = honeycombUserService.getHoneycombUserIds(userIds);
                if(MapUtils.isNotEmpty(userListMap)){
                    Set<Long> honeycombIds = userListMap.values().stream().flatMap(List::stream).collect(Collectors.toSet());
                    List<HoneycombFansStatistics> dataList = fansStatisticsDao.loadByUsersAndDays(honeycombIds, days);
                    if(CollectionUtils.isNotEmpty(dataList)){
                        totalDataList.addAll(dataList);
                    }
                }
            }
        }

        return generateData(group.getId(), AgentConstants.INDICATOR_TYPE_GROUP, group.getGroupName(), totalDataList);
    }

    public List<HoneycombFansStatisticsData> getUserStatisticsData(Collection<Long> userIds, Collection<Integer> days){
        List<HoneycombFansStatisticsData> resultList = new ArrayList<>();
        if(CollectionUtils.isEmpty(userIds)){
            return resultList;
        }

        List<Future<HoneycombFansStatisticsData>> futureList = new ArrayList<>();
        for (Long userId : userIds) {
            futureList.add(AlpsThreadPool.getInstance().submit(() -> getUserStatisticsData(userId, days)));
        }
        for(Future<HoneycombFansStatisticsData> future : futureList) {
            try {
                HoneycombFansStatisticsData item = future.get();
                if(item != null){
                    resultList.add(item);
                }
            } catch (Exception e) {
            }
        }
        return resultList;
    }

    public HoneycombFansStatisticsData getUserStatisticsData(Long userId, Collection<Integer> days){
        AgentUser user = baseOrgService.getUser(userId);
        if(user == null){
            return null;
        }

        List<HoneycombFansStatistics> totalDataList = new ArrayList<>();

        if(CollectionUtils.isNotEmpty(days)){

            List<Long> honeycombIds = honeycombUserService.getHoneycombUserIds(userId);
            if(CollectionUtils.isNotEmpty(honeycombIds)){
                List<HoneycombFansStatistics> dataList = fansStatisticsDao.loadByUsersAndDays(honeycombIds, days);
                if(CollectionUtils.isNotEmpty(dataList)){
                    totalDataList.addAll(dataList);
                }
            }
        }

        return generateData(user.getId(), AgentConstants.INDICATOR_TYPE_USER, user.getRealName(), totalDataList);
    }

    private HoneycombFansStatisticsData generateData(Long id, Integer idType, String name, List<HoneycombFansStatistics> totalDataList){
        HoneycombFansStatisticsData data = new HoneycombFansStatisticsData();
        data.setId(id);
        data.setIdType(idType);
        data.setName(name);

        int totalCount = 0;
        for(HoneycombFansStatistics statistics : totalDataList){
            totalCount += SafeConverter.toInt(statistics.getCount());
        }

        data.setTotalCount(totalCount);
        return data;
    }
}
