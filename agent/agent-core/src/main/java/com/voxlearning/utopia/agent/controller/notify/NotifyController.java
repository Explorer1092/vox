/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.agent.controller.notify;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.service.notify.AgentNotifyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;

/**
 * Created by Shuai.Huan on 2014/7/21.
 */
@Controller
@RequestMapping("/notify/info")
@Slf4j
public class NotifyController extends AbstractAgentController {

    @Inject
    private AgentNotifyService agentNotifyService;

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    String index(Model model) {
        model.addAttribute("notifies", agentNotifyService.getNotifiesByUserId(getCurrentUserId()));
        return "notify/notifyindex";
    }

    @RequestMapping(value = "readnotify.vpage", method = RequestMethod.POST)
    @ResponseBody
    MapMessage readNotify(Long id) {

        MapMessage mapMessage = new MapMessage();
        try {
            agentNotifyService.readNotify(getCurrentUserId(), id);
            mapMessage.setSuccess(true);
        } catch (Exception ex) {
            log.error("标记消息已读失败,userId:{},notifyId:{},msg:{}", getCurrentUserId(), id, ex.getMessage(), ex);
            mapMessage.setSuccess(false);
            mapMessage.setInfo("标记消息已读失败!" + ex.getMessage());
        }
        return mapMessage;
    }
}
