package com.voxlearning.utopia.agent.service.indicator.support.offline;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.agent.bean.indicator.OfflineIndicator;
import com.voxlearning.utopia.agent.bean.indicator.sum.SumOfflineIndicator;
import com.voxlearning.utopia.agent.bean.indicator.sum.SumOfflineIndicatorWithBudget;
import com.voxlearning.utopia.agent.cache.AgentCacheSystem;
import com.voxlearning.utopia.agent.constants.AgentConstants;
import com.voxlearning.utopia.agent.persist.entity.exam.AgentExamContract;
import com.voxlearning.utopia.agent.persist.exam.AgentExamContractPersistence;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.indicator.OfflineIndicatorService;
import com.voxlearning.utopia.agent.service.region.AgentRegionService;
import com.voxlearning.utopia.agent.support.AgentSchoolLevelSupport;
import com.voxlearning.utopia.agent.utils.AgentSchoolLevelUtils;
import com.voxlearning.utopia.entity.crm.CrmSchoolSummary;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentServiceRange;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentDictSchool;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentDictSchoolLoaderClient;
import com.voxlearning.utopia.service.user.consumer.CrmSummaryLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * ChannelOfflineIndicator
 *
 * @author song.wang
 * @date 2018/11/7
 */
@Named
public class ChannelOfflineIndicator extends OfflineIndicatorFactory {

    @Inject
    private AgentSchoolLevelSupport agentSchoolLevelSupport;
    @Inject
    private AgentExamContractPersistence agentExamContractPersistence;
    @Inject
    private BaseOrgService baseOrgService;
    @Inject
    private OfflineIndicatorService offlineIndicatorService;
    @Inject
    private CrmSummaryLoaderClient crmSummaryLoaderClient;
    @Inject
    private AgentCacheSystem agentCacheSystem;
    @Inject
    private AgentRegionService agentRegionService;
    @Inject
    private AgentDictSchoolLoaderClient agentDictSchoolLoaderClient;

