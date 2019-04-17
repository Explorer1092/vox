package com.voxlearning.utopia.agent.controller.mobile.data;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.constants.AgentConstants;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.service.data.AgentDataParentService;
import com.voxlearning.utopia.agent.service.indicator.IndicatorService;
import com.voxlearning.utopia.agent.service.mobile.PerformanceService;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentServiceType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 首页-数据-家长
 */
@Controller
@RequestMapping("/mobile/data/parent")
public class DataParentController extends AbstractAgentController {

    @Inject
    private AgentDataParentService agentDataParentService;
    @Inject
    private PerformanceService performanceService;
    @Inject
    private IndicatorService indicatorService;

    /**
     * 柱状图
     * @return
     */
    @RequestMapping(value = "histogram.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage histogram(){
        Map<String, Object> parameterMap = agentDataParentService.getTargetParameterMap(getCurrentUserId(),1);
        Long id = (Long)parameterMap.getOrDefault("id", 0);
        Integer idType = (Integer)parameterMap.getOrDefault("idType", AgentConstants.INDICATOR_TYPE_USER);
        int schoolLevelFlag = (Integer)parameterMap.getOrDefault("schoolLevelFlag", 1); // 学校阶段   1:小学   24：初高中
        if(id <= 0 || (!Objects.equals(idType, AgentConstants.INDICATOR_TYPE_GROUP) && !Objects.equals(idType, AgentConstants.INDICATOR_TYPE_USER))){
            return MapMessage.errorMessage("参数错误！");
        }
        if (schoolLevelFlag != 1){
            return MapMessage.errorMessage("包含“小学”业务类型的部门人员有权限!");
        }
        return MapMessage.successMessage().add("dataMap",agentDataParentService.histogram(id,idType,schoolLevelFlag));
    }

    /**
     * 指标概览
     * @return
     */
    @RequestMapping(value = "indicator_overview.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage indicatorOverview(){
        Map<String, Object> parameterMap = agentDataParentService.getTargetParameterMap(getCurrentUserId(),1);
        Long id = (Long)parameterMap.getOrDefault("id", 0);
        Integer idType = (Integer)parameterMap.getOrDefault("idType", AgentConstants.INDICATOR_TYPE_USER);
        int schoolLevelFlag = (Integer)parameterMap.getOrDefault("schoolLevelFlag", 1); // 学校阶段   1:小学   24：初高中
        if(id <= 0 || (!Objects.equals(idType, AgentConstants.INDICATOR_TYPE_GROUP) && !Objects.equals(idType, AgentConstants.INDICATOR_TYPE_USER))){
            return MapMessage.errorMessage("参数错误！");
        }
        if (schoolLevelFlag != 1){
            return MapMessage.errorMessage("包含“小学”业务类型的部门人员有权限!");
        }
        MapMessage mapMessage = MapMessage.successMessage();
        Integer day = performanceService.lastSuccessDataDay();
        mapMessage.put("day", DateUtils.dateToString(performanceService.lastSuccessDataDate(), "yyyy-MM-dd"));
        mapMessage.put("indicatorOverview",agentDataParentService.indicatorOverview(id,idType,schoolLevelFlag,day));

        AuthCurrentUser currentUser = getCurrentUser();
        if(currentUser.isCountryManager() || currentUser.isAdmin()){
            List<AgentGroup> groupList = baseOrgService.getAgentGroupByRole(AgentGroupRoleType.Marketing);
            if(CollectionUtils.isNotEmpty(groupList)){
                AgentGroup juniorGroup = groupList.stream().filter(p -> p != null && p.fetchServiceTypeList().contains(AgentServiceType.JUNIOR_SCHOOL)).findFirst().orElse(null);
                if(juniorGroup != null){
                    mapMessage.put("juniorMarketingId", juniorGroup.getId());
                }
                // 中学业务部的数据
                AgentGroup middleGroup = groupList.stream().filter(p -> p != null && (p.fetchServiceTypeList().contains(AgentServiceType.MIDDLE_SCHOOL) || p.fetchServiceTypeList().contains(AgentServiceType.SENIOR_SCHOOL))).findFirst().orElse(null);
                if(middleGroup != null){
                    mapMessage.put("middleMarketingId", middleGroup.getId());
                }
            }
        }
        return mapMessage;
    }

