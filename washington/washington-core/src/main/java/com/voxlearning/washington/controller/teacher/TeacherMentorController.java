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

import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.spi.common.DataProvider;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.MentorCategory;
import com.voxlearning.utopia.api.constant.MentorType;
import com.voxlearning.utopia.entity.ucenter.MentorHistory;
import com.voxlearning.utopia.service.business.client.AsyncBusinessCacheServiceClient;
import com.voxlearning.utopia.service.certification.client.CertificationServiceClient;
import com.voxlearning.utopia.service.invitation.client.AsyncInvitationServiceClient;
import com.voxlearning.utopia.service.mentor.client.MentorServiceClient;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.user.api.entities.InviteHistory;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.client.UserSmsServiceClient;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.api.legacy.MemcachedKeyConstants.MENTOR_SYSTEM_TEACHER_MENTOR_LIST;

/**
 * @author RuiBao
 * @version 0.1
 * @since 5/7/2015
 */
@Controller
@RequestMapping("/teacher/mentor")
public class TeacherMentorController extends AbstractTeacherController {

    @Inject private RaikouSystem raikouSystem;
    @Inject private AsyncBusinessCacheServiceClient asyncBusinessCacheServiceClient;
    @Inject private AsyncInvitationServiceClient asyncInvitationServiceClient;

    @Inject private CertificationServiceClient certificationServiceClient;
    @Inject private UserSmsServiceClient userSmsServiceClient;
    @Inject private MentorServiceClient mentorServiceClient;

    // 获取未认证教师的mentor或者可以选择的mentor列表
    @RequestMapping(value = "mentor.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage mentor() {
        Long teacherId = currentUserId();
        try {
            return businessTeacherServiceClient.findMyMentorOrCandidates(teacherId, MentorCategory.MENTOR_AUTHENTICATION);
        } catch (Exception ex) {
            logger.error("UNAUTH TEACHER INDEX MENTOR ERROR. TID {}", teacherId, ex);
            return MapMessage.errorMessage();
        }
    }

    // 未认证教师选择mentor
    @RequestMapping(value = "choosementor.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage chooseMentor() {
        User mentee = currentUser();
        Long mentorId = getRequestLong("mentorId");
        try {
            MapMessage mesg = atomicLockManager.wrapAtomic(businessTeacherServiceClient)
                    .keyPrefix("MENTOR_SYSTEM")
                    .keys(mentee.getId())
                    .proxy()
                    .setUpMMRelationship(mentorId, mentee.getId(), MentorCategory.MENTOR_AUTHENTICATION, MentorType.MENTEE_INITIATIVE);
            if (mesg.isSuccess()) {
                Map<Long, UserAuthentication> uas = userLoaderClient.loadUserAuthentications(Arrays.asList(mentorId, mentee.getId()));
                UserAuthentication mentor_ua = uas.get(mentorId);
                UserAuthentication mentee_ua = uas.get(mentee.getId());
                if (mentor_ua != null && mentor_ua.isMobileAuthenticated()) {
                    String amMentee = mentee_ua == null ? null : sensitiveUserDataServiceClient.showUserMobile(mentee_ua.getId(), "/open/wechat/teacher/chosementor", SafeConverter.toString(mentee_ua.getId()));
                    String payload = "同校新老师" + mentee.fetchRealname() + "使用一起作业遇到困难向你求帮助！！登录网站“有奖互助”页";
                    if (mentee_ua != null && mentee_ua.isMobileAuthenticated()) {
                        payload += "帮Ta（" + amMentee + "）";
                    } else {
                        payload += "帮Ta";
                    }
                    payload += "还有园丁豆奖励！";
                    userSmsServiceClient.buildSms().to(mentor_ua).content(payload).type(SmsType.MENTOR_MENTEE).send();
                }
            }
            return mesg;
        } catch (Exception ex) {
            if (ex instanceof DuplicatedOperationException) {
                return MapMessage.successMessage("正在处理，请不要重复提交");
            }
            logger.error("TEACHER {} CHOOSE MENTOR ERROR.", mentee.getId(), ex);
            return MapMessage.errorMessage();
        }
    }

    // 认证教师选择mentee
    @RequestMapping(value = "choosementee.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage chooseMentee() {
        Long mentorId = currentUserId();
        Long menteeId = getRequestLong("menteeId");
        String mentorCategory = getRequestString("mentorCategory");
        try {
            MentorCategory category = MentorCategory.valueOf(mentorCategory);
            if (category == null) {
                return MapMessage.errorMessage("类型错误");
            }
            return atomicLockManager.wrapAtomic(businessTeacherServiceClient)
                    .keyPrefix("MENTOR_SYSTEM")
                    .keys(menteeId)
                    .proxy()
                    .setUpMMRelationship(mentorId, menteeId, category, MentorType.MENTOR_INITIATIVE);
        } catch (Exception ex) {
            if (ex instanceof DuplicatedOperationException) {
                return MapMessage.successMessage("正在处理，请不要重复提交");
            }
            logger.error("TEACHER {} CHOOSE MENTEE ERROR.", mentorId, ex);
            return MapMessage.errorMessage();
        }
    }

