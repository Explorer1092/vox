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

package com.voxlearning.washington.controller.open.v1.teacher;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.conversation.api.mapper.ConversationPagination;
import com.voxlearning.washington.controller.open.AbstractTeacherApiController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.voxlearning.washington.controller.open.ApiConstants.*;
import static com.voxlearning.washington.controller.open.v1.teacher.TeacherApiConstants.*;

/**
 * teacher message api controller
 * Created by Shuai Huan on 2015/1/16.
 */
@Controller
@RequestMapping(value = "/v1/teacher/message")
@Slf4j
public class TeacherMessageApiController extends AbstractTeacherApiController {

    private static final int pageSize = 10;

    private static final String[] messageFilterList = {"【查看详情】", "［查看详情］", "【查看】", "查看详情", "查看"};

    /**
     * 获取老师学生之间，老师家长之间对话
     */
    @RequestMapping(value = "/getconversation.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage getConversation() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequiredNumber(REQ_USER_TYPE, "用户类型");
            validateEnum(REQ_USER_TYPE, "用户类型", "2", "3");
            validateRequiredNumber(REQ_PAGE_NUMBER, "页码");
            validateRequest(REQ_USER_TYPE, REQ_PAGE_NUMBER);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
        resultMap.add(RES_MESSAGE, RES_RESULT_SUSPEND_MSG);
        return resultMap;

//        String[] categories = new String[2]; //老师学生之间对话
//        User curUser = getApiRequestUser();
//        // 根据UserType发送验证码
//        UserType userType = UserType.of(getRequestInt(REQ_USER_TYPE));
//        if (userType == UserType.STUDENT) {//老师学生之间对话
//            categories[0] = "T_S";
//            categories[1] = "S_T";
//        } else if (userType == UserType.PARENT) {//老师家长之间对话
//            categories[0] = "T_P";
//            categories[1] = "P_T";
//        } else {
//            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
//            resultMap.add(RES_MESSAGE, RES_RESULT_WRONG_USER_ROLE);
//            return resultMap;
//        }
//
//        Integer currentPage = getRequestInt(REQ_PAGE_NUMBER);
//        ConversationPagination pagination = conversationLoaderClient.getConversation(curUser.narrow(), currentPage - 1, pageSize, categories);
//        pagination = conversationLoaderClient.fillUserInformation(pagination, userLoaderClient);
//
//        List<ConversationPagination.Conversation> conversations = pagination.getContent();
//        List<Map<String, Object>> conversationList = new LinkedList<>();
//        if (CollectionUtils.isNotEmpty(conversations)) {
//            for (ConversationPagination.Conversation conversation : conversations) {
//                Map<String, Object> conversationMap = new HashMap<>();
//                conversationMap.put(RES_MESSAGE_PAYLOAD, conversation.getPayload());
//                conversationMap.put(RES_MESSAGE_CREATE_TIME, conversation.getCreateTime());
//                conversationMap.put(RES_MESSAGE_ID, conversation.getUniqueId());
//                conversationMap.put(RES_LETTER_ID, conversation.getLetterId());
//                conversationMap.put(RES_MESSAGE_MODERATOR, conversation.getModerator());
//                StringBuilder sb = new StringBuilder();
//                if (conversation.getModerator()) {
//                    if (CollectionUtils.isEmpty(conversation.getReceivers())) {
//                        sb.append("您说");
//                    } else {
//                        sb.append("您对");
//                        for (ConversationPagination.Receiver receiver : conversation.getReceivers()) {
//                            sb.append(receiver.getReceiverName()).append(",");
//                        }
//                        sb.replace(sb.length() - 1, sb.length(), "说");
//                    }
//                } else {
//                    sb.append(conversation.getSender().getSenderName());
//                    sb.append("对您说");
//                }
//                conversationMap.put(RES_MESSAGE_TITLE, sb.toString());
//                conversationMap.put(RES_MESSAGE_SENDER_AVATAR, getUserAvatarImgUrl(conversation.getSender().getSenderImageUrl()));
//
//                List<Map<String, Object>> replyList = new LinkedList<>();
//                for (ConversationPagination.Reply reply : conversation.getTalkReplies()) {
//                    addReply(reply, replyList);
//                    constructReplyList(reply, replyList);
//                }
//                conversationMap.put(RES_REPLY_LIST, replyList);
//                conversationList.add(conversationMap);
//            }
//        }
//
//        resultMap.add(RES_MESSAGE_LIST, conversationList);
//        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
//        return resultMap;
    }

