package com.voxlearning.utopia.agent.service.indicator;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.agent.DayUtils;
import com.voxlearning.utopia.agent.athena.LoadNewSchoolServiceClient;
import com.voxlearning.utopia.agent.bean.indicator.history.HistoryOnlineIndicator;
import com.voxlearning.utopia.agent.bean.indicator.school.SchoolOfflineIndicator;
import com.voxlearning.utopia.agent.bean.indicator.school.SchoolOnlineIndicator;
import com.voxlearning.utopia.agent.bean.indicator.sum.SumOfflineIndicatorWithBudget;
import com.voxlearning.utopia.agent.bean.indicator.sum.SumOnlineIndicator;
import com.voxlearning.utopia.agent.bean.indicator.sum.SumOnlineIndicatorWithBudget;
import com.voxlearning.utopia.agent.constants.AgentConstants;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.indicator.support.IndicatorFactorySelector;
import com.voxlearning.utopia.agent.service.indicator.support.offline.OfflineIndicatorFactory;
import com.voxlearning.utopia.agent.service.indicator.support.online.OnlineIndicatorFactory;
import com.voxlearning.utopia.agent.service.mobile.PerformanceService;
import com.voxlearning.utopia.agent.support.AgentIndicatorSupport;
import com.voxlearning.utopia.agent.support.AgentSchoolLevelSupport;
import com.voxlearning.utopia.agent.utils.AgentSchoolLevelUtils;
import com.voxlearning.utopia.agent.utils.MathUtils;
import com.voxlearning.utopia.agent.view.indicator.SumOfflineIndicatorView;
import com.voxlearning.utopia.agent.view.performance.Performance17ViewData;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.School;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Named
public class IndicatorService extends AbstractAgentService {

    @Inject
    private BaseOrgService baseOrgService;
    @Inject
    private OnlineIndicatorService onlineIndicatorService;
    @Inject
    private OfflineIndicatorService offlineIndicatorService;
    @Inject
    private LoadNewSchoolServiceClient loadNewSchoolServiceClient;
    @Inject
    private SchoolLoaderClient schoolLoaderClient;
    @Inject
    private PerformanceService performanceService;
    @Inject
    private AgentSchoolLevelSupport agentSchoolLevelSupport;

    @Inject
    private IndicatorFactorySelector indicatorFactorySelector;
    @Inject
    private HistoryIndicatorService historyIndicatorService;


    public boolean judgeGroupDimension(AgentGroupRoleType groupRoleType, Integer dimension){
        boolean result = false;
        if(groupRoleType == AgentGroupRoleType.Marketing || groupRoleType == AgentGroupRoleType.Region){ // 业务部或大区的情况
            if(dimension == 1 || dimension == 2 || dimension == 3 || dimension == 4 || dimension == 5){
                result = true;
            }
        }else if(groupRoleType == AgentGroupRoleType.Area){  // 区域的情况
            if(dimension == 1 || dimension == 3 || dimension == 4 || dimension == 5){
                result = true;
            }
        }else if(groupRoleType == AgentGroupRoleType.City){
            if(dimension == 1 || dimension == 4 || dimension == 5){
                result = true;
            }
        }
        return result;
    }

