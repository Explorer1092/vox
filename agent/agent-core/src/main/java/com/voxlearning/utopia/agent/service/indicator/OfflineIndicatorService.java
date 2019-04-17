package com.voxlearning.utopia.agent.service.indicator;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.repackaged.org.apache.commons.beanutils.PropertyUtils;
import com.voxlearning.utopia.agent.athena.LoadNewSchoolServiceClient;
import com.voxlearning.utopia.agent.bean.indicator.sum.SumOfflineIndicator;
import com.voxlearning.utopia.agent.bean.indicator.sum.SumOfflineIndicatorWithBudget;
import com.voxlearning.utopia.agent.cache.AgentCacheSystem;
import com.voxlearning.utopia.agent.constants.AgentConstants;
import com.voxlearning.utopia.agent.constants.AgentKpiType;
import com.voxlearning.utopia.agent.persist.entity.AgentKpiBudget;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.service.budget.AgentKpiBudgetService;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.utils.AgentSchoolLevelUtils;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * OfflineIndicatorService
 *
 * @author song.wang
 * @date 2018/8/23
 */
@Named
public class OfflineIndicatorService extends AbstractAgentService {
    @Inject
    private AgentCacheSystem agentCacheSystem;
    @Inject
    private LoadNewSchoolServiceClient loadNewSchoolServiceClient;
    @Inject
    private BaseOrgService baseOrgService;
    @Inject
    private AgentKpiBudgetService agentKpiBudgetService;

    private final int cacheDays = 2;

    public Map<Long, SumOfflineIndicator> loadUserSumData(Collection<Long> userIds, Integer day, Collection<Integer> schoolLevels){
        return loadSumDataByIds(userIds, AgentConstants.INDICATOR_TYPE_USER, day, schoolLevels);
    }

    public Map<Long, SumOfflineIndicator> loadGroupSumData(Collection<Long> groupIds, Integer day, Collection<Integer> schoolLevels){
        return loadSumDataByIds(groupIds, AgentConstants.INDICATOR_TYPE_GROUP, day, schoolLevels);
    }

    private Map<Long, SumOfflineIndicator> loadSumDataByIds(Collection<Long> ids, Integer dataType, Integer day, Collection<Integer> schoolLevels){
        Map<Long, SumOfflineIndicator> resultMap = new HashMap<>();
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

        List<Future<Map<Long, SumOfflineIndicator>>> futureList = new ArrayList<>();
        for(List<Long> itemList : splitIds){
            futureList.add(AlpsThreadPool.getInstance().submit(() -> loadSumDataByIdsWithCache(itemList, dataType, day, schoolLevels)));
        }

        for(Future<Map<Long, SumOfflineIndicator>> future : futureList) {
            try {
                Map<Long, SumOfflineIndicator> subMap = future.get();
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

    private Map<Long, SumOfflineIndicator> loadSumDataByIdsWithCache(Collection<Long> ids, Integer dataType, Integer day, Collection<Integer> schoolLevels){
        Map<Long, SumOfflineIndicator> resultMap = new HashMap<>();
        if(CollectionUtils.isEmpty(ids) || day == null || dataType == null || CollectionUtils.isEmpty(schoolLevels)){
            return resultMap;
        }

        Integer schoolLevel = AgentSchoolLevelUtils.generateCompositeSchoolLevel(schoolLevels);
        List<Long> unCachedIdList = new ArrayList<>();
        ids.forEach(p -> {
            String cacheKey = SumOfflineIndicator.ck_id_type_day_level(p, dataType, day, schoolLevel);
            SumOfflineIndicator cacheObject = agentCacheSystem.CBS.flushable.load(cacheKey);
            if(cacheObject != null){
                resultMap.put(p, cacheObject);
                return;
            }
            unCachedIdList.add(p);
        });

        if(CollectionUtils.isNotEmpty(unCachedIdList)){
            Map<Long, SumOfflineIndicator> unCachedDataMap = loadNewSchoolServiceClient.loadSumOfflineIndicator(unCachedIdList, dataType, day, schoolLevels);

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
                    String cacheKey = SumOfflineIndicator.ck_id_type_day_level(k, v.getDataType(), v.getDay(), v.getSchoolLevel());
                    agentCacheSystem.CBS.flushable.set(cacheKey, SafeConverter.toInt(DateUtils.addDays(new Date(), this.cacheDays).getTime() / 1000), v);
                }
            });
        }
        return resultMap;
    }


