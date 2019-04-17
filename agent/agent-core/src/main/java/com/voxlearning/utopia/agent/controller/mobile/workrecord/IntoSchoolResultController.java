package com.voxlearning.utopia.agent.controller.mobile.workrecord;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.constants.AgentErrorCode;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.service.mobile.v2.WorkRecordService;
import com.voxlearning.utopia.agent.service.mobile.workrecord.IntoSchoolMonthService;
import com.voxlearning.utopia.agent.service.mobile.workrecord.IntoSchoolTodayService;
import com.voxlearning.utopia.agent.view.*;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUserSchool;
import com.voxlearning.utopia.service.crm.api.entities.agent.CrmWorkRecord;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 进校效果
 * Created by yaguang.wang
 * on 2017/9/29.
 */

@Controller
@RequestMapping("/mobile/into_school")
public class IntoSchoolResultController extends AbstractAgentController {
    @Inject private WorkRecordService workRecordService;
    @Inject private IntoSchoolTodayService intoSchoolTodayService;
    @Inject private IntoSchoolMonthService intoSchoolMonthService;

    // 进校统计
    @RequestMapping(value = "into_school_statistics.vpage", method = RequestMethod.GET)
    public String intoSchoolStatistics(Model model) {
        String range = getRequestString("range");
        int roleType = getRequestInt("roleType",-1);
        model.addAttribute("range", range);
        model.addAttribute("roleType", roleType);
        AuthCurrentUser currentUser = getCurrentUser();
        Long groupId = getRequestLong("groupId");
        if (groupId == 0L) {
            AgentGroup agentGroup = baseOrgService.getGroupFirstOne(currentUser.getUserId(), null);
            if (agentGroup == null) {
                return errorInfoPage(AgentErrorCode.INTO_SCHOOL_GROUP_ERROR, "部门信息错误，用户部门未找到", model);
            }
            groupId = agentGroup.getId();
        }
        model.addAttribute("groupId", groupId);
        return "rebuildViewDir/mobile/intoSchool/into_school_statistics";
    }

    // 进校统计数据
    @RequestMapping(value = "into_school_statistics_data.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage intoSchoolStatisticsData() {
        String range = getRequestString("range");
        AuthCurrentUser currentUser = getCurrentUser();
        Long groupId = getRequestLong("groupId");
        if (groupId == 0L) {
            AgentGroup agentGroup = baseOrgService.getGroupFirstOne(currentUser.getUserId(), null);
            if (agentGroup == null) {
                return MapMessage.errorMessage("部门信息错误，用户部门未找到");
            }
            groupId = agentGroup.getId();
        }
        List<Long> bdIds = baseOrgService.getAllSubGroupUserIdsByGroupIdAndRole(groupId, AgentRoleType.BusinessDeveloper.getId()); // 获取该部门下指定角色的人员列表
        Map<Long, List<AgentUserSchool>> bdSchools = baseOrgService.getUserSchoolByUsers(bdIds);
        Map<Long, Set<Long>> bdSchoolMap = workRecordService.bdSchoolMap(bdSchools);

        MapMessage resultMessage = MapMessage.successMessage();
        if ("day".equals(range)){
            Map<String, List<AgentTodayIntoSchoolView>> groupViews = intoSchoolTodayService.generateGroupCategoryMap(groupId, bdSchoolMap);
            if (MapUtils.isNotEmpty(groupViews)) {
                if (CollectionUtils.isNotEmpty(groupViews.get("Bd"))) {
                    resultMessage.put("bdViews", groupViews.get("Bd"));
                    groupViews.remove("Bd");
                }
                if (CollectionUtils.isEmpty(groupViews.get("Region"))) {
                    groupViews.remove("Region");
                }
                if (CollectionUtils.isEmpty(groupViews.get("City"))) {
                    groupViews.remove("City");
                }
                resultMessage.put("groupViews", groupViews);
            }
        }else if ("month".equals(range)){
            Map<String, List<AgentIntoSchoolStatisticsView>> monthGroupViews = intoSchoolMonthService.generateGroupCategoryMap(groupId, bdSchoolMap);
            if (MapUtils.isNotEmpty(monthGroupViews)) {
                if (CollectionUtils.isNotEmpty(monthGroupViews.get("Bd"))) {
                    resultMessage.put("monthBdViews", monthGroupViews.get("Bd"));
                    monthGroupViews.remove("Bd");
                }
                if (CollectionUtils.isEmpty(monthGroupViews.get("Region"))) {
                    monthGroupViews.remove("Region");
                }
                if (CollectionUtils.isEmpty(monthGroupViews.get("City"))) {
                    monthGroupViews.remove("City");
                }
                resultMessage.put("monthGroupViews", monthGroupViews);
            }
        }
        return resultMessage;
    }