    // 获取指定维度的部门列表
    public Collection<Long> fetchGroupList(Long groupId, AgentGroupRoleType groupRoleType, Integer dimension){
        if(dimension == 5 || (groupRoleType == AgentGroupRoleType.City && dimension == 1)){ // 专员列表的情况下，  分区的默认数据是专员
            return Collections.emptyList();
        }
        if(!judgeGroupDimension(groupRoleType, dimension)){
            return Collections.emptyList();
        }

        List<AgentGroup> groupList = new ArrayList<>();
        if(dimension == 1){  // 获取直接子部门
            groupList.addAll(baseOrgService.getGroupListByParentId(groupId));
        }else if((groupRoleType == AgentGroupRoleType.Region && dimension == 2) || (groupRoleType == AgentGroupRoleType.Area && dimension == 3) || (groupRoleType == AgentGroupRoleType.City && dimension == 4)){  // 看同级别的兄弟部门数据
            AgentGroup parentGroup = baseOrgService.getParentGroup(groupId);
            if(parentGroup != null){
                List<AgentGroup> subGroupList = baseOrgService.getGroupListByParentId(parentGroup.getId());
                subGroupList.forEach(p -> {
                    if(p.fetchGroupRoleType() == groupRoleType){
                        groupList.add(p);
                    }
                });
            }
        }else {
            List<AgentGroup> subGroupList = baseOrgService.getSubGroupList(groupId);

            AgentGroupRoleType targetGroupRole = null;
            if(dimension == 2){
                targetGroupRole = AgentGroupRoleType.Region;
            }else if(dimension == 3){
                targetGroupRole = AgentGroupRoleType.Area;
            }else if(dimension == 4){
                targetGroupRole = AgentGroupRoleType.City;
            }
            for (AgentGroup p : subGroupList) {
                if(p.fetchGroupRoleType() == targetGroupRole){
                    groupList.add(p);
                }
            }
        }
        return groupList.stream().filter(item -> null != item && null != item.fetchGroupRoleType()).map(AgentGroup::getId).collect(Collectors.toSet());
    }

    // 获取部门的指标数据
    public List<Map<String, Object>> generateGroupIndicatorViewList(Long groupId, AgentGroupRoleType groupRoleType, Integer day, Integer schoolLevelFlag, int mode, int dimension, int indicator, int subject, int monthOrDay){
        List<Map<String, Object>> resultList = new ArrayList<>();

        Collection<Long> targetGroupList = fetchGroupList(groupId, groupRoleType, dimension);
        if(CollectionUtils.isEmpty(targetGroupList)){
            return resultList;
        }
        Integer selectorMode = indicatorFactorySelector.getSelectorMode(groupId, AgentConstants.INDICATOR_TYPE_GROUP);
        if(mode == AgentConstants.MODE_ONLINE){
            OnlineIndicatorFactory indicatorFactory = indicatorFactorySelector.fetchOnlineIndicatorFactory(selectorMode);
            Map<Long, SumOnlineIndicatorWithBudget> groupDataMap = indicatorFactory.generateGroupDataList(targetGroupList, day, schoolLevelFlag);
            resultList.addAll(generateOnlineIndicatorViewMap(groupDataMap.values(), schoolLevelFlag, indicator, subject, monthOrDay, selectorMode));
        }else {
            OfflineIndicatorFactory indicatorFactory = indicatorFactorySelector.fetchOfflineIndicatorFactory(selectorMode);
            Map<Long, SumOfflineIndicatorWithBudget> groupDataMap = indicatorFactory.generateGroupDataList(targetGroupList, day, schoolLevelFlag);
            resultList.addAll(generateOfflineIndicatorViewMap(groupDataMap.values(), schoolLevelFlag, indicator, subject, monthOrDay));
        }
        return resultList;
    }
//    // 获取部门的指标数据
//    public List<Map<String, Object>> generateGroupIndicatorViewList(Long groupId, AgentGroupRoleType groupRoleType, Integer day, Integer schoolLevelFlag, int mode, int dimension, int indicator, int subject, int monthOrDay){
//        List<Map<String, Object>> resultList = new ArrayList<>();
//
//        Collection<Long> targetGroupList = fetchGroupList(groupId, groupRoleType, dimension);
//
//        if(CollectionUtils.isNotEmpty(targetGroupList)){
//            Map<Long, Integer> groupCompositeLevelMap = fetchTargetCompositeSchoolLevel(targetGroupList, AgentConstants.INDICATOR_TYPE_GROUP, schoolLevelFlag);
//            Map<Integer, List<Long>> levelGroupListMap = new HashMap<>();
//            groupCompositeLevelMap.forEach((k, v) -> {
//                List<Long> groupList = levelGroupListMap.get(v);
//                if(CollectionUtils.isEmpty(groupList)){
//                    groupList = new ArrayList<>();
//                    levelGroupListMap.put(v, groupList);
//                }
//                groupList.add(k);
//            });
//            if(mode == AgentConstants.MODE_ONLINE){
//                Map<Long, SumOnlineIndicatorWithBudget> indicatorWithBudgetMap = new HashMap<>();
//                levelGroupListMap.forEach((k, v) -> indicatorWithBudgetMap.putAll(onlineIndicatorService.loadGroupSumDataWithBudget(v, day, AgentSchoolLevelUtils.fetchFromCompositeSchoolLevel(k))));
//                resultList.addAll(generateOnlineIndicatorViewMap(indicatorWithBudgetMap.values(), schoolLevelFlag, indicator, subject, monthOrDay));
//            }else if(mode == AgentConstants.MODE_OFFLINE){
//                Map<Long, SumOfflineIndicatorWithBudget> indicatorWithBudgetMap = new HashMap<>();
//                levelGroupListMap.forEach((k, v) -> indicatorWithBudgetMap.putAll(offlineIndicatorService.loadGroupSumDataWithBudget(v, day, AgentSchoolLevelUtils.fetchFromCompositeSchoolLevel(k))));
//                resultList.addAll(generateOfflineIndicatorViewMap(indicatorWithBudgetMap.values(), schoolLevelFlag, indicator, subject, monthOrDay));
//            }
//        }
//        return resultList;
//    }