    @RequestMapping(value = "/deleteconversation.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage deleteConversation() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_MESSAGE_ID, "消息ID");
            validateRequest(REQ_MESSAGE_ID);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
        resultMap.add(RES_MESSAGE, RES_RESULT_SUSPEND_MSG);
        return resultMap;

//        User curUser = getApiRequestUser();
//        String uniqueId = getRequestString(REQ_MESSAGE_ID);
//        boolean success = conversationServiceClient.deleteConversation(curUser.narrow(), uniqueId).isSuccess();
//        if (success) {
//            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
//        } else {
//            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
//            resultMap.add(RES_MESSAGE, RES_RESULT_INTERNAL_ERROR_MSG);
//        }
//        return resultMap;
    }

    @RequestMapping(value = "/replyconversation.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage replyConversation() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_MESSAGE_ID, "消息ID");
            validateRequired(REQ_MESSAGE_CONTENT, "消息内容");
            validateRequest(REQ_MESSAGE_ID, REQ_MESSAGE_CONTENT);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
        resultMap.add(RES_MESSAGE, RES_RESULT_SUSPEND_MSG);
        return resultMap;

//        User curUser = getApiRequestUser();
//        String letterId = getRequestString(REQ_MESSAGE_ID);
//        String content = getRequestString(REQ_MESSAGE_CONTENT);
//        MapMessage response = conversationServiceClient.replyLetter(curUser.narrow(), letterId, content);
//        if (!response.isSuccess()) {
//            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
//            resultMap.add(RES_MESSAGE, RES_RESULT_INTERNAL_ERROR_MSG);
//        } else {
//            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
//        }
//        return resultMap;
    }

    @RequestMapping(value = "/createconversation.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage createConversation() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequiredNumber(REQ_USER_TYPE, "用户类型");
            validateEnum(REQ_USER_TYPE, "用户类型", "2", "3");
            validateRequired(REQ_STUDENT_LIST, "学生ID列表");
            validateRequired(REQ_MESSAGE_CONTENT, "消息内容");
            validateRequest(REQ_USER_TYPE, REQ_STUDENT_LIST, REQ_MESSAGE_CONTENT);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
        resultMap.add(RES_MESSAGE, RES_RESULT_SUSPEND_MSG);
        return resultMap;

//        // 根据UserType发送验证码
//        UserType userType = UserType.of(getRequestInt(REQ_USER_TYPE));
//        User curUser = getApiRequestUser();
//        String idList = getRequestString(REQ_STUDENT_LIST);
//        String content = getRequestString(REQ_MESSAGE_CONTENT);
//        List<Long> studentIds = StringHelper.toLongList(idList);
//        MapMessage response;
//        if (userType == UserType.STUDENT) {
//            Set<User> students = new LinkedHashSet<>();
//            for (Long userId : studentIds) {
//                User student = userLoaderClient.loadUser(userId);
//                MiscUtils.addNonNullElement(students, student);
//            }
//            response = conversationServiceClient.createConversation(
//                    curUser.narrow(),
//                    students.stream().map(User::narrow).collect(Collectors.toList()),
//                    content);
//        } else if (userType == UserType.PARENT) {
//            Set<User> parents = new LinkedHashSet<>();
//            Map<Long, Student> studentMap = studentLoaderClient.loadStudents(studentIds);
//            Map<Long, Clazz> clazzMap = clazzLoaderClient.loadStudentClazzs(studentIds);
//            Map<Long, Set<Long>> studentBindWechatParentMap = wechatServiceClient.studentBindWechatParentMap(studentIds);
//            for (Long userId : studentIds) {
//                Set<Long> parentIds = studentBindWechatParentMap.get(userId);
//                if (CollectionUtils.isNotEmpty(parentIds)) {
//                    for (Long parentId : parentIds) {
//                        User parent = new User();
//                        parent.setId(parentId);
//                        parent.setUserType(UserType.PARENT.getType());
//                        parents.add(parent);
//
//                        //发送到微信消息
//                        Student student = studentMap.get(userId);
//                        if (student == null) {
//                            continue;
//                        }
//                        Map<String, Object> extensionMap = new HashMap<>();
//                        extensionMap.put("teacherId", curUser.getId());
//                        extensionMap.put("teacherName", curUser.getProfile().getRealname());
//                        extensionMap.put("studentId", userId);
//                        extensionMap.put("studentName", student.getProfile().getRealname());
//                        extensionMap.put("msg", content);
//                        Clazz clazz = clazzMap.get(userId);
//                        if (clazz != null) {
//                            extensionMap.put("clazzId", clazz.getId());
//                            extensionMap.put("clazzName", clazz.formalizeClazzName());
//                        }
//                        wechatServiceClient.processWechatNotice(WechatNoticeProcessorType.TeacherToParentNotice, parentId, extensionMap, WechatType.PARENT);
//                    }
//                }
//            }
//            response = conversationServiceClient.createConversation(
//                    curUser.narrow(),
//                    parents.stream().map(User::narrow).collect(Collectors.toList()),
//                    content);
//        } else {
//            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
//            resultMap.add(RES_MESSAGE, RES_RESULT_WRONG_USER_ROLE);
//            return resultMap;
//        }
//
//        if (response.isSuccess()) {
//            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
//        } else {
//            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
//            resultMap.add(RES_MESSAGE, RES_RESULT_INTERNAL_ERROR_MSG);
//        }
//        return resultMap;
    }

