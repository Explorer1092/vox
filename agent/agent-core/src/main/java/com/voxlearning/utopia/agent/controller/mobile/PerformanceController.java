package com.voxlearning.utopia.agent.controller.mobile;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.DayUtils;
import com.voxlearning.utopia.agent.annotation.OperationCode;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.bean.ClazzAlterStatistics;
import com.voxlearning.utopia.agent.bean.VisitSchoolResultDetailData;
import com.voxlearning.utopia.agent.bean.hierarchicalstructure.NodeStructure;
import com.voxlearning.utopia.agent.constants.AgentErrorCode;
import com.voxlearning.utopia.agent.constants.AgentUsedProductType;
import com.voxlearning.utopia.agent.constants.AppContentStateType;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.mapper.ClazzAlterMapper;
import com.voxlearning.utopia.agent.persist.entity.AgentAppContentPacket;
import com.voxlearning.utopia.agent.persist.entity.AgentNeedFollowUp;
import com.voxlearning.utopia.agent.persist.entity.AgentRegionMessage;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.mobile.AgentNeedFollowUpService;
import com.voxlearning.utopia.agent.service.mobile.AgentPerformanceRankingService;
import com.voxlearning.utopia.agent.service.mobile.AgentRegionMessageService;
import com.voxlearning.utopia.agent.service.mobile.ClazzAlterService;
import com.voxlearning.utopia.agent.service.mobile.resource.AgentResourceService;
import com.voxlearning.utopia.agent.service.mobile.v2.WorkRecordService;
import com.voxlearning.utopia.agent.service.taskmanage.AgentTaskManageService;
import com.voxlearning.utopia.agent.service.workspace.AgentAppContentPacketService;
import com.voxlearning.utopia.agent.utils.NodeStructureUtil;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.user.api.constants.ClazzTeacherAlterationState;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

/**
 * @author Jia HuanYin
 * @since 2016/2/18
 */
@Controller
@RequestMapping("/mobile/performance")
public class PerformanceController extends AbstractAgentController {

//    @Inject
//    PerformanceService performanceService;
    @Inject
    AgentPerformanceRankingService agentPerformanceRankingService;
    @Inject
    BaseOrgService baseOrgService;
    @Inject
    WorkRecordService workRecordService;
    @Inject
    AgentRegionMessageService agentRegionMessageService;
    @Inject
    AgentNeedFollowUpService agentNeedFollowUpService;
    @Inject private AgentAppContentPacketService agentAppContentPacketService;
    @Inject private AgentResourceService agentResourceService;
    @Inject private ClazzAlterService clazzAlterService;
//    @Inject private Base17PerformanceService base17PerformanceService;
//    @Inject private BaseKlxPerformanceService baseKlxPerformanceService;
    @Inject
    private AgentTaskManageService agentTaskManageService;

//    // 用户业绩首页
//    @RequestMapping(value = "index.vpage")
//    String index(Model model) {
//        AuthCurrentUser user = getCurrentUser();
//        Date day = performanceService.lastSuccessDataDate();
//        model.addAttribute("date", day);
////        完成率
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(day);
//        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
//        int totalDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
//        model.addAttribute("finishRate", (currentDay * 100) / totalDay);
//
//        int count = 0;
//        model.addAttribute("notifyCount", count);
//
//        if (user.isCityManager() || user.isBusinessDeveloper() || user.isCountryManager() || user.isRegionManager() || user.isBuManager() || user.isAreaManager()) {
//            List<Long> managedSchools = baseOrgService.getManagedSchoolList(getCurrentUserId());
//            model.addAttribute("teacherCount", agentResourceService.countPendingClazzAlterationBySchool(managedSchools, 10,0));
//        }
//        if (user.isCityManager() || user.isBusinessDeveloper()){
//            List<AgentGroupUser> groupUserList = baseOrgService.getGroupUserByUser(user.getUserId());
//            if (CollectionUtils.isNotEmpty(groupUserList)){
//                AgentGroup parentGroupByRole = baseOrgService.getParentGroupByRole(groupUserList.get(0).getGroupId(), AgentGroupRoleType.BusinessUnit);
//                if (null != parentGroupByRole && parentGroupByRole.getGroupName().contains("中学")){
//                    model.addAttribute("canGotoLargeExamManage",true);
//                }
//            }
//        }
//        //当前用户未完成任务数量
//        model.addAttribute("unFinishedTaskNum",agentTaskCenterService.unFinishedTaskNum(user.getUserId()));
//        //断是否有权限显示维护老师
//        model.addAttribute("showMaintainTeacher",agentTaskCenterService.showMaintainTeacher(user.getUserId()));
//        return "rebuildViewDir/mobile/home/index";
//    }