    // 未认证教师列表
    @RequestMapping(value = "unauthteacherlist.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getUncertificatedTeacherList() {
        TeacherDetail teacher = currentTeacherDetail();
        if (teacher == null || teacher.getTeacherSchoolId() == null || teacher.fetchCertificationState() != AuthenticationState.SUCCESS) {
            return MapMessage.errorMessage();
        }
        try {
            int pageNum = getRequestInt("pageNum", 1);
            int pageSize = getRequestInt("pageSize", 5);
            PageImpl<Map<String, Object>> dataPage = businessTeacherServiceClient
                    .getUncertificatedTeacherListPage(teacher.getTeacherSchoolId(), pageNum - 1, pageSize);
            return MapMessage.successMessage().add("dataPage", dataPage);
        } catch (Exception ex) {
            logger.error("MENTOR SYSTEM GET SCHOOL UNAUTH TEACHER LIST ERROR. TID {}", teacher.getId(), ex);
            return MapMessage.errorMessage();
        }
    }

    // 帮助扩大学生数列表
    @RequestMapping(value = "incrscountlist.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getIncrStudentCountTeacherList() {
        TeacherDetail teacher = currentTeacherDetail();
        if (teacher == null || teacher.getTeacherSchoolId() == null || teacher.fetchCertificationState() != AuthenticationState.SUCCESS) {
            return MapMessage.errorMessage();
        }
        //我必须是认证时间在30天以外的老师
        Date authDate = certificationServiceClient.getRemoteReference()
                .getAuthenticationDate(teacher.getId())
                .getUninterruptibly();
        if (authDate == null || new Date().before(DateUtils.calculateDateDay(authDate, 30))) {
            return MapMessage.errorMessage();
        }
        try {
            Long schoolId = teacher.getTeacherSchoolId();
            List<Map<String, Object>> iclist = asyncBusinessCacheServiceClient.getAsyncBusinessCacheService()
                    .MentorCacheManager_pureLoad(schoolId)
                    .getUninterruptibly();
            if (iclist == null) {
                DataProvider<Long, List<Map<String, Object>>> provider = sid -> businessTeacherServiceClient.getIncrStudentCountTeacherList(sid);
                iclist = provider.provide(schoolId);
                asyncBusinessCacheServiceClient.getAsyncBusinessCacheService()
                        .MentorCacheManager_pureAdd(schoolId, iclist)
                        .awaitUninterruptibly();
            }
            //去除我的邀请关系
            List<Long> myInviteIds = asyncInvitationServiceClient.loadByInviter(teacher.getId())
                    .originalLocationsAsList()
                    .stream()
                    .filter(t -> t.getInviteeId() != 0)
                    .map(InviteHistory.Location::getInviteeId)
                    .distinct()
                    .collect(Collectors.toList());
            List<Map<String, Object>> teacherList = iclist.stream()
                    .filter(i -> !myInviteIds.contains(ConversionUtils.toLong(i.get("userId"))))
                    .collect(Collectors.toList());
            return MapMessage.successMessage().add("teacherList", teacherList);
        } catch (Exception ex) {
            logger.error("MENTOR SYSTEM GET SCHOOL INCR STUDENT COUNT TEACHER LIST ERROR. TID {}", teacher.getId(), ex);
            return MapMessage.errorMessage();
        }
    }

    // 正在帮助的教师列表
    @RequestMapping(value = "mentoringlist.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getMentoringTeacherList() {
        User teacher = currentUser();
        if (teacher == null || teacher.fetchCertificationState() != AuthenticationState.SUCCESS) {
            return MapMessage.errorMessage();
        }
        try {
            List<Map<String, Object>> teacherList = washingtonCacheSystem.CBS.flushable
                    .wrapCache(businessTeacherServiceClient)
                    .expiration(300)
                    .keyPrefix(MENTOR_SYSTEM_TEACHER_MENTOR_LIST)
                    .keys(teacher.getId())
                    .proxy()
                    .getMentoringTeacherList(teacher.getId());
            return MapMessage.successMessage().add("teacherList", teacherList);
        } catch (Exception ex) {
            logger.error("MENTOR SYSTEM GET TEACHER MENTOR LIST ERROR. TID {}", teacher.getId(), ex);
            return MapMessage.errorMessage();
        }
    }

