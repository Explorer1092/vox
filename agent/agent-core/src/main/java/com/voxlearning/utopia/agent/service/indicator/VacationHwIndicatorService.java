package com.voxlearning.utopia.agent.service.indicator;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.beanutils.BeanUtilsBean2;
import com.voxlearning.athena.api.LoadVacationHwService;
import com.voxlearning.utopia.agent.athena.LoadVacationHwClient;
import com.voxlearning.utopia.agent.bean.indicator.sum.SumVacationHwIndicator;
import com.voxlearning.utopia.agent.cache.AgentCacheSystem;
import com.voxlearning.utopia.agent.constants.AgentConstants;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.utils.AgentSchoolLevelUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.concurrent.Future;

/**
 * 假期作业指标service
 *
 * @author deliang.che
 * @since  2019/1/3
 */
@Named
public class VacationHwIndicatorService extends AbstractAgentService {
    @ImportService(interfaceClass = LoadVacationHwService.class)
    private LoadVacationHwService loadVacationHwService;
    @Inject
    private AgentCacheSystem agentCacheSystem;
    @Inject
    private LoadVacationHwClient loadVacationHwClient;

    private final int cacheDays = 2;

    /**
     * 假期作业人员汇总指标数据
     * @param userIds
     * @param day
     * @param schoolLevels
     * @param subjectCode
     * @return
     */
    public Map<Long, SumVacationHwIndicator> loadVacationHwUserSumData(Collection<Long> userIds, Integer day, Collection<Integer> schoolLevels, Integer subjectCode){
        return loadSumDataByIds(userIds, AgentConstants.INDICATOR_TYPE_USER, day, schoolLevels,subjectCode);
    }

    /**
     * 假期作业部门汇总指标数据
     * @param groupIds
     * @param day
     * @param schoolLevels
     * @param subjectCode
     * @return
     */
    public Map<Long, SumVacationHwIndicator> loadVacationHwGroupSumData(Collection<Long> groupIds, Integer day, Collection<Integer> schoolLevels, Integer subjectCode){
        return loadSumDataByIds(groupIds, AgentConstants.INDICATOR_TYPE_GROUP, day, schoolLevels,subjectCode);
    }

    /**
     * 假期作业学校指标数据
     * @param schoolIds
     * @param day
     * @param subjectCode
     * @return
     */
    public Map<Long, SumVacationHwIndicator> loadVacationHwSchoolData(Collection<Long> schoolIds, Integer day, Integer subjectCode){
        return loadSchoolDataByIds(schoolIds, day, subjectCode);
    }

