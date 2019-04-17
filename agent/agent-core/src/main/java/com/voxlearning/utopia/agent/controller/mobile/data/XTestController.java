/**
 * Author:   xianlong.zhang
 * Date:     2018/9/21 15:03
 * Description: x测相关接口
 * History:
 */
package com.voxlearning.utopia.agent.controller.mobile.data;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.constants.AgentConstants;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.service.exam.XTestService;
import com.voxlearning.utopia.agent.service.indicator.IndicatorService;
import com.voxlearning.utopia.agent.service.mobile.PerformanceService;
import com.voxlearning.utopia.agent.support.AgentSchoolLevelSupport;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentServiceType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUserSchool;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/mobile/xtest")
public class XTestController extends AbstractAgentController {

    @Inject
    private PerformanceService performanceService;

    @Inject
    private XTestService xTestService;

    @Inject
    private IndicatorService indicatorService;

    @Inject
    private AgentSchoolLevelSupport agentSchoolLevelSupport;

    // 数据概览
    @RequestMapping(value = "getXTestOrgSumData.vpage")
    @ResponseBody
    public MapMessage getXTestOrgSumData(){
        Long id = getRequestLong("id");
        Set<Long> ids = requestLongSet("ids");
        Integer idType = getRequestInt("idType");
        Integer testType = getRequestInt("testType");
        Integer sumType = getRequestInt("sumType");
        Integer date = getRequestInt("data",20181008);
        return xTestService.loadXOrgSumData(new ArrayList<>(ids),idType,Arrays.asList(1),testType,sumType,date);
    }
    // 数据概览
    @RequestMapping(value = "getXTestOrgSumDataDev.vpage")
    @ResponseBody
    public MapMessage getXTestOrgSumDataDev(){


        return xTestService.loadXOrgSumDataDev();
    }
    // 学校明细
    @RequestMapping(value = "getXTestXSchoolDetailData.vpage")
    @ResponseBody
    public MapMessage getXTestXSchoolDetailData(){
        Long id = getRequestLong("id");
        Integer testType = getRequestInt("testType");
        Integer sumType = getRequestInt("sumType");
        Integer date = getRequestInt("data",20181007);
        return xTestService.loadXSchoolDetailData(Arrays.asList(id),testType,sumType,date);
    }

    // 班级明细
    @RequestMapping(value = "getXClassDetailData.vpage")
    @ResponseBody
    public MapMessage getXClassDetailData(){
        return xTestService.loadXClassDetailData();
    }

    // 年级明细
    @RequestMapping(value = "getXGradeDetailData.vpage")
    @ResponseBody
    public MapMessage getXGradeDetailData(){
        return xTestService.loadXGradeDetailData();
    }
    // 老师明细
    @RequestMapping(value = "loadXTeacherDetailData.vpage")
    @ResponseBody
    public MapMessage getXTeacherDetailData(){
        return xTestService.loadXTeacherDetailData();
    }
    // 班组明细
    @RequestMapping(value = "getXGroupDetailData.vpage")
    @ResponseBody
    public MapMessage getXGroupDetailData(){
        return xTestService.loadXGroupDetailData();
    }


    @RequestMapping(value = "xtest_survey.vpage")
    @ResponseBody
    public MapMessage getXTestSurvey(){
        Map<String, Object> parameterMap = getTargetParameterMap();
        Integer dataType = getRequestInt("dataType",1);//1:日维度，2:月维度
        Long id = (Long)parameterMap.getOrDefault("id", 0);
        Integer idType = (Integer)parameterMap.getOrDefault("idType", AgentConstants.INDICATOR_TYPE_USER);
        int schoolLevelFlag = (Integer)parameterMap.getOrDefault("schoolLevel", 1); // 学校阶段   1:小学   2：初高中
        if(id <= 0 || (!Objects.equals(idType, AgentConstants.INDICATOR_TYPE_GROUP) && !Objects.equals(idType, AgentConstants.INDICATOR_TYPE_USER))){
            return MapMessage.errorMessage("参数错误！");
        }

        MapMessage mapMessage =  xTestService.getXTestFirstPageData(idType,id,schoolLevelFlag , dataType);
        if(getCurrentUser().isCountryManager() || getCurrentUser().isAdmin()){
            List<AgentGroup> groupList = baseOrgService.getAgentGroupByRole(AgentGroupRoleType.Marketing);
            if(CollectionUtils.isNotEmpty(groupList)){
                AgentGroup group = groupList.stream().filter(p -> p != null && p.fetchServiceTypeList().contains(AgentServiceType.JUNIOR_SCHOOL)).findFirst().orElse(null);
                if(group != null){
                    mapMessage.put("juniorMarketingId", group.getId());
                }
            }
        }
        return mapMessage;
    }

