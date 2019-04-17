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

package com.voxlearning.utopia.admin.controller.crm;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.time.FastDateFormat;
import com.voxlearning.utopia.admin.service.crm.CrmRegisterServiceImpl;
import com.voxlearning.utopia.api.constant.RegisterFeedbackCategory;
import com.voxlearning.utopia.service.feedback.api.entities.RegisterFeedback;
import com.voxlearning.utopia.service.feedback.client.FeedbackServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author xin.xin
 * @since 2014-03-03
 */
@Controller
@RequestMapping("/crm/registerfeedback")
@Slf4j
public class CrmRegisterFeedbackController extends CrmAbstractController {
    @Inject
    private CrmRegisterServiceImpl crmRegisterService;

    @Inject private FeedbackServiceClient feedbackServiceClient;

    @RequestMapping(value = "index.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String index(Model model) {
        FastDateFormat sdf = FastDateFormat.getInstance(DateUtils.FORMAT_SQL_DATE);
        String today = sdf.format(new Date());

        int state = getRequestInt("state", 1);
        String startDate = getRequestParameter("startDate", today);
        String endDate = getRequestParameter("endDate", "");

        Date start = null;
        Date end = null;
        try {
            start = sdf.parse(startDate);
            end = sdf.parse(endDate);
        } catch (Exception ignored) {
        }

        List<Map<String, Object>> feedbacks = crmRegisterService.findRegisterFeedback(state, start, end);
        feedbacks = feedbacks.stream().
                filter(source -> RegisterFeedbackCategory.CALL_AMBASSADOR != ((RegisterFeedback) source.get("feedback")).getCategory())
                .collect(Collectors.toList());

        model.addAttribute("feedbacks", feedbacks);
        model.addAttribute("state", state);

        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        return "/crm/feedback/registerfeedbackindex";
    }

    @RequestMapping(value = "ambassadorindex.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String ambassadorIndex(Model model) {
        FastDateFormat sdf = FastDateFormat.getInstance(DateUtils.FORMAT_SQL_DATE);
        String today = sdf.format(new Date());

        int state = getRequestInt("state", 1);
        String startDate = getRequestParameter("startDate", today);
        String endDate = getRequestParameter("endDate", "");

        Date start = null;
        Date end = null;
        try {
            start = sdf.parse(startDate);
            end = sdf.parse(endDate);
        } catch (Exception ex) {
        }

        List<Map<String, Object>> feedbacks = crmRegisterService.findRegisterFeedback(state, start, end);
        feedbacks = feedbacks.stream()
                .filter(source -> RegisterFeedbackCategory.CALL_AMBASSADOR == ((RegisterFeedback) source.get("feedback")).getCategory())
                .collect(Collectors.toList());

        model.addAttribute("feedbacks", feedbacks);
        model.addAttribute("state", state);

        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        return "/crm/feedback/ambassadorindex";
    }

    @RequestMapping(value = "process.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage process(@RequestParam Long id, @RequestParam String desc, @RequestParam int state) {
        try {
            MapMessage message = feedbackServiceClient.getFeedbackService().processRegisterFeedback(id, desc, state, getCurrentAdminUser().getAdminUserName());
            if (message.isSuccess()) {
                addAdminLog("处理注册反馈", null, null, "处理注册反馈[" + desc + "]", message.get("mobile"));
            }
            return message;
        } catch (Exception ex) {
            log.error("处理注册验证反馈失败，[id:{},desc:{},state:{}],msg:{}", id, desc, state, ex.getMessage(), ex);
            return MapMessage.errorMessage("处理失败");
        }
    }

    @RequestMapping(value = "processambassadorcheck.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage processAmbassadorcheck(Model model) {
        FastDateFormat sdf = FastDateFormat.getInstance(DateUtils.FORMAT_SQL_DATE);
        Date today = null;
        try {
            today = sdf.parse(DateUtils.getTodaySqlDate());
        } catch (Exception ex) {
        }
        List<Map<String, Object>> feedbacks = crmRegisterService.findRegisterFeedback(1, today, null);
        feedbacks = feedbacks.stream()
                .filter(source -> RegisterFeedbackCategory.CALL_AMBASSADOR == ((RegisterFeedback) source.get("feedback")).getCategory())
                .collect(Collectors.toList());

        MapMessage message = new MapMessage();
        if (feedbacks.size() > 0) {
            message.setSuccess(true);
            message.setInfo(String.valueOf(feedbacks.size()));
        } else {
            message.setSuccess(false);
        }
        return message;
    }

    @RequestMapping(value = "processcheck.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage processcheck(Model model) {
        FastDateFormat sdf = FastDateFormat.getInstance(DateUtils.FORMAT_SQL_DATE);
        Date today = null;
        try {
            today = sdf.parse(DateUtils.getTodaySqlDate());
        } catch (Exception ex) {
        }
        List<Map<String, Object>> feedbacks = crmRegisterService.findRegisterFeedback(1, today, null);
        feedbacks = feedbacks.stream().filter(source -> RegisterFeedbackCategory.CALL_AMBASSADOR != ((RegisterFeedback) source.get("feedback")).getCategory())
                .collect(Collectors.toList());

        MapMessage message = new MapMessage();
        if (feedbacks.size() > 0) {
            message.setSuccess(true);
            message.setInfo(String.valueOf(feedbacks.size()));
        } else {
            message.setSuccess(false);
        }
        return message;
    }
}
