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

package com.voxlearning.washington.controller.teacher;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.service.conversation.client.ConversationServiceClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.api.mappers.GroupTeacherMapper;
import com.voxlearning.utopia.service.user.client.AsyncTeacherServiceClient;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalCategory;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalType;
import com.voxlearning.utopia.service.zone.api.constant.GiftCategory;
import com.voxlearning.utopia.service.zone.api.constant.GiftHistoryType;
import com.voxlearning.utopia.service.zone.api.entity.Gift;
import com.voxlearning.utopia.service.zone.client.GiftServiceClient;
import com.voxlearning.utopia.service.zone.client.ZoneConfigServiceClient;
import com.voxlearning.utopia.service.zone.client.ZoneQueueServiceClient;
import com.voxlearning.washington.support.AbstractController;
import com.voxlearning.washington.support.InternalGiftLoader;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author RuiBao
 * @version 0.1
 * @since 13-9-3
 */
@Controller
@RequestMapping("/teacher/gift")
public class TeacherGiftController extends AbstractController {

    @Inject private RaikouSystem raikouSystem;
    @Inject private AsyncTeacherServiceClient asyncTeacherServiceClient;
    @Inject private ConversationServiceClient conversationServiceClient;
    @Inject private GiftServiceClient giftServiceClient;
    @Inject private ZoneConfigServiceClient zoneConfigServiceClient;
    @Inject private ZoneQueueServiceClient zoneQueueServiceClient;
    @Inject private InternalGiftLoader internalGiftLoader;

    /**
     * 教师礼物首页 -- 显示接收人
     */
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        try {
            Teacher teacher = currentTeacher();
            if (!teacher.hasValidSubject() || !teacher.hasValidKtwelve()) {
                return "redirect:/teacherv3/index.vpage";
            }

            List<Map<String, Object>> clazzList = new ArrayList<>();
            List<Map<String, Object>> studentList = new ArrayList<>();

            // 包班制支持
            // 读取老师主副账号的所有班级
            Set<Long> relTeacherIds = teacherLoaderClient.loadRelTeacherIds(teacher.getId());
            Collection<Clazz> nonterminalClazzs = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(relTeacherIds).values()
                    .stream()
                    .flatMap(Collection::stream)
                    .filter(Clazz::isPublicClazz)
                    .filter(e -> !e.isTerminalClazz())
                    .distinct()
                    .sorted(new Clazz.ClazzLevelAndNameComparator())
                    .collect(Collectors.toList());

            // 读取老师分组,同时包含学生
            Map<Long, List<GroupTeacherMapper>> clazzGroups = deprecatedGroupLoaderClient.loadTeacherGroups(relTeacherIds, true).values()
                    .stream()
                    .flatMap(Collection::stream)
                    .collect(Collectors.groupingBy(GroupMapper::getClazzId));

            for (Clazz clazz : nonterminalClazzs) {
                GroupTeacherMapper group = clazzGroups.getOrDefault(clazz.getId(), Collections.emptyList()).stream().findFirst().orElse(null);
                if (group == null) {
                    continue;
                }

                List<GroupMapper.GroupUser> students = group.getStudents().stream().filter(s -> StringUtils.isNotBlank(s.getName())).collect(Collectors.toList());

                if (!students.isEmpty()) {
                    Map<String, Object> clazzMap = new HashMap<>();
                    clazzMap.put("clazzId", clazz.getId());
                    clazzMap.put("clazzName", clazz.formalizeClazzName());
                    clazzList.add(clazzMap);
                }

                for (GroupMapper.GroupUser student : students) {
                    Map<String, Object> studentMap = new HashMap<>();
                    studentMap.put("studentId", student.getId());
                    studentMap.put("studentName", student.getName());
                    studentMap.put("clazzId", clazz.getId());
                    studentList.add(studentMap);
                }
            }
            model.addAttribute("clazzList", clazzList);
            model.addAttribute("studentList", studentList);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return "teacherv3/gift/index";
    }

    /**
     * 教师赠送礼物页面 -- 显示礼物切片
     */
    @RequestMapping(value = "list.vpage", method = RequestMethod.GET)
    public String displayGift(Model model) {
        GiftCategory category = GiftCategory.valueOf(getRequest().getParameter("category"));
        int currentPage = getRequestInt("currentPage") < 0 ? 0 : getRequestInt("currentPage");
        Pageable pageable = new PageRequest(currentPage, 6);
        model.addAttribute("gifts", zoneConfigServiceClient.getTeacherAvailableGiftsFromBuffer(category, pageable));
        model.addAttribute("category", category);
        return "teacherv3/gift/list";
    }

