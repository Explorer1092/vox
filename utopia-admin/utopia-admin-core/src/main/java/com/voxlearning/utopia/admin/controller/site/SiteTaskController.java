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

package com.voxlearning.utopia.admin.controller.site;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.admin.constant.TaskList;
import com.voxlearning.utopia.api.constant.TaskStatus;
import com.voxlearning.utopia.entity.activity.ActivityRechargeTeacher;
import com.voxlearning.utopia.service.ambassador.api.document.AmbassadorRewardHistory;
import com.voxlearning.utopia.service.ambassador.client.AmbassadorLoaderClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by XiaoPeng.Yang on 15-4-8.
 */
@Controller
@Slf4j
@RequestMapping(value = "/site/task")
public class SiteTaskController extends SiteAbstractController {

    @Inject private AmbassadorLoaderClient ambassadorLoaderClient;

    @RequestMapping(value = "taskhome.vpage", method = RequestMethod.GET)
    public String taskHome(Model model) {
        Map<String, String> taskList = new HashMap<>();
        for (TaskList task : TaskList.values()) {
            taskList.put(task.name(), task.getTaskDesc());
        }
        model.addAttribute("taskList", taskList);
        return "site/task/taskhome";
    }

    @RequestMapping(value = "gettaskdata.vpage", method = RequestMethod.POST)
    public String getTaskData(Model model) {
        String taskName = getRequestString("taskName");
        Integer pageNumber = getRequestInt("pageNumber", 1);
        Pageable pageable = new PageRequest(pageNumber - 1, 20);
        String whereStr = "A.MONTH=:month AND A.STATUS=:status";
        TaskList taskList = TaskList.valueOf(taskName);
        //当月确认的是上个月的数据
        String month = DateUtils.dateToString(MonthRange.current().previous().getEndDate(), "yyyyMM");
        switch (taskList) {
            case AutoActivityRechargeTeacherTask:
                Page<ActivityRechargeTeacher> teachers = activityRechargeTeacherPersistence
                        .loadByMonthAndStatus(pageable, ConversionUtils.toInt(month), TaskStatus.NEW.getType());
                model.addAttribute("teachers", teachers);
                model.addAttribute("pageNumber", pageNumber);
                return "site/task/rechargechip";
            case AutoRewardTeacherByCampusActiveRate:
                Page<AmbassadorRewardHistory> rewardHistories = ambassadorLoaderClient.getAmbassadorLoader().findAmbassadorRewardHistories(pageable, month, TaskStatus.NEW.getType());
                model.addAttribute("rewardHistories", rewardHistories);
                model.addAttribute("pageNumber", pageNumber);
                return "site/task/rewardhischip";
            default:
        }
        return "site/task/rewardhischip";
    }

    @RequestMapping(value = "confirmdata.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage confirmData() {
        String taskName = getRequestString("taskName");
        TaskList taskList = TaskList.valueOf(taskName);
        String month = DateUtils.dateToString(MonthRange.current().previous().getEndDate(), "yyyyMM");
        String updateSql = "";
        int result = 0;
        switch (taskList) {
            case AutoActivityRechargeTeacherTask:
                updateSql = "UPDATE VOX_ACTIVITY_RECHARGE_TEACHER SET STATUS=2,UPDATE_DATETIME=NOW() WHERE MONTH=? AND STATUS=1";
                result = utopiaSql.withSql(updateSql).useParamsArgs(ConversionUtils.toInt(month)).executeUpdate();
                break;
            case AutoRewardTeacherByCampusActiveRate:
                updateSql = "UPDATE VOX_AMBASSADOR_REWARD_HISTORY SET STATUS=2,UPDATE_DATETIME=NOW() WHERE MONTH=? AND STATUS=1";
                result = utopiaSql.withSql(updateSql).useParamsArgs(month).executeUpdate();
                break;
            default:
        }
        if (StringUtils.isNotBlank(updateSql)) {
            if (result > 0) {
                return MapMessage.successMessage("确认成功");
            }
        }
        return MapMessage.errorMessage("无数据执行");
    }
}
