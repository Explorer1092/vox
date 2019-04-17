package com.voxlearning.utopia.agent.service.data;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.agent.DayUtils;
import com.voxlearning.utopia.agent.athena.LoadParentServiceClient;
import com.voxlearning.utopia.agent.bean.indicator.OnlineIndicator;
import com.voxlearning.utopia.agent.bean.indicator.ParentIndicator;
import com.voxlearning.utopia.agent.bean.indicator.history.HistoryParentIndicator;
import com.voxlearning.utopia.agent.bean.indicator.school.SchoolParentIndicator;
import com.voxlearning.utopia.agent.bean.indicator.sum.SumOnlineIndicator;
import com.voxlearning.utopia.agent.bean.indicator.sum.SumParentIndicator;
import com.voxlearning.utopia.agent.constants.AgentConstants;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.indicator.HistoryIndicatorService;
import com.voxlearning.utopia.agent.service.indicator.IndicatorService;
import com.voxlearning.utopia.agent.service.indicator.OnlineIndicatorService;
import com.voxlearning.utopia.agent.service.indicator.ParentIndicatorService;
import com.voxlearning.utopia.agent.service.indicator.support.online.MarketOnlineIndicator;
import com.voxlearning.utopia.agent.service.mobile.PerformanceService;
import com.voxlearning.utopia.agent.support.AgentSchoolLevelSupport;
import com.voxlearning.utopia.agent.utils.AgentSchoolLevelUtils;
import com.voxlearning.utopia.agent.utils.MathUtils;
import com.voxlearning.utopia.agent.view.indicator.ParentIndicatorView;
import com.voxlearning.utopia.agent.view.indicator.ParentIndicatorViewSupport;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentServiceType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.School;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 首页-数据-家长
 */
@Named
public class AgentDataParentService {
    @Inject
    private PerformanceService performanceService;
    @Inject
    private LoadParentServiceClient loadParentServiceClient;
    @Inject
    private AgentSchoolLevelSupport agentSchoolLevelSupport;
    @Inject
    private ParentIndicatorService parentIndicatorService;
    @Inject
    private BaseOrgService baseOrgService;
    @Inject
    private IndicatorService indicatorService;
    @Inject
    private MarketOnlineIndicator marketOnlineIndicator;
    @Inject
    private SchoolLoaderClient schoolLoaderClient;
    @Inject
    private HistoryIndicatorService historyIndicatorService;
    @Inject
    private OnlineIndicatorService onlineIndicatorService;

    /**
     * 柱状图
     * @return
     */
    public Map<String ,Object> histogram(Long id,Integer idType,Integer schoolLevelFlag){
        Map<String ,Object> dataMap = new HashMap<>();
        Integer day = performanceService.lastSuccessDataDay();
        Date date = DateUtils.stringToDate(SafeConverter.toString(day), "yyyyMMdd");
        List<Integer> dayList = new ArrayList<>();
        for(int i = 0; i < 30; i++){
            dayList.add(SafeConverter.toInt(DateUtils.dateToString(DayUtils.addDay(date, -i), "yyyyMMdd")));
        }

        Map<Integer, HistoryParentIndicator> dayParentIndicatorMap = new HashMap<>();
        List<Integer> schoolLevelIds = agentSchoolLevelSupport.fetchTargetSchoolLevelIds(id, idType, schoolLevelFlag);
        if (Objects.equals(idType, AgentConstants.INDICATOR_TYPE_USER)){
            dayParentIndicatorMap.putAll(historyIndicatorService.loadUserParentHistory(id,dayList,schoolLevelIds));
        }else {
            dayParentIndicatorMap.putAll(historyIndicatorService.loadGroupParentHistory(id,dayList,schoolLevelIds));
        }

        List<Map<String,Object>> monthDataList = new ArrayList<>();
        for (Integer dayItem : dayList) {
            Map<String,Object> itemMap = new HashMap<>();
            itemMap.put("day",dayItem);
            HistoryParentIndicator parentIndicator = dayParentIndicatorMap.get(dayItem);
            itemMap.put("bindStuParentNum",parentIndicator != null ? SafeConverter.toInt(parentIndicator.getBindStuParentNum()) : 0);
            monthDataList.add(itemMap);
        }
        dataMap.put("monthDataList",monthDataList);
        return dataMap;
    }


