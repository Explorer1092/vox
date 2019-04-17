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

package com.voxlearning.washington.controller.student;


import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.message.client.MessageServiceClient;
import com.voxlearning.washington.support.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;

@Controller
@RequestMapping("/student/message")
public class StudentMessageController extends AbstractController {

    @Inject private MessageServiceClient messageServiceClient;

    /**
     * 显示系统消息和站内信页面
     */
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index() {
        return "redirect:" + ProductConfig.getUcenterUrl() + "/student/message/index.vpage";
    }

    /**
     * 显示系统消息列表页面-分页。
     */
    @RequestMapping(value = "list.vpage", method = RequestMethod.GET)
    public String list(Model model) {
        return "redirect:" + ProductConfig.getUcenterUrl() + "/student/message/list.vpage";
    }

    /**
     * 标记系统消息已读
     */
    @RequestMapping(value = "mark.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage mark() {
        String messageId = getRequest().getParameter("messageId");
        messageServiceClient.getMessageService().readMessage(currentUserId(), messageId);
        return MapMessage.successMessage();
    }

    /**
     * 标记系统消息删除
     */
    @RequestMapping(value = "deleteSysMes.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteMessage() {
        String messageId = getRequest().getParameter("messageId");
        return messageServiceClient.getMessageService().deleteMessage(currentUserId(), messageId);
    }
}
