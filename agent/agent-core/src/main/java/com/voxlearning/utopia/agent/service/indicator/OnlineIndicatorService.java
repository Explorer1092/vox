package com.voxlearning.utopia.agent.service.indicator;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.beanutils.PropertyUtils;
import com.voxlearning.utopia.agent.athena.LoadNewSchoolServiceClient;
import com.voxlearning.utopia.agent.bean.indicator.sum.SumOnlineIndicator;
import com.voxlearning.utopia.agent.bean.indicator.sum.SumOnlineIndicatorWithBudget;
import com.voxlearning.utopia.agent.cache.AgentCacheSystem;
import com.voxlearning.utopia.agent.constants.AgentConstants;
import com.voxlearning.utopia.agent.constants.AgentKpiType;
import com.voxlearning.utopia.agent.persist.entity.AgentKpiBudget;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.service.budget.AgentKpiBudgetService;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.utils.AgentSchoolLevelUtils;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * OnlineIndicatorService
 *
 * @author song.wang
 * @date 2018/8/21
 */
@Named
public class OnlineIndicatorService extends AbstractAgentService {

    @Inject
    private AgentCacheSystem agentCacheSystem;
    @Inject
    private LoadNewSchoolServiceClient loadNewSchoolServiceClient;
    @Inject
    private BaseOrgService baseOrgService;
    @Inject
    private AgentKpiBudgetService agentKpiBudgetService;

    private final int cacheDays = 2;

    public Map<Long, SumOnlineIndicator> loadUserSumData(Collection<Long> userIds, Integer day, Collection<Integer> schoolLevels){
        return loadSumDataByIds(userIds, AgentConstants.INDICATOR_TYPE_USER, day, schoolLevels);
    }

    public Map<Long, SumOnlineIndicator> loadGroupSumData(Collection<Long> groupIds, Integer day, Collection<Integer> schoolLevels){
        return loadSumDataByIds(groupIds, AgentConstants.INDICATOR_TYPE_GROUP, day, schoolLevels);
    }

    private Map<Long, SumOnlineIndicator> loadSumDataByIds(Collection<Long> ids, Integer dataType, Integer day, Collection<Integer> schoolLevels){
        Map<Long, SumOnlineIndicator> resultMap = new HashMap<>();
        if(CollectionUtils.isEmpty(ids) || day == null || dataType == null || CollectionUtils.isEmpty(schoolLevels)){
            return resultMap;
        }

        List<List<Long>> splitIds = new ArrayList<>();
        if(ids.size() > 100){
            int count = ids.size() / 100 + 1;
            splitIds.addAll(CollectionUtils.splitList(new ArrayList<>(ids), count));
        }else {
            splitIds.add(new ArrayList<>((ids)));
        }

        List<Future<Map<Long, SumOnlineIndicator>>> futureList = new ArrayList<>();
        for(List<Long> itemList : splitIds){
            futureList.add(AlpsThreadPool.getInstance().submit(() -> loadSumDataByIdsWithCache(itemList, dataType, day, schoolLevels)));
        }

        for(Future<Map<Long, SumOnlineIndicator>> future : futureList) {
            try {
                Map<Long, SumOnlineIndicator> subMap = future.get();
                if(MapUtils.isNotEmpty(subMap)){
                    resultMap.putAll(subMap);
                }
            }catch (Exception e){
                String methodName = this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName();
                logger.error(methodName + " error", e);
            }
        }

        return resultMap;
    }