    /**
     * 小红点显示
     * @return
     */
    @RequestMapping(value = "little_red_dot_show.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage littleRedDotShow() {
        Map<String,Object> dataMap = new HashMap<>();
        AuthCurrentUser user = getCurrentUser();
        if (user.isCityManager() || user.isBusinessDeveloper() || user.isCountryManager() || user.isRegionManager() || user.isBuManager() || user.isAreaManager()) {
            List<Long> managedSchools = baseOrgService.getManagedSchoolList(getCurrentUserId());
            dataMap.put("teacherCount",agentResourceService.countPendingClazzAlterationBySchool(managedSchools, 10,0));
        }
        dataMap.put("unFinishedTaskNum",agentTaskManageService.unFinishedTaskNum(user.getUserId()));
        return MapMessage.successMessage().add("dataMap",dataMap);
    }

//    @RequestMapping(value = "performanceProgress.vpage", method = RequestMethod.GET)
//    @ResponseBody
//    @OperationCode("714a6c8263fa46b2")
//    public MapMessage performanceProgress() {
//        AuthCurrentUser user = getCurrentUser();
//        Long userId = user.getUserId();
//        if(user.isProductOperator() && user.getShadowId() != null){ // 如果是产品运营角色， 则使用影子账号，以全国总监的身份查看数据
//            userId = user.getShadowId();
//        }
//        Integer day = performanceService.lastSuccessDataDay();
//
//        Agent17PerformanceSumDataWithBudget junior17Data = null;
//        Agent17PerformanceSumDataWithBudget middle17Data = null;
//        AgentKlxPerformanceSumDataWithBudget middleKlxData = null;
//        List<Long> groupIds = baseOrgService.getManagedGroupIdListByUserId(userId);
//        if(CollectionUtils.isEmpty(groupIds)){  // 专员的情况
//            // 获取用户所在的部门
//            Long groupId = 0L;
//            List<Long> userGroupIds = baseOrgService.getGroupIdListByUserId(userId);
//            if(CollectionUtils.isNotEmpty(userGroupIds)){
//                groupId = userGroupIds.get(0);
//            }
//
//            // 获取用户所在的业务部
//            AgentGroup businessUnit = baseOrgService.getParentGroupByRole(groupId, AgentGroupRoleType.BusinessUnit);
//            if(businessUnit != null){
//                if(StringUtils.contains(businessUnit.getGroupName(), "小学")){
//                    junior17Data = base17PerformanceService.loadUserSumDataWithBudget(userId, day, 1);
//                }else {
//                    middle17Data = base17PerformanceService.loadUserSumDataWithBudget(userId, day, 24);
//                    middleKlxData = baseKlxPerformanceService.loadUserSumDataWithBudget(userId, day, 24);
//                }
//            }
//        }else {
//            Long groupId = groupIds.get(0);
//            AgentGroupRoleType groupRoleType = baseOrgService.getGroupRole(groupId);
//            if(groupRoleType == AgentGroupRoleType.Country){ // 全国的情况
//                List<AgentGroup> groupList = baseOrgService.getGroupListByParentId(groupId);
//                // 小学业务部的数据
//                AgentGroup juniorBusinessUnit = groupList.stream().filter(p -> p.fetchGroupRoleType() == AgentGroupRoleType.BusinessUnit && StringUtils.contains(p.getGroupName(), "小学")).findFirst().orElse(null);
//                if(juniorBusinessUnit != null){
//                    junior17Data = base17PerformanceService.loadGroupSumDataWithBudget(juniorBusinessUnit.getId(), day, 1);
//                }
//
//                // 中学业务部的数据
//                AgentGroup middleBusinessUnit = groupList.stream().filter(p -> p.fetchGroupRoleType() == AgentGroupRoleType.BusinessUnit && StringUtils.contains(p.getGroupName(), "中学")).findFirst().orElse(null);
//                if(middleBusinessUnit != null){
//                    middle17Data = base17PerformanceService.loadGroupSumDataWithBudget(middleBusinessUnit.getId(), day, 24);
//                    middleKlxData = baseKlxPerformanceService.loadGroupSumDataWithBudget(middleBusinessUnit.getId(), day, 24);
//                }
//            }else {
//                // 部门数据
//                AgentGroup businessUnit = baseOrgService.getParentGroupByRole(groupId, AgentGroupRoleType.BusinessUnit);
//                if(businessUnit != null){
//                    if(StringUtils.contains(businessUnit.getGroupName(), "小学")){
//                        junior17Data = base17PerformanceService.loadGroupSumDataWithBudget(groupId, day, 1);
//                    }else {
//                        middle17Data = base17PerformanceService.loadGroupSumDataWithBudget(groupId, day, 24);
//                        middleKlxData = baseKlxPerformanceService.loadGroupSumDataWithBudget(groupId, day, 24);
//                    }
//                }
//            }
//        }
//
//        MapMessage message = MapMessage.successMessage();
//
//        if(junior17Data != null){
//            message.add("junior17Data", junior17Data.generate17ViewData(Agent17PerformanceSumDataWithBudget.VIEW_TYPE_KEY_JUNIOR).generateDataMap());
//        }
//
//        if(middle17Data != null){
//            message.add("middle17Data", middle17Data.generate17ViewData(Agent17PerformanceSumDataWithBudget.VIEW_TYPE_KEY_MIDDLE).generateDataMap());
//        }
//
//        if(middleKlxData != null){
//            message.add("middleKlxData", middleKlxData.generateKlxViewData(AgentKlxPerformanceSumDataWithBudget.VIEW_TYPE_KEY_MIDDLE).generateDataMap());
//        }
//
//        return message;
//    }
//
//    // 业绩概览
//    @RequestMapping(value = "performance_overview.vpage")
//    @ResponseBody
//    public MapMessage performanceOverview(){
//        Long id = getRequestLong("id");
//        String idType = requestString("idType");
//        int schoolLevel = getRequestInt("schoolLevel", 1); // 学校阶段   1:小学   24：初高中
//        int mode = getRequestInt("mode", 1);       // 模式： 1:17作业   2：快乐学
//
//
//        if(id <= 0 || StringUtils.isBlank(idType) || (!Objects.equals(idType, AgentPerformanceConstants.ID_TYPE_GROUP) && !Objects.equals(idType, AgentPerformanceConstants.ID_TYPE_USER)) || (mode != 1 && mode != 2)){
//            return MapMessage.errorMessage("参数错误！");
//        }
//
//        MapMessage message = MapMessage.successMessage();
//
//        if(getCurrentUser().isCountryManager()){
//            List<Long> groupIds = baseOrgService.getManagedGroupIdListByUserId(getCurrentUserId());
//            if(CollectionUtils.isNotEmpty(groupIds)){
//                List<AgentGroup> groupList = baseOrgService.getGroupListByParentId(groupIds.get(0));
//                AgentGroup juniorBusinessUnit = groupList.stream().filter(p -> p.fetchGroupRoleType() == AgentGroupRoleType.BusinessUnit && StringUtils.contains(p.getGroupName(), "小学")).findFirst().orElse(null);
//                if(juniorBusinessUnit != null){
//                    message.put("juniorBusinessUnitId", juniorBusinessUnit.getId());
//                }
//                // 中学业务部的数据
//                AgentGroup middleBusinessUnit = groupList.stream().filter(p -> p.fetchGroupRoleType() == AgentGroupRoleType.BusinessUnit && StringUtils.contains(p.getGroupName(), "中学")).findFirst().orElse(null);
//                if(middleBusinessUnit != null){
//                    message.put("middleBusinessUnitId", middleBusinessUnit.getId());
//                }
//            }
//        }
//
//        Integer day = performanceService.lastSuccessDataDay();
//        Map<String, Object> performanceOverview = new HashMap<>();
//        if(mode == 1){   // 17作业模式
//            Agent17PerformanceSumDataWithBudget agent17PerformanceSumDataWithBudget = null;
//            if(Objects.equals(idType, AgentPerformanceConstants.ID_TYPE_GROUP)){
//                agent17PerformanceSumDataWithBudget = base17PerformanceService.loadGroupSumDataWithBudget(id, day, schoolLevel);
//            }else if(Objects.equals(idType, AgentPerformanceConstants.ID_TYPE_USER)){
//                agent17PerformanceSumDataWithBudget = base17PerformanceService.loadUserSumDataWithBudget(id, day, schoolLevel);
//            }
//            if(agent17PerformanceSumDataWithBudget != null){
//                performanceOverview.putAll(agent17PerformanceSumDataWithBudget.generate17ViewData(schoolLevel * 1000000).generateDataMap());
//            }
//        }else{
//            AgentKlxPerformanceSumDataWithBudget agentKlxPerformanceSumDataWithBudget = null;
//            if(Objects.equals(idType, AgentPerformanceConstants.ID_TYPE_GROUP)){
//                agentKlxPerformanceSumDataWithBudget = baseKlxPerformanceService.loadGroupSumDataWithBudget(id, day, schoolLevel);
//            }else if(Objects.equals(idType, AgentPerformanceConstants.ID_TYPE_USER)){
//                agentKlxPerformanceSumDataWithBudget = baseKlxPerformanceService.loadUserSumDataWithBudget(id, day, schoolLevel);
//            }
//            if(agentKlxPerformanceSumDataWithBudget != null){
//                performanceOverview.putAll(agentKlxPerformanceSumDataWithBudget.generateKlxViewData(schoolLevel * 1000000).generateDataMap());
//            }
//        }
//
//        message.add("performanceOverview", performanceOverview);
//        return message;
//    }
//
//    // 业绩列表
//    @RequestMapping(value = "performance_list_page.vpage")
//    String performanceListPage(Model model){
//        Long id = getRequestLong("id");
//        String idType = requestString("idType");
//        int schoolLevel = getRequestInt("schoolLevel", 1); // 学校阶段   1:小学   24：初高中
//        int mode = getRequestInt("mode", 1);       // 模式： 1:17作业   2：快乐学
//
//        if(Objects.equals(idType, PerformanceData.ID_TYPE_GROUP)){
//            AgentGroupRoleType groupRoleType = baseOrgService.getGroupRole(id);
//            model.addAttribute("groupRole", groupRoleType);
//        }
//
//        model.addAttribute("id", id);
//        model.addAttribute("idType", idType);
//        model.addAttribute("schoolLevel", schoolLevel); // 学校阶段
//        model.addAttribute("mode", mode);  // 模式
//        if(mode == 1 && schoolLevel == 1){ // // FIXME: 2018/3/7 子奇
//            return "rebuildViewDir/mobile/home/information/informationJunior";
//        }else if(mode == 1 && schoolLevel == 24){
//            return "rebuildViewDir/mobile/home/information/informationOnline";
//        }
//        return "rebuildViewDir/mobile/home/information/informationScanning";
//    }
//
//    @RequestMapping(value = "performance_data_list.vpage")
//    @ResponseBody
//    public MapMessage performanceDataList(){
//        Long id = getRequestLong("id");
//        String idType = requestString("idType");
//        int schoolLevel = getRequestInt("schoolLevel", 1); // 学校阶段   1:小学   24：初高中
//        int mode = getRequestInt("mode", 1);       // 模式： 1:online模式   2：offline模式
//
//        int dimension = getRequestInt("dimension", 1);  // 1:默认   2：大区   3：区域   4：分区   5：专员
//        int indicator = getRequestInt("indicator", 1);  //  online模式： 01：概况  02：月活  03：新增  04：回流  05：留存  06：口测     offline模式：01：扫描  02：大考
//        int subject = getRequestInt("subject", 0);      //  00：全部  01：单科  02：英语  03：数学  04：语文
//        int monthOrDay = getRequestInt("monthOrDay", 1);   // 01：本月  02：昨日
//
//
//
//        MapMessage message = MapMessage.successMessage();
//
//        Integer day = performanceService.lastSuccessDataDay();
//        List<Map<String, Object>> dataList = new ArrayList<>();
//        if (Objects.equals(idType, AgentPerformanceConstants.ID_TYPE_GROUP)) {
//            AgentGroupRoleType groupRoleType = baseOrgService.getGroupRole(id);
//            message.add("groupRoleType", groupRoleType);
//            boolean flag = performanceService.judgeGroupDimension(groupRoleType, dimension);
//            if(!flag){
//                return MapMessage.errorMessage("参数组合有误！");
//            }
//
//            if(dimension == 5 || (groupRoleType == AgentGroupRoleType.City && dimension == 1)){ // 专员列表
//                dataList.addAll(performanceService.generateGroupUserPerformanceViewList(id, groupRoleType, day, schoolLevel, mode, dimension, indicator, subject, monthOrDay));
//            }else {  // 部门列表
//                dataList.addAll(performanceService.generateGroupPerformanceViewList(id, groupRoleType, day, schoolLevel, mode, dimension, indicator, subject, monthOrDay));
//            }
//
//
//            dataList.forEach(p -> {
//                p.put("clickable", true);           // 默认可点击下钻
//                p.put("self", false);               // 是否是当前部门或用户
//                if (Objects.equals(p.get("id"), id) && Objects.equals(p.get("idType"), idType)) { // 当前用户或部门的情况下， 设置背景色
//                    p.put("self", true);
//                }
//                // 大区看大区， 区域看区域， 分区看分区的情况下，前端页面不能点击下钻
//                if((groupRoleType == AgentGroupRoleType.Region && dimension == 2) || (groupRoleType == AgentGroupRoleType.Area && dimension == 3) || (groupRoleType == AgentGroupRoleType.City && dimension == 4)) {
//                    p.put("clickable", false);           // 不可点击下钻
//                }
//            });
//
//        }else if(Objects.equals(idType, AgentPerformanceConstants.ID_TYPE_USER)){   // 当前用户是专员的情况下， 返回专员所在部门的专员列表
//            List<Long> groupIdList = baseOrgService.getGroupIdListByUserId(id);
//            if(CollectionUtils.isNotEmpty(groupIdList)){
//                List<Long> userList = baseOrgService.getGroupUsersByRole(groupIdList.get(0), AgentRoleType.BusinessDeveloper);
//                dataList.addAll(performanceService.generateUserPerformanceViewList(userList, day, schoolLevel, mode, indicator, subject, monthOrDay));
//
//                dataList.forEach(p -> {
//                    p.put("clickable", false);           // 不可点击下钻
//                    p.put("self", false);               // 是否是当前部门或用户
//                    if(Objects.equals(p.get("id"), id) && Objects.equals(p.get("idType"), idType)){ // 当前用户或部门的情况下， 设置背景色
//                        p.put("clickable", true);
//                        p.put("self", true);
//                    }
//                });
//            }
//        }
//
//        message.add("dataList", dataList);
//        message.add("dimensions", fetchDimensionList(id, idType));
//        return message;
//    }
//
//    private List<Map<String, Object>> fetchDimensionList(Long id, String idType){
//        List<Map<String, Object>> resultList = new ArrayList<>();
//        if (Objects.equals(idType, AgentPerformanceConstants.ID_TYPE_GROUP)) {
//            Map<String, Object> defaultItem = new HashMap<>();
//            defaultItem.put("code", 1);
//            defaultItem.put("desc", "默认");
//            resultList.add(defaultItem);
//
//            List<AgentGroup> groupList = new ArrayList<>();
//            groupList.add(baseOrgService.getGroupById(id));
//            groupList.addAll(baseOrgService.getSubGroupList(id));
//            Set<AgentGroupRoleType> groupRoleTypes = groupList.stream().map(AgentGroup::fetchGroupRoleType)
//                    .filter(p -> p == AgentGroupRoleType.Region || p == AgentGroupRoleType.Area || p == AgentGroupRoleType.City)
//                    .collect(Collectors.toSet());
//
//            if(groupRoleTypes.contains(AgentGroupRoleType.Region)){  // 业务部或大区
//                Map<String, Object> itemMap = new HashMap<>();
//                itemMap.put("code", 2);
//                itemMap.put("desc", "大区");
//                resultList.add(itemMap);
//            }
//            if(groupRoleTypes.contains(AgentGroupRoleType.Area)){
//                Map<String, Object> itemMap = new HashMap<>();
//                itemMap.put("code", 3);
//                itemMap.put("desc", "区域");
//                resultList.add(itemMap);
//            }
//            if(groupRoleTypes.contains(AgentGroupRoleType.City)){
//                Map<String, Object> itemMap = new HashMap<>();
//                itemMap.put("code", 4);
//                itemMap.put("desc", "分区");
//                resultList.add(itemMap);
//            }
//
//            Map<String, Object> itemMap = new HashMap<>();
//            itemMap.put("code", 5);
//            itemMap.put("desc", "专员");
//            resultList.add(itemMap);
//        }else if(Objects.equals(idType, AgentPerformanceConstants.ID_TYPE_USER)){
//            Map<String, Object> itemMap = new HashMap<>();
//            itemMap.put("code", 5);
//            itemMap.put("desc", "专员");
//            resultList.add(itemMap);
//        }
//
//        return resultList;
//    }
//
//
//
//    @RequestMapping(value = "performance_list_data.vpage")
//    @ResponseBody
//    public MapMessage performanceListData(){
//        Long id = getRequestLong("id");
//        String idType = requestString("idType");
//        int level = getRequestInt("level", 1); // 查看指标类型   1:小学   2：初高中线上  3：初高中扫描
//        Integer role = requestInteger("role");    // 1：大区   2：分区  3：城市  4：专员
//        int maucOrRate = getRequestInt("maucOrRate", 1);   //  1：完成   2：月环比
//        int permeability = getRequestInt("permeability", 1);   // 1：全部学校   2：低渗   3：中渗   4：高渗   5：超高渗
//
//        Integer day = performanceService.lastSuccessDataDay();
//        boolean isUser = false;
//        List<PerformanceData> performanceDataList = new ArrayList<>();
//        if(Objects.equals(idType, PerformanceData.ID_TYPE_GROUP)){
//            AgentGroupRoleType groupRoleType = baseOrgService.getGroupRole(id);
//            if(role == null){
//                if(groupRoleType == AgentGroupRoleType.Country){
//                    role = 1;
//                }else if(groupRoleType == AgentGroupRoleType.Region){
//                    role = 2;
//                }else{
//                    role = 4;
//                }
//            }
//
//            if(role == 1 || role == 2){ // 看大区 或 看分区
//                List<Long> groupList = new ArrayList<>();
//                if((groupRoleType == AgentGroupRoleType.Region && role == 1) || (groupRoleType == AgentGroupRoleType.City && role == 2)){
//                    AgentGroup parentGroup = baseOrgService.getParentGroup(id);
//                    if(parentGroup != null){
//                        groupList = baseOrgService.getGroupListByParentId(parentGroup.getId()).stream().map(AgentGroup::getId).collect(Collectors.toList());
//                    }
//                }else {
//                    List<AgentGroup> subGroupList = baseOrgService.getSubGroupList(id);
//                    if(role == 1){
//                        groupList = subGroupList.stream().filter(p -> p.fetchGroupRoleType() == AgentGroupRoleType.Region).map(AgentGroup::getId).collect(Collectors.toList());
//                    }else {
//                        groupList = subGroupList.stream().filter(p -> p.fetchGroupRoleType() == AgentGroupRoleType.City).map(AgentGroup::getId).collect(Collectors.toList());
//                    }
//                }
//
//                Map<Long, PerformanceData> performanceDataMap = basePerformanceService.loadGroupData(groupList, day);
//                if(MapUtils.isNotEmpty(performanceDataMap)){
//                    performanceDataMap.values().forEach(performanceDataList::add);
//                }
//            }else if(role == 3){ // 看城市
//                List<PerformanceData> cityPerformanceList = performanceService.generateGroupCityPerformanceDataList(id, groupRoleType, day);
//                if(CollectionUtils.isNotEmpty(cityPerformanceList)){
//                    performanceDataList.addAll(cityPerformanceList);
//                }
//            }else if(role == 4){ // 看专员
//                List<PerformanceData> userPerformanceList = performanceService.generateGroupUserPerformanceDataList(id, groupRoleType, AgentRoleType.BusinessDeveloper, day);
//                if(CollectionUtils.isNotEmpty(userPerformanceList)){
//                    performanceDataList.addAll(userPerformanceList);
//                    isUser = true;
//                }
//            }
//        }else if(Objects.equals(idType, PerformanceData.ID_TYPE_USER)){
//            List<Long> groupIdList = baseOrgService.getGroupIdListByUserId(id);
//            if(CollectionUtils.isNotEmpty(groupIdList)){
//                Set<Long> userIds = baseOrgService.getGroupUserByGroups(groupIdList).stream().filter(p -> p.getUserRoleType() == AgentRoleType.BusinessDeveloper).map(AgentGroupUser::getUserId).collect(Collectors.toSet());
//                Map<Long, PerformanceData> userPerformanceMap = basePerformanceService.loadUserData(userIds, day);
//                performanceDataList.addAll(userPerformanceMap.values());
//                isUser = true;
//            }
//        }
//
//        Map<String, List<Map<String, Object>>> dataMap = performanceService.generatePerformanceViewMap(performanceDataList, level, maucOrRate, permeability);
//
//        // 如果是专员列表，设置专员所在的部门信息
//        if(isUser){
//            setGroupDataForUser(dataMap);
//        }
//
//        MapMessage message = MapMessage.successMessage();
//        message.add("dataMap", dataMap);
//        return message;
//    }
//
//    // 设置专员所属的部门信息
//    private void setGroupDataForUser(Map<String, List<Map<String, Object>>> dataMap){
//        if(MapUtils.isEmpty(dataMap)){
//            return;
//        }
//        Map<Long, AgentGroup> userGroupMap = new HashMap<>();
//        for(List<Map<String, Object>> userDataList : dataMap.values()){
//            if(CollectionUtils.isEmpty(userDataList)){
//                continue;
//            }
//            for(Map<String, Object> userData : userDataList){
//                String idType = (String)userData.get("idType");
//                if(Objects.equals(idType, PerformanceData.ID_TYPE_USER)){
//                    Long userId = (Long)userData.get("id");
//                    AgentGroup group = userGroupMap.get(userId);
//                    if(group == null){
//                        group = baseOrgService.getUserGroups(userId).stream().findFirst().orElse(null);
//                        userGroupMap.put(userId, group);
//                    }
//                    userData.put("groupId", group.getId());
//                    userData.put("groupName", group.getGroupName());
//                }
//            }
//        }
//    }
//
//
//    // 业绩详情页
//    @RequestMapping(value = "performance_detail.vpage")
//    String performanceDetail(Model model){
//        Long id = getRequestLong("id");
//        String idType = requestString("idType");
//        int level = getRequestInt("level", 1); // 查看指标类型   1:小学   2：初高中线上  3：初高中扫描
//        if(id <= 0 || StringUtils.isBlank(idType) || (!Objects.equals(idType, PerformanceData.ID_TYPE_GROUP) && !Objects.equals(idType, PerformanceData.ID_TYPE_USER) && !Objects.equals(idType, PerformanceData.ID_TYPE_GROUP_OTHER))){
//            model.addAttribute("performanceOverview", new HashMap<String, Object>());
//
//            model.addAttribute("id", id);
//            model.addAttribute("idType", idType);
//            model.addAttribute("level", level); // 当前业绩类型
//
//            return "rebuildViewDir/mobile/home/information/groupInformationInfo";
//        }
//
//
//        Integer day = performanceService.lastSuccessDataDay();
//        PerformanceData performanceData = new PerformanceData();
//        if(Objects.equals(idType, PerformanceData.ID_TYPE_GROUP)){
//            performanceData = basePerformanceService.loadGroupData(id, day);
//        }else if (Objects.equals(idType, PerformanceData.ID_TYPE_USER)) {
//            performanceData = basePerformanceService.loadUserData(id, day);
//        } else if (Objects.equals(idType, PerformanceData.ID_TYPE_GROUP_OTHER)) {
//            performanceData = basePerformanceService.loadGroupOtherData(id, day);
//        }
//
//        // 业绩概览
//        Map<String, Object> performanceOverview = performanceData.generateViewData(level * 10000).generateDateMap();
//        model.addAttribute("id", id);
//        model.addAttribute("idType", idType);
//        model.addAttribute("level", level); // 当前业绩类型
//        model.addAttribute("performanceOverview", performanceOverview);
//
//        return "rebuildViewDir/mobile/home/information/groupInformationInfo";
//
//    }
//
//    // 部门业绩下钻
//    @RequestMapping(value = "group_performance.vpage")
//    String groupPerformance(Model model) {
//        Long groupId = getRequestLong("id");
//        int viewType = getRequestInt("viewType", PerformanceData.VIEW_TYPE_OVERVIEW_JUNIOR);
//        String idType = requestString("idType");
//        int tabIndex = getRequestInt("tabIndex", 1);//中小学切换和页面下钻的兼容处理 概览:1 大区:2 分区:3 专员:4 学校:5  下钻+1
//        int schoolLevel = getRequestInt("schoolLevel", 1); // 查看小学还是初高中
//
//        if(groupId <= 0L || StringUtils.isBlank(idType) || !Objects.equals(idType, PerformanceData.ID_TYPE_GROUP)){
//            model.addAttribute("performanceOverview", new HashMap<String, Object>());
//            model.addAttribute("groupPerformanceViewMap", new HashMap<String, Map<String, List<Map<String, Object>>>>());
//            model.addAttribute("userPerformanceViewMap", new HashMap<String, List<Map<String, Object>>>());
//            model.addAttribute("groupRoleType", null);  // 当前部门的角色
//
//            model.addAttribute("id", groupId);
//            model.addAttribute("idType", idType);
//            model.addAttribute("viewType", viewType); // 当前业绩类型
//            model.addAttribute("tabIndex", tabIndex);
//            model.addAttribute("schoolLevel", schoolLevel);
//            return "rebuildViewDir/mobile/home/information/groupInformance";
//        }
//
//        int mode = 1;
//        if(schoolLevel == 1){ // 17作业模式
//            mode = 1;
//        }else if(schoolLevel == 2 || schoolLevel == 4){ // 快乐学模式
//            mode = 2;
//        }
//
//        Integer day = performanceService.lastSuccessDataDay();
//        PerformanceData groupPerformance = basePerformanceService.loadGroupData(groupId, day);
//        // 业绩概览
//        Map<String, Object> performanceOverview = groupPerformance.generateViewData(mode == 1 ? PerformanceData.VIEW_TYPE_OVERVIEW_JUNIOR : PerformanceData.VIEW_TYPE_OVERVIEW_MIDDLE).generateDateMap();
//        model.addAttribute("performanceOverview", performanceOverview);
//
//        AgentGroupRoleType groupRoleType = baseOrgService.getGroupRole(groupId);
//
//        // 设置部门业绩数据（按大区查看，按分区查看）
//        // freemarker 处理的map看key 需是String类型
//        Map<String, Map<String, List<Map<String, Object>>>> groupPerformanceViewMap = performanceService.generateGroupPerformanceViewMap(groupId, groupRoleType, day, mode);
//
//
//        // 生成专员列表
//        Map<String, List<Map<String, Object>>> userPerformanceViewMap = performanceService.generateGroupUserPerformanceViewMap(groupId, groupRoleType, AgentRoleType.BusinessDeveloper, day, mode);
//
//        // 生成部门在各个城市的业绩
//        Map<String, List<Map<String, Object>>> groupCityPerformanceViewMap = performanceService.generateGroupCityPerformanceViewMap(groupId, groupRoleType, day, mode);
//
//        model.addAttribute("groupPerformanceViewMap", groupPerformanceViewMap);
//        model.addAttribute("userPerformanceViewMap", userPerformanceViewMap);
//        model.addAttribute("groupCityPerformanceViewMap", groupCityPerformanceViewMap);
//        model.addAttribute("groupRoleType", groupRoleType);  // 当前部门的角色
//
//        model.addAttribute("id", groupId);
//        model.addAttribute("idType", idType);
//        model.addAttribute("viewType", viewType); // 当前业绩类型
//        model.addAttribute("tabIndex", tabIndex);
//        model.addAttribute("schoolLevel", schoolLevel);
//
//        if(mode == 1 ){
//            return "rebuildViewDir/mobile/home/information/groupInformance";
//        }
//        return "rebuildViewDir/mobile/home/information/groupInformance_klx";
//    }
//
//    @RequestMapping(value = "school_performance.vpage", method = RequestMethod.GET)
//    public String userSchoolPerformance(Model model) {
//        Long id = getRequestLong("id");
//        String idType = requestString("idType");
//        int schoolLevel = getRequestInt("schoolLevel", 1);        // 学校阶段   1:小学   24：初高中
//        int mode = getRequestInt("mode", 1);       // 模式： 1:17作业   2：快乐学
//
//        if(id <= 0L || StringUtils.isBlank(idType) || (!Objects.equals(idType, AgentPerformanceConstants.ID_TYPE_USER) && !Objects.equals(idType, AgentPerformanceConstants.ID_TYPE_GROUP_OTHER))){
//            model.addAttribute("id", id);
//            model.addAttribute("idType", idType);
//            model.addAttribute("schoolLevel", schoolLevel);
//            model.addAttribute("mode", mode);
//            return "rebuildViewDir/mobile/home/information/schoolInformation";
//        }
//
//        model.addAttribute("id", id);
//        model.addAttribute("idType", idType);
//        model.addAttribute("schoolLevel", schoolLevel);
//        model.addAttribute("mode", mode);
//        if(mode == 1){
//            return "rebuildViewDir/mobile/home/information/schoolInformation";
//        }
//        return "rebuildViewDir/mobile/home/information/schoolInformation_klx";
//    }
//
//    @RequestMapping(value = "fetch_school_list.vpage")
//    @ResponseBody
//    public MapMessage fetchSchoolList(){
//        Long id = getRequestLong("id");
//        String idType = requestString("idType");
//        int schoolLevel = getRequestInt("schoolLevel", 1);        // 学校阶段   1:小学   24：初高中
//        int mode = getRequestInt("mode", 1);       // 模式： 1:17作业   2：快乐学
//
//        if(id <= 0L || StringUtils.isBlank(idType) || (!Objects.equals(idType, AgentPerformanceConstants.ID_TYPE_USER) && !Objects.equals(idType, AgentPerformanceConstants.ID_TYPE_GROUP_OTHER))){
//            return MapMessage.errorMessage("参数错误");
//        }
//
//        Set<Long> schoolIds = new HashSet<>();
//        if (Objects.equals(idType, AgentPerformanceConstants.ID_TYPE_USER)) {
//            List<AgentUserSchool> userSchools = baseOrgService.getUserSchoolByUser(id);
//            schoolIds = userSchools.stream().map(AgentUserSchool::getSchoolId).collect(Collectors.toSet());
//        } else if (Objects.equals(idType, AgentPerformanceConstants.ID_TYPE_GROUP_OTHER)) {
//            schoolIds = baseOrgService.getCityManageOtherSchoolByGroupId(id).stream().collect(Collectors.toSet());
//        }
//
//
//        Integer day = performanceService.lastSuccessDataDay();
//        List<Map<String, Object>> schoolDataList = performanceService.generateSchoolPerformanceViewMap(schoolIds, day, schoolLevel, mode);
//        MapMessage message = MapMessage.successMessage();
//        message.put("schoolDataList", schoolDataList);
//        return message;
//    }
//
//
//    // 部门业绩下钻
//    @RequestMapping(value = "group_overview.vpage")
//    String groupOverview(Model model) {
//        Long groupId = getRequestLong("id");
//        String idType = requestString("idType");
//        String viewType = getRequestParameter("viewType", OverviewData.VIEW_TYPE_JUNIOR);
//        int maucOrDf = getRequestInt("maucOrDf", 1); // 查看月活还是日浮
//
//        if(groupId <= 0L || StringUtils.isBlank(idType) || !Objects.equals(idType, PerformanceData.ID_TYPE_GROUP)){
//            model.addAttribute("groupOverview", new ArrayList<OverviewViewData>());
//            model.addAttribute("groupOverviewViewMap", new HashMap<String, Map<String, List<OverviewViewData>>>());
//            model.addAttribute("userOverviewViewMap", new HashMap<String, List<OverviewViewData>>());
//            model.addAttribute("groupRoleType", null);  // 当前部门的角色
//
//            model.addAttribute("id", groupId);
//            model.addAttribute("idType", idType);
//            model.addAttribute("viewType", viewType); // 当前业绩类型
//            model.addAttribute("maucOrDf", maucOrDf);
//            return "rebuildViewDir/mobile/home/group_overview";
//        }
//
//
//        Integer day = performanceService.lastSuccessDataDay();
//        // 获取当前部门概况
//        OverviewData overviewData = baseOverviewService.loadGroupData(groupId, day);
//        List<OverviewViewData> viewDataList = overviewData.generateViewDateList().stream().collect(Collectors.toList());
//        model.addAttribute("groupOverview", viewDataList);
//
//        AgentGroupRoleType groupRoleType = baseOrgService.getGroupRole(groupId);
//
//        // 设置部门业绩数据（按大区查看，按城市查看）
//        // key   JUNIOR: 小学  MIDDLE:中学
//        Map<String, Map<String, List<OverviewViewData>>> groupOverviewViewMap = performanceService.generateGroupOverviewViewMap(groupId, groupRoleType, day);
//
//        // 生成专员列表
//        // key   JUNIOR: 小学  MIDDLE:中学
//        Map<String, List<OverviewViewData>> userOverviewViewMap = performanceService.generateGroupUserOverviewViewMap(groupId, groupRoleType, AgentRoleType.BusinessDeveloper, day);
//
//        model.addAttribute("groupOverviewViewMap", groupOverviewViewMap);
//        model.addAttribute("userOverviewViewMap", userOverviewViewMap);
//        model.addAttribute("groupRoleType", groupRoleType);  // 当前部门的角色
//
//        model.addAttribute("id", groupId);
//        model.addAttribute("idType", idType);
//        model.addAttribute("viewType", viewType); // 当前业绩类型
//        model.addAttribute("maucOrDf", maucOrDf);
//        return "rebuildViewDir/mobile/home/information/groupInformance";
//    }
//
//    @RequestMapping(value = "school_overview.vpage", method = RequestMethod.GET)
//    public String userSchoolOverview(Model model) {
//        Long id = getRequestLong("id");
//        String viewType = getRequestParameter("viewType", OverviewData.VIEW_TYPE_JUNIOR);
//        String idType = requestString("idType");
//        int maucOrDf = getRequestInt("maucOrDf", 1); // 查看月活还是日浮
//
//        if(id <= 0L || StringUtils.isBlank(idType) || (!Objects.equals(idType, OverviewData.ID_TYPE_USER) && !Objects.equals(idType, "OTHER_SCHOOL"))){
//
//            model.addAttribute("overviewData", new ArrayList<OverviewViewData>());
//            model.addAttribute("schoolOverviewViewMap", new HashMap<String, Object>());
//
//            model.addAttribute("id", id);
//            model.addAttribute("idType", idType);
//            model.addAttribute("viewType", viewType); // 当前业绩类型
//            model.addAttribute("maucOrDf", maucOrDf);
//            return "rebuildViewDir/mobile/home/schoolOverView";
//        }
//
//        Set<Long> schoolIds = new HashSet<>();
//        if (Objects.equals(idType, OverviewData.ID_TYPE_USER)) {
//            List<AgentUserSchool> userSchools = baseOrgService.getUserSchoolByUser(id);
//            schoolIds = userSchools.stream().map(AgentUserSchool::getSchoolId).collect(Collectors.toSet());
//        } else if (Objects.equals(idType, "OTHER_SCHOOL")) {
//            schoolIds = performanceService.getCityManageOtherSchoolByGroupId(id).stream().collect(Collectors.toSet());
//        }
//
//
//        Integer day = performanceService.lastSuccessDataDay();
//        OverviewData overviewData = new OverviewData(day);
//        Map<Long, OverviewData> schoolOverviewMap = baseOverviewService.loadSchoolData(schoolIds, day);
//        schoolOverviewMap.values().stream().reduce(overviewData, OverviewData::appendData);
//        // 获取统计数据
//        List<OverviewViewData> data = overviewData.generateViewDateList().stream().collect(Collectors.toList());
//        model.addAttribute("overviewData", data);
//
//        // 设置学校列表数据
//        Map<String, Object> schoolOverviewViewMap = performanceService.generateSchoolOverviewViewMap(schoolIds, day);
//        model.addAttribute("schoolOverviewViewMap", schoolOverviewViewMap);
//
//        model.addAttribute("viewType", viewType); // 当前业绩类型
//        model.addAttribute("maucOrDf", maucOrDf);
//        model.addAttribute("id", id);
//        model.addAttribute("idType", idType);
//        return "rebuildViewDir/mobile/home/schoolOverView";
//    }
//
//    @RequestMapping(value = "group_performance_df_chart.vpage")
//    @ResponseBody
//    public MapMessage groupPerformanceDfChart() {
//        MapMessage msg = MapMessage.successMessage();
//        Long groupId = getRequestLong("id");
//        String idType = requestString("idType");
//        int viewType = getRequestInt("viewType", PerformanceData.VIEW_TYPE_JUNIOR_ENG);
//        if(groupId <= 0L || StringUtils.isBlank(idType) || !Objects.equals(idType, PerformanceData.ID_TYPE_GROUP)){
//            return MapMessage.errorMessage("参数有误");
//        }
//
//        int tmpMode = 1; // 17作业模式
//        if(viewType == 4 || viewType == 5){ // 快乐学模式
//            tmpMode = 2;
//        }
//        int mode = tmpMode;
//
//        Integer endDay = performanceService.lastSuccessDataDay();
//        Date endDate = DateUtils.stringToDate(String.valueOf(endDay), "yyyyMMdd");
//        Date startDate = DateUtils.calculateDateDay(endDate, -31);
//        Integer startDay = ConversionUtils.toInt(DateUtils.dateToString(startDate, "yyyyMMdd"));
//
//        List<Integer> dayList = DayUtils.getEveryDays(startDay, endDay);
//        List<PerformanceData> daysPerformanceList = dayList.stream().map(p -> basePerformanceService.loadGroupData(groupId, p)).collect(Collectors.toList());
//        Map<String, Map<String, List<Integer>>> daysPerformanceDfMap = performanceService.generateDaysPerformanceDfMap(daysPerformanceList, mode);
//        msg.put("daysPerformanceDfMap", daysPerformanceDfMap);
//
//        List<String> days = dayList.stream().map(p -> DateUtils.dateToString(DateUtils.stringToDate(String.valueOf(p), "yyyyMMdd"), "M-d")).collect(Collectors.toList());
//        msg.put("days", days);
//        return msg;
//    }
//
//    @RequestMapping(value = "school_performance_df_chart.vpage")
//    @ResponseBody
//    public MapMessage schoolPerformanceDfChart() {
//        MapMessage msg = MapMessage.successMessage();
//        Long id = getRequestLong("id");
//        String idType = requestString("idType");
//        int viewType = getRequestInt("viewType", PerformanceData.VIEW_TYPE_JUNIOR_ENG);
//        if(id <= 0L || StringUtils.isBlank(idType) || (!Objects.equals(idType, OverviewData.ID_TYPE_USER) && !Objects.equals(idType, "OTHER_SCHOOL"))){
//            return MapMessage.errorMessage("参数有误");
//        }
//
//        int tmpMode = 1; // 17作业模式
//        if(viewType == 4 || viewType == 5){ // 快乐学模式
//            tmpMode = 2;
//        }
//        int mode = tmpMode;
//
//        Set<Long> schoolIds = new HashSet<>();
//        if (Objects.equals(idType, OverviewData.ID_TYPE_USER)) {
//            List<AgentUserSchool> userSchools = baseOrgService.getUserSchoolByUser(id);
//            schoolIds = userSchools.stream().map(AgentUserSchool::getSchoolId).collect(Collectors.toSet());
//        } else if (Objects.equals(idType, "OTHER_SCHOOL")) {
//            schoolIds = baseOrgService.getCityManageOtherSchoolByGroupId(id).stream().collect(Collectors.toSet());
//        }
//
//        Integer endDay = performanceService.lastSuccessDataDay();
//        Date endDate = DateUtils.stringToDate(String.valueOf(endDay), "yyyyMMdd");
//        Date startDate = DateUtils.calculateDateDay(endDate, -31);
//        Integer startDay = ConversionUtils.toInt(DateUtils.dateToString(startDate, "yyyyMMdd"));
//
//        List<Integer> dayList = DayUtils.getEveryDays(startDay, endDay);
//
//        List<PerformanceData> daysPerformanceList = new ArrayList<>();
//        for(Integer day : dayList){
//            PerformanceData performanceData = new PerformanceData(day);
//            Map<Long, PerformanceData> schoolPerformanceMap = basePerformanceService.loadSchoolData(schoolIds, day);
//            schoolPerformanceMap.values().stream().reduce(performanceData, PerformanceData::appendData);
//            daysPerformanceList.add(performanceData);
//        }
//
//        Map<String, Map<String, List<Integer>>> daysPerformanceDfMap = performanceService.generateDaysPerformanceDfMap(daysPerformanceList, mode);
//        msg.put("daysPerformanceDfMap", daysPerformanceDfMap);
//        List<String> days = dayList.stream().map(p -> DateUtils.dateToString(DateUtils.stringToDate(String.valueOf(p), "yyyyMMdd"), "M-d")).collect(Collectors.toList());
//        msg.put("days", days);
//        return msg;
//    }
//
//
//    @RequestMapping(value = "group_overview_df_chart.vpage")
//    @ResponseBody
//    public MapMessage groupOverviewDfChart() {
//        MapMessage msg = MapMessage.successMessage();
//        Long groupId = getRequestLong("id");
//        String idType = requestString("idType");
//        if(groupId <= 0L || StringUtils.isBlank(idType) || !Objects.equals(idType, OverviewData.ID_TYPE_GROUP)){
//            return MapMessage.errorMessage("参数有误");
//        }
//
//        Integer endDay = performanceService.lastSuccessDataDay();
//        Date endDate = DateUtils.stringToDate(String.valueOf(endDay), "yyyyMMdd");
//        Date startDate = DateUtils.calculateDateDay(endDate, -31);
//        Integer startDay = ConversionUtils.toInt(DateUtils.dateToString(startDate, "yyyyMMdd"));
//
//        List<Integer> dayList = DayUtils.getEveryDays(startDay, endDay);
//        List<OverviewData> daysOverviewList = dayList.stream().map(p -> baseOverviewService.loadGroupData(groupId, p)).collect(Collectors.toList());
//        Map<String, Map<String, List<Integer>>> daysPerformanceDfMap = performanceService.generateDaysOverviewDfMap(daysOverviewList);
//        msg.put("daysOverviewDfMap", daysPerformanceDfMap);
//
//        List<String> days = dayList.stream().map(p -> DateUtils.dateToString(DateUtils.stringToDate(String.valueOf(p), "yyyyMMdd"), "M-d")).collect(Collectors.toList());
//        msg.put("days", days);
//        return msg;
//    }
//
//
//    @RequestMapping(value = "school_overview_df_chart.vpage")
//    @ResponseBody
//    public MapMessage schoolOverviewDfChart() {
//        MapMessage msg = MapMessage.successMessage();
//        Long id = getRequestLong("id");
//        String idType = requestString("idType");
//        if(id <= 0L || StringUtils.isBlank(idType) || (!Objects.equals(idType, OverviewData.ID_TYPE_USER) && !Objects.equals(idType, "OTHER_SCHOOL"))){
//            return MapMessage.errorMessage("参数有误");
//        }
//
//        Set<Long> schoolIds = new HashSet<>();
//        if (Objects.equals(idType, OverviewData.ID_TYPE_USER)) {
//            List<AgentUserSchool> userSchools = baseOrgService.getUserSchoolByUser(id);
//            schoolIds = userSchools.stream().map(AgentUserSchool::getSchoolId).collect(Collectors.toSet());
//        } else if (Objects.equals(idType, "OTHER_SCHOOL")) {
//            schoolIds = baseOrgService.getCityManageOtherSchoolByGroupId(id).stream().collect(Collectors.toSet());
//        }
//
//        Integer endDay = performanceService.lastSuccessDataDay();
//        Date endDate = DateUtils.stringToDate(String.valueOf(endDay), "yyyyMMdd");
//        Date startDate = DateUtils.calculateDateDay(endDate, -31);
//        Integer startDay = ConversionUtils.toInt(DateUtils.dateToString(startDate, "yyyyMMdd"));
//
//        List<Integer> dayList = DayUtils.getEveryDays(startDay, endDay);
//
//        List<OverviewData> daysOverviewList = new ArrayList<>();
//        for(Integer day : dayList){
//            OverviewData overviewData = new OverviewData(day);
//            Map<Long, OverviewData> schoolOverviewMap = baseOverviewService.loadSchoolData(schoolIds, day);
//            schoolOverviewMap.values().stream().reduce(overviewData, OverviewData::appendData);
//            daysOverviewList.add(overviewData);
//        }
//
//        Map<String, Map<String, List<Integer>>> daysOverviewDfMap = performanceService.generateDaysOverviewDfMap(daysOverviewList);
//        msg.put("daysOverviewDfMap", daysOverviewDfMap);
//
//        List<String> days = dayList.stream().map(p -> DateUtils.dateToString(DateUtils.stringToDate(String.valueOf(p), "yyyyMMdd"), "M-d")).collect(Collectors.toList());
//        msg.put("days", days);
//        return msg;
//    }


