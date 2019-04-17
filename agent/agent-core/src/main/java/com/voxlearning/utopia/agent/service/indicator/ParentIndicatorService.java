package com.voxlearning.utopia.agent.service.indicator;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.agent.athena.LoadParentServiceClient;
import com.voxlearning.utopia.agent.bean.indicator.sum.SumParentIndicator;
import com.voxlearning.utopia.agent.cache.AgentCacheSystem;
import com.voxlearning.utopia.agent.constants.AgentConstants;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.utils.AgentSchoolLevelUtils;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.concurrent.Future;

/**
 * ParentIndicatorService
 *
 * @author deliang.che
 * @since  2019/3/7
 */
@Named
public class ParentIndicatorService extends AbstractAgentService {

    @Inject
    private AgentCacheSystem agentCacheSystem;
    @Inject
    private LoadParentServiceClient loadParentServiceClient;
    @Inject
    private BaseOrgService baseOrgService;

    private final int cacheDays = 2;

    public Map<Long, SumParentIndicator> loadUserSumData(Collection<Long> userIds, Integer day, Collection<Integer> schoolLevels){
        return loadSumDataByIds(userIds, AgentConstants.INDICATOR_TYPE_USER, day, schoolLevels);
    }

    public Map<Long, SumParentIndicator> loadGroupSumData(Collection<Long> groupIds, Integer day, Collection<Integer> schoolLevels){
        return loadSumDataByIds(groupIds, AgentConstants.INDICATOR_TYPE_GROUP, day, schoolLevels);
    }

    private Map<Long, SumParentIndicator> loadSumDataByIds(Collection<Long> ids, Integer dataType, Integer day, Collection<Integer> schoolLevels){
        Map<Long, SumParentIndicator> resultMap = new HashMap<>();
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

        List<Future<Map<Long, SumParentIndicator>>> futureList = new ArrayList<>();
        for(List<Long> itemList : splitIds){
            futureList.add(AlpsThreadPool.getInstance().submit(() -> loadSumDataByIdsWithCache(itemList, dataType, day, schoolLevels)));
        }

        for(Future<Map<Long, SumParentIndicator>> future : futureList) {
            try {
                Map<Long, SumParentIndicator> subMap = future.get();
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

    private Map<Long, SumParentIndicator> loadSumDataByIdsWithCache(Collection<Long> ids, Integer idType, Integer day, Collection<Integer> schoolLevels){
        Map<Long, SumParentIndicator> resultMap = new HashMap<>();
        if(CollectionUtils.isEmpty(ids) || day == null || idType == null|| CollectionUtils.isEmpty(schoolLevels)){
            return resultMap;
        }

        Integer schoolLevel = AgentSchoolLevelUtils.generateCompositeSchoolLevel(schoolLevels);
        List<Long> unCachedIdList = new ArrayList<>();
        ids.forEach(p -> {
            String cacheKey = SumParentIndicator.ck_id_type_day_level(p, idType, day, schoolLevel);
            SumParentIndicator cacheObject = agentCacheSystem.CBS.flushable.load(cacheKey);
            if(cacheObject != null){
                resultMap.put(p, cacheObject);
                return;
            }
            unCachedIdList.add(p);
        });

        if(CollectionUtils.isNotEmpty(unCachedIdList)){
            Map<Long, SumParentIndicator> unCachedDataMap = loadParentServiceClient.loadSumParentIndicator(unCachedIdList, idType, day, schoolLevels);
            if(Objects.equals(idType, AgentConstants.INDICATOR_TYPE_GROUP)) {
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
                    String cacheKey = SumParentIndicator.ck_id_type_day_level(k, v.getIdType(), v.getDay(), v.getSchoolLevel());
                    agentCacheSystem.CBS.flushable.set(cacheKey, SafeConverter.toInt(DateUtils.addDays(new Date(), this.cacheDays).getTime() / 1000), v);
                }
            });
        }
        return resultMap;
    }

    public SumParentIndicator loadGroupUnallocatedSumData(Long groupId, Integer day, Collection<Integer> schoolLevels){
        if(groupId == null || day == null || CollectionUtils.isEmpty(schoolLevels)){
            return null;
        }
        Integer schoolLevel = AgentSchoolLevelUtils.generateCompositeSchoolLevel(schoolLevels);

        String cacheKey = SumParentIndicator.ck_id_type_day_level(groupId, AgentConstants.INDICATOR_TYPE_UNALLOCATED, day, schoolLevel);
        SumParentIndicator cacheObject = agentCacheSystem.CBS.flushable.load(cacheKey);
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

        SumParentIndicator sumParentIndicator = loadParentServiceClient.loadSchoolSumParentIndicator(schoolIds, day);
        if(sumParentIndicator != null){
            sumParentIndicator.setId(groupId);
            sumParentIndicator.setIdType(AgentConstants.INDICATOR_TYPE_UNALLOCATED);
            sumParentIndicator.setName("未分配");

            sumParentIndicator.setSchoolLevel(schoolLevel);
            if(sumParentIndicator.hasAllDimension()){
                agentCacheSystem.CBS.flushable.set(cacheKey, SafeConverter.toInt(DateUtils.addDays(new Date(), this.cacheDays).getTime() / 1000), sumParentIndicator);
            }
        }
        return sumParentIndicator;
    }
}