    public SumOfflineIndicator loadGroupUnallocatedSumData(Long groupId, Integer day, Collection<Integer> schoolLevels){
        if(groupId == null || day == null || CollectionUtils.isEmpty(schoolLevels)){
            return null;
        }
        Integer schoolLevel = AgentSchoolLevelUtils.generateCompositeSchoolLevel(schoolLevels);

        String cacheKey = SumOfflineIndicator.ck_id_type_day_level(groupId, AgentConstants.INDICATOR_TYPE_UNALLOCATED, day, schoolLevel);
        SumOfflineIndicator cacheObject = agentCacheSystem.CBS.flushable.load(cacheKey);
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

        SumOfflineIndicator sumOfflineIndicator = loadNewSchoolServiceClient.loadSchoolSumOfflineIndicator(schoolIds, day);
        if(sumOfflineIndicator != null){  // 完善其他信息
            sumOfflineIndicator.setId(groupId);
            sumOfflineIndicator.setDataType(AgentConstants.INDICATOR_TYPE_UNALLOCATED);
            sumOfflineIndicator.setName("未分配");

            sumOfflineIndicator.setSchoolLevel(schoolLevel);
            if(sumOfflineIndicator.hasAllDimension()){
                agentCacheSystem.CBS.flushable.set(cacheKey, SafeConverter.toInt(DateUtils.addDays(new Date(), this.cacheDays).getTime() / 1000), sumOfflineIndicator);
            }
        }
        return sumOfflineIndicator;
    }

    public SumOfflineIndicatorWithBudget loadUserSumDataWithBudget(Long userId, Integer day, Collection<Integer> schoolLevels) {
        if(userId == null || day == null || CollectionUtils.isEmpty(schoolLevels)){
            return null;
        }
        Map<Long, SumOfflineIndicatorWithBudget> resultMap = loadUserSumDataWithBudget(Collections.singleton(userId), day, schoolLevels);
        return resultMap.get(userId);
    }

    public Map<Long, SumOfflineIndicatorWithBudget> loadUserSumDataWithBudget(Collection<Long> userIds, Integer day, Collection<Integer> schoolLevels){
        return loadSumDataWithBudgetByIds(userIds, AgentConstants.INDICATOR_TYPE_USER, day, schoolLevels);
    }

    public SumOfflineIndicatorWithBudget loadGroupSumDataWithBudget(Long groupId, Integer day, Collection<Integer> schoolLevels){
        if(groupId == null || day == null || CollectionUtils.isEmpty(schoolLevels)){
            return null;
        }
        Map<Long, SumOfflineIndicatorWithBudget> resultMap = loadGroupSumDataWithBudget(Collections.singleton(groupId), day, schoolLevels);
        return resultMap.get(groupId);
    }

    public Map<Long, SumOfflineIndicatorWithBudget> loadGroupSumDataWithBudget(Collection<Long> groupIds, Integer day, Collection<Integer> schoolLevels){
        return loadSumDataWithBudgetByIds(groupIds, AgentConstants.INDICATOR_TYPE_GROUP, day, schoolLevels);
    }