    // 进校效果页面
    @RequestMapping(value = "visit_school_result_page.vpage", method = RequestMethod.GET)
    public String visitSchoolResultPage(Model model) {
        Long businessDeveloperId = getRequestLong("bdId");
        AgentUser dbUser = null;
        if(businessDeveloperId != 0L){
            dbUser = baseUserService.getById(businessDeveloperId);
        }
        model.addAttribute("user", dbUser);

        Date currentTime = new Date();
        model.addAttribute("currentTime", DateUtils.dateToString(currentTime,"yyyy-MM-dd HH:mm:ss"));
        Date preMonthTime = new Date(DayUtils.getFirstDayOfMonth(currentTime).getTime() -1);
        model.addAttribute("preMonthTime", DateUtils.dateToString(preMonthTime,"yyyy-MM-dd HH:mm:ss"));
        return "rebuildViewDir/mobile/home/intoSchoolEffectNew";
    }

    // 进校效果数据
    @RequestMapping(value = "visit_school_result_data.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage visitSchoolResultData() {
        Long userId = getRequestLong("userId");
        if(userId == 0L){
            userId = getCurrentUserId();
        }
        Date endTime = requestDate("endTime", "yyyy-MM-dd HH:mm:ss");
        if(endTime == null){
            endTime = new Date();
        }

        Date startDate = DateUtils.stringToDate(DateUtils.dateToString(DayUtils.addDay(endTime, -7), "yyyyMMdd"), "yyyyMMdd");
        if(DayUtils.getMonth(startDate) != DayUtils.getMonth(endTime)){
            startDate = DayUtils.getFirstDayOfMonth(endTime);
        }
        List<VisitSchoolResultDetailData> visitResultDetailList = workRecordService.getVisitSchoolResultDetailData(userId, startDate, endTime);
        Map<Integer, List<VisitSchoolResultDetailData>> visitResultMap = visitResultDetailList.stream().collect(Collectors.groupingBy(VisitSchoolResultDetailData::getDay));
        List<Map<String, Object>> visitResultList = visitResultMap.entrySet().stream().sorted((o1, o2) -> o2.getKey() - o1.getKey()).map(this::convertVisitDataMapData).collect(Collectors.toList());
        MapMessage message = MapMessage.successMessage();
        message.put("visitResultMap", visitResultList);
        message.put("nextDate", DateUtils.dateToString(startDate, "yyyy-MM-dd HH:mm:ss"));
        if(startDate.getTime() == DayUtils.getFirstDayOfMonth(endTime).getTime()){
            message.put("hasMore", false);
        }else {
            message.put("hasMore", true);
        }
        return message;
    }