    @RequestMapping(value = "/getclazzstudents.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage getClazzStudentList() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequiredNumber(REQ_USER_TYPE, "用户类型");
            validateEnum(REQ_USER_TYPE, "用户类型", "2", "3");
            validateRequest(REQ_USER_TYPE);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
        resultMap.add(RES_MESSAGE, RES_RESULT_SUSPEND_MSG);
        return resultMap;

//        // 根据UserType发送验证码
//        UserType userType = UserType.of(getRequestInt(REQ_USER_TYPE));
//        User curUser = getApiRequestUser();
//        List<Clazz> clazzs = clazzLoaderClient.loadTeacherClazzs(curUser.getId());
//        List<Map<String, Object>> clazzList = new LinkedList<>();
//        if (userType == UserType.STUDENT) {                    //给班级学生发消息，学生列表
//            for (Clazz clazz : clazzs) {
//                if (clazz.getClazzLevel().getLevel() > 6) {
//                    continue;
//                }
//                List<User> students = studentLoaderClient.loadClazzStudents(clazz.getId());
//                if (CollectionUtils.isEmpty(students)) {
//                    continue;
//                }
//                Map<String, Object> clazzMap = new HashMap<>();
//                clazzMap.put(RES_CLAZZ_ID, clazz.getId());
//                clazzMap.put(RES_CLAZZ_NAME, clazz.formalizeClazzName());
//                List<Map<String, Object>> studentList = new LinkedList<>();
//                for (User user : students) {
//                    Map<String, Object> studentMap = new HashMap<>();
//                    studentMap.put(RES_USER_ID, user.getId());
//                    studentMap.put(RES_REAL_NAME, user.getProfile().getRealname());
//                    studentList.add(studentMap);
//                }
//                clazzMap.put(RES_STUDENT_LIST, studentList);
//                clazzList.add(clazzMap);
//            }
//        } else if (userType == UserType.PARENT) {            //给班级家长发消息，学生列表（有家长微信绑定的才进入列表）
//
//            for (Clazz clazz : clazzs) {
//                if (clazz.getClazzLevel().getLevel() > 6) {
//                    continue;
//                }
//                List<User> students = studentLoaderClient.loadClazzStudents(clazz.getId());
//                if (CollectionUtils.isEmpty(students)) {
//                    continue;
//                }
//                Map<String, Object> clazzMap = new HashMap<>();
//                clazzMap.put(RES_CLAZZ_ID, clazz.getId());
//                clazzMap.put(RES_CLAZZ_NAME, clazz.formalizeClazzName());
//                List<Map<String, Object>> studentList = new LinkedList<>();
//
//                List<Long> studentIds = students.stream()
//                        .map(User::getId)
//                        .filter(t -> t != null)
//                        .collect(Collectors.toList());
//                Map<Long, Set<Long>> studentBindWechatParentMap = wechatServiceClient.studentBindWechatParentMap(studentIds);
//                for (User user : students) {
//                    if (CollectionUtils.isEmpty(studentBindWechatParentMap.get(user.getId()))) { //家长没绑定微信的学生全过滤掉
//                        continue;
//                    }
//                    Map<String, Object> studentMap = new HashMap<>();
//                    studentMap.put(RES_USER_ID, user.getId());
//                    studentMap.put(RES_REAL_NAME, user.getProfile().getRealname());
//                    studentList.add(studentMap);
//                }
//                if (CollectionUtils.isEmpty(studentList)) {
//                    continue;
//                }
//                clazzMap.put(RES_STUDENT_LIST, studentList);
//                clazzList.add(clazzMap);
//            }
//        } else {
//            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
//            resultMap.add(RES_MESSAGE, RES_RESULT_WRONG_USER_ROLE);
//            return resultMap;
//        }
//        resultMap.add(RES_TEACHER_CLAZZ_LIST, clazzList);
//        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
//        return resultMap;
    }

