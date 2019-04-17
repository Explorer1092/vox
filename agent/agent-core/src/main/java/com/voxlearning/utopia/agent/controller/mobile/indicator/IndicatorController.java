package com.voxlearning.utopia.agent.controller.mobile.indicator;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.athena.LoadNewSchoolServiceClient;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.bean.indicator.OnlineIndicator;
import com.voxlearning.utopia.agent.bean.indicator.sum.SumOfflineIndicatorWithBudget;
import com.voxlearning.utopia.agent.bean.indicator.sum.SumOnlineIndicatorWithBudget;
import com.voxlearning.utopia.agent.constants.AgentConstants;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.service.indicator.IndicatorService;
import com.voxlearning.utopia.agent.service.indicator.support.IndicatorFactorySelector;
import com.voxlearning.utopia.agent.service.indicator.support.offline.OfflineIndicatorFactory;
import com.voxlearning.utopia.agent.service.indicator.support.online.OnlineIndicatorFactory;
import com.voxlearning.utopia.agent.service.mobile.PerformanceService;
import com.voxlearning.utopia.agent.support.AgentIndicatorSupport;
import com.voxlearning.utopia.agent.support.AgentSchoolLevelSupport;
import com.voxlearning.utopia.agent.view.indicator.SumOfflineIndicatorView;
import com.voxlearning.utopia.agent.view.performance.Performance17ViewData;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentServiceType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/mobile/indicator")
public class IndicatorController extends AbstractAgentController {

    @Inject
    private PerformanceService performanceService;
    @Inject
    private IndicatorService indicatorService;
    @Inject
    private LoadNewSchoolServiceClient loadNewSchoolServiceClient;
    @Inject
    private IndicatorFactorySelector indicatorFactorySelector;
    @Inject
    private AgentSchoolLevelSupport agentSchoolLevelSupport;