    // 获取Online某个维度的指标数据
    private List<Map<String, Object>> generateOnlineIndicatorViewMap(Collection<SumOnlineIndicatorWithBudget> indicatorWithBudgets, Integer schoolLevelFlag, int indicator, int subject, int monthOrDay, Integer selectorMode){
        if(CollectionUtils.isEmpty(indicatorWithBudgets)){
            return Collections.emptyList();
        }

        List<Map<String, Object>> resultList = new ArrayList<>();
        Integer viewType = schoolLevelFlag * 1000000 + indicator * 10000 + subject * 100 + monthOrDay;
        indicatorWithBudgets.forEach(p -> {
            if(p != null){
                Performance17ViewData viewData = AgentIndicatorSupport.generateOnlineViewData(p, viewType);
                if(viewData != null){
                    resultList.add(viewData.generateIndicatorDataMap());
                }
            }
        });
        if(selectorMode == 1){
            // 重置留存率，8/9月份取5月分科目认证3套月活
            resetOnlineMrtRate(resultList);
        }
        return resultList;
    }

    // 获取Offline某个维度的指标数据
    private List<Map<String, Object>> generateOfflineIndicatorViewMap(Collection<SumOfflineIndicatorWithBudget> indicatorWithBudgets, Integer schoolLevelFlag, int indicator, int subject, int monthOrDay){
        if(CollectionUtils.isEmpty(indicatorWithBudgets)){
            return Collections.emptyList();
        }

        List<Map<String, Object>> resultList = new ArrayList<>();
        Integer viewType = schoolLevelFlag * 1000000 + indicator * 10000 + subject * 100 + monthOrDay;
        indicatorWithBudgets.forEach(p -> {
            if(p != null){
                SumOfflineIndicatorView viewData = AgentIndicatorSupport.generateOfflineViewData(p, viewType);
                resultList.add(viewData.generateDataMap());
            }
        });
        return resultList;
    }


    // 部门下专员数据， 如果是分区的话，包含未分配数据
    public List<Map<String, Object>> generateGroupUserIndicatorViewList(Long groupId, AgentGroupRoleType groupRoleType, Integer day, Integer schoolLevelFlag, int mode, int dimension, int indicator, int subject, int monthOrDay){
        List<Map<String, Object>> resultList = new ArrayList<>();

        List<Long> userList = baseOrgService.getAllSubGroupUserIdsByGroupIdAndRole(groupId, AgentRoleType.BusinessDeveloper.getId());

        resultList.addAll(generateUserIndicatorViewList(userList, day, schoolLevelFlag, mode, indicator, subject, monthOrDay));

        Integer selectorMode = indicatorFactorySelector.getSelectorMode(groupId, AgentConstants.INDICATOR_TYPE_GROUP);

        // 分区的情况下， 设置未分配数据
        if(groupRoleType == AgentGroupRoleType.City && dimension == 1){
            if(mode == AgentConstants.MODE_ONLINE){
                OnlineIndicatorFactory indicatorFactory = indicatorFactorySelector.fetchOnlineIndicatorFactory(selectorMode);
                SumOnlineIndicatorWithBudget unallocatedData = indicatorFactory.generateUnallocatedData(groupId, day, schoolLevelFlag);
                if(unallocatedData != null){
                    resultList.addAll(generateOnlineIndicatorViewMap(Collections.singleton(unallocatedData), schoolLevelFlag, indicator, subject, monthOrDay, selectorMode));
                }
            }else if(mode == AgentConstants.MODE_OFFLINE){
                OfflineIndicatorFactory indicatorFactory = indicatorFactorySelector.fetchOfflineIndicatorFactory(selectorMode);
                SumOfflineIndicatorWithBudget unallocatedData = indicatorFactory.generateUnallocatedData(groupId, day, schoolLevelFlag);
                if(unallocatedData != null){
                    resultList.addAll(generateOfflineIndicatorViewMap(Collections.singleton(unallocatedData), schoolLevelFlag, indicator, subject, monthOrDay));
                }
            }
        }
        return resultList;
    }

