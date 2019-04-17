package com.voxlearning.utopia.agent.service.indicator;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.agent.athena.LoadMarketHistoryServiceClient;
import com.voxlearning.utopia.agent.bean.indicator.history.HistoryOnlineIndicator;
import com.voxlearning.utopia.agent.bean.indicator.history.HistoryParentIndicator;
import com.voxlearning.utopia.agent.cache.AgentCacheSystem;
import com.voxlearning.utopia.agent.constants.AgentConstants;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.utils.AgentSchoolLevelUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.concurrent.Future;

@Named
public class HistoryIndicatorService extends AbstractAgentService {
    @Inject
    private LoadMarketHistoryServiceClient loadMarketHistoryServiceClient;
    @Inject
    private AgentCacheSystem agentCacheSystem;

    private final int cacheDays = 2;

    public Map<Integer, HistoryParentIndicator> loadUserParentHistory(Long userId, Collection<Integer> days, Collection<Integer> schoolLevels){
        return loadParentHistory(userId, AgentConstants.INDICATOR_TYPE_USER, days, schoolLevels);
    }

    public Map<Integer, HistoryParentIndicator> loadGroupParentHistory(Long groupId, Collection<Integer> days, Collection<Integer> schoolLevels){
        return loadParentHistory(groupId, AgentConstants.INDICATOR_TYPE_GROUP, days, schoolLevels);
    }

    public Map<Integer, HistoryOnlineIndicator> loadUserHwHistory(Long userId, Collection<Integer> days, Collection<Integer> schoolLevels){
        return loadHwHistory(userId, AgentConstants.INDICATOR_TYPE_USER, days, schoolLevels);
    }

    public Map<Integer, HistoryOnlineIndicator> loadGroupHwHistory(Long groupId, Collection<Integer> days, Collection<Integer> schoolLevels){
        return loadHwHistory(groupId, AgentConstants.INDICATOR_TYPE_GROUP, days, schoolLevels);
    }


    private Map<Integer, HistoryParentIndicator> loadParentHistory(Long id, Integer idType, Collection<Integer> days, Collection<Integer> schoolLevels){
        Map<Integer, HistoryParentIndicator> resultMap = new HashMap<>();
        if(CollectionUtils.isEmpty(days) || id == null || idType == null || CollectionUtils.isEmpty(schoolLevels)){
            return resultMap;
        }

        int count = days.size() / 10;
        List<List<Integer>> splitDays = new ArrayList<>(CollectionUtils.splitList(new ArrayList<>(days), count));

        List<Future<Map<Integer, HistoryParentIndicator>>> futureList = new ArrayList<>();
        for(List<Integer> itemDays : splitDays){
            futureList.add(AlpsThreadPool.getInstance().submit(() -> loadParentHistoryWithCache(id,idType,itemDays,schoolLevels)));
        }

        for(Future<Map<Integer, HistoryParentIndicator>> future : futureList) {
            try {
                Map<Integer, HistoryParentIndicator> subMap = future.get();
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

    private Map<Integer, HistoryParentIndicator> loadParentHistoryWithCache(Long id, Integer idType, Collection<Integer> days, Collection<Integer> schoolLevels){
        Map<Integer, HistoryParentIndicator> resultMap = new HashMap<>();
        if(CollectionUtils.isEmpty(days) || id == null || idType == null|| CollectionUtils.isEmpty(schoolLevels)){
            return resultMap;
        }

        Integer schoolLevel = AgentSchoolLevelUtils.generateCompositeSchoolLevel(schoolLevels);
        List<Integer> unCachedDayList = new ArrayList<>();
        days.forEach(day -> {
            String cacheKey = HistoryParentIndicator.ck_id_type_day_level(id, idType, day, schoolLevel);
            HistoryParentIndicator cacheObject = agentCacheSystem.CBS.flushable.load(cacheKey);
            if(cacheObject != null){
                resultMap.put(day, cacheObject);
                return;
            }
            unCachedDayList.add(day);
        });

        if(CollectionUtils.isNotEmpty(unCachedDayList)){
            Map<Integer, HistoryParentIndicator> unCachedDataMap = loadMarketHistoryServiceClient.loadParentHistory(id, idType, unCachedDayList, schoolLevels);
            resultMap.putAll(unCachedDataMap);
            unCachedDataMap.forEach((k, v) -> {
                // 将业绩数据保存到缓存中
                String cacheKey = HistoryParentIndicator.ck_id_type_day_level(id,idType, k, schoolLevel);
                agentCacheSystem.CBS.flushable.set(cacheKey, SafeConverter.toInt(DateUtils.addDays(new Date(), this.cacheDays).getTime() / 1000), v);
            });
        }
        return resultMap;
    }


    private Map<Integer, HistoryOnlineIndicator> loadHwHistory(Long id, Integer idType, Collection<Integer> days, Collection<Integer> schoolLevels){
        Map<Integer, HistoryOnlineIndicator> resultMap = new HashMap<>();
        if(CollectionUtils.isEmpty(days) || id == null || idType == null || CollectionUtils.isEmpty(schoolLevels)){
            return resultMap;
        }

        int count = days.size() / 10;
        List<List<Integer>> splitDays = new ArrayList<>(CollectionUtils.splitList(new ArrayList<>(days), count));

        List<Future<Map<Integer, HistoryOnlineIndicator>>> futureList = new ArrayList<>();
        for(List<Integer> itemDays : splitDays){
            futureList.add(AlpsThreadPool.getInstance().submit(() -> loadHwHistoryWithCache(id,idType,itemDays,schoolLevels)));
        }

        for(Future<Map<Integer, HistoryOnlineIndicator>> future : futureList) {
            try {
                Map<Integer, HistoryOnlineIndicator> subMap = future.get();
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

    private Map<Integer,HistoryOnlineIndicator> loadHwHistoryWithCache(Long id, Integer idType, Collection<Integer> days, Collection<Integer> schoolLevels){
        Map<Integer, HistoryOnlineIndicator> resultMap = new HashMap<>();
        if(CollectionUtils.isEmpty(days) || id == null || idType == null|| CollectionUtils.isEmpty(schoolLevels)){
            return resultMap;
        }

        Integer schoolLevel = AgentSchoolLevelUtils.generateCompositeSchoolLevel(schoolLevels);
        List<Integer> unCachedDayList = new ArrayList<>();
        days.forEach(day -> {
            String cacheKey = HistoryOnlineIndicator.ck_id_type_day_level(id, idType, day, schoolLevel);
            HistoryOnlineIndicator cacheObject = agentCacheSystem.CBS.flushable.load(cacheKey);
            if(cacheObject != null){
                resultMap.put(day, cacheObject);
                return;
            }
            unCachedDayList.add(day);
        });

        if(CollectionUtils.isNotEmpty(unCachedDayList)){
            Map<Integer,HistoryOnlineIndicator> unCachedDataMap = loadMarketHistoryServiceClient.loadHwHistory(id, idType, unCachedDayList, schoolLevels);
            resultMap.putAll(unCachedDataMap);
            unCachedDataMap.forEach((k, v) -> {
                // 将业绩数据保存到缓存中
                String cacheKey = HistoryOnlineIndicator.ck_id_type_day_level(id,idType, k, schoolLevel);
                agentCacheSystem.CBS.flushable.set(cacheKey, SafeConverter.toInt(DateUtils.addDays(new Date(), this.cacheDays).getTime() / 1000), v);
            });
        }
        return resultMap;
    }
}