    private Map<String, Object> convertVisitDataMapData(Map.Entry<Integer, List<VisitSchoolResultDetailData>> dayVisitList){
        Map<String, Object> retMap = new HashMap<>();
        Integer day = dayVisitList.getKey();
        String dayStr = DateUtils.dateToString(DateUtils.stringToDate(String.valueOf(day), "yyyyMMdd"), "yyyy-MM-dd");
        retMap.put("day", dayStr);
        retMap.put("visitDetailList", dayVisitList.getValue());
        return retMap;
    }

    // 获取大区寄语
    @RequestMapping(value = "get_region_message.vpage", method = RequestMethod.GET)
    @ResponseBody
    @OperationCode("1ec445da91c14e11")
    public MapMessage getRegionMessage() {
        AuthCurrentUser user = getCurrentUser();
        AgentRegionMessage agentRegionMessage = agentRegionMessageService.findRegionMessageForUser(user.getUserId());
        MapMessage message = MapMessage.successMessage();
        message.put("regionMessage", agentRegionMessage);
        return message;
    }

    @RequestMapping(value = "regionmsg.vpage", method = RequestMethod.GET)
    public String regionmsg(Model model) {
        Long groupId = requestLong("groupId");
        model.addAttribute("groupId", groupId);
        return "rebuildViewDir/mobile/home/regionmsg";
    }