    private Map<Long, SumOnlineIndicator> loadSumDataByIdsWithCache(Collection<Long> ids, Integer dataType, Integer day, Collection<Integer> schoolLevels){
        Map<Long, SumOnlineIndicator> resultMap = new HashMap<>();
        if(CollectionUtils.isEmpty(ids) || day == null || dataType == null|| CollectionUtils.isEmpty(schoolLevels)){
            return resultMap;
        }

        Integer schoolLevel = AgentSchoolLevelUtils.generateCompositeSchoolLevel(schoolLevels);
        List<Long> unCachedIdList = new ArrayList<>();
        ids.forEach(p -> {
            String cacheKey = SumOnlineIndicator.ck_id_type_day_level(p, dataType, day, schoolLevel);
            SumOnlineIndicator cacheObject = agentCacheSystem.CBS.flushable.load(cacheKey);
            if(cacheObject != null){
                resultMap.put(p, cacheObject);
                return;
            }
            unCachedIdList.add(p);
        });

        if(CollectionUtils.isNotEmpty(unCachedIdList)){
            Map<Long, SumOnlineIndicator> unCachedDataMap = loadNewSchoolServiceClient.loadSumOnlineIndicator(unCachedIdList, dataType, day, schoolLevels);

            if(Objects.equals(dataType, AgentConstants.INDICATOR_TYPE_GROUP)) {
                // 设置部门级子部门中专员人数
                unCachedDataMap.forEach((k, v) -> {
                    List<AgentGroupUser> groupUserList = baseOrgService.getAllSubGroupUsersByGroupIdAndRole(k, AgentRoleType.BusinessDeveloper.getId());
                    if (CollectionUtils.isNotEmpty(groupUserList)) {
                        v.setHeadCount(groupUserList.size());
                    }
                });
            }

            resultMap.putAll(unCachedDataMap);
            unCachedDataMap.forEach((k, v) -> {
                // 将业绩数据保存到缓存中
                if(v.hasAllDimension()){
                    String cacheKey = SumOnlineIndicator.ck_id_type_day_level(k, v.getDataType(), v.getDay(), v.getSchoolLevel());
                    agentCacheSystem.CBS.flushable.set(cacheKey, SafeConverter.toInt(DateUtils.addDays(new Date(), this.cacheDays).getTime() / 1000), v);
                }
            });
        }
        return resultMap;
    }


    public SumOnlineIndicator loadGroupUnallocatedSumData(Long groupId, Integer day, Collection<Integer> schoolLevels){
        if(groupId == null || day == null || CollectionUtils.isEmpty(schoolLevels)){
            return null;
        }
        Integer schoolLevel = AgentSchoolLevelUtils.generateCompositeSchoolLevel(schoolLevels);

        String cacheKey = SumOnlineIndicator.ck_id_type_day_level(groupId, AgentConstants.INDICATOR_TYPE_UNALLOCATED, day, schoolLevel);
        SumOnlineIndicator cacheObject = agentCacheSystem.CBS.flushable.load(cacheKey);
        if(cacheObject != null){
            return cacheObject;
        }

        AgentGroup group = baseOrgService.getGroupById(groupId);
        if(group == null){
            return null;
        }
        List<Long> schoolIds = baseOrgService.fetchGroupUnallocatedSchools(groupId, schoolLevels);
        if(CollectionUtils.isEmpty(schoolIds)){
            return null;
        }

        SumOnlineIndicator sumOnlineIndicator = loadNewSchoolServiceClient.loadSchoolSumOnlineIndicator(schoolIds, day);
        if(sumOnlineIndicator != null){  // 完善其他信息
            sumOnlineIndicator.setId(groupId);
            sumOnlineIndicator.setDataType(AgentConstants.INDICATOR_TYPE_UNALLOCATED);
            sumOnlineIndicator.setName("未分配");

            sumOnlineIndicator.setSchoolLevel(schoolLevel);
            if(sumOnlineIndicator.hasAllDimension()){
                agentCacheSystem.CBS.flushable.set(cacheKey, SafeConverter.toInt(DateUtils.addDays(new Date(), this.cacheDays).getTime() / 1000), sumOnlineIndicator);
            }
        }
        return sumOnlineIndicator;
    }