    /**
     * 家长指标列表
     * @return
     */
    @RequestMapping(value = "data_list.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage dataList(){
        Long id = getRequestLong("id");
        Integer idType = getRequestInt("idType", AgentConstants.INDICATOR_TYPE_USER);
        int schoolLevelFlag = getRequestInt("schoolLevel",1);
        int dimension = getRequestInt("dimension", 1);  // 1:默认   2：大区   3：区域   4：分区   5：专员
        int indicator = getRequestInt("indicator", 1);  //  01：绑定  02：活跃
        int monthOrDay = getRequestInt("monthOrDay", 1);   // 01：本月  02：昨日

        Integer day = performanceService.lastSuccessDataDay();
        List<Map<String, Object>> dataList = new ArrayList<>();

        MapMessage message = MapMessage.successMessage();

        if (Objects.equals(idType, AgentConstants.INDICATOR_TYPE_GROUP)) {
            AgentGroupRoleType groupRoleType = baseOrgService.getGroupRole(id);
            message.add("groupRoleType", groupRoleType);
            boolean flag = indicatorService.judgeGroupDimension(groupRoleType, dimension);
            if(!flag){
                return MapMessage.errorMessage("参数组合有误！");
            }
            if(dimension == 5 || (groupRoleType == AgentGroupRoleType.City && dimension == 1)){ // 专员列表
                dataList.addAll(agentDataParentService.generateGroupUserIndicatorViewList(id, groupRoleType, day, schoolLevelFlag, dimension, indicator, monthOrDay));
            }else {  // 部门列表
                dataList.addAll(agentDataParentService.generateGroupIndicatorViewList(id, groupRoleType, day, schoolLevelFlag, dimension, indicator, monthOrDay));
            }
            dataList.forEach(p -> {
                p.put("clickable", true);           // 默认可点击下钻
                p.put("self", false);               // 是否是当前部门或用户
                if (Objects.equals(p.get("id"), id) && Objects.equals(p.get("idType"), String.valueOf(idType))) { // 当前用户或部门的情况下， 设置背景色
                    p.put("self", true);
                }
                // 大区看大区， 区域看区域， 分区看分区的情况下，前端页面不能点击下钻
                if((groupRoleType == AgentGroupRoleType.Region && dimension == 2) || (groupRoleType == AgentGroupRoleType.Area && dimension == 3) || (groupRoleType == AgentGroupRoleType.City && dimension == 4)) {
                    p.put("clickable", false);           // 不可点击下钻
                }
            });
        }else if(Objects.equals(idType, AgentConstants.INDICATOR_TYPE_USER)) {
            List<Long> groupIdList = baseOrgService.getGroupIdListByUserId(id);
            if(CollectionUtils.isNotEmpty(groupIdList)){
                List<Long> userList = baseOrgService.getGroupUsersByRole(groupIdList.get(0), AgentRoleType.BusinessDeveloper);
                dataList.addAll(agentDataParentService.generateUserIndicatorViewList(userList, day, schoolLevelFlag, indicator, monthOrDay));
                dataList.forEach(p -> {
                    p.put("clickable", false);           // 不可点击下钻
                    p.put("self", false);               // 是否是当前部门或用户
                    if(Objects.equals(p.get("id"), id) && Objects.equals(String.valueOf(p.get("idType")), String.valueOf(idType))){ // 当前用户或部门的情况下， 设置背景色
                        p.put("clickable", true);
                        p.put("self", true);
                    }
                });
            }
        }
        message.add("dataList", dataList);
        message.add("dimensions", baseOrgService.fetchDimensionList(id, idType));
        return message;
    }

    @RequestMapping(value = "school_list.vpage")
    @ResponseBody
    public MapMessage schoolList(){
        Long id = getRequestLong("id");
        Integer idType = getRequestInt("idType", AgentConstants.INDICATOR_TYPE_USER);
        int schoolLevelFlag = getRequestInt("schoolLevel", 1);        // 学校阶段   1:小学

        int indicator = getRequestInt("indicator", 1);  //  01: 新绑定家长， 02：新绑定学生， 03：活跃1次家长，04：活跃3次家长 05：学生家长双活
        int monthOrDay = getRequestInt("monthOrDay", 1);   // 01：本月  02：昨日

        if(id <= 0L ||  (!Objects.equals(idType, AgentConstants.INDICATOR_TYPE_USER) && !Objects.equals(idType, AgentConstants.INDICATOR_TYPE_UNALLOCATED))){
            return MapMessage.errorMessage("参数错误");
        }

        Integer day = performanceService.lastSuccessDataDay();
        List<Map<String, Object>> schoolDataList = agentDataParentService.generateSchoolIndicatorViewList(id, idType, day, indicator, monthOrDay, schoolLevelFlag);
        MapMessage message = MapMessage.successMessage();
        message.put("dataList", schoolDataList);
        return message;
    }
}