    /**
     * 获取系统消息
     */
    @RequestMapping(value = "/getsysmessage.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage getSystemMessage() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequiredNumber(REQ_PAGE_NUMBER, "页码");
            validateRequest(REQ_PAGE_NUMBER);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
        resultMap.add(RES_MESSAGE, RES_RESULT_SUSPEND_MSG);
        return resultMap;

//        User curUser = getApiRequestUser();
//        Integer currentPage = getRequestInt(REQ_PAGE_NUMBER);
//        Page<UserMessage> pagination = messageLoaderClient.getMessages(curUser.narrow(), currentPage - 1, pageSize);
//        List<Map<String, Object>> messageList = new LinkedList<>();
//        List<UserMessage> userMessages = pagination.getContent();
//        if (CollectionUtils.isNotEmpty(userMessages)) {
//            for (UserMessage userMessage : userMessages) {
//                Map<String, Object> userMessageMap = new HashMap<>();
//                userMessageMap.put(RES_MESSAGE_ID, userMessage.getId());
//                userMessageMap.put(RES_MESSAGE_CREATE_TIME, userMessage.getCreateTime());
//                userMessageMap.put(RES_MESSAGE_PAYLOAD, filterMore(html2Text(userMessage.getPayload())));
//                userMessageMap.put(RES_MESSAGE_STATUS, userMessage.getStatus());
//                messageList.add(userMessageMap);
//            }
//        }
//        resultMap.add(RES_MESSAGE_LIST, messageList);
//        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
//        return resultMap;
    }

    @RequestMapping(value = "/readsysmessage.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage readSystemMessage() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_MESSAGE_ID, "消息ID");
            validateRequest(REQ_MESSAGE_ID);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
        resultMap.add(RES_MESSAGE, RES_RESULT_SUSPEND_MSG);
        return resultMap;

//        String messageId = getRequestString(REQ_MESSAGE_ID);
//        User curUser = getApiRequestUser();
//        boolean success = messageServiceClient.readMessage(curUser.getId(), messageId).isSuccess();
//        if (success) {
//            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
//        } else {
//            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
//            resultMap.add(RES_MESSAGE, RES_RESULT_INTERNAL_ERROR_MSG);
//        }
//        return resultMap;
    }

    @RequestMapping(value = "/deletesysmessage.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage deleteSystemMessage() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_MESSAGE_ID, "消息ID");
            validateRequest(REQ_MESSAGE_ID);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
        resultMap.add(RES_MESSAGE, RES_RESULT_SUSPEND_MSG);
        return resultMap;

//        String messageId = getRequestString(REQ_MESSAGE_ID);
//        User curUser = getApiRequestUser();
//        boolean success = messageServiceClient.deleteMessage(curUser.getId(), messageId).isSuccess();
//        if (success) {
//            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
//        } else {
//            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
//            resultMap.add(RES_MESSAGE, RES_RESULT_INTERNAL_ERROR_MSG);
//        }
//        return resultMap;
    }

    /**
     * 获取未读通知数量
     */
    @RequestMapping(value = "/getunreadmessagecount.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage getUnreadMessageCount() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequest();
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
        resultMap.add(RES_MESSAGE, RES_RESULT_SUSPEND_MSG);
        return resultMap;

//        User user = getApiRequestUser();
//        int count = messageLoaderClient.getUnreadMessageCount(user.narrow());
//        resultMap.add(RES_UNREAD_MESSAGE_COUNT, count);
//        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
//        return resultMap;
    }

    private void addReply(ConversationPagination.Reply reply, List<Map<String, Object>> replyList) {
        Map<String, Object> replyMap = new HashMap<>();
        replyMap.put(RES_REPLY_ID, reply.getId());
        replyMap.put(RES_REPLY_TIME, reply.getCreateTime());
        replyMap.put(RES_REPLY_SENDER_ID, reply.getSender().getSenderId());
        replyMap.put(RES_REPLY_SENDER_NAME, reply.getSender().getSenderName());
        replyMap.put(RES_REPLY_PAYLOAD, reply.getPayload());
        replyList.add(replyMap);
    }

    private void constructReplyList(ConversationPagination.Reply reply, List<Map<String, Object>> replyList) {
        List<ConversationPagination.Reply> children = reply.getChildren();
        if (CollectionUtils.isNotEmpty(children)) {
            for (ConversationPagination.Reply childReply : children) {
                addReply(childReply, replyList);
                constructReplyList(childReply, replyList);
            }
        }
    }

    //以下的代码手动处理一下message里的html标签，以及查看更多吧。。原始数据都存了，app无法展示
    private String html2Text(String inputString) {
        if (StringUtils.isEmpty(inputString)) {
            return null;
        }
        java.util.regex.Pattern pattern;
        java.util.regex.Matcher matcher;

        try {
            String regEx = "<[^>]+>"; //定义HTML标签的正则表达式
            pattern = Pattern.compile(regEx, Pattern.CASE_INSENSITIVE);
            matcher = pattern.matcher(inputString);
            return matcher.replaceAll(""); //过滤html标签
        } catch (Exception e) {
        }
        return null;
    }

    private String filterMore(String inputString) {
        if (StringUtils.isEmpty(inputString)) {
            return null;
        }
        for (String filterMessage : messageFilterList) {
            inputString = inputString.replace(filterMessage, "");
        }
        return inputString;
    }

}