    public SumOnlineIndicatorWithBudget loadUserSumDataWithBudget(Long userId, Integer day, Collection<Integer> schoolLevels) {
        if(userId == null || day == null || CollectionUtils.isEmpty(schoolLevels)){
            return null;
        }
        Map<Long, SumOnlineIndicatorWithBudget> resultMap = loadUserSumDataWithBudget(Collections.singleton(userId), day, schoolLevels);
        return resultMap.get(userId);
    }

    public Map<Long, SumOnlineIndicatorWithBudget> loadUserSumDataWithBudget(Collection<Long> userIds, Integer day, Collection<Integer> schoolLevels){
        return loadSumDataWithBudgetByIds(userIds, AgentConstants.INDICATOR_TYPE_USER, day, schoolLevels);
    }

    public SumOnlineIndicatorWithBudget loadGroupSumDataWithBudget(Long groupId, Integer day, Collection<Integer> schoolLevels){
        if(groupId == null || day == null || CollectionUtils.isEmpty(schoolLevels)){
            return null;
        }
        Map<Long, SumOnlineIndicatorWithBudget> resultMap = loadGroupSumDataWithBudget(Collections.singleton(groupId), day, schoolLevels);
        return resultMap.get(groupId);
    }

    public Map<Long, SumOnlineIndicatorWithBudget> loadGroupSumDataWithBudget(Collection<Long> groupIds, Integer day, Collection<Integer> schoolLevels){
        return loadSumDataWithBudgetByIds(groupIds, AgentConstants.INDICATOR_TYPE_GROUP, day, schoolLevels);
    }

    private Map<Long, SumOnlineIndicatorWithBudget> loadSumDataWithBudgetByIds(Collection<Long> ids, Integer dataType, Integer day, Collection<Integer> schoolLevels){
        Map<Long, SumOnlineIndicatorWithBudget> resultMap = new HashMap<>();
        if(CollectionUtils.isEmpty(ids) || day == null || dataType == null || CollectionUtils.isEmpty(schoolLevels)){
            return resultMap;
        }

        List<List<Long>> splitIds = new ArrayList<>();
        if(ids.size() > 100){
            int count = ids.size() / 100 + 1;
            splitIds.addAll(CollectionUtils.splitList(new ArrayList<>(ids), count));
        }else {
            splitIds.add(new ArrayList<>((ids)));
        }

        List<Future<Map<Long, SumOnlineIndicatorWithBudget>>> futureList = new ArrayList<>();
        for(List<Long> itemList : splitIds){
            futureList.add(AlpsThreadPool.getInstance().submit(() -> loadSumDataWithBudgetByIdsWithCache(itemList, dataType, day, schoolLevels)));
        }

        for(Future<Map<Long, SumOnlineIndicatorWithBudget>> future : futureList) {
            try {
                Map<Long, SumOnlineIndicatorWithBudget> subMap = future.get();
                if(MapUtils.isNotEmpty(subMap)){
                    resultMap.putAll(subMap);
                }
            }catch (Exception e){
                String methodName = this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName();
                logger.error(methodName + " error", e);
            }
        }

        return resultMap;
    }

    private Map<Long, SumOnlineIndicatorWithBudget> loadSumDataWithBudgetByIdsWithCache(Collection<Long> ids, Integer dataType, Integer day, Collection<Integer> schoolLevels){
        Map<Long, SumOnlineIndicatorWithBudget> resultMap = new HashMap<>();
        if(CollectionUtils.isEmpty(ids) || day == null || dataType == null|| CollectionUtils.isEmpty(schoolLevels)){
            return resultMap;
        }

        Integer schoolLevel = AgentSchoolLevelUtils.generateCompositeSchoolLevel(schoolLevels);
        List<Long> unCachedIdList = new ArrayList<>();
        ids.forEach(p -> {
            String cacheKey = SumOnlineIndicatorWithBudget.ck_id_type_day_level(p, dataType, day, schoolLevel);
            SumOnlineIndicatorWithBudget cacheObject = agentCacheSystem.CBS.flushable.load(cacheKey);
            if(cacheObject != null){
                resultMap.put(p, cacheObject);
                return;
            }
            unCachedIdList.add(p);
        });

        if(CollectionUtils.isNotEmpty(unCachedIdList)){
            Map<Long, SumOnlineIndicatorWithBudget> unCachedDataMap = innerLoadSumDataWithBudgetByIds(unCachedIdList, dataType, day, schoolLevels);
            resultMap.putAll(unCachedDataMap);
            unCachedDataMap.forEach((k, v) -> {
                // 将业绩数据保存到缓存中
                if(v.hasAllDimension()){
                    String cacheKey = SumOnlineIndicatorWithBudget.ck_id_type_day_level(k, v.getDataType(), v.getDay(), v.getSchoolLevel());
                    agentCacheSystem.CBS.flushable.set(cacheKey, SafeConverter.toInt(DateUtils.addDays(new Date(), this.cacheDays).getTime() / 1000), v);
                }
            });
        }
        return resultMap;
    }