    // 业绩概览
    @RequestMapping(value = "overview.vpage")
    @ResponseBody
    public MapMessage indicatorOverview(){
        Map<String, Object> parameterMap = getTargetParameterMap();
        Long id = (Long)parameterMap.getOrDefault("id", 0);
        Integer dataType = (Integer)parameterMap.getOrDefault("dataType", AgentConstants.INDICATOR_TYPE_USER);
        int schoolLevelFlag = (Integer)parameterMap.getOrDefault("schoolLevel", 1); // 学校阶段   1:小学   24：初高中
        int mode = (Integer)parameterMap.getOrDefault("mode", AgentConstants.MODE_ONLINE);       // 模式： 1:online   2：offline


        if(id <= 0 || (!Objects.equals(dataType, AgentConstants.INDICATOR_TYPE_GROUP) && !Objects.equals(dataType, AgentConstants.INDICATOR_TYPE_USER)) || (mode != AgentConstants.MODE_ONLINE && mode != AgentConstants.MODE_OFFLINE)){
            return MapMessage.errorMessage("参数错误！");
        }

        MapMessage message = MapMessage.successMessage();

        Integer selectorMode = indicatorFactorySelector.getSelectorMode(id, dataType);
        message.put("isChannel", selectorMode == 2);
        if(selectorMode == 1){
            if(getCurrentUser().isCountryManager() || getCurrentUser().isAdmin()){
                List<AgentGroup> groupList = baseOrgService.getAgentGroupByRole(AgentGroupRoleType.Marketing);
                if(CollectionUtils.isNotEmpty(groupList)){
                    AgentGroup group = groupList.stream().filter(p -> p != null && p.fetchServiceTypeList().contains(AgentServiceType.JUNIOR_SCHOOL)).findFirst().orElse(null);
                    if(group != null){
                        message.put("juniorMarketingId", group.getId());
                    }
                    // 中学业务部的数据
                    AgentGroup group2 = groupList.stream().filter(p -> p != null && (p.fetchServiceTypeList().contains(AgentServiceType.MIDDLE_SCHOOL) || p.fetchServiceTypeList().contains(AgentServiceType.SENIOR_SCHOOL))).findFirst().orElse(null);
                    if(group != null){
                        message.put("middleMarketingId", group2.getId());
                    }
                }
            }
        }else if(selectorMode == 2){
            List<SchoolLevel> schoolLevelList = baseOrgService.getUserServiceSchoolLevels(id);
            if(schoolLevelList.contains(SchoolLevel.JUNIOR) && (schoolLevelList.contains(SchoolLevel.MIDDLE) || schoolLevelList.contains(SchoolLevel.HIGH))){
                message.put("juniorMarketingId", id);
                message.put("middleMarketingId", id);
            }
        }

        Integer day = performanceService.lastSuccessDataDay();
        message.put("day", DateUtils.dateToString(performanceService.lastSuccessDataDate(), "yyyy-MM-dd"));

        Map<String, Object> indicatorOverview = new HashMap<>();
        if(mode == AgentConstants.MODE_ONLINE){   // online模式
            OnlineIndicatorFactory onlineIndicatorFactory = indicatorFactorySelector.fetchOnlineIndicatorFactory(id, dataType);
            SumOnlineIndicatorWithBudget indicatorWithBudget = onlineIndicatorFactory.generateOverview(id, dataType, day, schoolLevelFlag);

            if(indicatorWithBudget != null){
                Performance17ViewData viewData = AgentIndicatorSupport.generateOnlineViewData(indicatorWithBudget, schoolLevelFlag * 1000000);
                if(viewData != null) {
                    indicatorOverview.putAll(viewData.generateIndicatorDataMap());
                }
                if(selectorMode == 1){
                    // 对比上月同期注册认证增幅， 昨日新增注册>50 学校数，昨日新增1套>50 学校数
                    indicatorService.generateOnlineExtIndicatorData(indicatorOverview, id, dataType, schoolLevelFlag);
                    // 重置流程率,  默认情况下基数取学期维度“分科目认证3套月活”, 8/9月份取5月分科目认证3套月活
                    indicatorService.resetOnlineMrtRate(Collections.singletonList(indicatorOverview));
                }

            }
        }else{
            OfflineIndicatorFactory indicatorFactory = indicatorFactorySelector.fetchOfflineIndicatorFactory(id, dataType);
            SumOfflineIndicatorWithBudget indicatorWithBudget = indicatorFactory.generateOverview(id, dataType, day, schoolLevelFlag);
            if(indicatorWithBudget != null){
                SumOfflineIndicatorView viewData = AgentIndicatorSupport.generateOfflineViewData(indicatorWithBudget, schoolLevelFlag * 1000000);
                indicatorOverview.putAll(viewData.generateDataMap());

                // 补充学生规模数据
                List<Integer> schoolLevelIds = agentSchoolLevelSupport.fetchTargetSchoolLevelIds(id, dataType, schoolLevelFlag);
                Map<Long, OnlineIndicator> onlineIndicatorMap = loadNewSchoolServiceClient.loadOnlineIndicator(Collections.singleton(id), dataType, day, schoolLevelIds, AgentConstants.ONLINE_INDICATOR_SUM);
                if(MapUtils.isNotEmpty(onlineIndicatorMap) && onlineIndicatorMap.get(id) != null){
                    OnlineIndicator onlineIndicator = onlineIndicatorMap.get(id);
                    int stuScale = SafeConverter.toInt(onlineIndicator.getStuScale());
                    indicatorOverview.put("stuCount", stuScale);
                }
            }
        }

        message.add("indicatorOverview", indicatorOverview);
        return message;
    }