    private Map<String, Object> getTargetParameterMap(){
        Map<String, Object> resultMap = new HashMap<>();
        Long id = getRequestLong("id");
        if(id > 0){
            Integer idType = getRequestInt("idType", AgentConstants.INDICATOR_TYPE_USER);
            int schoolLevel = getRequestInt("schoolLevel", 1); // 学校阶段   1:小学 2 中学
            resultMap.put("id", id);
            resultMap.put("idType", idType);
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
                        resultMap.put("idType", AgentConstants.INDICATOR_TYPE_GROUP);
                        resultMap.put("schoolLevel", 1);
                    }else {
                        AgentGroup group2 = groupList.stream().filter(p -> p != null && (p.fetchServiceTypeList().contains(AgentServiceType.MIDDLE_SCHOOL) || p.fetchServiceTypeList().contains(AgentServiceType.SENIOR_SCHOOL))).findFirst().orElse(null);
                        if(group2 != null){
                            resultMap.put("id", group2.getId());
                            resultMap.put("idType", AgentConstants.INDICATOR_TYPE_GROUP);
                            resultMap.put("schoolLevel", 2);
                        }
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
                        resultMap.put("schoolLevel", group.fetchServiceTypeList().contains(AgentServiceType.JUNIOR_SCHOOL) ? 1 : 2);
                    }
                }else {
                    resultMap.put("id", userId);
                    resultMap.put("idType", AgentConstants.INDICATOR_TYPE_USER);
                    List<SchoolLevel> schoolLevelList = baseOrgService.getUserServiceSchoolLevels(userId);
                    resultMap.put("schoolLevel", schoolLevelList.contains(SchoolLevel.JUNIOR) ? 1 : 2);
                }
            }
        }
        return resultMap;
    }

    @RequestMapping(value = "data_list.vpage")
    @ResponseBody
    public MapMessage dataList(){
        Long id = getRequestLong("id");
        Integer idType = getRequestInt("idType", AgentConstants.INDICATOR_TYPE_USER);
        int schoolLevelFlag = getRequestInt("schoolLevel", 1); // 学校阶段   1:小学   2中学  目前x测只有小学
        int dimension = getRequestInt("dimension", 1);  // 1:默认   2：大区   3：区域   4：分区   5：专员
        int indicator = getRequestInt("indicator", 1);  //  01 统考概况 02 统考学生 03 活动概况 04 活动学生
        int subject = getRequestInt("subject", 0);      //  01：英语  02：数学  03：语文
        int dataType = getRequestInt("dataType", 1);   // 01：本月  02：昨日

        MapMessage message = MapMessage.successMessage();

        Integer day = performanceService.lastSuccessDataDay();
        List<Map<String, Object>> dataList = new ArrayList<>();
        if (Objects.equals(idType, AgentConstants.INDICATOR_TYPE_GROUP)) {
            AgentGroupRoleType groupRoleType = baseOrgService.getGroupRole(id);
            message.add("groupRoleType", groupRoleType);
            boolean flag = indicatorService.judgeGroupDimension(groupRoleType, dimension);
            if(!flag){
                return MapMessage.errorMessage("参数组合有误！");
            }

            if(dimension == 5 || (groupRoleType == AgentGroupRoleType.City && dimension == 1)){ // 专员列表
                dataList.addAll(xTestService.generateGroupUserIndicatorViewList(id, day, schoolLevelFlag, indicator, subject, dataType));
            }else {  // 部门列表
                dataList.addAll(xTestService.generateGroupIndicatorViewList(id, groupRoleType, day, schoolLevelFlag, dimension, indicator, subject, dataType));
            }

            dataList.forEach(p -> {
                p.put("clickable", true);           // 默认可点击下钻
                p.put("self", false);               // 是否是当前部门或用户
                if (Objects.equals(p.get("id"), id) && Objects.equals(p.get("dataType"), String.valueOf(idType))) { // 当前用户或部门的情况下， 设置背景色
                    p.put("self", true);
                }
                // 大区看大区， 区域看区域， 分区看分区的情况下，前端页面不能点击下钻
                if((groupRoleType == AgentGroupRoleType.Region && dimension == 2) || (groupRoleType == AgentGroupRoleType.Area && dimension == 3) || (groupRoleType == AgentGroupRoleType.City && dimension == 4)) {
                    p.put("clickable", false);           // 不可点击下钻
                }
            });

        }else if(Objects.equals(idType, AgentConstants.INDICATOR_TYPE_USER)){   // 当前用户是专员的情况下， 返回专员所在部门的专员列表
            List<Long> groupIdList = baseOrgService.getGroupIdListByUserId(id);
            if(CollectionUtils.isNotEmpty(groupIdList)){
                List<Long> userList = baseOrgService.getGroupUsersByRole(groupIdList.get(0), AgentRoleType.BusinessDeveloper);
                dataList.addAll(xTestService.generateUserIndicatorViewList(userList, day, schoolLevelFlag, indicator, subject, dataType));

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
        message.add("dimensions", fetchDimensionList(id, idType));
        return message;
    }

    private List<Map<String, Object>> fetchDimensionList(Long id, Integer idType){
        List<Map<String, Object>> resultList = new ArrayList<>();
        if (Objects.equals(idType, AgentConstants.INDICATOR_TYPE_GROUP)) {
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
        }else if(Objects.equals(idType, AgentConstants.INDICATOR_TYPE_USER)){
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
//        int schoolLevelFlag = getRequestInt("schoolLevel", 1);        // 学校阶段   1:小学   2：初高中

        int indicator = getRequestInt("indicator", 1);  //  01 统考学生 02 统考新学生 03 活动学生 04 活动新学生
        int subject = getRequestInt("subject", 2);      //  01：全部  02：英语  03：数学  04：语文
        int monthOrDay = getRequestInt("monthOrDay", 1);   // 01：本月  02：昨日

        if(id <= 0L ||  (!Objects.equals(dataType, AgentConstants.INDICATOR_TYPE_USER) && !Objects.equals(dataType, AgentConstants.INDICATOR_TYPE_UNALLOCATED))){
            return MapMessage.errorMessage("参数错误");
        }

        List<Long> schoolIds = new ArrayList<>();
        if (Objects.equals(dataType, AgentConstants.INDICATOR_TYPE_USER)) {
            List<Integer> schoolLevels = agentSchoolLevelSupport.fetchTargetSchoolLevelIds(id, AgentConstants.INDICATOR_TYPE_USER, 1);
            if(CollectionUtils.isNotEmpty(schoolLevels)){
                List<AgentUserSchool> userSchools = baseOrgService.getUserSchoolByUser(id);
                List<Long> tmpSchoolIds = userSchools.stream().map(AgentUserSchool::getSchoolId).collect(Collectors.toList());
                schoolIds = baseOrgService.getSchoolListByLevels(tmpSchoolIds, schoolLevels.stream().map(SchoolLevel::safeParse).collect(Collectors.toList()));
            }

        } else if (Objects.equals(dataType, AgentConstants.INDICATOR_TYPE_UNALLOCATED)) {
            List<Integer> schoolLevels = agentSchoolLevelSupport.fetchTargetSchoolLevelIds(id, AgentConstants.INDICATOR_TYPE_GROUP, 1);
            if(CollectionUtils.isNotEmpty(schoolLevels)){
                List<Long> tmpSchoolIds = baseOrgService.getCityManageOtherSchoolByGroupId(id).stream().collect(Collectors.toList());
                schoolIds = baseOrgService.getSchoolListByLevels(tmpSchoolIds, schoolLevels.stream().map(SchoolLevel::safeParse).collect(Collectors.toList()));
            }

        }


        Integer day = performanceService.lastSuccessDataDay();
        List<Map<String, Object>> schoolDataList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(schoolIds)){
            schoolDataList = xTestService.generateSchoolIndicatorViewList(schoolIds, day, indicator, subject, monthOrDay);
        }
        MapMessage message = MapMessage.successMessage();
        message.put("dataList", schoolDataList);
        return message;
    }
}
