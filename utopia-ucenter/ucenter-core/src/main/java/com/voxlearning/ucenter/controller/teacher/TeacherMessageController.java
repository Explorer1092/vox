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

package com.voxlearning.ucenter.controller.teacher;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.ucenter.support.controller.AbstractWebController;
import com.voxlearning.utopia.service.conversation.api.mapper.ConversationLetterCounter;
import com.voxlearning.utopia.service.conversation.api.mapper.ConversationLetterStatus;
import com.voxlearning.utopia.service.conversation.client.ConversationLoaderClient;
import com.voxlearning.utopia.service.message.api.entity.UserMessage;
import com.voxlearning.utopia.service.message.client.MessageLoaderClient;
import com.voxlearning.utopia.service.message.client.MessageServiceClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.List;

/**
 * @author changyuan.liu
 * @since 2015.12.12
 */
@Controller
@RequestMapping("/teacher/message")
public class TeacherMessageController extends AbstractWebController {

    @Inject private ConversationLoaderClient conversationLoaderClient;
    @Inject private MessageLoaderClient messageLoaderClient;
    @Inject private MessageServiceClient messageServiceClient;

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        //0： 系统消息；1：家长留言；2：学生留言
        int userType = getRequestInt("userType");

        //第一次访问留言主页应该访问到未阅读的留言
        Teacher teacher = currentTeacher();
        if (!teacher.hasValidSubject() || !teacher.hasValidKtwelve()) {
            return "redirect:/teacher/index.vpage";
        }
        //未处理信件以及回复
        ConversationLetterCounter counter = conversationLoaderClient.getConversationLoader().getLetterCount(teacher.getId(), ConversationLetterStatus.UNREAD);
        int unreadCountFromStudent = counter.count(UserType.STUDENT.getType());
        int unreadCountFromParent = counter.count(UserType.PARENT.getType());
        if (userType > 0 && unreadCountFromParent == 0 && unreadCountFromStudent > 0) {
            userType = 2;
        } else if (userType > 0 && unreadCountFromParent > 0) {
            userType = 1;
        }
        model.addAttribute("userType", userType);
        if (teacher.isKLXTeacher() || teacher.isJuniorMathTeacher()) {
            return "teacherv3/message/kuailexue/index";
        } else {
            return "teacherv3/message/index";
        }
    }

    /**
     * 标记系统消息删除
     */
    @RequestMapping(value = "deleteSysMessage.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteMessage() {
        String messageId = getRequest().getParameter("messageId");
        return messageServiceClient.getMessageService().deleteMessage(currentUserId(), messageId);
    }

    /**
     * 显示系统消息列表页面--分页
     */
    @RequestMapping(value = "list.vpage", method = RequestMethod.GET)
    public String list(Model model) {
        Teacher user = currentTeacher();
        int currentPage = getRequestInt("currentPage");
        Page<UserMessage> pagination = messageLoaderClient.getMessageLoader().getMessages(user.narrow(), currentPage - 1, 10);
        model.addAttribute("pagination", pagination);
        model.addAttribute("currentPage", currentPage);

        // 在加载之后mark所有消息已读
        // redmine 21870
        pagination.getContent().forEach(m -> {
            if (m != null) {
                messageServiceClient.getMessageService().readMessage(user.getId(), m.getId());
            }
        });

        if (user.isKLXTeacher() || user.isJuniorMathTeacher()) {
            return "teacherv3/message/kuailexue/list";
        } else {
            return "teacherv3/message/list";
        }
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
     * 通过班级id获取班级学生家长
     */
    @RequestMapping(value = "parent/clazzstudents.vpage", method = RequestMethod.GET)
    public String getStudentByClassId(Model model) {
        Long clazzId = getRequestLong("clazzId");
        if (!hasClazzTeachingPermission(currentUserId(), clazzId)) {
            return "redirect:/";
        }
        List<User> students = userAggregationLoaderClient.loadTeacherStudentsByClazzId(clazzId, currentUserId());
        model.addAttribute("parents", students);
        return "teacherv3/message/parent/clazzsstudent";
    }

    /**
     * 通过班级id获取班级学生
     */
    @RequestMapping(value = "student/clazzstudents.vpage", method = RequestMethod.GET)
    public String getStudentByClassId1(Model model) {
        Long clazzId = getRequestLong("clazzId");
        if (!hasClazzTeachingPermission(currentUserId(), clazzId)) {
            return "redirect:/";
        }

        List<User> students = userAggregationLoaderClient.loadTeacherStudentsByClazzId(clazzId, currentUserId());

        model.addAttribute("userMappers", students);
        return "teacherv3/message/student/clazzsstudents";
    }

    private boolean hasClazzTeachingPermission(Long teacherId, Long clazzId) {
        return teacherLoaderClient.isTeachingClazz(teacherId, clazzId);
    }
}
