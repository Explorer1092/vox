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

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.collections4.Transformer;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.ucenter.support.controller.AbstractWebController;
import com.voxlearning.utopia.service.conversation.api.mapper.ConversationPagination;
import com.voxlearning.utopia.service.conversation.client.ConversationLoaderClient;
import com.voxlearning.utopia.service.conversation.client.ConversationServiceClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Xiaohai Zhang
 * @since 2013-11-28 21:33
 */
@Controller
@RequestMapping("/student/conversation")
public class StudentConversationController extends AbstractWebController {

    private static final int CONVERSATION_PER_PAGE = 10;

    @Inject private RaikouSystem raikouSystem;
    @Inject private ConversationLoaderClient conversationLoaderClient;
    @Inject private ConversationServiceClient conversationServiceClient;

    /**
     * Student conversation index (start page).
     */
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {

        return "redirect:/student/message/index.vpage"; //留言板下线 2017-03-22

        /*List<Teacher> teachers = userAggregationLoaderClient.loadStudentTeachers(currentUserId()).stream().map(ClazzTeacher::getTeacher).collect(Collectors.toList());
        StudentDetail studentDetail = currentStudentDetail();
        List<User> students = userAggregationLoaderClient.loadLinkedClassmatesByClazzId(studentDetail.getClazzId(), studentDetail.getId());
        model.addAttribute("teachers", teachers);
        model.addAttribute("students", students);
        return "studentv3/conversation/index";*/
    }

    /**
     * Student creates conversation, the attendance should be teacher or student.
     */
    @RequestMapping(value = "createconversation.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage createConversation(@RequestBody Map<String, Object> body) {
        String payload = (String) body.get("payload");

        // 过滤敏感词
        if (badWordCheckerClient.containsConversationBadWord(payload)) {
            return MapMessage.errorMessage("留言审核中。");
        }

        //noinspection unchecked
        List userIds = new LinkedList((List) body.get("userIds"));
        CollectionUtils.transform(userIds, new Transformer() {
            @Override
            public Object transform(Object input) {
                return conversionService.convert(input, Long.class);
            }
        });
        if (StringUtils.isBlank(payload) || CollectionUtils.isEmpty(userIds)) {
            return MapMessage.errorMessage("发送失败了！");
        }
        User moderator = currentUser();
        List<User> attendances = new ArrayList<>();
        for (Object each : userIds) {
            Long userId = (Long) each;
            CollectionUtils.addNonNullElement(attendances, raikouSystem.loadUser(userId));
        }
        MapMessage response = conversationServiceClient.createConversation2(
                moderator.narrow(),
                attendances.stream().map(User::narrow).collect(Collectors.toList()),
                payload,
                teacherIds -> {
                    return teacherLoaderClient.loadMainTeacherIds(teacherIds);
                });
        if (!response.isSuccess()) {
            response.setInfo("发送失败了！");
        }
        return response;
    }

    /**
     * Student gets his/her own conversations.
     */
    @RequestMapping(value = "conversations.vpage", method = RequestMethod.GET)
    public String getConversation(Model model) {
        User user = currentUser();
        int currentPage = getRequestInt("currentPage");
        ConversationPagination pagination = conversationLoaderClient.getConversation2(userId -> {
            User u = raikouSystem.loadUser(userId);
            return u == null ? null : u.narrow();
        }, user.narrow(), currentPage - 1, CONVERSATION_PER_PAGE);
        model.addAttribute("pagination", pagination);
        model.addAttribute("currentUserId", currentUserId());
        return "studentv3/conversation/conversations";
    }

    /**
     * Student deletes conversation.
     */
    @RequestMapping(value = "deleteconversation.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteConversation(@RequestBody Map<String, Object> postBody) {
        String uniqueId = (String) postBody.get("uniqueId");
        User user = currentUser();
        return conversationServiceClient.getConversationService().deleteConversation2(user.narrow(), uniqueId);
    }

    /**
     * Student replies the letter.
     */
    @RequestMapping(value = "replyletter.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage replyLetter(@RequestBody Map<String, Object> postBody) {
        String payload = (String) postBody.get("payload");

        // 过滤敏感词
        if (badWordCheckerClient.containsConversationBadWord(payload)) {
            return MapMessage.errorMessage("留言审核中。");
        }

        String letterId = (String) postBody.get("letterId");
        User user = currentUser();
        MapMessage response = conversationServiceClient.getConversationService().replyLetter2(user.narrow(), letterId, payload);
        if (!response.isSuccess()) {
            response.setInfo("发送失败了！");
        }
        return response;
    }
}