    private Map<Long, SumOnlineIndicatorWithBudget> innerLoadSumDataWithBudgetByIds(Collection<Long> ids, Integer dataType, Integer day, Collection<Integer> schoolLevels){
        Map<Long, SumOnlineIndicatorWithBudget> result = new HashMap<>();
        Map<Long, SumOnlineIndicator> sumDataMap = loadSumDataByIds(ids, dataType, day, schoolLevels);
        Integer month = day / 100;

        Map<Long, List<AgentKpiBudget>> allBudgetMap = loadBudgetByIds(ids, dataType, month);

        ids.forEach(p -> {
            SumOnlineIndicator sumData = sumDataMap.get(p);
            if(sumData == null){
                return;
            }
            SumOnlineIndicatorWithBudget resultItem = convertToSumDataWithBudget(sumData);

            List<AgentKpiBudget> kpiBudgetList = allBudgetMap.get(p);
            if(CollectionUtils.isNotEmpty(kpiBudgetList)) {
                // 不区分预算是否已确认
                Map<AgentKpiType, Integer> budgetMap = kpiBudgetList.stream().collect(Collectors.toMap(AgentKpiBudget::getKpiType, AgentKpiBudget::getBudget, (o1, o2) -> o1));
                resultItem.getKpiBudgetMap().putAll(budgetMap);
            }
            result.put(p, resultItem);
        });
        return result;
    }

    private Map<Long, List<AgentKpiBudget>> loadBudgetByIds(Collection<Long> ids, Integer dataType, Integer month){
        Map<Long, List<AgentKpiBudget>> resultMap = new HashMap<>();
        if(CollectionUtils.isEmpty(ids) || dataType == null || month == null){
            return resultMap;
        }
        if(Objects.equals(dataType, AgentConstants.INDICATOR_TYPE_GROUP)){
            ids.forEach(p -> resultMap.put(p, agentKpiBudgetService.fetchGroupBudget(p, month)));
        }else if(Objects.equals(dataType, AgentConstants.INDICATOR_TYPE_USER)){
            Map<Long, List<Long>> userGroupIdList = baseOrgService.getUserGroupIdList(ids);
            ids.forEach(p -> {
                List<Long> groupIds = userGroupIdList.get(p);
                if(CollectionUtils.isNotEmpty(groupIds)){
                    resultMap.put(p, agentKpiBudgetService.fetchUserBudget(p, groupIds.get(0), month));
                }
            });
        }
        return resultMap;
    }


    public SumOnlineIndicatorWithBudget convertToSumDataWithBudget(SumOnlineIndicator sumData){
        if(sumData == null){
            return null;
        }
        SumOnlineIndicatorWithBudget result = new SumOnlineIndicatorWithBudget();
        try {
            PropertyUtils.copyProperties(result, sumData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    public SumOnlineIndicatorWithBudget loadGroupUnallocatedSumDataWithBudget(Long groupId, Integer day, Collection<Integer> schoolLevels){
        return convertToSumDataWithBudget(loadGroupUnallocatedSumData(groupId, day, schoolLevels));
    }

}
