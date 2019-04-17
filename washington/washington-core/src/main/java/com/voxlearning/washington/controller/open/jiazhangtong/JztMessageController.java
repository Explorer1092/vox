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

package com.voxlearning.washington.controller.open.jiazhangtong;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.washington.controller.open.AbstractOpenController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Longlong Yu
 * @since 2014-10-21 16:34
 */
@Controller
@RequestMapping(value = "/open/jzt/message")
@Slf4j
public class JztMessageController extends AbstractOpenController {

    private static final String SUSPEND_MSG = "老版本家长通已停止服务，请下载最新版本一起作业家长通。";

    // 获取通知公告列表
    @RequestMapping(value = "list.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage list() {
        return MapMessage.errorMessage(SUSPEND_MSG);
    }

    // 获取公告列表
    @RequestMapping(value = "announcementlist.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage announcementList() {
        return MapMessage.errorMessage(SUSPEND_MSG);
    }

    // 显示公告详情
    @RequestMapping(value = "getannouncement.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getAnnouncement() {
        return MapMessage.errorMessage(SUSPEND_MSG);
    }

    // 获得孩子班级列表
    @RequestMapping(value = "clazzlist.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage clazzList() {
        return MapMessage.errorMessage(SUSPEND_MSG);
    }

    // 回复公告
    @RequestMapping(value = "replyannouncement.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage replyAnnouncement() {
        return MapMessage.errorMessage(SUSPEND_MSG);
    }

    // 获得家长圈聊天记录
    @RequestMapping(value = "parentchatlist.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage parentChatList() {
        return MapMessage.errorMessage(SUSPEND_MSG);
    }

    // 家长圈发送消息
    @RequestMapping(value = "sendchatmsg.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendChatMsg() {
        return MapMessage.errorMessage(SUSPEND_MSG);
    }

    // 家长圈获得联系人列表
    @RequestMapping(value = "clazzparentlist.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage clazzParentList() {
        return MapMessage.errorMessage(SUSPEND_MSG);
    }
}