    private Map<String, Object> getTargetParameterMap(){
        Map<String, Object> resultMap = new HashMap<>();
        Long id = getRequestLong("id");
        int mode = getRequestInt("mode", AgentConstants.MODE_ONLINE);       // 模式： 1:online   2：offline
        resultMap.put("mode", mode);
        if(id > 0){
            Integer dataType = getRequestInt("dataType", AgentConstants.INDICATOR_TYPE_USER);
            int schoolLevel = getRequestInt("schoolLevel", 1); // 学校阶段   1:小学   24：初高中

            resultMap.put("id", id);
            resultMap.put("dataType", dataType);
            resultMap.put("schoolLevel", schoolLevel);
        }else {
            Long userId = getCurrentUserId();
            AuthCurrentUser currentUser = getCurrentUser();
            if(currentUser.isCountryManager() || currentUser.isAdmin() || currentUser.isProductOperator() || currentUser.isTargetRole(AgentRoleType.RiskManager.getId())){
                List<AgentGroup> groupList = baseOrgService.getAgentGroupByRole(AgentGroupRoleType.Marketing);
                if(CollectionUtils.isNotEmpty(groupList)){
                    AgentGroup group = groupList.stream().filter(p -> p != null && p.fetchServiceTypeList().contains(AgentServiceType.JUNIOR_SCHOOL)).findFirst().orElse(null);
                    if(group != null){
                        resultMap.put("id", group.getId());
                        resultMap.put("dataType", AgentConstants.INDICATOR_TYPE_GROUP);
                        resultMap.put("schoolLevel", 1);
                    }else {
                        AgentGroup group2 = groupList.stream().filter(p -> p != null && (p.fetchServiceTypeList().contains(AgentServiceType.MIDDLE_SCHOOL) || p.fetchServiceTypeList().contains(AgentServiceType.SENIOR_SCHOOL))).findFirst().orElse(null);
                        if(group2 != null){
                            resultMap.put("id", group2.getId());
                            resultMap.put("dataType", AgentConstants.INDICATOR_TYPE_GROUP);
                            resultMap.put("schoolLevel", 24);
                        }
                    }
                }
            }else {
                Integer selectorMode = indicatorFactorySelector.getSelectorMode(userId, AgentConstants.INDICATOR_TYPE_USER);
                if(selectorMode == 1){
                    List<Long> managedGroupIds = baseOrgService.getManagedGroupIdListByUserId(userId);
                    // 用户是部门经理
                    if(CollectionUtils.isNotEmpty(managedGroupIds)){
                        AgentGroup group = baseOrgService.getGroupById(managedGroupIds.get(0));
                        if(group != null){
                            resultMap.put("id", group.getId());
                            resultMap.put("dataType", AgentConstants.INDICATOR_TYPE_GROUP);
                            resultMap.put("schoolLevel", group.fetchServiceTypeList().contains(AgentServiceType.JUNIOR_SCHOOL) ? 1 : 24);
                        }
                    }else {
                        resultMap.put("id", userId);
                        resultMap.put("dataType", AgentConstants.INDICATOR_TYPE_USER);
                        List<SchoolLevel> schoolLevelList = baseOrgService.getUserServiceSchoolLevels(userId);
                        resultMap.put("schoolLevel", schoolLevelList.contains(SchoolLevel.JUNIOR) ? 1 : 24);
                    }
                }else if(selectorMode == 2){
                    resultMap.put("id", userId);
                    resultMap.put("dataType", AgentConstants.INDICATOR_TYPE_USER);
                    List<SchoolLevel> schoolLevelList = baseOrgService.getUserServiceSchoolLevels(userId);
                    resultMap.put("schoolLevel", schoolLevelList.contains(SchoolLevel.JUNIOR) ? 1 : 24);
                }
            }
        }
        return resultMap;
    }