    // 获取用户指标数据
    // 获取用户指标数据
    public List<Map<String, Object>>  generateUserIndicatorViewList(Collection<Long> userIds, Integer day, Integer schoolLevelFlag, int mode, int indicator, int subject, int monthOrDay) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        if (CollectionUtils.isEmpty(userIds)) {
            return resultList;
        }
        Long firstUserId =(new ArrayList<>(userIds)).get(0);
        Integer selectorMode = indicatorFactorySelector.getSelectorMode(firstUserId, AgentConstants.INDICATOR_TYPE_USER);
        if(mode == AgentConstants.MODE_ONLINE){
            OnlineIndicatorFactory indicatorFactory = indicatorFactorySelector.fetchOnlineIndicatorFactory(selectorMode);
            Map<Long, SumOnlineIndicatorWithBudget> userDataMap = indicatorFactory.generateUserDataList(userIds, day, schoolLevelFlag);
            resultList.addAll(generateOnlineIndicatorViewMap(userDataMap.values(), schoolLevelFlag, indicator, subject, monthOrDay, selectorMode));
        }else if(mode == AgentConstants.MODE_OFFLINE){
            OfflineIndicatorFactory indicatorFactory = indicatorFactorySelector.fetchOfflineIndicatorFactory(selectorMode);
            Map<Long, SumOfflineIndicatorWithBudget> userDataMap = indicatorFactory.generateUserDataList(userIds, day, schoolLevelFlag);
            resultList.addAll(generateOfflineIndicatorViewMap(userDataMap.values(), schoolLevelFlag, indicator, subject, monthOrDay));
        }
        return resultList;
    }
