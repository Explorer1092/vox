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
import com.voxlearning.utopia.service.wechat.api.entities.WechatQuestion;
import com.voxlearning.utopia.service.wechat.client.WechatServiceClient;
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

/**
 * Created by Hailong Yang on 2015/10/19.
 */
@Controller
@RequestMapping("/crm/question")
@Slf4j
public class CrmWechatQuestionController extends CrmAbstractController {

    @Inject private WechatServiceClient wechatServiceClient;

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

        List<WechatQuestion> wechatQuestions = wechatServiceClient
                .findWechatQuestionByCreateTimeOrState(state, start, end);
        model.addAttribute("wechatQuestions", wechatQuestions);
        model.addAttribute("state", state);

        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        return "/crm/feedback/wehcatquestion";
    }

    @RequestMapping(value = "process.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage process(@RequestParam Long id, @RequestParam String desc, @RequestParam int state) {
        try {
            MapMessage message = wechatServiceClient
                    .processWechatQuestion(id, desc, state, getCurrentAdminUser().getAdminUserName());
            if (message.isSuccess()) {
                addAdminLog("处理注册反馈", null, null, "处理注册反馈[" + desc + "]", message.get("mobile"));
            }
            return message;
        } catch (Exception ex) {
            log.error("处理注册验证反馈失败，[id:{},desc:{},state:{}],msg:{}", id, desc, state, ex.getMessage(), ex);
            return MapMessage.errorMessage("处理失败");
        }
    }

}