    // 业绩概览
    @RequestMapping(value = "data_list.vpage")
    @ResponseBody
    public MapMessage indicatorDataList(){
        Long id = getRequestLong("id");
        Integer dataType = getRequestInt("dataType", AgentConstants.INDICATOR_TYPE_USER);
        int schoolLevelFlag = getRequestInt("schoolLevel", 1); // 学校阶段   1:小学   24：初高中
        int mode = getRequestInt("mode", 1);       // 模式： 1:online模式   2：offline模式

        int dimension = getRequestInt("dimension", 1);  // 1:默认   2：大区   3：区域   4：分区   5：专员
        int indicator = getRequestInt("indicator", 1);  //  online模式： 01：概况  02：月活  03：新增  04：回流  05：留存  06：口测     offline模式： 11: 周测1套   12：周测2套
        int subject = getRequestInt("subject", 0);      //  00：全部  01：单科  02：英语  03：数学  04：语文
        int monthOrDay = getRequestInt("monthOrDay", 1);   // 01：本月  02：昨日

        MapMessage message = MapMessage.successMessage();

        Integer day = performanceService.lastSuccessDataDay();
        List<Map<String, Object>> dataList = new ArrayList<>();
        if (Objects.equals(dataType, AgentConstants.INDICATOR_TYPE_GROUP)) {
            AgentGroupRoleType groupRoleType = baseOrgService.getGroupRole(id);
            message.add("groupRoleType", groupRoleType);
            boolean flag = indicatorService.judgeGroupDimension(groupRoleType, dimension);
            if(!flag){
                return MapMessage.errorMessage("参数组合有误！");
            }

            if(dimension == 5 || (groupRoleType == AgentGroupRoleType.City && dimension == 1)){ // 专员列表
                dataList.addAll(indicatorService.generateGroupUserIndicatorViewList(id, groupRoleType, day, schoolLevelFlag, mode, dimension, indicator, subject, monthOrDay));
            }else {  // 部门列表
                dataList.addAll(indicatorService.generateGroupIndicatorViewList(id, groupRoleType, day, schoolLevelFlag, mode, dimension, indicator, subject, monthOrDay));
            }


            dataList.forEach(p -> {
                p.put("clickable", true);           // 默认可点击下钻
                p.put("self", false);               // 是否是当前部门或用户
                if (Objects.equals(p.get("id"), id) && Objects.equals(p.get("dataType"), String.valueOf(dataType))) { // 当前用户或部门的情况下， 设置背景色
                    p.put("self", true);
                }
                // 大区看大区， 区域看区域， 分区看分区的情况下，前端页面不能点击下钻
                if((groupRoleType == AgentGroupRoleType.Region && dimension == 2) || (groupRoleType == AgentGroupRoleType.Area && dimension == 3) || (groupRoleType == AgentGroupRoleType.City && dimension == 4)) {
                    p.put("clickable", false);           // 不可点击下钻
                }
            });

        }else if(Objects.equals(dataType, AgentConstants.INDICATOR_TYPE_USER)){   // 当前用户是专员的情况下， 返回专员所在部门的专员列表
            List<Long> groupIdList = baseOrgService.getGroupIdListByUserId(id);
            if(CollectionUtils.isNotEmpty(groupIdList)){
                List<Long> userList = baseOrgService.getGroupUsersByRole(groupIdList.get(0), AgentRoleType.BusinessDeveloper);
                dataList.addAll(indicatorService.generateUserIndicatorViewList(userList, day, schoolLevelFlag, mode, indicator, subject, monthOrDay));

                dataList.forEach(p -> {
                    p.put("clickable", false);           // 不可点击下钻
                    p.put("self", false);               // 是否是当前部门或用户
                    if(Objects.equals(p.get("id"), id) && Objects.equals(String.valueOf(p.get("dataType")), String.valueOf(dataType))){ // 当前用户或部门的情况下， 设置背景色
                        p.put("clickable", true);
                        p.put("self", true);
                    }
                });
            }
        }

        message.add("dataList", dataList);
        message.add("dimensions", fetchDimensionList(id, dataType));
        return message;

    }