    // 设置大区寄语
    @RequestMapping(value = "save_region_message.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveRegionMessage() {
        Long groupId = requestLong("groupId");
        String message = requestString("message");
        agentRegionMessageService.saveRegionMessage(groupId, message);
        return MapMessage.successMessage();
    }

    // 首页线索（需要跟进的学校及老师）
    @RequestMapping(value = "needFollowUp.vpage", method = RequestMethod.GET)
    public String needFollowUp(Model model) {
        AuthCurrentUser user = getCurrentUser();
        Date endDate = new Date();
        Date startDate = DayUtils.addDay(endDate, -15);
        int startDay = ConversionUtils.toInt(DateUtils.dateToString(startDate, "yyyyMMdd"));
        int endDay = ConversionUtils.toInt(DateUtils.dateToString(endDate, "yyyyMMdd"));
        Map<Integer, List<AgentNeedFollowUp>> dayNeedFollowUpMap = agentNeedFollowUpService.getNeedFollowUpList(user.getUserId(), startDay, endDay);

        List<Map<String, Object>> dataList = new ArrayList<>();
        if (MapUtils.isNotEmpty(dayNeedFollowUpMap)) {

            Map<Integer, List<Map<String, Object>>> dayFollowUpListMap = new HashMap<>();
            dayNeedFollowUpMap.forEach((k, v) -> {
                Map<Integer, List<AgentNeedFollowUp>> typeFollowUpMap = v.stream().collect(Collectors.groupingBy(AgentNeedFollowUp::getType, Collectors.toList()));
                dayFollowUpListMap.put(k, convertMapToSortedList(typeFollowUpMap, "type", "schoolList", true));
            });
            dataList = convertMapToSortedList(dayFollowUpListMap, "day", "needFollowList", false);
        }

        model.addAttribute("dataList", dataList);

        return "rebuildViewDir/mobile/home/clue";
    }

