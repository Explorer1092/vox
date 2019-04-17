/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.washington.controller.open.wechat.teacher;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.message.api.constant.MessageStatus;
import com.voxlearning.utopia.service.message.api.entity.UserMessage;
import com.voxlearning.utopia.service.message.client.MessageLoaderClient;
import com.voxlearning.utopia.service.message.client.MessageServiceClient;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.washington.controller.open.AbstractOpenController;
import com.voxlearning.washington.data.OpenAuthContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static com.voxlearning.washington.controller.open.OpenApiReturnCode.SUCCESS_CODE;
import static com.voxlearning.washington.controller.open.OpenApiReturnCode.SYSTEM_ERROR_CODE;

/**
 * Created by Summer Yang on 2015/7/31.
 * 老师微信系统消息
 */
@Controller
@RequestMapping(value = "/open/wechat/teacher/message")
@Slf4j
public class WechatTeacherMessageController extends AbstractOpenController {

    @Inject private MessageLoaderClient messageLoaderClient;
    @Inject private MessageServiceClient messageServiceClient;

    //获取我的消息
    @RequestMapping(value = "list.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext clazzList(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        Map<String, Object> jsonMap = openAuthContext.getParams();
        Long userId = SafeConverter.toLong(jsonMap.get("uid"), Long.MIN_VALUE);
        if (userId == Long.MIN_VALUE) {
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
            openAuthContext.setError("invalid data");
            return openAuthContext;
        }
        try {
            Teacher user = teacherLoaderClient.loadTeacher(userId);
            int currentPage = 1;
            List<UserMessage> msgList = messageLoaderClient.getMessageLoader().getMessages(user.narrow(), currentPage - 1, 100).getContent();
            List<Map<String, Object>> dataList = new ArrayList<>();
            for (UserMessage message : msgList) {
                Map<String, Object> data = new HashMap<>();
                data.put("id", message.getId());
                data.put("content", message.getPayload());
                data.put("createTime", DateUtils.dateToString(new Date(message.getCreateTime()), "yyyy-MM-dd HH:mm"));
                data.put("isRead", message.getStatus() == MessageStatus.READ);
                dataList.add(data);
            }
            openAuthContext.setCode(SUCCESS_CODE);
            openAuthContext.add("messageList", dataList);
        } catch (Exception ex) {
            log.error("load teacher message list failed.", ex);
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
            openAuthContext.setError("查询老师系统消息失败");
        }
        return openAuthContext;
    }

    /**
     * 标记系统消息已读
     */
    @RequestMapping(value = "mark.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext mark(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        Map<String, Object> jsonMap = openAuthContext.getParams();
        Long userId = SafeConverter.toLong(jsonMap.get("uid"), Long.MIN_VALUE);
        String messageId = ConversionUtils.toString(jsonMap.get("mid"));
        if (userId == Long.MIN_VALUE || StringUtils.isBlank(messageId)) {
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
            openAuthContext.setError("invalid data");
            return openAuthContext;
        }
        try {
            messageServiceClient.getMessageService().readMessage(userId, messageId);
            openAuthContext.setCode(SUCCESS_CODE);
        } catch (Exception ex) {
            log.error("mark read teacher message failed.", ex);
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
            openAuthContext.setError("标记系统消息已读失败");
        }
        return openAuthContext;
    }

    /**
     * 根据ID 获取系统消息
     */
    @RequestMapping(value = "get.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext get(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        Map<String, Object> jsonMap = openAuthContext.getParams();
        Long userId = ConversionUtils.toLong(jsonMap.get("uid"));
        String messageId = ConversionUtils.toString(jsonMap.get("mid"));
        if (userId == 0L || StringUtils.isBlank(messageId)) {
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
            openAuthContext.setError("invalid data");
            return openAuthContext;
        }
        try {
            UserMessage message = messageLoaderClient.getMessageLoader().loadUserMessageById(messageId);
            if (null == message) {
                openAuthContext.setCode(SYSTEM_ERROR_CODE);
                openAuthContext.setError("未查询到系统消息");
                return openAuthContext;
            }
            Map<String, Object> data = new HashMap<>();
            data.put("id", message.getId());
            data.put("content", message.getPayload());
            data.put("createTime", DateUtils.dateToString(new Date(message.getCreateTime()), "yyyy-MM-dd HH:mm"));
            data.put("isRead", message.getStatus() == MessageStatus.READ);
            openAuthContext.add("msg", data);
            openAuthContext.setCode(SUCCESS_CODE);
        } catch (Exception ex) {
            log.error("get teacher message failed.uid:{},messageId:{}", userId, messageId, ex);
            openAuthContext.setCode(SYSTEM_ERROR_CODE);
            openAuthContext.setError("获取系统消息失败");
        }
        return openAuthContext;
    }
}