    private Map<Long, SumOfflineIndicatorWithBudget> loadSumDataWithBudgetByIds(Collection<Long> ids, Integer dataType, Integer day, Collection<Integer> schoolLevels){
        Map<Long, SumOfflineIndicatorWithBudget> resultMap = new HashMap<>();
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

        List<Future<Map<Long, SumOfflineIndicatorWithBudget>>> futureList = new ArrayList<>();
        for(List<Long> itemList : splitIds){
            futureList.add(AlpsThreadPool.getInstance().submit(() -> loadSumDataWithBudgetByIdsWithCache(itemList, dataType, day, schoolLevels)));
        }

        for(Future<Map<Long, SumOfflineIndicatorWithBudget>> future : futureList) {
            try {
                Map<Long, SumOfflineIndicatorWithBudget> subMap = future.get();
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

    private Map<Long, SumOfflineIndicatorWithBudget> loadSumDataWithBudgetByIdsWithCache(Collection<Long> ids, Integer dataType, Integer day, Collection<Integer> schoolLevels){
        Map<Long, SumOfflineIndicatorWithBudget> resultMap = new HashMap<>();
        if(CollectionUtils.isEmpty(ids) || day == null || dataType == null|| CollectionUtils.isEmpty(schoolLevels)){
            return resultMap;
        }

        Integer schoolLevel = AgentSchoolLevelUtils.generateCompositeSchoolLevel(schoolLevels);
        List<Long> unCachedIdList = new ArrayList<>();
        ids.forEach(p -> {
            String cacheKey = SumOfflineIndicatorWithBudget.ck_id_type_day_level(p, dataType, day, schoolLevel);
            SumOfflineIndicatorWithBudget cacheObject = agentCacheSystem.CBS.flushable.load(cacheKey);
            if(cacheObject != null){
                resultMap.put(p, cacheObject);
                return;
            }
            unCachedIdList.add(p);
        });

        if(CollectionUtils.isNotEmpty(unCachedIdList)){
            Map<Long, SumOfflineIndicatorWithBudget> unCachedDataMap = innerLoadSumDataWithBudgetByIds(unCachedIdList, dataType, day, schoolLevels);
            resultMap.putAll(unCachedDataMap);
            unCachedDataMap.forEach((k, v) -> {
                // 将业绩数据保存到缓存中
                if(v.hasAllDimension()){
                    String cacheKey = SumOfflineIndicatorWithBudget.ck_id_type_day_level(k, v.getDataType(), v.getDay(), v.getSchoolLevel());
                    agentCacheSystem.CBS.flushable.set(cacheKey, SafeConverter.toInt(DateUtils.addDays(new Date(), this.cacheDays).getTime() / 1000), v);
                }
            });
        }
        return resultMap;
    }

    private Map<Long, SumOfflineIndicatorWithBudget> innerLoadSumDataWithBudgetByIds(Collection<Long> ids, Integer dataType, Integer day, Collection<Integer> schoolLevels){
        Map<Long, SumOfflineIndicatorWithBudget> result = new HashMap<>();
        Map<Long, SumOfflineIndicator> sumDataMap = loadSumDataByIds(ids, dataType, day, schoolLevels);
        Integer month = day / 100;

        Map<Long, List<AgentKpiBudget>> allBudgetMap = loadBudgetByIds(ids, dataType, month);

        ids.forEach(p -> {
            SumOfflineIndicator sumData = sumDataMap.get(p);
            if(sumData == null){
                return;
            }
            SumOfflineIndicatorWithBudget resultItem = convertToSumDataWithBudget(sumData);

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




    public SumOfflineIndicatorWithBudget convertToSumDataWithBudget(SumOfflineIndicator sumData){
        if(sumData == null){
            return null;
        }
        SumOfflineIndicatorWithBudget result = new SumOfflineIndicatorWithBudget();
        try {
            PropertyUtils.copyProperties(result, sumData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public SumOfflineIndicatorWithBudget loadGroupUnallocatedSumDataWithBudget(Long groupId, Integer day, Collection<Integer> schoolLevels){
        return convertToSumDataWithBudget(loadGroupUnallocatedSumData(groupId, day, schoolLevels));
    }
}