    private List<Map<String, Object>> fetchDimensionList(Long id, Integer dataType){
        List<Map<String, Object>> resultList = new ArrayList<>();
        if (Objects.equals(dataType, AgentConstants.INDICATOR_TYPE_GROUP)) {
            Map<String, Object> defaultItem = new HashMap<>();
            defaultItem.put("code", 1);
            defaultItem.put("desc", "默认");
            resultList.add(defaultItem);

            List<AgentGroup> groupList = new ArrayList<>();
            groupList.add(baseOrgService.getGroupById(id));
            groupList.addAll(baseOrgService.getSubGroupList(id));
            Set<AgentGroupRoleType> groupRoleTypes = groupList.stream().map(AgentGroup::fetchGroupRoleType)
                    .filter(p -> p == AgentGroupRoleType.Region || p == AgentGroupRoleType.Area || p == AgentGroupRoleType.City)
                    .collect(Collectors.toSet());

            if(groupRoleTypes.contains(AgentGroupRoleType.Region)){  // 业务部或大区
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("code", 2);
                itemMap.put("desc", "大区");
                resultList.add(itemMap);
            }
            if(groupRoleTypes.contains(AgentGroupRoleType.Area)){
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("code", 3);
                itemMap.put("desc", "区域");
                resultList.add(itemMap);
            }
            if(groupRoleTypes.contains(AgentGroupRoleType.City)){
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("code", 4);
                itemMap.put("desc", "分区");
                resultList.add(itemMap);
            }

            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("code", 5);
            itemMap.put("desc", "专员");
            resultList.add(itemMap);
        }else if(Objects.equals(dataType, AgentConstants.INDICATOR_TYPE_USER)){
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("code", 5);
            itemMap.put("desc", "专员");
            resultList.add(itemMap);
        }

        return resultList;
    }


    @RequestMapping(value = "school_list.vpage")
    @ResponseBody
    public MapMessage fetchSchoolList(){
        Long id = getRequestLong("id");
        Integer dataType = getRequestInt("dataType", AgentConstants.INDICATOR_TYPE_USER);
        int schoolLevelFlag = getRequestInt("schoolLevel", 1);        // 学校阶段   1:小学   24：初高中
        int mode = getRequestInt("mode", AgentConstants.MODE_ONLINE);       // 模式： 1:17作业   2：快乐学

        int indicator = getRequestInt("indicator", 1);  //  01: 新增注册， 02：新增1套， 03：新增3套，04：回流1套 05：回流3套， 06：周测1套， 07：周测2套
        int subject = getRequestInt("subject", 2);      //  00：全部  01：单科  02：英语  03：数学  04：语文
        int monthOrDay = getRequestInt("monthOrDay", 1);   // 01：本月  02：昨日

        if(id <= 0L ||  (!Objects.equals(dataType, AgentConstants.INDICATOR_TYPE_USER) && !Objects.equals(dataType, AgentConstants.INDICATOR_TYPE_UNALLOCATED))){
            return MapMessage.errorMessage("参数错误");
        }

        Integer day = performanceService.lastSuccessDataDay();
        List<Map<String, Object>> schoolDataList = indicatorService.generateSchoolIndicatorViewList(id, dataType, day, mode, indicator, subject, monthOrDay, schoolLevelFlag);
        MapMessage message = MapMessage.successMessage();
        message.put("dataList", schoolDataList);
        return message;
    }


    /**
     * 柱状图
     * @return
     */
    @RequestMapping(value = "histogram.vpage")
    @ResponseBody
    public MapMessage histogram(){
        Map<String, Object> parameterMap = getTargetParameterMap();
        Long id = (Long)parameterMap.getOrDefault("id", 0);
        Integer idType = (Integer)parameterMap.getOrDefault("dataType", AgentConstants.INDICATOR_TYPE_USER);
        int schoolLevel = (Integer)parameterMap.getOrDefault("schoolLevel", 1); // 学校阶段   1:小学   24：初高中
        int mode = (Integer)parameterMap.getOrDefault("mode", AgentConstants.MODE_ONLINE);       // 模式： 1:online   2：offline

        if(id <= 0 || (!Objects.equals(idType, AgentConstants.INDICATOR_TYPE_GROUP) && !Objects.equals(idType, AgentConstants.INDICATOR_TYPE_USER)) || (mode != AgentConstants.MODE_ONLINE && mode != AgentConstants.MODE_OFFLINE)){
            return MapMessage.errorMessage("参数错误！");
        }

        MapMessage message = MapMessage.successMessage();
        message.add("dataMap",indicatorService.histogram(id,idType,schoolLevel));
        return message;
    }
}