    private List<Map<String, Object>> convertMapToSortedList(Map<Integer, ?> map, String keyName, String valueName, boolean isAsc) {
        if (MapUtils.isEmpty(map)) {
            return emptyList();
        }

        List<Integer> keyList = new ArrayList<>(map.keySet());
        if (isAsc) {
            Collections.sort(keyList, (o1, o2) -> (o1 - o2));
        } else {
            Collections.sort(keyList, (o1, o2) -> (o2 - o1));
        }
        List<Map<String, Object>> retList = new ArrayList<>();
        for (Integer key : keyList) {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put(keyName, key);
            itemMap.put(valueName, map.get(key));
            retList.add(itemMap);
        }
        return retList;
    }

    /**
     * 进校效果选人
     */
    @RequestMapping(value = "choose_agent.vpage", method = RequestMethod.GET)
    public String chooseAgent(Model model) {
        String breakUrl = getRequestString("breakUrl");
        model.addAttribute("selectedUser", requestLong("selectedUser"));
        model.addAttribute("needCityManage",requestInteger("needCityManage"));
        model.addAttribute("breakUrl",breakUrl);
        return "rebuildViewDir/mobile/home/chooseAgent";
    }


    //活动页列表
    @RequestMapping(value = "product_activity.vpage", method = RequestMethod.GET)
    public String productActivity(Model model) {
        AuthCurrentUser user = getCurrentUser();
        if (user == null) {
            return errorInfoPage(AgentErrorCode.AUTH_FAILED, "登录状态失效，请重新登录", model);
        }
        List<AgentAppContentPacket> result = agentAppContentPacketService.loadUserActivity(user.getUserId());
        result = result.stream().filter(p -> p.getState() == AppContentStateType.RELEASE && (p.getActivityEndDate().after(DateUtils.getTodayStart()) || Objects.equals(p.getActivityEndDate().getTime(), DateUtils.getTodayStart().getTime()))).collect(Collectors.toList());
        model.addAttribute("activity", createActivityList(result));
        return "rebuildViewDir/mobile/home/productActivity";
    }

