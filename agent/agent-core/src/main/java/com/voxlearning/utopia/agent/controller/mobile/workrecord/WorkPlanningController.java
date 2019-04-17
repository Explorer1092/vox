/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.agent.controller.mobile.workrecord;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.constants.AgentErrorCode;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.service.mobile.v2.CrmVisitPlanService;
import com.voxlearning.utopia.entity.crm.CrmVisitPlan;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.School;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Date;

/**
 * 进校计划
 * Created by alex on 2016/7/25.
 */
@Controller
@RequestMapping("/mobile/work_record")
public class WorkPlanningController extends AbstractAgentController {

    @Inject private SchoolLoaderClient schoolLoaderClient;

    @Inject CrmVisitPlanService crmVisitPlanService;

    // 添加计划页
    @RequestMapping(value = "add_visit_plan.vpage", method = RequestMethod.GET)
    public String addVisitPlan(Model model) {
        CrmVisitPlan plan = getSessionCrmVisitPlan();
        model.addAttribute("plan", plan);
        model.addAttribute("startDate", DateUtils.nextDay(new Date(), 1));
        return "rebuildViewDir/mobile/intoSchool/addvisitplan";
    }

    // 修改计划时间
    @RequestMapping(value = "updatePlanTime.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updatePlanTime() {
        Long userId = getCurrentUserId();
        String recordId = getRequestString("recordId");
        Date updateTime = getRequestDate("updateTime");
        if (updateTime == null) {
            return MapMessage.errorMessage("时间格式错误");
        }
        long timestamp = DayRange.current().getStartTime() + 86400 * 1000;
        if (updateTime.before(new Date(timestamp))) {
            return MapMessage.errorMessage("日期只能选择次日及以后的日期");
        }
        return crmVisitPlanService.updateVisitPTime(userId, recordId, updateTime);
    }

    // 删除计划
    @RequestMapping(value = "removeProgram.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage removeProgram() {
        Long userId = getCurrentUserId();
        String recordId = getRequestString("recordId");
        return crmVisitPlanService.deleteCrmVisitPlan(userId, recordId);
    }

    /**
     * 保存计划
     */
    @RequestMapping(value = "savePlan.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage savePlan() {
        AuthCurrentUser user = getCurrentUser();
        Long schoolId = getRequestLong("schoolId");
        Date visitTime = getRequestDate("visitTime");
        String programContent = getRequestString("content");

        try {
            MapMessage msg = crmVisitPlanService.saveCrmVisitPlan(schoolId, user, visitTime, programContent);
            if (!msg.isSuccess()) {
                return msg;
            }
            cleanSessionCrmVisitPlan();
            return msg;
        } catch (Exception ex) {
            logger.error("save visit plan is failed schoolId:{}", schoolId, ex);
            return MapMessage.errorMessage("保存计划失败");
        }
    }

    // 拜访计划(计划列表)
    @RequestMapping(value = "visitplan.vpage", method = RequestMethod.GET)
    public String visitPlan(Model model) {
        Long userId = getCurrentUserId();
        try {
            model.addAttribute("msgList", crmVisitPlanService.getUserVisitPlan(userId, DayRange.current().getStartDate(), null));
        } catch (Exception ex) {
            logger.error("load visit plan is failed userId:{}", userId, ex);
            //去统一的错误页
            return errorInfoPage(AgentErrorCode.AGENT_PLAN_ERROR, String.format("计划列表页,加载用户的计划列表出现异常，用户ID为%d", userId), model);
        }
//        return "mobile/work_record/visitplan";
        return "rebuildViewDir/mobile/intoSchool/visitplan";
    }

    // 选择学校
    @RequestMapping(value = "chooseSchool.vpage", method = RequestMethod.GET)
    public String addVisitSchool(Model model) {
        String back = getRequestString("back");
        model.addAttribute("backUrl", back);
        model.addAttribute("choiceTeacherAble",getRequestBool("choiceTeacherAble"));
//        return "mobile/work_record/chooseSchool";
        return "rebuildViewDir/mobile/intoSchool/chooseSchool";
    }

    // 搜索学校
    @RequestMapping(value = "searchVisitSchool.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage searchVisitSchool() {
        AuthCurrentUser user = getCurrentUser();
        Integer scope = getRequestInt("scope", 1);
        String schoolKey = getRequestString("schoolKey");
        try {
            return crmVisitPlanService.searchVisibleSchool(user, scope, schoolKey);
        } catch (Exception ex) {
            logger.error("search visit school is failed userId:{},schoolKey:{}", ConversionUtils.toString(user.getUserId()), schoolKey, ex);
            return MapMessage.errorMessage("未搜索到对应的学校");
        }
    }

    // 保存学校到缓存中
    @RequestMapping(value = "saveVisitSchool.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveVisitSchool() {
        Long schoolId = getRequestLong("schoolId");

        School school = schoolLoaderClient.getSchoolLoader()
                .loadSchool(schoolId)
                .getUninterruptibly();
        if (school == null) {
            return MapMessage.errorMessage("学校ID错误或不存在,学校ID为" + schoolId);
        }
        CrmVisitPlan plan = getSessionCrmVisitPlan();
        plan.setSchoolId(schoolId);
        plan.setSchoolName(school.getCname());
        setSessionCrmVisitPlan(plan);
        return MapMessage.successMessage();
    }

    // 保存计划到缓存中
    @RequestMapping(value = "saveVisitPlan.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveVisitPlan() {
        Date visitTime = getRequestDate("visitTime");
        String content = getRequestString("content");
        try {
            CrmVisitPlan plan = getSessionCrmVisitPlan();
            plan.setVisitTime(visitTime);
            plan.setContent(content);
            setSessionCrmVisitPlan(plan);
            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error("save visit school to session is failed visitTime:{},content:{}", visitTime, content, ex);
            return MapMessage.errorMessage("保存页面信息失败");
        }
    }

    // 保存计划对象到缓存当中
    private CrmVisitPlan getSessionCrmVisitPlan() {
        Long userId = getCurrentUserId();
        Object obj = agentCacheSystem.getUserSessionAttribte(userId, "crm_visit_plan");
        if (obj != null && obj instanceof CrmVisitPlan) {
            return (CrmVisitPlan) obj;
        }

        return new CrmVisitPlan();
    }

    // 将计划对象存入缓存中
    private void setSessionCrmVisitPlan(CrmVisitPlan plan) {
        Long userId = getCurrentUserId();
        agentCacheSystem.addUserSessionAttribte(userId, "crm_visit_plan", plan);
    }

    // 清空计划对象的缓存
    private void cleanSessionCrmVisitPlan() {
        Long userId = getCurrentUserId();
        agentCacheSystem.addUserSessionAttribte(userId, "crm_visit_plan", new CrmVisitPlan());
    }

}