    // mentor和mentee之间发送系统通知。。。
    @RequestMapping(value = "mmnotice.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendSystemNoticeBetweenMm() {
        User sender = currentUser();
        Long receiverId = getRequestLong("receiverId");
        String payload = StringUtils.cleanXSS(getRequestString("payload"));
        if (StringUtils.isBlank(payload) || badWordCheckerClient.containsConversationBadWord(payload)) {
            return MapMessage.errorMessage("发送失败");
        }
        User receiver = raikouSystem.loadUser(receiverId);
        if (receiver == null) {
            return MapMessage.errorMessage("发送失败");
        }

        String key = CacheKeyGenerator.generateCacheKey("MENTOR_MENTEE_CONVERSATION",
                new String[]{"senderId", "receiverId"}, new Object[]{sender.getId(), receiver.getId()});
        if (washingtonCacheSystem.CBS.unflushable.incr(key, 1, 1, DateUtils.getCurrentToDayEndSecond()) > 20) {
            return MapMessage.errorMessage("一天只能发送二十次");
        }

        // 检查下两者是不是有互相帮助关系
        List<MentorHistory> mentorHistories = mentorServiceClient.getRemoteReference().findMentorHistoriesByMentorId(sender.getId()).getUninterruptibly();
        MentorHistory mentorHistory = mentorHistories.stream()
                .filter(p -> Objects.equals(p.getMenteeId(), receiverId))
                .findAny().orElse(null);
        if (mentorHistory == null) {
            return MapMessage.errorMessage("发送失败");
        }

        String payloadPrefix = sender.fetchRealname() + "给" + receiver.fetchRealname() + "留言：";
        teacherLoaderClient.sendTeacherMessage(receiverId, payloadPrefix + payload);
        return MapMessage.successMessage();
    }

    // mentor向mentee发送检查作业提醒和完成认证提醒
    @RequestMapping(value = "mmnoticehwc.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage mentorSendNoticeToMentee() {
        User sender = currentUser();
        Long receiverId = getRequestLong("receiverId");
        String type = getRequestString("type");
        if (!StringUtils.equals("homework", type) && !StringUtils.equals("certification", type)) {
            return MapMessage.errorMessage("发送失败");
        }
        UserAuthentication ua = userLoaderClient.loadUserAuthentication(receiverId);
        if (ua == null || !ua.isMobileAuthenticated()) {
            return MapMessage.errorMessage("对方还没有绑定手机哦");
        }
        String payload = sender.fetchRealname() + "老师提醒你多多布置、检查新作业，只需帮助8名学生完成3次作业就能通过一起作业网认证！";
        String key;
        if (StringUtils.equals("homework", type)) {
            key = CacheKeyGenerator.generateCacheKey("MENTOR_MENTEE_HOMEWORK_NOTICE",
                    new String[]{"senderId", "receiverId"}, new Object[]{sender.getId(), receiverId});
        } else {
            key = CacheKeyGenerator.generateCacheKey("MENTOR_MENTEE_CERTIFICATION_NOTICE",
                    new String[]{"senderId", "receiverId"}, new Object[]{sender.getId(), receiverId});
        }
        if (washingtonCacheSystem.CBS.unflushable.add(key, DateUtils.getCurrentToDayEndSecond(), "dummy")) {
            userSmsServiceClient.buildSms().to(ua).content(payload).type(SmsType.MENTOR_MENTEE).send();
            return MapMessage.successMessage();
        }
        return MapMessage.errorMessage("一天只能发送一次");
    }

    // mentor帮助mentee下载学号
//    @RequestMapping(value = "mmbatchdownload.vpage", method = {RequestMethod.GET, RequestMethod.POST})
//    @ResponseBody
//    public void mentorDownloadForMentee() throws IOException {
//        // 避免学生登录账号访问老师班级管理页面中的下载学生账号信息，抛异常。
//        User user = currentUser();
//        if (!User.isTeacherUser(user)) {
//            getResponse().sendError(HttpServletResponse.SC_FORBIDDEN);
//            return;
//        }
//
//        Teacher teacher = currentTeacher();
//        Long mhid = getRequestLong("mhid");
//        MentorHistory history = businessTeacherServiceClient.loadByMentorHisoryId(mhid);
//        if (history == null || !history.getMentorId().equals(teacher.getId())) {
//            getResponse().sendError(HttpServletResponse.SC_FORBIDDEN);
//            return;
//        }
//        TeacherDetail mentee = teacherLoaderClient.loadTeacherDetail(history.getMenteeId());
//        if (mentee == null || mentee.fetchCertificationState() == AuthenticationState.SUCCESS) {
//            getResponse().sendError(HttpServletResponse.SC_FORBIDDEN);
//            return;
//        }
//
//        DownloadContent downloadContent;
//        try {
//            List<Clazz> clazzs = clazzLoaderClient.getRemoteReference().loadTeacherCreatedClazzs(mentee.getId());
//            // 记录下用户下载过名单
//            userAttributeServiceClient.setExtensionAttribute(mentee.getId(), UserExtensionAttributeKeyType.TEACHER_DOWNLOAD_LIST);
//            downloadContent = teacherLoaderClient.getTeacherResourceLoaderClient().downloadClazzStudentInformation(mentee, clazzs, configLoaderClient);
//        } catch (Exception ex) {
//            logger.error("FAILED TO DOWNLOAD TEACHER '{}' STUDENT INFORMATION", teacher.getId(), ex);
//            downloadContent = null;
//        }
//        if (downloadContent == null) {
//            getResponse().sendError(HttpServletResponse.SC_FORBIDDEN);
//            return;
//        }
//
//        try {
//            getWebRequestContext().downloadOctetStreamFile(
//                    downloadContent.getFilename(),
//                    downloadContent.getContent()
//            );
//        } catch (IOException ex) {
//            getResponse().sendError(HttpServletResponse.SC_FORBIDDEN);
//        }
//    }
}