    public List<Map<String, Object>> createActivityList(List<AgentAppContentPacket> activity) {
        List<Map<String, Object>> result = new ArrayList<>();
        activity.forEach(p -> {
            Map<String, Object> info = new HashMap<>();
            info.put("id", p.getId());
            info.put("activityName", p.getActivityName());
            info.put("startDate", p.getActivityStartDate());
            info.put("endDate", p.getActivityEndDate());
            List<AgentUsedProductType> agentUsedEntrance = p.getActivityEntrance();
            info.put("activityEntrance", agentUsedEntrance == null ? "" : StringUtils.join(agentUsedEntrance.stream().map(AgentUsedProductType::getEntranceName).collect(Collectors.toList()), ","));
            //info.put("timeOut", !p.getActivityEndDate().after(DateUtils.getTodayStart()) && !Objects.equals(p.getActivityEndDate().getTime(), DateUtils.getTodayStart().getTime()));
            info.put("fileUrl", "/mobile/notice/noticeReader.vpage?contentId=" + p.getId());
            result.add(info);
        });
        return result;
    }

    @RequestMapping(value = "choose_business_developer.vpage")
    @ResponseBody
    public MapMessage chooseBusinessDeveloper() {
        Long userId = getCurrentUserId();
        Long allreadySelectedUser = requestLong("selectedUser");
        Integer needCityManage = getRequestInt("needCityManage");
        AgentRoleType userRole = baseOrgService.getUserRole(userId);


        List<Long> groupIdList = new ArrayList<>();
        if (AgentRoleType.Country == userRole || AgentRoleType.Region == userRole || AgentRoleType.AreaManager == userRole) {
            List<Long> parentGroupIdList = baseOrgService.getManagedGroupIdListByUserId(userId);
            Map<Long, List<AgentGroup>> subGroupMap = baseOrgService.getGroupListByParentIds(parentGroupIdList);
            if (MapUtils.isNotEmpty(subGroupMap)) {
                subGroupMap.values().forEach(p -> p.forEach(k -> groupIdList.add(k.getId())));
            }
        } else if (AgentRoleType.CityManager == userRole) {
            List<Long> cityGroupIdList = baseOrgService.getManagedGroupIdListByUserId(userId);
            if (CollectionUtils.isNotEmpty(cityGroupIdList)) {
                groupIdList.addAll(cityGroupIdList);
            }
        }
        List<NodeStructure> nodeStructureList = new ArrayList<>();
        List<AgentRoleType> needRole = new ArrayList<>();
        needRole.add(AgentRoleType.BusinessDeveloper);
        if (needCityManage == 1) {
            needRole.add(AgentRoleType.CityManager);
        }
        groupIdList.stream().map(p -> this.generateGroupAndMemberTree(p, 0L, AgentGroupRoleType.City, needRole)).forEach(k -> {
            if (CollectionUtils.isNotEmpty(k)) {
                nodeStructureList.addAll(k);
            }
        });
        if (allreadySelectedUser != null && allreadySelectedUser != 0) {
            Set<Long> selectedGroupIdList = new HashSet<>();
            List<Long> regionGroupIdList = baseOrgService.getGroupListByRole(allreadySelectedUser, AgentGroupRoleType.Region);
            if (CollectionUtils.isNotEmpty(regionGroupIdList)) {
                selectedGroupIdList.addAll(regionGroupIdList);
            }
            List<Long> cityGroupIdList = baseOrgService.getGroupListByRole(allreadySelectedUser, AgentGroupRoleType.City);
            if (CollectionUtils.isNotEmpty(cityGroupIdList)) {
                selectedGroupIdList.addAll(cityGroupIdList);
            }

            if (CollectionUtils.isNotEmpty(selectedGroupIdList)) {
                nodeStructureList.stream().forEach(p -> {
                    if (Objects.equals(p.getType(), "dp") && selectedGroupIdList.contains(Long.valueOf(p.getId()))) {
                        p.setIsSelected(true);
                    }
                });
                nodeStructureList.stream().forEach(p -> {
                    if (Objects.equals(p.getType(), "bd") && Objects.equals(Long.valueOf(p.getId()), allreadySelectedUser)) {
                        p.setIsSelected(true);
                    }
                });
            }

        }
        return NodeStructureUtil.formatNode(nodeStructureList, "dp");
    }

