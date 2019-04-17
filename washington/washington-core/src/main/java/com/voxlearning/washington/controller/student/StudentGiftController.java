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

package com.voxlearning.washington.controller.student;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.service.newhomework.api.service.StudentAccomplishmentService;
import com.voxlearning.utopia.service.popup.client.UserPopupServiceClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.ClazzTeacher;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.client.AsyncStudentServiceClient;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.api.constant.PopupCategory.LOWER_RIGHT;
import static com.voxlearning.utopia.api.constant.PopupType.GIFT;

/**
 * Student gift related controller implementation.
 *
 * @author Rui Bao
 * @since 2013-09-03
 */
@Controller
@RequestMapping("/student/gift")
public class StudentGiftController extends AbstractController {

    @Inject private AsyncStudentServiceClient asyncStudentServiceClient;
    @Inject private UserPopupServiceClient userPopupServiceClient;
    @Inject private GiftServiceClient giftServiceClient;
    @Inject private ZoneConfigServiceClient zoneConfigServiceClient;
    @Inject private ZoneQueueServiceClient zoneQueueServiceClient;
    @Inject private InternalGiftLoader internalGiftLoader;

    private StudentAccomplishmentService studentAccomplishmentService;

    @ImportService(interfaceClass = StudentAccomplishmentService.class)
    public void setStudentAccomplishmentService(StudentAccomplishmentService studentAccomplishmentService) {
        this.studentAccomplishmentService = studentAccomplishmentService;
    }