    /**
     * 指标概览
     * @param id
     * @param idType
     * @param schoolLevelFlag
     * @param day
     * @return
     */
    public ParentIndicatorView indicatorOverview(Long id, Integer idType, Integer schoolLevelFlag, Integer day){

        Date dayDate = DateUtils.stringToDate(String.valueOf(day), "yyyyMMdd");
        Date lastMonthDate = MonthRange.newInstance(dayDate.getTime()).previous().getEndDate();
        Integer lastMonthDay = Integer.valueOf(DateUtils.dateToString(lastMonthDate,"yyyyMMdd"));

        List<Integer> schoolLevelIds = agentSchoolLevelSupport.fetchTargetSchoolLevelIds(id, idType, schoolLevelFlag);

        Map<Long, SumParentIndicator> tmParentIndicatorMap = new HashMap<>();
        Map<Long, SumParentIndicator> lmParentIndicatorMap = new HashMap<>();
        Map<Long, SumOnlineIndicator> tmOnlineIndicatorMap = new HashMap<>();
        if(Objects.equals(idType, AgentConstants.INDICATOR_TYPE_GROUP)){
            tmParentIndicatorMap.putAll(parentIndicatorService.loadGroupSumData(Collections.singleton(id), day, schoolLevelIds));
            lmParentIndicatorMap.putAll(parentIndicatorService.loadGroupSumData(Collections.singleton(id), lastMonthDay, schoolLevelIds));
            tmOnlineIndicatorMap.putAll(onlineIndicatorService.loadGroupSumData(Collections.singleton(id), day, schoolLevelIds));
        }else if(Objects.equals(idType, AgentConstants.INDICATOR_TYPE_USER)){
            tmParentIndicatorMap.putAll(parentIndicatorService.loadUserSumData(Collections.singleton(id), day, schoolLevelIds));
            lmParentIndicatorMap.putAll(parentIndicatorService.loadUserSumData(Collections.singleton(id), lastMonthDay,schoolLevelIds));
            tmOnlineIndicatorMap.putAll(onlineIndicatorService.loadUserSumData(Collections.singleton(id),day,schoolLevelIds));
        }

        SumParentIndicator tmParentIndicator = tmParentIndicatorMap.get(id);
        SumParentIndicator lmParentIndicator = lmParentIndicatorMap.get(id);
        SumOnlineIndicator tmOnlineIndicator = tmOnlineIndicatorMap.get(id);

        ParentIndicatorView dataView = new ParentIndicatorView();
        dataView.setId(id);
        dataView.setIdType(idType);

        if(tmParentIndicator != null){
            dataView.setSchoolLevel(tmParentIndicator.getSchoolLevel());

            ParentIndicator dayParentIndicator = tmParentIndicator.fetchDayData();
            ParentIndicator monthParentIndicator = tmParentIndicator.fetchMonthData();
            ParentIndicator sumParentIndicator = tmParentIndicator.fetchSumData();

            dataView.setTmBindParentStuNum(SafeConverter.toInt(monthParentIndicator.getBindParentStuNum()));
            dataView.setTmBindStuParentNum(SafeConverter.toInt(monthParentIndicator.getBindStuParentNum()));
            dataView.setBindParentStuNum(SafeConverter.toInt(sumParentIndicator.getBindParentStuNum()));
            dataView.setBindStuParentNum(SafeConverter.toInt(sumParentIndicator.getBindStuParentNum()));
            if (tmOnlineIndicator != null){
                OnlineIndicator onlineIndicator = tmOnlineIndicator.fetchSumData();
                if (onlineIndicator != null){
                    dataView.setRegStuNum(SafeConverter.toInt(onlineIndicator.getRegStuCount()));
                }
            }
            dataView.setParentPermeateRate(MathUtils.doubleDivide(dataView.getBindParentStuNum(),dataView.getRegStuNum()));

            dataView.setTmLoginGte1BindStuParentNum(SafeConverter.toInt(monthParentIndicator.getTmLoginGte1BindStuParentNum()));
            dataView.setPdLoginGte1BindStuParentNum(SafeConverter.toInt(dayParentIndicator.getTmLoginGte1BindStuParentNum()));

            dataView.setTmLoginGte3BindStuParentNum(SafeConverter.toInt(monthParentIndicator.getTmLoginGte3BindStuParentNum()));
            dataView.setPdLoginGte3BindStuParentNum(SafeConverter.toInt(dayParentIndicator.getTmLoginGte3BindStuParentNum()));

            dataView.setTmBackFlowParentNum(SafeConverter.toInt(monthParentIndicator.getBackFlowParentNum()));
            dataView.setTmIncParentNum(SafeConverter.toInt(monthParentIndicator.getTmLoginGte3BindStuParentNum()) - SafeConverter.toInt(monthParentIndicator.getBackFlowParentNum()));

            dataView.setTmParentStuActiveSettlementNum(SafeConverter.toInt(monthParentIndicator.getParentStuActiveSettlementNum()));
            dataView.setPdParentStuActiveSettlementNum(SafeConverter.toInt(dayParentIndicator.getParentStuActiveSettlementNum()));

            dataView.setTmNewParentOldStuActiveSettlementNum(SafeConverter.toInt(monthParentIndicator.getNewParentOldStuActiveSettlementNum()));
            dataView.setTmNewParentNewStuActiveSettlementNum(SafeConverter.toInt(monthParentIndicator.getNewParentNewStuActiveSettlementNum()));
            dataView.setTmNewParentActiveSettlementNum(SafeConverter.toInt(monthParentIndicator.getNewParentActiveSettlementNum()));
        }
        if (lmParentIndicator != null){
            ParentIndicator monthParentIndicator = lmParentIndicator.fetchMonthData();

            dataView.setLmBindParentStuNum(SafeConverter.toInt(monthParentIndicator.getBindParentStuNum()));
            dataView.setLmBindStuParentNum(SafeConverter.toInt(monthParentIndicator.getBindStuParentNum()));
        }

        return dataView;
    }

