package com.voxlearning.utopia.agent.controller.mobile.daily;


import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.service.daily.AgentDailyService;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Date;


/**
 * @author deliang.che
 * @since 2018/9/19
 */
@Controller
@RequestMapping(value = "/mobile/daily")
public class DailyMobileController extends AbstractAgentController {
    @Inject
    private AgentDailyService agentDailyService;

    /**
     * 创建日报之前
     * @return
     */
    @RequestMapping(value = "before_add_daily.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage beforeAddDaily(){
        Long userId = getRequestLong("userId");
        if (userId == 0L){
            userId = getCurrentUserId();
        }
        return agentDailyService.beforeAddDaily(userId);
    }

    /**
     * 创建日报进校选择学校
     * @return
     */
    @RequestMapping(value = "daily_into_school_search.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage dailyIntoSchoolSearch(){
        String searchKey = getRequestString("searchKey");
        Integer scene = getRequestInt("scene", 2);
        Boolean isDefault = getRequestBool("isDefault");
        Long userId = getRequestLong("userId");
        if (userId == 0L){
            userId = getCurrentUserId();
        }
        return MapMessage.successMessage().add("schoolInfoList",agentDailyService.dailyIntoSchoolSearch(userId,searchKey,scene,isDefault));
    }

    /**
     * 创建日报资源拓维选择上层资源
     * @return
     */
    @RequestMapping(value = "daily_outer_resource_search.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage dailyOuterResourceSearch(){
        String searchName = getRequestString("searchName");
        Boolean isDefault = getRequestBool("isDefault");
        Long userId = getRequestLong("userId");
        if (userId == 0L){
            userId = getCurrentUserId();
        }
        return MapMessage.successMessage().add("dataMap",agentDailyService.dailyOuterResourceSearch(userId,searchName,isDefault));
    }

    /***
     * 创建日报选择陪同对象
     * @return
     */
    @RequestMapping(value = "daily_partner_search.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage dailyPartnerSearch(){
        return MapMessage.successMessage().add("dataList",agentDailyService.dailyPartnerSearch(getCurrentUser()));
    }


