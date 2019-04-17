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

package com.voxlearning.washington.controller.teacher;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.conversation.client.ConversationServiceClient;
import com.voxlearning.utopia.service.user.api.entities.StudentParent;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.washington.support.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Teacher conversation related controller implementation.
 *
 * @author Xiaohai Zhang
 * @since 11/29/13 6:19 PM
 */
@Controller
@RequestMapping("/teacher/conversation")
public class TeacherConversationController extends AbstractController {

    @Inject private RaikouSystem raikouSystem;
    @Inject private ConversationServiceClient conversationServiceClient;

    // 作业部分可以写留言，无法移除
    @RequestMapping(value = "createconversation.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage createConversation(@RequestBody Map body) {
        int userType = getRequestInt("userType");
        UserType type = UserType.of(userType);
        if (userType == -1) {
            // Keep back compatibility
            type = UserType.PARENT;
        }
        String payload = (String) body.get("content");
        //noinspection unchecked
        List userIds = new LinkedList((List) body.get("userIds"));
        CollectionUtils.transform(userIds, input -> conversionService.convert(input, Long.class));
        if (CollectionUtils.isEmpty(userIds)) {
            return MapMessage.errorMessage();
        }
        User teacher = currentUser();
        switch (type) {
            case PARENT: {
                Set<User> parents = new LinkedHashSet<>();
                for (Object userId : userIds) {
                    List<StudentParent> studentParents = parentLoaderClient.loadStudentParents((Long) userId);
                    if (CollectionUtils.isNotEmpty(studentParents)) {
                        for (StudentParent studentParent : studentParents) {
                            User parent = new User();
                            parent.setId(studentParent.getParentUser().getId());
                            parent.setUserType(UserType.PARENT.getType());
                            if (!parents.contains(parent)) {
                                parents.add(parent);
                            }
                        }
                    }
                }
                return conversationServiceClient.createConversation2(
                        teacher.narrow(),
                        parents.stream().map(User::narrow).collect(Collectors.toList()),
                        payload,
                        teacherIds -> {
                            return teacherLoaderClient.loadMainTeacherIds(teacherIds);
                        });
            }
            case STUDENT: {
                Set<User> students = new LinkedHashSet<>();
                for (Object userId : userIds) {
                    User student = raikouSystem.loadUser((Long) userId);
                    if (student == null) {
                        continue;
                    }
                    if (!students.contains(student)) {
                        students.add(student);
                    }
                }
                return conversationServiceClient.createConversation2(
                        teacher.narrow(),
                        students.stream().map(User::narrow).collect(Collectors.toList()),
                        payload,
                        teacherIds -> {
                            return teacherLoaderClient.loadMainTeacherIds(teacherIds);
                        });
            }
            default: {
                return MapMessage.errorMessage();
            }
        }
    }

    /**
     * Teacher gets conversations between with parent.
     */
    @RequestMapping(value = "parentconversations.vpage", method = RequestMethod.GET)
    public String getConversationParent(Model model) {
        return "redirect:" + ProductConfig.getUcenterUrl() + "/teacher/conversation/parentconversations.vpage";
    }

    /**
     * Teacher gets conversations between with student.
     */
    @RequestMapping(value = "studentconversations.vpage", method = RequestMethod.GET)
    public String getConversationStudent(Model model) {
        return "redirect:" + ProductConfig.getUcenterUrl() + "/teacher/conversation/studentconversations.vpage";
    }
}