    /**
     * 学生礼物首页 -- 显示接收人
     */
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        try {
            // 是否有支付密码
            UserAuthentication ua = userLoaderClient.loadUserAuthentication(currentUserId());
            model.addAttribute("hasPaymentPassword", StringUtils.isNotBlank(ua.getPaymentPassword()));

            Long studentId = currentUserId();

            Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(studentId);
            if (clazz != null) {
                model.addAttribute("teachers", ClazzTeacher.toTeacherList(
                        userAggregationLoaderClient.loadStudentTeachersByClazzId(clazz.getId(), studentId)));
                List<User> students = userAggregationLoaderClient.loadLinkedClassmatesByClazzId(clazz.getId(), studentId);
                students = students.stream()
                        .filter(u -> StringUtils.isNotBlank(u.getProfile().getRealname()))
                        .collect(Collectors.toList());
                model.addAttribute("classmates", students);
                // 判断是否需要弹出输入密码的弹窗
                model.addAttribute("passwordPopup", StringUtils.isBlank(getWebRequestContext().getCookieManager().getCookie("lupld", "")));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return "studentv3/gift/index";
    }

    /**
     * 学生礼物首页 -- 显示礼物切片
     */
    @RequestMapping(value = "list.vpage", method = RequestMethod.GET)
    public String displayGift(Model model) {
        GiftCategory category = GiftCategory.of(getRequest().getParameter("category"));
        int currentPage = getRequestInt("currentPage") < 0 ? 0 : getRequestInt("currentPage");
        Pageable pageable = new PageRequest(currentPage, 5);
        // 这里添加灰度
        boolean needPay = false;
        School school = asyncStudentServiceClient.getAsyncStudentService()
                .loadStudentSchool(currentUserId())
                .getUninterruptibly();
        if (school != null && school.getId() % 2 == 0) {
            needPay = true;
        }
        model.addAttribute("gifts", zoneConfigServiceClient.getStudentAvailableGiftsFromBuffer(category, pageable, needPay));
        model.addAttribute("category", category);
        return "studentv3/gift/list";
    }

    /**
     * 学生给老师赠送礼物
     */
    @RequestMapping(value = "studentsendgifttoteacher.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage studentSendGiftToTeacher() throws InterruptedException {
        long senderId = currentUserId();

        long giftId = getRequestLong("giftId");
        String paymentPassword = getRequest().getParameter("paymentPassword");
        String receiverId = getRequestParameter("receiverId", "");
        Long historyId = getRequestLong("historyId");
        List<Long> receiverIds = new ArrayList<>();
        if (StringUtils.isNotBlank(receiverId)) {
            List<Long> tempIds = new ArrayList<>();
            for (String uid : StringUtils.split(receiverId, ",")) {
                tempIds.add(Long.valueOf(uid));
            }
            Map<Long, Long> mainTeacherIds = teacherLoaderClient.loadMainTeacherIds(tempIds);
            tempIds.forEach(id -> {
                receiverIds.add(mainTeacherIds.get(id) != null ? mainTeacherIds.get(id) : id);
            });
        }

        if (receiverIds.contains(senderId)) {
            return MapMessage.errorMessage("不能给自己送礼物哦");
        }

        String postscript = StringUtils.cleanXSS(getRequest().getParameter("postscript"));
        postscript = availableStringForMySQLConvertor(postscript);

        boolean finishedQuizOrHomeworkWithinToday = studentAccomplishmentService
                .finishedQuizOrHomeworkWithinToday(senderId).get();

        MapMessage message = clazzZoneServiceClient.createGift(GiftHistoryType.STUDENT_TO_TEACHER)
                .sender(senderId)
                .receivers(receiverIds)
                .gift(giftId)
                .postscript(postscript)
                .paymentPassword(paymentPassword)
                .finishedQuizOrHomeworkWithinToday(finishedQuizOrHomeworkWithinToday)
                .send();
        if (message.isSuccess()) {
            StudentDetail sender = studentLoaderClient.loadStudentDetail(senderId);
            // 发送右下角弹窗
            for (Long uid : receiverIds) {
                sendGiftPopup(uid, GiftHistoryType.STUDENT_TO_TEACHER);
                // 是否答谢
                if (historyId != 0) {
                    // 发送新鲜事
                    Gift gift = (Gift) message.remove("gift");
                    Teacher receiver = teacherLoaderClient.loadTeacher(uid);
                    if (gift != null && sender != null && sender.getClazz() != null) {
                        String content = "<span class=\"w-green\"> " + sender.fetchRealname() + " </span>答谢<span class=\"w-green\"> " + receiver.fetchRealname() + "老师 </span>礼物";
                        List<GroupMapper> groups = deprecatedGroupLoaderClient.loadStudentGroups(sender.getId(), false);
                        //TODO currently send to one group for the student
                        //TODO or one message will be saw multi-times for other students
                        if (groups.size() > 0) {
                            zoneQueueServiceClient.createClazzJournal(sender.getClazzId())
                                    .withUser(sender.getId())
                                    .withUser(sender.fetchUserType())
                                    .withClazzJournalType(ClazzJournalType.SEND_GIFT)
                                    .withClazzJournalCategory(ClazzJournalCategory.MISC)
                                    .withJournalJson(JsonUtils.toJson(MiscUtils.m("content", content, "img", gift.getImgUrl())))
                                    .withGroup(groups.get(0).getId())
                                    .commit();
                        }
                        // 更新答谢状态
                        giftServiceClient.getGiftService().updateThanks(historyId);
                    }
                }
            }

        }
        return message;
    }

    /**
     * 同学之间互相赠送礼物
     */
    @RequestMapping(value = "studentsendgifttostudent.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage studentSendGiftToStudent() throws InterruptedException {
        StudentDetail sender = currentStudentDetail();
        long giftId = getRequestLong("giftId");
        String paymentPassword = getRequest().getParameter("paymentPassword");
        String receiverId = getRequestParameter("receiverId", "");
        Long historyId = getRequestLong("historyId");
        List<Long> receiverIds = new ArrayList<>();
        if (StringUtils.isNotBlank(receiverId)) {
            for (String uid : StringUtils.split(receiverId, ",")) {
                receiverIds.add(Long.valueOf(uid));
            }
        }

        if (receiverIds.contains(sender.getId())) {
            return MapMessage.errorMessage("不能给自己送礼物哦");
        }

        String postscript = StringUtils.cleanXSS(getRequest().getParameter("postscript"));
        postscript = availableStringForMySQLConvertor(postscript);

        boolean finishedQuizOrHomeworkWithinToday = studentAccomplishmentService
                .finishedQuizOrHomeworkWithinToday(sender.getId()).get();

        MapMessage message = clazzZoneServiceClient.createGift(GiftHistoryType.STUDENT_TO_STUDENT)
                .sender(sender.getId())
                .receivers(receiverIds)
                .gift(giftId)
                .postscript(postscript)
                .paymentPassword(paymentPassword)
                .finishedQuizOrHomeworkWithinToday(finishedQuizOrHomeworkWithinToday)
                .send();

        if (message.isSuccess()) {
            // 发送新鲜事
            Gift gift = (Gift) message.get("gift");
            Map<Long, StudentDetail> studentDetailMap = studentLoaderClient.loadStudentDetails(receiverIds);
            // 生日祝福单独处理
            if (gift != null && gift.getGiftCategory() == GiftCategory.BIRTHDAY) {
                for (Long uid : receiverIds) {
                    StudentDetail receiver = studentDetailMap.get(uid);
                    if (receiver.getClazz() != null) {
                        String content = "<span class=\"w-green\"> " + receiver.fetchRealname() + " </span>收到<span class=\"w-green\"> " + sender.fetchRealname() + " </span>的生日祝福！";
                        List<GroupMapper> groups = deprecatedGroupLoaderClient.loadStudentGroups(receiver.getId(), false);
                        //TODO currently send to one group for the student
                        //TODO or one message will be saw multi-times for other students
                        if (groups.size() > 0) {
                            zoneQueueServiceClient.createClazzJournal(receiver.getClazzId())
                                    .withUser(receiver.getId())
                                    .withUser(receiver.fetchUserType())
                                    .withClazzJournalType(ClazzJournalType.SEND_BIRTHDAY_GIFT)
                                    .withClazzJournalCategory(ClazzJournalCategory.MISC)
                                    .withJournalJson(JsonUtils.toJson(MiscUtils.m("content", content, "img", gift.getImgUrl(), "receiverId", receiver.getId())))
                                    .withGroup(groups.get(0).getId())
                                    .commit();
                        }
                    }
                }
            } else {
                String receiverNames = "";
                List<StudentDetail> detailList = new ArrayList<>(studentDetailMap.values());
                for (int i = 0; i < detailList.size(); i++) {
                    if (i > 2) {
                        break;
                    }
                    if (StringUtils.isNotBlank(detailList.get(i).fetchRealname())) {
                        receiverNames = receiverNames + "、" + detailList.get(i).fetchRealname();
                    }
                }
                if (detailList.size() > 3) {
                    receiverNames = receiverNames + "等";
                }
                receiverNames = StringUtils.substring(receiverNames, 1);
                if (gift != null && StringUtils.isNotBlank(receiverNames) && sender.getClazz() != null) {
                    String content = "<span class=\"w-green\"> " + sender.fetchRealname() + " </span>赠送<span class=\"w-green\"> " + receiverNames + " </span>礼物";
                    // 是否答谢
                    if (historyId != 0) {
                        content = "<span class=\"w-green\"> " + sender.fetchRealname() + " </span>答谢<span class=\"w-green\"> " + receiverNames + " </span>礼物";
                    }
                    List<GroupMapper> groups = deprecatedGroupLoaderClient.loadStudentGroups(sender.getId(), false);
                    //TODO currently send to one group for the student
                    //TODO or one message will be saw multi-times for other students
                    if (groups.size() > 0) {
                        zoneQueueServiceClient.createClazzJournal(sender.getClazzId())
                                .withUser(sender.getId())
                                .withUser(sender.fetchUserType())
                                .withClazzJournalType(ClazzJournalType.SEND_GIFT)
                                .withClazzJournalCategory(ClazzJournalCategory.MISC)
                                .withJournalJson(JsonUtils.toJson(MiscUtils.m("content", content, "img", gift.getImgUrl())))
                                .withGroup(groups.get(0).getId())
                                .commit();
                    }
                }
            }
            for (Long uid : receiverIds) {
                // 发送右下角弹窗
                sendGiftPopup(uid, GiftHistoryType.STUDENT_TO_STUDENT);
            }
            if (historyId != 0) {
                // 更新答谢状态
                giftServiceClient.getGiftService().updateThanks(historyId);
            }
        }
        return message;
    }


    /**
     * 收到的礼物
     */
    @RequestMapping(value = "receive/index.vpage", method = RequestMethod.GET)
    public String displayGiftReceived() {
        return "studentv3/gift/receive/index";
    }

    /**
     * 收到的礼物---分页
     */
    @RequestMapping(value = "receive/list.vpage", method = RequestMethod.GET)
    public String displayGiftReceivedList(Model model) {
        Long studentId = currentUserId();
        int currentPage = getRequestInt("currentPage") < 0 ? 0 : getRequestInt("currentPage");
        Pageable pageable = new PageRequest(currentPage, 12);
        model.addAttribute("giftReceivedPage", internalGiftLoader.loadReceivedGifts(studentId, pageable));
        return "studentv3/gift/receive/list";
    }

    /**
     * 送出的礼物
     */
    @RequestMapping(value = "send/index.vpage", method = RequestMethod.GET)
    public String displayGiftSend() {
        return "studentv3/gift/send/index";
    }

    /**
     * 送出的礼物---分页
     */
    @RequestMapping(value = "send/list.vpage", method = RequestMethod.GET)
    public String displayGiftSendList(Model model) {
        Long studentId = currentUserId();
        int currentPage = getRequestInt("currentPage") < 0 ? 0 : getRequestInt("currentPage");
        Pageable pageable = new PageRequest(currentPage, 12);
        model.addAttribute("giftSendOutPage", internalGiftLoader.loadSentGifts(studentId, pageable));
        return "studentv3/gift/send/list";
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

    private void sendGiftPopup(Long receiverId, GiftHistoryType type) {
        String content = "";
        switch (type) {
            case STUDENT_TO_STUDENT:
                content = "<br/>" + "您收到了一份礼物。" + "<a href=\"/student/gift/receive/index.vpage\">查看详情</a>" + "<br/>";
                break;
            case STUDENT_TO_TEACHER:
                content = "<br/>" + "您收到了一份礼物。" + "<a href=\"/teacher/gift/receive/index.vpage\">查看详情</a>" + "<br/>";
                break;
        }
        userPopupServiceClient.createPopup(receiverId)
                .content(content)
                .type(GIFT)
                .category(LOWER_RIGHT)
                .unique(true)
                .create();
    }

    public String availableStringForMySQLConvertor(String source) {
        if (StringUtils.isBlank(source)) {
            return source;
        }
        char[] sourceChars = source.toCharArray();
        int length = sourceChars.length - 1;
        for (int i = 0; i < length; i++) {
            if (Character.isHighSurrogate(sourceChars[i]) && Character.isLowSurrogate(sourceChars[i + 1])) {
                sourceChars[i++] = ' ';
                sourceChars[i] = ' ';
            }
        }
        return new String(sourceChars);
    }

}
