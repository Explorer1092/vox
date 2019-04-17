package com.voxlearning.utopia.agent.service.indicator.support.online;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.agent.bean.indicator.sum.SumOnlineIndicator;
import com.voxlearning.utopia.agent.bean.indicator.sum.SumOnlineIndicatorWithBudget;
import com.voxlearning.utopia.agent.cache.AgentCacheSystem;
import com.voxlearning.utopia.agent.constants.AgentConstants;
import com.voxlearning.utopia.agent.persist.entity.exam.AgentExamContract;
import com.voxlearning.utopia.agent.persist.exam.AgentExamContractPersistence;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.indicator.OnlineIndicatorService;
import com.voxlearning.utopia.agent.service.region.AgentRegionService;
import com.voxlearning.utopia.agent.support.AgentSchoolLevelSupport;
import com.voxlearning.utopia.agent.utils.AgentSchoolLevelUtils;
import com.voxlearning.utopia.entity.crm.CrmSchoolSummary;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentServiceRange;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.user.consumer.CrmSummaryLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * 渠道下各个部分数据的实现
 *
 * @author song.wang
 * @date 2018/11/6
 */
@Named
public class ChannelOnlineIndicator extends OnlineIndicatorFactory {

    @Inject
    private AgentSchoolLevelSupport agentSchoolLevelSupport;
    @Inject
    private AgentExamContractPersistence agentExamContractPersistence;
    @Inject
    private BaseOrgService baseOrgService;
    @Inject
    private OnlineIndicatorService onlineIndicatorService;
    @Inject
    private CrmSummaryLoaderClient crmSummaryLoaderClient;
    @Inject
    private AgentCacheSystem agentCacheSystem;


    @Override
    public SumOnlineIndicatorWithBudget generateOverview(Long id, Integer dataType, Integer day, Integer schoolLevelFlag) {
        List<Integer> schoolLevelIds = agentSchoolLevelSupport.fetchTargetSchoolLevelIds(id, dataType, schoolLevelFlag);
        if(CollectionUtils.isEmpty(schoolLevelIds)){
            return null;
        }

        Integer schoolLevel = AgentSchoolLevelUtils.generateCompositeSchoolLevel(schoolLevelIds);

        String cacheKey = SumOnlineIndicator.ck_id_type_day_level(id, dataType, day, schoolLevel);
        SumOnlineIndicator cacheObject = agentCacheSystem.CBS.flushable.load(cacheKey);
        if(cacheObject != null){
            return onlineIndicatorService.convertToSumDataWithBudget(cacheObject);
        }

        Long groupId = null;
        String name = "";
        if(Objects.equals(dataType, AgentConstants.INDICATOR_TYPE_GROUP)){
            groupId = id;
            AgentGroup group = baseOrgService.getGroupById(id);
            name = group == null ? "" : group.getGroupName();
        }else if(Objects.equals(dataType, AgentConstants.INDICATOR_TYPE_USER)){
            List<Long> groupIds = baseOrgService.getGroupIdListByUserId(id);
            if(CollectionUtils.isNotEmpty(groupIds)){
                groupId = groupIds.get(0);
                AgentUser user = baseOrgService.getUser(id);
                name = user == null ? "" : user.getRealName();
            }
        }


        SumOnlineIndicator sumOnlineIndicator = null;
        if(groupId != null){
            List<Integer> regionCodeList = baseOrgService.getGroupRegionCodeList(groupId);
            Collection<Long> schoolIds = fetchRegionCoveredContractSchools(regionCodeList, schoolLevelIds);
            sumOnlineIndicator = loadNewSchoolServiceClient.loadSchoolSumOnlineIndicator(schoolIds, day);
        }

        if(sumOnlineIndicator != null){
            sumOnlineIndicator.setId(id);
            sumOnlineIndicator.setDay(day);
            sumOnlineIndicator.setDataType(dataType);
            sumOnlineIndicator.setSchoolLevel(schoolLevel);
            sumOnlineIndicator.setName(name);
            if(sumOnlineIndicator.hasAllDimension()){
                agentCacheSystem.CBS.flushable.set(cacheKey, SafeConverter.toInt(DateUtils.addDays(new Date(), 2).getTime() / 1000), sumOnlineIndicator);
            }
        }
        return onlineIndicatorService.convertToSumDataWithBudget(sumOnlineIndicator);
    }

    @Override
    public Map<Long, SumOnlineIndicatorWithBudget> generateGroupDataList(Collection<Long> groupIds, Integer day, Integer schoolLevelFlag) {
        return new HashMap<>();
    }

    @Override
    public Map<Long, SumOnlineIndicatorWithBudget> generateUserDataList(Collection<Long> userIds, Integer day, Integer schoolLevelFlag) {
        return new HashMap<>();
    }

    @Override
    public SumOnlineIndicatorWithBudget generateUnallocatedData(Long groupId, Integer day, Integer schoolLevelFlag) {
        return null;
    }

    @Override
    public Collection<Long> fetchSchoolList(Long id, Integer dataType, Integer schoolLevelFlag) {
        List<Integer> schoolLevelIds = agentSchoolLevelSupport.fetchTargetSchoolLevelIds(id, dataType, schoolLevelFlag);
        if(CollectionUtils.isEmpty(schoolLevelIds)){
            return new ArrayList<>();
        }

        Long groupId = null;
        if(Objects.equals(dataType, AgentConstants.INDICATOR_TYPE_GROUP)){
            groupId = id;
        }else if(Objects.equals(dataType, AgentConstants.INDICATOR_TYPE_USER)){
            List<Long> groupIds = baseOrgService.getGroupIdListByUserId(id);
            if(CollectionUtils.isNotEmpty(groupIds)){
                groupId = groupIds.get(0);
            }
        }
        List<Integer> regionCodeList = new ArrayList<>();
        if(groupId != null){
            regionCodeList = baseOrgService.getGroupRegionCodeList(groupId);
        }
        return fetchRegionCoveredContractSchools(regionCodeList, schoolLevelIds);
    }

    private Collection<Long> fetchRegionCoveredContractSchools(Collection<Integer> regionCodeList, Collection<Integer> schoolLevelIds){
        List<Long> schoolIds = new ArrayList<>();
        if(CollectionUtils.isEmpty(regionCodeList) || CollectionUtils.isEmpty(schoolLevelIds)){
            return schoolIds;
        }

        List<AgentExamContract> contractList = agentExamContractPersistence.loadInServiceContract(new Date());
        if(CollectionUtils.isEmpty(contractList)){
            return schoolIds;
        }
        List<Long> contactSchoolList = new ArrayList<>();
        contractList.forEach(p -> {
            List<AgentServiceRange> serviceRangeList = AgentServiceRange.toList(p.getServiceRange());
            if(serviceRangeList.contains(AgentServiceRange.ONLINE_HOMEWORK_PRODUCT) && !contactSchoolList.contains(p.getSchoolId())){
                contactSchoolList.add(p.getSchoolId());
            }
        });

        Map<Long, CrmSchoolSummary> schoolSummaryMap = crmSummaryLoaderClient.loadSchoolSummary(contactSchoolList);
        schoolSummaryMap.values().forEach(p -> {
            if((regionCodeList.contains(p.getProvinceCode()) || regionCodeList.contains(p.getCityCode()) || regionCodeList.contains(p.getCountyCode()))
                    && (p.getSchoolLevel() != null && schoolLevelIds.contains(p.getSchoolLevel().getLevel()))){
                schoolIds.add(p.getSchoolId());
            }
        });
        return schoolIds;
    }
}