    /**
     * 老师给学生赠送礼物
     */
    @RequestMapping(value = "teachersendgifttostudent.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage teacherSendGiftToStudent() {
        User sender = currentUser();
        String receiverId = getRequestParameter("receiverId", "");
        List<Long> receiverIds = new ArrayList<>();
        if (StringUtils.isNotBlank(receiverId)) {
            for (String uid : StringUtils.split(receiverId, ",")) {
                receiverIds.add(Long.valueOf(uid));
            }
        }
        long giftId = getRequestLong("giftId");

        if (receiverIds.contains(sender.getId()))
            return MapMessage.successMessage("不能给自己送礼物哦");

        String postscript = StringUtils.cleanXSS(getRequest().getParameter("postscript"));
        MapMessage message = clazzZoneServiceClient.createGift(GiftHistoryType.TEACHER_TO_STUDENT)
                .sender(sender.getId())
                .receivers(receiverIds)
                .gift(giftId)
                .postscript(postscript)
                .skipPaymentPasswordValidation()
                .send();
        if (message.isSuccess()) {
            // 发送新鲜事
            Gift gift = (Gift) message.remove("gift");
            Map<Long, StudentDetail> studentDetailMap = studentLoaderClient.loadStudentDetails(receiverIds);
            for (Long uid : studentDetailMap.keySet()) {
                StudentDetail receiver = studentDetailMap.get(uid);
                if (gift != null && receiver != null && receiver.getClazz() != null) {
                    String content = receiver.fetchRealname() + "同学收到" + sender.fetchRealname() + "老师送来的礼物。";
                    if (StringUtils.isNotBlank(postscript)) {
                        content += "<br/>赠言：" + postscript;
                    }
                    List<GroupMapper> groups = deprecatedGroupLoaderClient.loadStudentGroups(receiver.getId(), false);
                    //TODO currently send to one group for the student
                    //TODO or one message will be saw multi-times for other students
                    if (groups.size() > 0) {
                        zoneQueueServiceClient.createClazzJournal(receiver.getClazzId())
                                .withUser(receiver.getId())
                                .withUser(receiver.fetchUserType())
                                .withClazzJournalType(ClazzJournalType.SEND_GIFT)
                                .withClazzJournalCategory(ClazzJournalCategory.MISC)
                                .withJournalJson(JsonUtils.toJson(MiscUtils.m("content", content, "img", gift.getImgUrl())))
                                .withGroup(groups.get(0).getId())
                                .commit();
                    }
                }
            }
        }
        return message;
    }

    /**
     * 教师收到的礼物
     */
    @RequestMapping(value = "receive/index.vpage", method = RequestMethod.GET)
    public String displayGiftReceived() {
        // 未查看礼物数量清零
//        userServiceClient.resetUncheckedGiftCount(currentUserId());
        return "teacherv3/gift/receive/index";
    }

    /**
     * 教师收到的礼物---分页
     */
    @RequestMapping(value = "receive/list.vpage", method = RequestMethod.GET)
    public String displayGiftReceivedList(Model model) {
        Long teacherId = currentUserId();
        int currentPage = getRequestInt("currentPage") < 0 ? 0 : getRequestInt("currentPage");
        Pageable pageable = new PageRequest(currentPage, 12);
        model.addAttribute("giftReceivedPage", internalGiftLoader.loadReceivedGifts(teacherId, pageable));
        return "teacherv3/gift/receive/list";
    }

    /**
     * 教师送出的礼物
     */
    @RequestMapping(value = "send/index.vpage", method = RequestMethod.GET)
    public String displayGiftSend() {
        return "teacherv3/gift/send/index";
    }

    /**
     * 教师送出的礼物---分页
     */
    @RequestMapping(value = "send/list.vpage", method = RequestMethod.GET)
    public String displayGiftSendList(Model model) {
        Long teacherId = currentUserId();
        int currentPage = getRequestInt("currentPage") < 0 ? 0 : getRequestInt("currentPage");
        Pageable pageable = new PageRequest(currentPage, 12);
        model.addAttribute("giftSendOutPage", internalGiftLoader.loadSentGifts(teacherId, pageable));
        return "teacherv3/gift/send/list";
    }

    /**
     * 教师收到礼物后给学生回复
     */
    @RequestMapping(value = "sendreply.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendReply() {
        Teacher sender = currentTeacher();
        String message = StringUtils.cleanXSS(getRequest().getParameter("message"));
        Long receiverId = getRequestLong("receiverId");
        Long giftHistoryId = getRequestLong("giftHistoryId");
        User receiver = raikouSystem.loadUser(receiverId);
        if (receiver == null) {
            return MapMessage.errorMessage();
        }
        MapMessage response = conversationServiceClient.createConversation2(
                sender.narrow(),
                Collections.singletonList(receiver.narrow()),
                message,
                teacherIds -> {
                    return teacherLoaderClient.loadMainTeacherIds(teacherIds);
                });
        if (!response.isSuccess()) {
            return response;
        }
        // 更新最后一次回复
        giftServiceClient.getGiftService().updateLatestReply(giftHistoryId, message);
        return response;
    }

    /**
     * 删除礼物
     */
    @RequestMapping(value = "deletegift.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteGift() {
        try {
            long giftHistoryId = getRequestInt("giftHistoryId");
            return giftServiceClient.getGiftService().deleteGiftHistoryById(giftHistoryId);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage();
        }
    }

    /**
     * 教师赠送礼物页面             // TODO 暂不开放
     */
    @RequestMapping(value = "sendpage.vpage", method = RequestMethod.GET)
    public String sendPage(Model model) {
        Long teacherId = currentUserId();
        School school = asyncTeacherServiceClient.getAsyncTeacherService()
                .loadTeacherSchool(teacherId)
                .getUninterruptibly();
        Long schoolId = school == null ? null : school.getId();

        List<Teacher> teachers = teacherLoaderClient.loadSchoolTeachers(schoolId);
        teachers = new LinkedList<>(teachers);
        for (Iterator<Teacher> it = teachers.iterator(); it.hasNext(); ) {
            if (Objects.equals(it.next().getId(), teacherId)) {
                it.remove();
            }
        }
        model.addAttribute("teachers", teachers);
        return "";
    }

    /**
     * 老师给老师赠送礼物      // TODO 暂不开放
     */
    @RequestMapping(value = "teachersendgifttoteacher.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage teacherSendGiftToTeacher() {
        long senderId = currentUserId();
        long receiverId = getRequestLong("receiverId");
        long giftId = getRequestLong("giftId");
        String postscript = StringUtils.cleanXSS(getRequest().getParameter("postscript"));
        return clazzZoneServiceClient.createGift(GiftHistoryType.TEACHER_TO_TEACHER)
                .sender(senderId)
                .receivers(receiverId)
                .gift(giftId)
                .postscript(postscript)
                .skipPaymentPasswordValidation()
                .send();
    }
}