    private Map<Long, SumVacationHwIndicator> loadSumDataByIds(Collection<Long> ids, Integer idType, Integer day, Collection<Integer> schoolLevels,Integer subjectCode){
        Map<Long, SumVacationHwIndicator> resultMap = new HashMap<>();
        if(CollectionUtils.isEmpty(ids) || day == null || idType == null || CollectionUtils.isEmpty(schoolLevels)){
            return resultMap;
        }

        List<List<Long>> splitIds = new ArrayList<>();
        if(ids.size() > 100){
            int count = ids.size() / 100 + 1;
            splitIds.addAll(CollectionUtils.splitList(new ArrayList<>(ids), count));
        }else {
            splitIds.add(new ArrayList<>((ids)));
        }

        List<Future<Map<Long, SumVacationHwIndicator>>> futureList = new ArrayList<>();
        for(List<Long> itemList : splitIds){
            futureList.add(AlpsThreadPool.getInstance().submit(() -> loadSumDataByIdsWithCache(itemList, idType, day, schoolLevels,subjectCode)));
        }

        for(Future<Map<Long, SumVacationHwIndicator>> future : futureList) {
            try {
                Map<Long, SumVacationHwIndicator> subMap = future.get();
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

    private Map<Long, SumVacationHwIndicator> loadSumDataByIdsWithCache(Collection<Long> ids, Integer idType, Integer day, Collection<Integer> schoolLevels,Integer subjectCode){
        Map<Long, SumVacationHwIndicator> resultMap = new HashMap<>();
        if(CollectionUtils.isEmpty(ids) || day == null || idType == null|| CollectionUtils.isEmpty(schoolLevels)){
            return resultMap;
        }

        Integer schoolLevel = AgentSchoolLevelUtils.generateCompositeSchoolLevel(schoolLevels);
        List<Long> unCachedIdList = new ArrayList<>();
        ids.forEach(p -> {
            String cacheKey = SumVacationHwIndicator.ck_id_type_day_level_subject(p, idType, day, schoolLevel,subjectCode);
            SumVacationHwIndicator cacheObject = agentCacheSystem.CBS.flushable.load(cacheKey);
            if(cacheObject != null){
                resultMap.put(p, cacheObject);
                return;
            }
            unCachedIdList.add(p);
        });

        if(CollectionUtils.isNotEmpty(unCachedIdList)){
            Map<Long, SumVacationHwIndicator> unCachedDataMap = loadVacationHwClient.loadVacationHwSumIndicator(unCachedIdList, idType, day, schoolLevels,subjectCode);


            resultMap.putAll(unCachedDataMap);
            unCachedDataMap.forEach((k, v) -> {
                // 将业绩数据保存到缓存中
                String cacheKey = SumVacationHwIndicator.ck_id_type_day_level_subject(k, v.getIdType(), v.getDay(), v.getSchoolLevel(),subjectCode);
                agentCacheSystem.CBS.flushable.set(cacheKey, SafeConverter.toInt(DateUtils.addDays(new Date(), this.cacheDays).getTime() / 1000), v);
            });
        }
        return resultMap;
    }

    private Map<Long, SumVacationHwIndicator> loadSchoolDataByIds(Collection<Long> ids, Integer day, Integer subjectCode){
        Map<Long, SumVacationHwIndicator> resultMap = new HashMap<>();
        if(CollectionUtils.isEmpty(ids) || day == null){
            return resultMap;
        }

        ids.forEach(p -> {
            SumVacationHwIndicator sumVacationHwIndicator = new SumVacationHwIndicator();
            sumVacationHwIndicator.setId(p);
            sumVacationHwIndicator.setDay(day);
            resultMap.put(p,sumVacationHwIndicator);
        });

        List<List<Long>> splitIds = new ArrayList<>();
        if(ids.size() > 100){
            int count = ids.size() / 100 + 1;
            splitIds.addAll(CollectionUtils.splitList(new ArrayList<>(ids), count));
        }else {
            splitIds.add(new ArrayList<>((ids)));
        }

        List<Future<MapMessage>> futureList = new ArrayList<>();
        for(List<Long> itemList : splitIds){
            futureList.add(AlpsThreadPool.getInstance().submit(() -> loadVacationHwService.vacationHwSchool(itemList, day,subjectCode)));
        }

        for(Future<MapMessage> future : futureList) {
            try {
                MapMessage msg = future.get();
                if(msg == null || !msg.isSuccess()){
                    continue;
                }
                Map<Long, Object> dataMap = (Map<Long, Object>) msg.get("dataMap");
                if(MapUtils.isEmpty(dataMap)){
                    continue;
                }
                dataMap.forEach((k, v) -> {
                    SumVacationHwIndicator sumVacationHwIndicator = resultMap.get(k);
                    if(sumVacationHwIndicator == null){
                        return;
                    }
                    try{
                        BeanUtilsBean2.getInstance().copyProperties(sumVacationHwIndicator, v);
                    }catch (Exception e){
                        logger.error("SumVacationHwIndicator copy error", e);
                    }
                });
            }catch (Exception e){
                String methodName = this.getClass().getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName();
                logger.error(methodName + " error", e);
            }
        }

        return resultMap;
    }

}
