/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.washington.controller.rstaff;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.washington.support.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: xinqiang.wang
 * Date: 13-8-28
 * Time: 下午1:19
 * To change this template use File | Settings | File Templates.
 */
@Controller
@RequestMapping("/rstaff/invite")
public class ResearchStaffInviteController extends AbstractController {

    /**
     * NEW 教研员
     * 教研员邀请老师 -- 首页
     */
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        return "redirect:/rstaff/index.vpage";
    }

    /**
     * NEW 教研员
     * 教研员批量邀请老师--短信邀请
     */
    @RequestMapping(value = "sms.vpage", method = RequestMethod.POST)
    @ResponseBody
    public Map teacherInviteTeacherBySms(HttpServletRequest request) throws Exception {
        try {
            User user = currentUser();
            String mobileStr = getRequest().getParameter("mobile");

            if (StringUtils.isNotBlank(mobileStr)) {
                String[] mobiles = mobileStr.split(",");
                return businessTeacherServiceClient.rstaffInviteTeacherBySms(user, mobiles);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return MapMessage.errorMessage("邀请失败");
    }

    /**
     * NEW 教研员
     * 教研员邀请记录
     */
    @RequestMapping(value = "list.vpage", method = RequestMethod.GET)
    public String list() {
        return "redirect:/rstaff/index.vpage";
    }

    /**
     * 教研员
     * 邀请成功记录
     *
     * @return
     * @author changyuan.liu
     */
    @RequestMapping(value = "successlist.vpage", method = RequestMethod.GET)
    public String successList() {
        return "redirect:/rstaff/index.vpage";
    }

    /**
     * NEW 教研员
     * 教研员邀请记录列表分页
     */
    @RequestMapping(value = "inviteHistory.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage inviteHistoryPageList() {
        try {
            List<Map<String, Object>> data = researchStaffServiceClient.getRemoteReference().findInviteHistoryByUserId(currentUserId());
//            List<Map<String, Object>> data = researchStaffServiceClient.getRemoteReference().findInviteHistoryByUserId(currentUserId(), nul);
            return MapMessage.successMessage().add("page", data);
        } catch (Exception e) {
            logger.error(getClass().getName() + e.getMessage(), e);
            return MapMessage.errorMessage();
        }
    }

    /**
     * NEW 教研员
     * 教研员邀请成功记录列表分页
     */
    @RequestMapping(value = "inviteSuccessfulHistory.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage inviteSuccessfulHistoryPageList() {
        String startDateStr = getRequestParameter("startDate", null);
        String endDateStr = getRequestParameter("endDate", null);
        Date startDate = null;
        if (startDateStr != null) {
            startDate = DateUtils.stringToDate(startDateStr, DateUtils.FORMAT_SQL_DATE);
        }
        Date endDate = null;
        if (endDateStr != null) {
            endDate = DateUtils.stringToDate(endDateStr, DateUtils.FORMAT_SQL_DATE);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(endDate);
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            endDate = calendar.getTime();
        }
        try {
            List<Map<String, Object>> data = researchStaffServiceClient.getRemoteReference().findInviteHistoryByUserId(currentUserId(), currentResearchStaff().getSubject(), Boolean.TRUE, startDate, endDate, true);
            int total = 0;
            for (Map<String, Object> row : data) {
                if (row.containsKey("studentCount")) {
                    total += (int) row.get("studentCount");
                }
            }
            return MapMessage.successMessage().add("page", data).add("total", total);
        } catch (Exception e) {
            logger.error(getClass().getName() + e.getMessage(), e);
            return MapMessage.errorMessage();
        }
    }

    /**
     * NEW 教研员
     */
    @RequestMapping(value = "notify.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage notifyTeacherBySms(HttpServletRequest request) throws Exception {
        try {
            User user = currentUser();
            String teacherId = getRequest().getParameter("teacherId");
            String flag = getRequest().getParameter("notifyFlag");
            if (teacherId == null) {
                return MapMessage.errorMessage("发送失败");
            }
            if (teacherId.equals("all")) {
                return businessTeacherServiceClient.rstaffNotifyAllBySms(currentUserId());
            } else {
                if (flag == null) {
                    return MapMessage.errorMessage("发送失败");
                }
                Long reciverId = conversionService.convert(teacherId, Long.class);
                return businessTeacherServiceClient.rstaffNotifyTeacherBySms(user, reciverId, flag);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return MapMessage.errorMessage("发送失败");
    }


}