    // 工作记录
    @RequestMapping(value = "into_school_result.vpage", method = RequestMethod.GET)
    public String intoSchoolResult(Model model) {
        Long bdId = getRequestLong("userId");
        AuthCurrentUser currentUser = getCurrentUser();
        if (currentUser.isBusinessDeveloper()) {
            bdId = currentUser.getUserId();
        }
        List<Long> bdManageJunior = baseOrgService.getUserSchools(bdId, SchoolLevel.JUNIOR.getLevel());
        List<CrmWorkRecord> monthCrmWorkRecords = workRecordService.loadBdJuniorIntoSchoolRecord(bdId, bdManageJunior, MonthRange.current().getStartDate(), new Date());
        // 专员进校统计
        BaseIntoSchoolStatisticsView monthView = new BaseIntoSchoolStatisticsView();
        if (CollectionUtils.isNotEmpty(monthCrmWorkRecords)) {
            monthView = workRecordService.loadBdIntoSchoolStatisticsByBdId(monthCrmWorkRecords, bdManageJunior);
        }
        monthView.setSchoolTotal(bdManageJunior.size());
        Map<Long, Date> teacherWorkMap = workRecordService.loadMapTeacher(monthCrmWorkRecords);
        // 布置作业老师
        Set<Long> teacherHwCount = workRecordService.loadTeacherIdHw(teacherWorkMap);
        List<IntoSchoolResultListView> intoSchoolResultListViews = workRecordService.loadIntoSchoolResultListView(monthCrmWorkRecords);
        model.addAttribute("monthView", monthView);
        model.addAttribute("userId", bdId);
        model.addAttribute("teacherSize", teacherWorkMap.keySet().stream().filter(p -> !teacherHwCount.contains(p)).count());
        model.addAttribute("schoolList", intoSchoolResultListViews);
        return "rebuildViewDir/mobile/intoSchool/into_school_result";
    }

    // 查询该员工未布置作业老师
    @RequestMapping(value = "visit_teacher.vpage", method = RequestMethod.GET)
    public String visitTeacherNoHomework(Model model) {
        Long userId = getRequestLong("userId");
        AuthCurrentUser currentUser = getCurrentUser();
        if (currentUser.isBusinessDeveloper()) {
            userId = currentUser.getUserId();
        }
        model.addAttribute("list", workRecordService.loadBdUnAssignHwTeacher(userId));
        return "rebuildViewDir/mobile/intoSchool/visit_teacher";
    }

    // 进校未达标信息
    @RequestMapping(value = "no_reach.vpage", method = RequestMethod.GET)
    public String noReachIntoSchool(Model model) {
        String date = getRequestString("date");
        Date startDate = DateUtils.stringToDate(date, "yyyyMMdd");
        if (startDate == null) {
            return errorInfoPage(AgentErrorCode.INTO_SCHOOL_NO_REACH_ERROR, "进校未达标专员时间格式错误", model);
        }
        AuthCurrentUser currentUser = getCurrentUser();
        if (!currentUser.isRegionManager()) {
            return errorInfoPage(AgentErrorCode.INTO_SCHOOL_ROLE_ERROR, "暂不支持您的角色查看进校未达标信息", model);
        }
        List<NoReachIntoSchoolView> views = workRecordService.loadNoReachIntoSchoolByUserId(currentUser.getUserId(), date);
        model.addAttribute("views", views);
        model.addAttribute("date", DateUtils.dateToString(startDate, "MM-dd"));
        return "rebuildViewDir/mobile/intoSchool/into_school_no_reach";
    }
}