    /**
     * 创建日报
     * @return
     */
    @RequestMapping(value = "add_daily.vpage",method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addDaily(){
        String otherWorkResult = getRequestString("otherWorkResult");   //其他工作达成
        String schoolIdStr = getRequestString("schoolIdStr");           //进校学校ID字符串
        String meetingNameStr = getRequestString("meetingNameStr");     //组会名称字符串
        String outerResourceIdStr = getRequestString("outerResourceIdStr");   //上层资源ID字符串
        String partnerIdStr = getRequestString("partnerIdStr");         //陪同对象ID字符串
        String otherWork = getRequestString("otherWork");               //其他工作

        Long userId = getRequestLong("userId");
        if (userId == 0L){
            userId = getCurrentUserId();
        }
        return agentDailyService.addOrEditDaily(null,userId,otherWorkResult,schoolIdStr,meetingNameStr,outerResourceIdStr,partnerIdStr,otherWork);
    }

    /**
     * 日报提示
     * @return
     */
    @RequestMapping(value = "daily_point_out.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage dailyPointOut(){
        Long userId = getRequestLong("userId");
        if (userId == 0L){
            userId = getCurrentUserId();
        }
        return agentDailyService.dailyPointOut(userId);
    }


    /**
     * 部门及角色列表
     * @return
     */
    @RequestMapping(value = "group_role_list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage groupUserRoleTypeList() {
        AuthCurrentUser currentUser = getCurrentUser();
        MapMessage mapMessage = MapMessage.successMessage();
        AgentRoleType userRole = baseOrgService.getUserRole(currentUser.getUserId());
        mapMessage.put("userRoleType",userRole);
        mapMessage.put("dataList",agentDailyService.groupRoleList(currentUser));
        return mapMessage;
    }

    /**
     * 日报列表
     * @return
     */
    @RequestMapping(value = "daily_list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage dailyList() {
        Date date = getRequestDate("date", "yyyyMMdd", new Date());
        Long groupId = getRequestLong("groupId");
        String userRoleType = getRequestString("userRoleType");
        AuthCurrentUser currentUser = getCurrentUser();
        //区域经理、大区经理、全国总监
        if (currentUser.isAreaManager() || currentUser.isRegionManager() || currentUser.isCountryManager()){
            if (groupId == 0L){
                return MapMessage.errorMessage("部门不正确！");
            }
            if (StringUtils.isBlank(userRoleType) || AgentRoleType.nameOf(userRoleType) == null){
                return MapMessage.errorMessage("人员角色不正确！");
            }
        }
        return MapMessage.successMessage().add("dataList",agentDailyService.dailyList(currentUser,date,groupId,userRoleType));
    }


    /**
     * 日报详情
     * @return
     */
    @RequestMapping(value = "daily_detail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage dailyDetail() {
        Date date = getRequestDate("date", "yyyyMMdd", new Date());
        Long userId = getRequestLong("userId");
        if (userId == 0L){
            userId = getCurrentUserId();
        }
        return MapMessage.successMessage().add("dataMap",agentDailyService.dailyDetail(date,userId,getCurrentUserId(),1));
    }

    /**
     * 编辑日报之前
     * @return
     */
    @RequestMapping(value = "before_edit_daily.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage beforeEditDaily() {
        Date date = getRequestDate("date", "yyyyMMdd", new Date());
        Long userId = getRequestLong("userId");
        if (userId == 0L){
            userId = getCurrentUserId();
        }
        return MapMessage.successMessage().add("dataMap",agentDailyService.dailyDetail(date,userId,getCurrentUserId(),2));
    }

    /**
     * 编辑日报
     * @return
     */
    @RequestMapping(value = "edit_daily.vpage",method = RequestMethod.POST)
    @ResponseBody
    public MapMessage editDaily(){
        String id = getRequestString("id");
        String otherWorkResult = getRequestString("otherWorkResult");   //其他工作达成
        String schoolIdStr = getRequestString("schoolIdStr");           //进校学校ID字符串
        String meetingNameStr = getRequestString("meetingNameStr");     //组会名称字符串
        String researcherIdStr = getRequestString("outerResourceIdStr");   //教研员ID字符串
        String partnerIdStr = getRequestString("partnerIdStr");         //陪同对象ID字符串
        String otherWork = getRequestString("otherWork");               //其他工作
        Long userId = getRequestLong("userId");
        if (userId == 0L){
            userId = getCurrentUserId();
        }
        return agentDailyService.addOrEditDaily(id,userId,otherWorkResult,schoolIdStr,meetingNameStr,researcherIdStr,partnerIdStr,otherWork);
    }

    /**
     * 范围、组织、角色三级联动
     * @return
     */
    @RequestMapping(value = "range_organization_role_linkage.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage rangeOrganizationRole(){
        Long groupId = getRequestLong("groupId");
        String groupRoleType = getRequestString("groupRoleType");
        String roleType = getRequestString("roleType");
        return MapMessage.successMessage().add("dataMap",agentDailyService.rangeOrganizationRole(groupId,groupRoleType,roleType,getCurrentUserId()));
    }

    /**
     * 获取当前部门列表
     * @return
     */
    @RequestMapping(value = "current_group_list.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage currentGroupList(){
        Long groupId = getRequestLong("groupId");
        return MapMessage.successMessage().add("dataMap",agentDailyService.currentGroupList(groupId,getCurrentUserId()));
    }

    /**
     * 选择部门下钻，获取子级部门
     * @return
     */
    @RequestMapping(value = "sub_group_list.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage subGroupList(){
        Long groupId = getRequestLong("groupId");
        return MapMessage.successMessage().add("dataList",agentDailyService.subGroupList(groupId));
    }

    /**
     * 选择部门返回，获取父级部门
     * @return
     */
    @RequestMapping(value = "parent_group_list.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage parentGroupList(){
        Long groupId = getRequestLong("groupId");
        return MapMessage.successMessage().add("dataMap",agentDailyService.parentGroupList(groupId,getCurrentUserId()));
    }

    /**
     * 日报统计
     * @return
     */
    @RequestMapping(value = "daily_statistic.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage dailyStatistic() {
        Date date = getRequestDate("date", "yyyyMMdd", new Date());
        Long groupId = getRequestLong("groupId");
        String groupRoleType = getRequestString("groupRoleType");
        String userRoleType = getRequestString("userRoleType");
        AuthCurrentUser currentUser = getCurrentUser();
        if (currentUser.isBusinessDeveloper() || currentUser.isCityManager()){
            return MapMessage.errorMessage("该角色不支持统计功能！");
        }
        if (groupId == 0L){
            return MapMessage.errorMessage("部门不正确！");
        }
        if (StringUtils.isBlank(groupRoleType) || AgentGroupRoleType.nameOf(groupRoleType) == null){
            return MapMessage.errorMessage("部门级别不正确！");
        }
        if (StringUtils.isBlank(userRoleType) || AgentRoleType.nameOf(userRoleType) == null){
            return MapMessage.errorMessage("人员角色不正确！");
        }
        return MapMessage.successMessage().add("dataList",agentDailyService.dailyStatistic(date,groupId,AgentGroupRoleType.nameOf(groupRoleType),AgentRoleType.nameOf(userRoleType)));
    }

    /**
     * 日报点评雷达图
     * @return
     */
    @RequestMapping(value = "daily_comments_radar_map.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage dailyCommentsRadarMap(){
        Date date = getRequestDate("date", "yyyyMMdd", new Date());
        Long userId = getRequestLong("userId");
        if (userId == 0L){
            userId = getCurrentUserId();
        }
        return agentDailyService.dailyCommentsRadarMap(date,userId);
    }

    /**
     * 日报分数概览
     * @return
     */
    @RequestMapping(value = "daily_score_overview.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage dailyScoreOverview(){
        String dailyId = getRequestString("dailyId");
        return MapMessage.successMessage().add("data",agentDailyService.dailyScoreOverview(dailyId));
    }
}