//    public List<Map<String, Object>>  generateUserIndicatorViewList(Collection<Long> userIds, Integer day, Integer schoolLevelFlag, int mode, int indicator, int subject, int monthOrDay){
//        List<Map<String, Object>> resultList = new ArrayList<>();
//        if(CollectionUtils.isEmpty(userIds)){
//            return resultList;
//        }
//        Map<Long, Integer> groupCompositeLevelMap = fetchTargetCompositeSchoolLevel(userIds, AgentConstants.INDICATOR_TYPE_USER, schoolLevelFlag);
//        Map<Integer, List<Long>> levelGroupListMap = new HashMap<>();
//        groupCompositeLevelMap.forEach((k, v) -> {
//            List<Long> groupList = levelGroupListMap.get(v);
//            if(CollectionUtils.isEmpty(groupList)){
//                groupList = new ArrayList<>();
//                levelGroupListMap.put(v, groupList);
//            }
//            groupList.add(k);
//        });
//
//        if(mode == AgentConstants.MODE_ONLINE){
//            Map<Long, SumOnlineIndicatorWithBudget> indicatorWithBudgetMap = new HashMap<>();
//            levelGroupListMap.forEach((k, v) -> indicatorWithBudgetMap.putAll(onlineIndicatorService.loadUserSumDataWithBudget(v, day, AgentSchoolLevelUtils.fetchFromCompositeSchoolLevel(k))));
//            resultList.addAll(generateOnlineIndicatorViewMap(indicatorWithBudgetMap.values(), schoolLevelFlag, indicator, subject, monthOrDay));
//        }else if(mode == AgentConstants.MODE_OFFLINE){
//            Map<Long, SumOfflineIndicatorWithBudget> indicatorWithBudgetMap = new HashMap<>();
//            levelGroupListMap.forEach((k, v) -> indicatorWithBudgetMap.putAll(offlineIndicatorService.loadUserSumDataWithBudget(v, day, AgentSchoolLevelUtils.fetchFromCompositeSchoolLevel(k))));
//            resultList.addAll(generateOfflineIndicatorViewMap(indicatorWithBudgetMap.values(), schoolLevelFlag, indicator, subject, monthOrDay));
//        }
//        return resultList;
//    }


    // 获取学校单一指标数据
    public List<Map<String, Object>> generateSchoolIndicatorViewList(Long id, Integer dataType, Integer day, int mode, int indicator, int subject, int monthOrDay, int schoolLevelFlag){
        List<Map<String, Object>> resultList = new ArrayList<>();

        if(mode == AgentConstants.MODE_ONLINE){
            OnlineIndicatorFactory indicatorFactory = indicatorFactorySelector.fetchOnlineIndicatorFactory(id, dataType);
            Map<Long, SchoolOnlineIndicator> schoolDataMap = indicatorFactory.generateSchoolData(id, dataType, day, schoolLevelFlag);
            schoolDataMap.values().forEach(p -> {
                Map<String, Object> map = AgentIndicatorSupport.generateSchoolOnlineViewData(p, indicator, subject, monthOrDay, schoolLevelFlag);
                if(MapUtils.isNotEmpty(map)){
                    resultList.add(map);
                }
            });
        }else if(mode == AgentConstants.MODE_OFFLINE){
            OfflineIndicatorFactory indicatorFactory = indicatorFactorySelector.fetchOfflineIndicatorFactory(id, dataType);
            Map<Long, SchoolOfflineIndicator> schoolDataMap = indicatorFactory.generateSchoolData(id, dataType, day, schoolLevelFlag);
            schoolDataMap.values().forEach(p -> {
                Map<String, Object> map = AgentIndicatorSupport.generateSchoolOfflineViewData(p, indicator, monthOrDay);
                if(MapUtils.isNotEmpty(map)){
                    resultList.add(map);
                }
            });
        }

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

//    public List<Map<String, Object>> generateSchoolIndicatorViewList(Collection<Long> schoolIds, Integer day, int mode, int indicator, int subject, int monthOrDay, int schoolLevelFlag) {
//        List<Map<String, Object>> resultList = new ArrayList<>();
//        if(CollectionUtils.isEmpty(schoolIds)){
//            return resultList;
//        }
//
//        Map<Long, School> schoolMap = schoolLoaderClient.getSchoolLoader()
//                .loadSchools(schoolIds)
//                .getUninterruptibly();
//
//        if(mode == AgentConstants.MODE_ONLINE){
//            Map<Long, SchoolOnlineIndicator> indicatorMap = loadNewSchoolServiceClient.loadSchoolOnlineIndicator(schoolIds, day);
//            indicatorMap.values().forEach(p -> {
//                Map<String, Object> map = AgentIndicatorSupport.generateSchoolOnlineViewData(p, indicator, subject, monthOrDay, schoolLevelFlag);
//                if(MapUtils.isNotEmpty(map)){
//                    resultList.add(map);
//                }
//            });
//        }else if(mode == AgentConstants.MODE_OFFLINE){
//            Map<Long, SchoolOfflineIndicator> indicatorMap = loadNewSchoolServiceClient.loadSchoolOfflineIndicator(schoolIds, day);
//            indicatorMap.values().forEach(p -> {
//                Map<String, Object> map = AgentIndicatorSupport.generateSchoolOfflineViewData(p, indicator, monthOrDay);
//                if(MapUtils.isNotEmpty(map)){
//                    resultList.add(map);
//                }
//            });
//        }
//
//        resultList.forEach(p -> {
//            Long schoolId = SafeConverter.toLong(p.get("id"));
//            School school = schoolMap.get(schoolId);
//            if(school != null){
//                p.put("name", school.getCname());
//            }
//        });
//
//        return resultList;
//    }


    // 获取Online其他数据
    // 对比上月同期注册认证增幅， 昨日新增注册>50 学校数，昨日新增1套>50 学校数
    public void generateOnlineExtIndicatorData(Map<String, Object> viewDataMap, Long id, Integer dataType, Integer schoolLevelFlag){

        if(MapUtils.isEmpty(viewDataMap)){
            return;
        }

        Integer day = performanceService.lastSuccessDataDay();

        // 上月同期
        Date date = performanceService.lastSuccessDataDate();
        Integer targetDay = SafeConverter.toInt(DateUtils.dateToString(DateUtils.addMonths(date, -1), "yyyyMMdd"));

        List<Integer> schoolLevelIds = agentSchoolLevelSupport.fetchTargetSchoolLevelIds(id, dataType, schoolLevelFlag);
        SumOnlineIndicator sumOnlineIndicator = null;
        if(Objects.equals(dataType, AgentConstants.INDICATOR_TYPE_GROUP)){
            sumOnlineIndicator = onlineIndicatorService.loadGroupSumData(Collections.singleton(id), targetDay, schoolLevelIds).get(id);
        }else if(Objects.equals(dataType, AgentConstants.INDICATOR_TYPE_USER)){
            sumOnlineIndicator = onlineIndicatorService.loadUserSumData(Collections.singleton(id), targetDay, schoolLevelIds).get(id);
        }

        // 设置对比上月同期注册认证增幅
        int compareLmRegStuCount = 0;
        int compareLmAuStuCount = 0;
        if(sumOnlineIndicator != null){
            Integer lmRegStuCount = SafeConverter.toInt(sumOnlineIndicator.fetchMonthData().getRegStuCount());
            Integer lmAuStuCount = SafeConverter.toInt(sumOnlineIndicator.fetchMonthData().getAuStuCount());
            compareLmRegStuCount = SafeConverter.toInt(viewDataMap.get("tmRegStuCount")) - lmRegStuCount;
            compareLmAuStuCount = SafeConverter.toInt(viewDataMap.get("tmAuStuCount")) - lmAuStuCount;

        }
        viewDataMap.put("compareLmRegStuCount", compareLmRegStuCount);
        viewDataMap.put("compareLmAuStuCount", compareLmAuStuCount);

//        // 设置新增注册>50的学校数
//        // 设置任意科目新增1套>50 的学校数
//        int pdRegStuGt50SchoolCount = 0;
//        int pdFinHwGte1SchoolCount = 0;
//        if(Objects.equals(dataType, AgentConstants.INDICATOR_TYPE_USER)){
//            List<AgentUserSchool> userSchools = baseOrgService.getUserSchoolByUser(id);
//            List<Long> tmpSchoolIds = userSchools.stream().map(AgentUserSchool::getSchoolId).collect(Collectors.toList());
//            List<Long> schoolIds = baseOrgService.getSchoolListByLevels(tmpSchoolIds, schoolLevelIds.stream().map(SchoolLevel::safeParse).collect(Collectors.toList()));
//            if(CollectionUtils.isNotEmpty(schoolIds)){
//                Map<Long, SchoolOnlineIndicator> indicatorMap = loadNewSchoolServiceClient.loadSchoolOnlineIndicator(schoolIds, day);
//                pdRegStuGt50SchoolCount = (int)indicatorMap.values().stream().filter(p -> p != null && SafeConverter.toInt(p.fetchDayData().getRegStuCount()) > 50).count();
//                pdFinHwGte1SchoolCount = (int)indicatorMap.values().stream().filter(p -> p != null && SafeConverter.toInt(p.fetchDayData().getFinSglSubjHwGte1StuCount()) > 50).count();
//            }
//        }
//        viewDataMap.put("pdRegStuGt50SchoolCount", pdRegStuGt50SchoolCount);
//        viewDataMap.put("pdFinHwGte1SchoolCount", pdFinHwGte1SchoolCount);
    }

    // 重置留存率基数及留存率
    //  默认情况下基数取学期维度“分科目认证3套月活”
    //  8/9月份取5月分科目认证3套月活
    public void resetOnlineMrtRate(List<Map<String, Object>> viewDataMapList){
        if(CollectionUtils.isEmpty(viewDataMapList)){
            return;
        }
        Date endDate = DateUtils.stringToDate("20181001", "yyyyMMdd");
        if(!performanceService.lastSuccessDataDate().before(endDate)){
            return;
        }
        Integer targetDay = 20180531;
        Map<Integer, Map<Integer, List<Long>>> groupedDataMap = new HashMap<>();
        viewDataMapList.forEach(p -> {
            Integer dataType = SafeConverter.toInt(p.get("dataType"));
            Map<Integer, List<Long>> typeMap = groupedDataMap.get(dataType);
            if(MapUtils.isEmpty(typeMap)){
                typeMap = new HashMap<>();
                groupedDataMap.put(dataType, typeMap);
            }
            Integer schoolLevel = (Integer)p.get("schoolLevel");
            List<Long> idList = typeMap.get(schoolLevel);
            if(CollectionUtils.isEmpty(idList)){
                idList = new ArrayList<>();
                typeMap.put(schoolLevel, idList);
            }
            idList.add((Long)p.get("id"));
        });

        Map<Integer, Map<Long, SumOnlineIndicator>> typeIndicatorMap = new HashMap<>();
        for(Integer dataType : groupedDataMap.keySet()){

            Map<Long, SumOnlineIndicator> indicatorMap = typeIndicatorMap.get(dataType);
            if(MapUtils.isEmpty(indicatorMap)){
                indicatorMap = new HashMap<>();
                typeIndicatorMap.put(dataType, indicatorMap);
            }

            Map<Integer, List<Long>> typeMap = groupedDataMap.get(dataType);
            for(Integer schoolLevel : typeMap.keySet()){
                List<Long> ids = typeMap.get(schoolLevel);
                if(CollectionUtils.isEmpty(ids)){
                    continue;
                }
                Map<Long, SumOnlineIndicator> tempMap = new HashMap<>();
                if(Objects.equals(dataType, AgentConstants.INDICATOR_TYPE_GROUP)) {
                    tempMap = onlineIndicatorService.loadGroupSumData(ids, targetDay, AgentSchoolLevelUtils.fetchFromCompositeSchoolLevel(schoolLevel));
                }else if(Objects.equals(dataType, AgentConstants.INDICATOR_TYPE_USER)) {
                    tempMap = onlineIndicatorService.loadUserSumData(ids, targetDay, AgentSchoolLevelUtils.fetchFromCompositeSchoolLevel(schoolLevel));
                }else if(Objects.equals(dataType, AgentConstants.INDICATOR_TYPE_UNALLOCATED)){
                    for(Long id : ids){
                        SumOnlineIndicator sumOnlineIndicator = onlineIndicatorService.loadGroupUnallocatedSumData(id, targetDay, AgentSchoolLevelUtils.fetchFromCompositeSchoolLevel(schoolLevel));
                        if(sumOnlineIndicator != null){
                            tempMap.put(id, sumOnlineIndicator);
                        }
                    }
                }
                if(MapUtils.isNotEmpty(tempMap)){
                    indicatorMap.putAll(tempMap);
                }
            }
        }

        resetOnlineMrtRate(viewDataMapList, typeIndicatorMap);
    }

    private void resetOnlineMrtRate(List<Map<String, Object>> viewDataMapList, Map<Integer, Map<Long, SumOnlineIndicator>> typeIndicatorMap){
        if(CollectionUtils.isEmpty(viewDataMapList) || MapUtils.isEmpty(typeIndicatorMap)){
            return;
        }
        viewDataMapList.forEach(p -> {
            Integer dataType = SafeConverter.toInt(p.get("dataType"));
            Map<Long, SumOnlineIndicator> indicatorMap = typeIndicatorMap.get(dataType);
            if(MapUtils.isEmpty(indicatorMap)){
                return;
            }
            Long id = (Long) p.get("id");
            SumOnlineIndicator sumOnlineIndicator = indicatorMap.get(id);
            if(sumOnlineIndicator == null){
                return;
            }

            Integer viewType = (Integer) p.get("viewType");
            if(viewType == AgentIndicatorSupport.VIEW_TYPE_OVERVIEW_JUNIOR || viewType == AgentIndicatorSupport.VIEW_TYPE_OVERVIEW_MIDDLE){
                Integer engMrtStuCount = SafeConverter.toInt(p.get("engMrtStuCount"));
                p.put("engMrtRate", MathUtils.doubleDivide(engMrtStuCount, SafeConverter.toInt(sumOnlineIndicator.fetchMonthData().getFinEngHwGte3AuStuCount())));

                Integer mathMrtStuCount = SafeConverter.toInt(p.get("mathMrtStuCount"));
                p.put("mathMrtRate", MathUtils.doubleDivide(mathMrtStuCount, SafeConverter.toInt(sumOnlineIndicator.fetchMonthData().getFinMathHwGte3AuStuCount())));

                if(viewType == AgentIndicatorSupport.VIEW_TYPE_OVERVIEW_JUNIOR){
                    Integer chnMrtStuCount = SafeConverter.toInt(p.get("chnMrtStuCount"));
                    p.put("chnMrtRate", MathUtils.doubleDivide(chnMrtStuCount, SafeConverter.toInt(sumOnlineIndicator.fetchMonthData().getFinChnHwGte3AuStuCount())));
                }
            }else if(viewType == AgentIndicatorSupport.VIEW_TYPE_JUNIOR_RT_ENG_TM
                    || viewType == AgentIndicatorSupport.VIEW_TYPE_MIDDLE_RT_ENG_TM
                    || viewType == AgentIndicatorSupport.VIEW_TYPE_JUNIOR_RT_MATH_TM
                    || viewType == AgentIndicatorSupport.VIEW_TYPE_MIDDLE_RT_MATH_TM
                    || viewType == AgentIndicatorSupport.VIEW_TYPE_JUNIOR_RT_CHN_TM
                    ){
                Integer lmFinHwGte3AuStuCount = 0;
                if (viewType == AgentIndicatorSupport.VIEW_TYPE_JUNIOR_RT_ENG_TM || viewType == AgentIndicatorSupport.VIEW_TYPE_MIDDLE_RT_ENG_TM) {
                    lmFinHwGte3AuStuCount = SafeConverter.toInt(sumOnlineIndicator.fetchMonthData().getFinEngHwGte3AuStuCount());
                } else if (viewType == AgentIndicatorSupport.VIEW_TYPE_JUNIOR_RT_MATH_TM || viewType == AgentIndicatorSupport.VIEW_TYPE_MIDDLE_RT_MATH_TM) {
                    lmFinHwGte3AuStuCount = SafeConverter.toInt(sumOnlineIndicator.fetchMonthData().getFinMathHwGte3AuStuCount());
                } else {
                    lmFinHwGte3AuStuCount = SafeConverter.toInt(sumOnlineIndicator.fetchMonthData().getFinChnHwGte3AuStuCount());
                }
                Integer mrtStuCount = SafeConverter.toInt(p.get("mrtStuCount"));
                p.put("lmFinHwGte3AuStuCount", lmFinHwGte3AuStuCount);
                p.put("mrtRate", MathUtils.doubleDivide(mrtStuCount, lmFinHwGte3AuStuCount));
            }
        });

    }

    /**
     * 柱状图
     * @param id
     * @param idType
     * @param schoolLevelFlag
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

        Map<Integer, HistoryOnlineIndicator> dayHwIndicatorMap = new HashMap<>();
        List<Integer> schoolLevelIds = agentSchoolLevelSupport.fetchTargetSchoolLevelIds(id, idType, schoolLevelFlag);
        if (Objects.equals(idType, AgentConstants.INDICATOR_TYPE_USER)){
            dayHwIndicatorMap.putAll(historyIndicatorService.loadUserHwHistory(id,dayList,schoolLevelIds));
        }else {
            dayHwIndicatorMap.putAll(historyIndicatorService.loadGroupHwHistory(id,dayList,schoolLevelIds));
        }
        List<Map<String,Object>> monthDataList = new ArrayList<>();
        for (Integer dayItem : dayList) {
            Map<String,Object> itemMap = new HashMap<>();
            itemMap.put("day",dayItem);
            HistoryOnlineIndicator historyOnlineIndicator = dayHwIndicatorMap.get(dayItem);
            itemMap.put("regStuNum",historyOnlineIndicator != null ? SafeConverter.toInt(historyOnlineIndicator.getRegStuNum()) : 0);
            monthDataList.add(itemMap);
        }
        dataMap.put("monthDataList",monthDataList);
        return dataMap;
    }
}