    private List<NodeStructure> generateGroupAndMemberTree(Long groupId, Long parentId, AgentGroupRoleType groupRoleType, List<AgentRoleType> needRole) {
        AgentGroup agentGroup = baseOrgService.getGroupById(groupId);
        if (AgentGroupRoleType.City != groupRoleType && AgentGroupRoleType.Region != groupRoleType && AgentGroupRoleType.Country != groupRoleType) {
            return emptyList();
        }
        if (AgentGroupRoleType.City != agentGroup.fetchGroupRoleType() && AgentGroupRoleType.Region != agentGroup.fetchGroupRoleType() && AgentGroupRoleType.Country != agentGroup.fetchGroupRoleType()) {
            return emptyList();
        }

        if (AgentGroupRoleType.Region == groupRoleType && AgentGroupRoleType.City == agentGroup.fetchGroupRoleType()) {
            return emptyList();
        }
        if (AgentGroupRoleType.Country == groupRoleType && (AgentGroupRoleType.Region == agentGroup.fetchGroupRoleType() || AgentGroupRoleType.City == agentGroup.fetchGroupRoleType())) {
            return emptyList();
        }

        List<NodeStructure> nodeStructureList = new ArrayList<>();
        NodeStructure groupNodeStructure = new NodeStructure();
        groupNodeStructure.setId(String.valueOf(groupId));
        groupNodeStructure.setPId(String.valueOf(parentId));
        groupNodeStructure.setName(agentGroup.getGroupName());
        groupNodeStructure.setPType("dp");      // 部门
        groupNodeStructure.setType("dp");       // 部门
        nodeStructureList.add(groupNodeStructure);

        List<AgentGroupUser> groupUserList = baseOrgService.getGroupUserByGroup(groupId);
        if (CollectionUtils.isNotEmpty(groupUserList)) {
            groupUserList = groupUserList.stream().filter(p -> needRole.contains(p.getUserRoleType())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(groupUserList)) {
                groupUserList.forEach(p -> {
                    AgentUser agentUser = baseOrgService.getUser(p.getUserId());
                    NodeStructure userNodeStructure = new NodeStructure();
                    userNodeStructure.setId(String.valueOf(p.getUserId()));
                    userNodeStructure.setPId(String.valueOf(p.getGroupId()));
                    userNodeStructure.setName(agentUser.getRealName());
                    userNodeStructure.setType("bd");    // 专员
                    userNodeStructure.setPType("dp");   // 部门
                    nodeStructureList.add(userNodeStructure);
                });
            }
        }

        List<AgentGroup> subGroupList = baseOrgService.getGroupListByParentId(groupId);
        if (CollectionUtils.isNotEmpty(subGroupList)) {
            for (AgentGroup subGroup : subGroupList) {
                List<NodeStructure> subGroupNodeStructureList = this.generateGroupAndMemberTree(subGroup.getId(), groupId, groupRoleType, needRole);
                if (CollectionUtils.isNotEmpty(subGroupNodeStructureList)) {
                    nodeStructureList.addAll(subGroupNodeStructureList);
                }
            }
        }
        return nodeStructureList;
    }


    @RequestMapping(value = "clazz_alter.vpage", method = RequestMethod.GET)
    public String clazzAlterList(Model model) {
        List<Long> managedSchools = new ArrayList<>();
        Long id = getRequestLong("id");
        String idType = getRequestString("idType");
        Long schoolId = getRequestLong("schoolId");//学校详情，待办任务，处理换班
        if (schoolId > 0){
            managedSchools.add(schoolId);
        }else {
            if (StringUtils.isNotEmpty(idType) && id != 0L){
                ClazzAlterService.IdType type = ClazzAlterService.IdType.valueOf(idType);
                if (ClazzAlterService.IdType.OTHER_SCHOOL.equals(type)){
                    managedSchools = baseOrgService.getCityManageOtherSchoolByGroupId(id);
                } else if (ClazzAlterService.IdType.USER.equals(type)){
                    managedSchools = baseOrgService.loadBusinessSchoolByUserId(id);
                }
            }else {
                if (getCurrentUser().isBusinessDeveloper() || getCurrentUser().isCityManager()) {
                    managedSchools = baseOrgService.loadBusinessSchoolByUserId(getCurrentUserId());
                }
            }
        }
        List<ClazzAlterMapper> clazzAlterationBySchool = agentResourceService.getClazzAlterationBySchool(managedSchools, 10,0);
        // 未处理, 其中未处理的需要根据 CC_PROCESS_STATE 去提示
        List<ClazzAlterMapper> pendingList = clazzAlterationBySchool.stream().filter(alt -> Objects.equals(ClazzTeacherAlterationState.PENDING, alt.getState())).collect(Collectors.toList());
        model.addAttribute("pendingList", pendingList);
        // 已处理
        List<ClazzAlterMapper> successList = clazzAlterationBySchool.stream().filter(alt -> Objects.equals(ClazzTeacherAlterationState.SUCCESS, alt.getState()) || Objects.equals(ClazzTeacherAlterationState.REJECT, alt.getState())).collect(Collectors.toList());
        model.addAttribute("overList", successList);
        return "rebuildViewDir/mobile/resource/clazz_alter";
    }
    //clazzAlterService
    @RequestMapping(value = "clazz_alter_statistics.vpage", method = RequestMethod.GET)
    @OperationCode("bfd610f14bda4cb3")
    public String clazzAlterStatistics(Model model){
        AuthCurrentUser currentUser = getCurrentUser();
        if (currentUser.isBusinessDeveloper()){
            return redirect("clazz_alter.vpage");
        }else{
            long groupId = getRequestLong("groupId");
            if (0L == groupId){
                List<AgentGroupUser> groupUserByUser = baseOrgService.getGroupUserByUser(currentUser.getUserId());
                if (CollectionUtils.isNotEmpty(groupUserByUser)){
                    groupId = groupUserByUser.get(0).getGroupId();
                }
            }
            List<ClazzAlterStatistics> clazzAlterStatistics = clazzAlterService.generateGroupClazzAlterStatistics(groupId);
            clazzAlterStatistics = clazzAlterStatistics.stream().filter(item ->item.getCount() > 0).collect(Collectors.toList());
            model.addAttribute("clazzAlterStatistics",clazzAlterStatistics);
        }
        return "rebuildViewDir/mobile/resource/clazz_alter_statistics";
    }
}