    @Override
    public SumOfflineIndicatorWithBudget generateOverview(Long id, Integer dataType, Integer day, Integer schoolLevelFlag) {
        List<Integer> schoolLevelIds = agentSchoolLevelSupport.fetchTargetSchoolLevelIds(id, dataType, schoolLevelFlag);
        if(CollectionUtils.isEmpty(schoolLevelIds)){
            return null;
        }
        Integer schoolLevel = AgentSchoolLevelUtils.generateCompositeSchoolLevel(schoolLevelIds);

        String cacheKey = SumOfflineIndicator.ck_id_type_day_level(id, dataType, day, schoolLevel);
        SumOfflineIndicator cacheObject = agentCacheSystem.CBS.flushable.load(cacheKey);
        if(cacheObject != null){
            return offlineIndicatorService.convertToSumDataWithBudget(cacheObject);
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

        SumOfflineIndicator contactOfflineIndicator = null;
        SumOfflineIndicator dictOfflineIndicator = null;
        if(groupId != null){
            List<Integer> regionCodeList = baseOrgService.getGroupRegionCodeList(groupId);
            Collection<Long> schoolIds = fetchRegionCoveredContractSchools(regionCodeList, schoolLevelIds);
            contactOfflineIndicator = loadNewSchoolServiceClient.loadSchoolSumOfflineIndicator(schoolIds, day);
            Collection<Long> dictSchoolIds = fetchRegionCoveredDictSchools(regionCodeList, schoolLevelIds);
            dictOfflineIndicator = loadNewSchoolServiceClient.loadSchoolSumOfflineIndicator(dictSchoolIds, day);
        }

        SumOfflineIndicator sumOfflineIndicator = mergeSomeData(contactOfflineIndicator, dictOfflineIndicator);
        if(sumOfflineIndicator != null){
            sumOfflineIndicator.setId(id);
            sumOfflineIndicator.setDay(day);
            sumOfflineIndicator.setDataType(dataType);
            sumOfflineIndicator.setSchoolLevel(schoolLevel);
            sumOfflineIndicator.setName(name);
            if(sumOfflineIndicator.hasAllDimension()){
                agentCacheSystem.CBS.flushable.set(cacheKey, SafeConverter.toInt(DateUtils.addDays(new Date(), 2).getTime() / 1000), sumOfflineIndicator);
            }
        }
        return offlineIndicatorService.convertToSumDataWithBudget(sumOfflineIndicator);
    }

    private SumOfflineIndicator mergeSomeData(SumOfflineIndicator contactOfflineIndicator, SumOfflineIndicator dictOfflineIndicator){

        if(contactOfflineIndicator == null && dictOfflineIndicator == null){
            return null;
        }
        if(contactOfflineIndicator == null){
            contactOfflineIndicator = new SumOfflineIndicator();
            contactOfflineIndicator.getIndicatorMap().put(AgentConstants.OFFLINE_INDICATOR_MONTH, new OfflineIndicator());
            contactOfflineIndicator.getIndicatorMap().put(AgentConstants.OFFLINE_INDICATOR_DAY, new OfflineIndicator());
            contactOfflineIndicator.getIndicatorMap().put(AgentConstants.OFFLINE_INDICATOR_SUM, new OfflineIndicator());
            contactOfflineIndicator.getIndicatorMap().put(AgentConstants.OFFLINE_INDICATOR_TERM, new OfflineIndicator());
        }

        if(dictOfflineIndicator != null){
            Integer scanTermStuNumSglSubj = SafeConverter.toInt(dictOfflineIndicator.fetchMonthData().getScanTermStuNumSglSubj());
            Integer tmScanStuNumSglSubj = SafeConverter.toInt(dictOfflineIndicator.fetchMonthData().getScanStuNumSglSubj());
            Integer pdScanStuNumSglSubj = SafeConverter.toInt(dictOfflineIndicator.fetchDayData().getScanStuNumSglSubj());

            if(scanTermStuNumSglSubj != 0 || tmScanStuNumSglSubj != 0){
                OfflineIndicator offlineIndicator = contactOfflineIndicator.fetchMonthData();
                if(scanTermStuNumSglSubj != 0){
                    offlineIndicator.setScanTermStuNumSglSubj(scanTermStuNumSglSubj);
                }
                if(tmScanStuNumSglSubj != 0){
                    offlineIndicator.setScanStuNumSglSubj(tmScanStuNumSglSubj);
                }
                if(!contactOfflineIndicator.getIndicatorMap().containsKey(AgentConstants.OFFLINE_INDICATOR_MONTH)){
                    contactOfflineIndicator.getIndicatorMap().put(AgentConstants.OFFLINE_INDICATOR_MONTH, offlineIndicator);
                }
            }

            if(pdScanStuNumSglSubj != 0 ){
                OfflineIndicator offlineIndicator = contactOfflineIndicator.fetchDayData();
                offlineIndicator.setScanStuNumSglSubj(pdScanStuNumSglSubj);
                if(!contactOfflineIndicator.getIndicatorMap().containsKey(AgentConstants.OFFLINE_INDICATOR_DAY)){
                    contactOfflineIndicator.getIndicatorMap().put(AgentConstants.OFFLINE_INDICATOR_MONTH, offlineIndicator);
                }
            }
        }
        return contactOfflineIndicator;
    }

    @Override
    public Map<Long, SumOfflineIndicatorWithBudget> generateGroupDataList(Collection<Long> groupIds, Integer day, Integer schoolLevelFlag) {
        return new HashMap<>();
    }

    @Override
    public Map<Long, SumOfflineIndicatorWithBudget> generateUserDataList(Collection<Long> userIds, Integer day, Integer schoolLevelFlag) {
        return new HashMap<>();
    }

    @Override
    public SumOfflineIndicatorWithBudget generateUnallocatedData(Long groupId, Integer day, Integer schoolLevelFlag) {
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
            if(serviceRangeList.contains(AgentServiceRange.EXAM_SERVICE_PRODUCT) && !contactSchoolList.contains(p.getSchoolId())){
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

    private Collection<Long> fetchRegionCoveredDictSchools(Collection<Integer> regionCodeList, Collection<Integer> schoolLevelIds){
        List<Long> schoolIds = new ArrayList<>();
        if(CollectionUtils.isEmpty(regionCodeList) || CollectionUtils.isEmpty(schoolLevelIds)){
            return schoolIds;
        }

        List<Integer> countyCodeList = agentRegionService.getCountyCodes(regionCodeList);
        Map<Integer, List<AgentDictSchool>> countyDictMap = agentDictSchoolLoaderClient.findByCountyCodes(countyCodeList);
        if(MapUtils.isEmpty(countyDictMap)){
            return schoolIds;
        }
        countyDictMap.values().stream().flatMap(List::stream).forEach(p -> {
            if(schoolLevelIds.contains(p.getSchoolLevel())){
                schoolIds.add(p.getSchoolId());
            }
        });
        return schoolIds;
    }
}