    public List<Map<String, Object>>  generateUserIndicatorViewList(Collection<Long> userIds, Integer day, Integer schoolLevelFlag, int indicator, int monthOrDay) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        if (CollectionUtils.isEmpty(userIds)) {
            return resultList;
        }
        Map<Long, SumParentIndicator> userDataMap = generateUserDataList(userIds, day, schoolLevelFlag);
        resultList.addAll(generateParentIndicatorViewMap(userDataMap.values(), schoolLevelFlag, indicator, monthOrDay));
        return resultList;
    }

    public Map<Long, SumParentIndicator> generateUserDataList(Collection<Long> userIds, Integer day, Integer schoolLevelFlag) {
        Map<Long, SumParentIndicator> indicatorWithBudgetMap = new HashMap<>();
        if(CollectionUtils.isEmpty(userIds)){
            return indicatorWithBudgetMap;
        }
        Map<Long, Integer> groupCompositeLevelMap = agentSchoolLevelSupport.fetchTargetCompositeSchoolLevel(userIds, AgentConstants.INDICATOR_TYPE_USER, schoolLevelFlag);
        Map<Integer, List<Long>> levelGroupListMap = new HashMap<>();
        groupCompositeLevelMap.forEach((k, v) -> {
            List<Long> groupList = levelGroupListMap.get(v);
            if(CollectionUtils.isEmpty(groupList)){
                groupList = new ArrayList<>();
                levelGroupListMap.put(v, groupList);
            }
            groupList.add(k);
        });

        levelGroupListMap.forEach((k, v) -> indicatorWithBudgetMap.putAll(parentIndicatorService.loadUserSumData(v, day, AgentSchoolLevelUtils.fetchFromCompositeSchoolLevel(k))));
        return indicatorWithBudgetMap;
    }

    private List<Map<String, Object>> generateParentIndicatorViewMap(Collection<SumParentIndicator> parentIndicators, Integer schoolLevel, int indicator, int monthOrDay){
        if(CollectionUtils.isEmpty(parentIndicators)){
            return Collections.emptyList();
        }

        List<Map<String, Object>> resultList = new ArrayList<>();
        Integer viewType = schoolLevel * 10000 + indicator * 100 + monthOrDay;
        parentIndicators.forEach(p -> {
            if(p != null){
                ParentIndicatorView viewData = ParentIndicatorViewSupport.generateParentViewData(p, viewType);
                if(viewData != null){
                    resultList.add(viewData.generateIndicatorDataMap());
                }
            }
        });
        return resultList;
    }

    // 获取部门的指标数据
    public List<Map<String, Object>> generateGroupIndicatorViewList(Long groupId, AgentGroupRoleType groupRoleType, Integer day, Integer schoolLevelFlag, int dimension, int indicator, int monthOrDay){
        List<Map<String, Object>> resultList = new ArrayList<>();

        Collection<Long> targetGroupList = indicatorService.fetchGroupList(groupId, groupRoleType, dimension);
        if(CollectionUtils.isEmpty(targetGroupList)){
            return resultList;
        }
        Map<Long, SumParentIndicator> groupDataMap = generateGroupDataList(targetGroupList, day, schoolLevelFlag);
        resultList.addAll(generateParentIndicatorViewMap(groupDataMap.values(), schoolLevelFlag, indicator,  monthOrDay));
        return resultList;
    }

    public Map<Long, SumParentIndicator> generateGroupDataList(Collection<Long> groupIds, Integer day, Integer schoolLevel) {
        Map<Long, SumParentIndicator> sumParentIndicatorMap = new HashMap<>();
        if(CollectionUtils.isEmpty(groupIds)){
            return sumParentIndicatorMap;
        }
        Map<Long, Integer> groupCompositeLevelMap = agentSchoolLevelSupport.fetchTargetCompositeSchoolLevel(groupIds, AgentConstants.INDICATOR_TYPE_GROUP, schoolLevel);
        Map<Integer, List<Long>> levelGroupListMap = new HashMap<>();
        groupCompositeLevelMap.forEach((k, v) -> {
            List<Long> groupList = levelGroupListMap.get(v);
            if(CollectionUtils.isEmpty(groupList)){
                groupList = new ArrayList<>();
                levelGroupListMap.put(v, groupList);
            }
            groupList.add(k);
        });

        levelGroupListMap.forEach((k, v) -> sumParentIndicatorMap.putAll(parentIndicatorService.loadGroupSumData(v, day, AgentSchoolLevelUtils.fetchFromCompositeSchoolLevel(k))));
        return sumParentIndicatorMap;
    }


    /**
     * 部门下专员数据， 如果是分区的话，包含未分配数据
     * @param groupId
     * @param groupRoleType
     * @param day
     * @param schoolLevelFlag
     * @param dimension
     * @param indicator
     * @param monthOrDay
     * @return
     */
    public List<Map<String, Object>> generateGroupUserIndicatorViewList(Long groupId, AgentGroupRoleType groupRoleType, Integer day, Integer schoolLevelFlag, int dimension, int indicator, int monthOrDay){
        List<Map<String, Object>> resultList = new ArrayList<>();

        List<Long> userList = baseOrgService.getAllSubGroupUserIdsByGroupIdAndRole(groupId, AgentRoleType.BusinessDeveloper.getId());

        resultList.addAll(generateUserIndicatorViewList(userList, day, schoolLevelFlag, indicator, monthOrDay));

        // 分区的情况下， 设置未分配数据
        if(groupRoleType == AgentGroupRoleType.City && dimension == 1){
            SumParentIndicator unallocatedData = generateUnallocatedData(groupId, day, schoolLevelFlag);
            if(unallocatedData != null){
                resultList.addAll(generateParentIndicatorViewMap(Collections.singleton(unallocatedData), schoolLevelFlag, indicator, monthOrDay));
            }
        }
        return resultList;
    }

    public SumParentIndicator generateUnallocatedData(Long groupId, Integer day, Integer schoolLevelFlag) {
        List<Integer> schoolLevelList = agentSchoolLevelSupport.fetchTargetSchoolLevelIds(groupId, AgentConstants.INDICATOR_TYPE_GROUP, schoolLevelFlag);
        return parentIndicatorService.loadGroupUnallocatedSumData(groupId, day, schoolLevelList);
    }

    public List<Map<String, Object>> generateSchoolIndicatorViewList(Long id, Integer dataType, Integer day, int indicator, int monthOrDay, int schoolLevelFlag){
        List<Map<String, Object>> resultList = new ArrayList<>();

        Map<Long, SchoolParentIndicator> schoolDataMap = generateSchoolData(id, dataType, day, schoolLevelFlag);
        schoolDataMap.values().forEach(p -> {
            Map<String, Object> map = ParentIndicatorViewSupport.generateSchoolParentViewData(p, indicator, monthOrDay);
            if(MapUtils.isNotEmpty(map)){
                resultList.add(map);
            }
        });

        if(CollectionUtils.isNotEmpty(resultList)){
            List<Long> schoolIds = resultList.stream().map(p -> SafeConverter.toLong(p.get("id"))).collect(Collectors.toList());
            Map<Long, School> schoolMap = schoolLoaderClient.getSchoolLoader()
                    .loadSchools(schoolIds)
                    .getUninterruptibly();
            resultList.forEach(p -> {
                Long schoolId = SafeConverter.toLong(p.get("id"));
                School school = schoolMap.get(schoolId);
                if(school != null){
                    p.put("name", school.getCname());
                }
            });
        }
        return resultList;
    }

    public Map<Long, SchoolParentIndicator> generateSchoolData(Long id, Integer dataType, Integer day, Integer schoolLevelFlag){
        Collection<Long> schoolIds = marketOnlineIndicator.fetchSchoolList(id, dataType, schoolLevelFlag);
        if(CollectionUtils.isEmpty(schoolIds)){
            return Collections.emptyMap();
        }
        return loadParentServiceClient.loadSchoolParentIndicator(schoolIds, day);
    }

    public Map<String, Object> getTargetParameterMap(Long userId,Integer schoolLevelFlag){
        Map<String, Object> resultMap = new HashMap<>();
        AgentRoleType userRole = baseOrgService.getUserRole(userId);
        if(userRole == AgentRoleType.Country || userRole == AgentRoleType.Admin || userRole == AgentRoleType.PRODUCT_OPERATOR || userRole == AgentRoleType.RiskManager){
            List<AgentGroup> groupList = baseOrgService.getAgentGroupByRole(AgentGroupRoleType.Marketing);
            if(CollectionUtils.isNotEmpty(groupList)){
                AgentGroup group = null;
                if (schoolLevelFlag == 1){
                    group = groupList.stream().filter(p -> p != null && p.fetchServiceTypeList().contains(AgentServiceType.JUNIOR_SCHOOL)).findFirst().orElse(null);
                }else if (schoolLevelFlag == 24){
                    group = groupList.stream().filter(p -> p != null && (p.fetchServiceTypeList().contains(AgentServiceType.MIDDLE_SCHOOL) || p.fetchServiceTypeList().contains(AgentServiceType.SENIOR_SCHOOL))).findFirst().orElse(null);
                }
                if(group != null){
                    resultMap.put("id", group.getId());
                    resultMap.put("idType", AgentConstants.INDICATOR_TYPE_GROUP);
                    resultMap.put("schoolLevelFlag", schoolLevelFlag);
                }
            }
        }else {
            List<Long> managedGroupIds = baseOrgService.getManagedGroupIdListByUserId(userId);
            // 用户是部门经理
            if(CollectionUtils.isNotEmpty(managedGroupIds)){
                AgentGroup group = baseOrgService.getGroupById(managedGroupIds.get(0));
                if(group != null){
                    resultMap.put("id", group.getId());
                    resultMap.put("idType", AgentConstants.INDICATOR_TYPE_GROUP);
                    resultMap.put("schoolLevelFlag", group.fetchServiceTypeList().contains(AgentServiceType.JUNIOR_SCHOOL) ? 1 : 24);
                }
            }else {
                resultMap.put("id", userId);
                resultMap.put("idType", AgentConstants.INDICATOR_TYPE_USER);
                List<SchoolLevel> schoolLevelList = baseOrgService.getUserServiceSchoolLevels(userId);
                resultMap.put("schoolLevelFlag", schoolLevelList.contains(SchoolLevel.JUNIOR) ? 1 : 24);
            }
        }
        return resultMap;
    }
}
