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

package com.voxlearning.ucenter.controller.student;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.ucenter.support.controller.AbstractWebController;
import com.voxlearning.utopia.service.message.api.entity.UserMessage;
import com.voxlearning.utopia.service.message.client.MessageLoaderClient;
import com.voxlearning.utopia.service.message.client.MessageServiceClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Objects;

/**
 * @author changyuan.liu
 * @since 2015.12.18
 */
@Controller
@RequestMapping("/student/message")
public class StudentMessageController extends AbstractWebController {

    @Inject private MessageLoaderClient messageLoaderClient;
    @Inject private MessageServiceClient messageServiceClient;

    /**
     * 显示系统消息和站内信页面
     */
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index() {
        return "studentv3/message/index";
    }

    /**
     * 显示系统消息列表页面-分页。
     */
    @RequestMapping(value = "list.vpage", method = RequestMethod.GET)
    public String list(Model model) {
        User user = currentUser();
        int currentPage = getRequestInt("currentPage");
        Page<UserMessage> pagination = messageLoaderClient.getMessageLoader().getMessages(user.narrow(), currentPage - 1, 10);
        model.addAttribute("pagination", pagination);
        model.addAttribute("currentPage", currentPage);

        // 在加载之后mark所有消息已读
        // redmine 21870
        pagination.getContent().stream()
                .filter(Objects::nonNull)
                .forEach(e -> messageServiceClient.getMessageService().readMessage(user.getId(), e.getId()));

        return "studentv3/message/list";
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
