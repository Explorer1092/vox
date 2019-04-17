/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
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
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.service.clazz.api.entity.GroupStudentTuple;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.ucenter.support.controller.AbstractWebController;
import com.voxlearning.utopia.service.conversation.api.mapper.ConversationPagination;
import com.voxlearning.utopia.service.conversation.client.ConversationLoaderClient;
import com.voxlearning.utopia.service.conversation.client.ConversationServiceClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.StudentParent;
import com.voxlearning.utopia.service.user.api.entities.User;
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
public class TeacherConversationController extends AbstractWebController {

    private static final int CONVERSATION_PER_PAGE = 10;

    @Inject private RaikouSDK raikouSDK;
    @Inject private RaikouSystem raikouSystem;

    @Inject private ConversationLoaderClient conversationLoaderClient;
    @Inject private ConversationServiceClient conversationServiceClient;

    /**
     * Teacher's clazz list.
     */
    @RequestMapping(value = "clazzlist.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage clazzList() {
        List<Map<String, Object>> clazzList = new ArrayList<>();
        List<Clazz> clazzs = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(currentUserId()).stream()
                .filter(Clazz::isPublicClazz)
                .sorted((o1, o2) -> {
                    int w1 = o1.isTerminalClazz() ? 1 : 0;
                    int w2 = o2.isTerminalClazz() ? 1 : 0;
                    int ret = Integer.compare(w1, w2);
                    if (ret != 0) return ret;
                    long id1 = o1.getId() == null ? 0 : o1.getId();
                    long id2 = o2.getId() == null ? 0 : o2.getId();
                    return Long.compare(id1, id2);
                })
                .collect(Collectors.toList());
        if (!clazzs.isEmpty()) {
            for (Clazz clazz : clazzs) {
                Map<String, Object> clazzInfo = new LinkedHashMap<>();
                clazzInfo.put("id", clazz.getId());
                clazzInfo.put("className", clazz.formalizeClazzName());
                clazzList.add(clazzInfo);
            }
        }
        return MapMessage.successMessage().add("clazzList", clazzList);
    }

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
        List<Object> list = (List<Object>) body.get("userIds");
        List<Long> userIds = list.stream().map(SafeConverter::toLong).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(userIds)) {
            return MapMessage.errorMessage();
        }

        Set<Long> groupIds = raikouSDK.getClazzClient()
                .getGroupStudentTupleServiceClient()
                .findByStudentIds(userIds)
                .stream()
                .filter(e -> e.getGroupId() != null)
                .map(GroupStudentTuple::getGroupId)
                .collect(Collectors.toSet());

        User teacher = currentUser();

        boolean noRight = true;
        for (Long groupId : groupIds) {
            if (teacherLoaderClient.hasRelTeacherTeachingGroup(teacher.getId(), groupId)) {
                noRight = false;
                break;
            }
        }
        if (noRight) {
            return MapMessage.errorMessage("您没有该权限");
        }

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
        String[] categories = {"T_P", "P_T"};
        User user = currentUser();
        int currentPage = getRequestInt("currentPage");
        ConversationPagination pagination = conversationLoaderClient.getConversation2(userId -> {
            User u = raikouSystem.loadUser(userId);
            return u == null ? null : u.narrow();
        }, user.narrow(), currentPage - 1, CONVERSATION_PER_PAGE, categories);
        model.addAttribute("pagination", pagination);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("id", user.getId());
        return "teacherv3/conversation/parentconversations";
    }

    /**
     * Teacher gets conversations between with student.
     */
    @RequestMapping(value = "studentconversations.vpage", method = RequestMethod.GET)
    public String getConversationStudent(Model model) {
        int currentPage = getRequestInt("currentPage");
        String[] categories = {"T_S", "S_T"};
        User user = currentUser();
        ConversationPagination pagination = conversationLoaderClient.getConversation2(userId -> {
            User u = raikouSystem.loadUser(userId);
            return u == null ? null : u.narrow();
        }, user.narrow(), currentPage - 1, CONVERSATION_PER_PAGE, categories);
        model.addAttribute("pagination", pagination);
        model.addAttribute("currentPage", currentPage);

        model.addAttribute("id", currentUserId());
        return "teacherv3/conversation/studentconversations";
    }

    /**
     * Teacher deletes conversation.
     */
    @RequestMapping(value = "deleteconversation.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteConversation(@RequestBody Map body) {
        String uniqueId = (String) body.get("uniqueId");
        return conversationServiceClient.getConversationService().deleteConversation2(currentUser().narrow(), uniqueId);
    }

    /**
     * Teacher replies letter.
     */
    @RequestMapping(value = "replyletter.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage replyLetter(@RequestBody Map body) {
        String letterId = (String) body.get("letterId");
        String payload = (String) body.get("content");
        return conversationServiceClient.getConversationService().replyLetter2(currentUser().narrow(), letterId, payload);
    }
}
