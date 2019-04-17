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

import com.voxlearning.alps.annotation.meta.Ktwelve;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.ucenter.support.controller.AbstractWebController;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.conversation.client.ConversationLoaderClient;
import com.voxlearning.utopia.service.message.client.MessageServiceClient;
import com.voxlearning.utopia.service.school.client.SchoolExtServiceClient;
import com.voxlearning.utopia.service.user.api.constants.LatestType;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.SchoolExtInfo;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.entities.latest.Latest_NewRegisterTeacher;
import com.voxlearning.utopia.service.user.client.AsyncTeacherServiceClient;
import com.voxlearning.utopia.service.user.client.AsyncUserCacheServiceClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author changyuan.liu
 * @since 2015.12.11
 */
@Controller
@RequestMapping("/teacher")
public class TeacherController extends AbstractWebController {

    @Inject private AsyncTeacherServiceClient asyncTeacherServiceClient;
    @Inject private AsyncUserCacheServiceClient asyncUserCacheServiceClient;
    @Inject private ConversationLoaderClient conversationLoaderClient;
    @Inject private MessageServiceClient messageServiceClient;
    @Inject private SchoolExtServiceClient schoolExtServiceClient;

    private static final String SSZ_REDIRECT_URL = "/redirector/apps/go.vpage?app_key=Shensz";

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String teacherIndex() {
        return "redirect:" + ProductConfig.getMainSiteBaseUrl();
    }


    // 气泡信息
    @RequestMapping(value = "bubbles.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage indexBubble() {
        try {
            Teacher teacher = currentTeacher();
            // part - 1 未处理换班申请
            int pendingApplicationCount = teacherAlterationServiceClient.countPendingApplicationSendIn(teacher.getId());
            // part - 2 未处理系统通知
            int unreadMessageCount = messageServiceClient.getMessageService().getUnreadMessageCount(teacher.narrow());
            // part - 3 未处理信件以及回复
            int unreadLetterCount = conversationLoaderClient.getConversationLoader().getUnreadLetterCount(teacher.getId());
            return MapMessage.successMessage()
                    .add("pendingApplicationCount", pendingApplicationCount)
                    .add("unreadNoticeCount", unreadMessageCount)
                    .add("unreadLetterAndReplyCount", unreadLetterCount);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage();
        }
    }

    @RequestMapping(value = "showtip.vpage", method = RequestMethod.GET)
    public String showtip() {
        Long teacherId = currentUserId();
        School school = asyncTeacherServiceClient.getAsyncTeacherService()
                .loadTeacherSchool(teacherId)
                .getUninterruptibly();
        if (null == school) {
            return "redirect:/teacher/index.vpage";
        }
        List<Clazz> clazzs = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(teacherId).stream()
                .filter(Clazz::isPublicClazz)
                .filter(t -> !t.isTerminalClazz())
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(clazzs)) {
            return "redirect:/teacher/index.vpage";
        }
        return "redirect:" + ProductConfig.getUcenterUrl()
                + "/teacher/systemclazz/clazzindex.vpage?step=showtip";
    }

    @RequestMapping(value = "selectschool.vpage", method = RequestMethod.GET)
    public String selectSchool(Model model) {
        Teacher teacher = currentTeacher();
        if (teacher.hasValidSubject()) {
            School school = asyncTeacherServiceClient.getAsyncTeacherService()
                    .loadTeacherSchool(teacher.getId())
                    .getUninterruptibly();
            if (school != null) {
                return getPlatformWebRedirectStr(teacher.getKtwelve(), "/teacher/index.vpage", (teacher.isKLXTeacher() || teacher.isJuniorMathTeacher()));
            }
        }

        String phone = sensitiveUserDataServiceClient.showUserMobile(teacher.getId(), "ucenter:selectSchool", SafeConverter.toString(teacher.getId()));
        model.addAttribute("currentUserProfileMobile", phone);
        return "teacherv3/guide/selectschoolinner";
    }

    // 选择学校学科
    @RequestMapping(value = "guide/selectschoolsubject.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage setSchool() {
        TeacherDetail teacher = currentTeacherDetail();
        if (teacher.hasValidSubject() && asyncTeacherServiceClient.getAsyncTeacherService()
                .loadTeacherSchool(teacher.getId()).getUninterruptibly() != null) {
            return MapMessage.errorMessage("您已经选择了学校和学科");
        }
        Long schoolId = getRequestLong("schoolId");

        String subjectText = getRequestString("subject");
        Subject subject = Subject.valueOf(subjectText);
        if (subject == null || subject == Subject.UNKNOWN) {
            return MapMessage.errorMessage("错误的学科");
        }

        String ktwelveText = getRequestString("ktwelve");
        Ktwelve ktwelve = Ktwelve.of(ktwelveText) == Ktwelve.UNKNOWN ? Ktwelve.PRIMARY_SCHOOL : Ktwelve.of(ktwelveText);
        if (ktwelve == null || ktwelve == Ktwelve.UNKNOWN) {
            return MapMessage.errorMessage("错误的学段");
        }

        MapMessage mesg = teacherServiceClient.setTeacherSubjectSchool(teacher, subject, ktwelve, schoolId);

        // FIXME 不要了
//        int actualTeachClazzCount = getRequestInt("actualTeachClazzCount");
//        if (actualTeachClazzCount != 0) {
//            teacherServiceClient.setTeacherUGCTeachClazzCount(teacher.getId(), actualTeachClazzCount);
//        }

        // 发送动态
        if (mesg.isSuccess()) {
            final Latest_NewRegisterTeacher detail = new Latest_NewRegisterTeacher();
            detail.setUserId(teacher.getId());
            detail.setUserName(teacher.fetchRealname());
            detail.setUserImg(teacher.fetchImageUrl());
            detail.setUserSubject(subject.getValue());
            userServiceClient.createSchoolLatest(schoolId, LatestType.NEW_REGISTER_TEACHER)
                    .withDetail(detail).send();
        }

        if (teacherLoaderClient.isWebGrayFunctionAvailable(teacherLoaderClient.loadTeacherDetail(teacher.getId()), "LoginToHttps", "Teacher", false)) {
            mesg.add("jumpHttpsUrl", ProductConfig.getUcenterUrl().replaceAll("^http://", "https://") + "/teacher/systemclazz/clazzindex.vpage?step=showtip");
        }

        if (teacher.isNotShensz()) {
            //学校没有阅卷机权限时,初中数学老师,高中数学老师进入快乐学首页
            SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService()
                    .loadSchoolExtInfo(schoolId)
                    .getUninterruptibly();
            if (!(schoolExtInfo != null && schoolExtInfo.isScanMachineFlag())) {//无阅卷机权限
                if (Objects.equals(subject, Subject.MATH) && (Objects.equals(ktwelve, Ktwelve.JUNIOR_SCHOOL) || Objects.equals(ktwelve, Ktwelve.SENIOR_SCHOOL))) {
                    if (mesg.containsKey("jumpHttpsUrl")) {
                        mesg.remove("jumpHttpsUrl");
                    }
                    mesg.add("jumpHttpsUrl", ProductConfig.getKuailexueUrl());
                }
            }
        }

        // pc 初中数学老师注册跳转
        if (subject == Subject.MATH && ktwelve == Ktwelve.JUNIOR_SCHOOL) {
            SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService()
                    .loadSchoolExtInfo(schoolId)
                    .getUninterruptibly();
            // 判断用户所在学校是否开通阅卷机权限
            // 未开通，则进入到极算首页
            if (schoolExtInfo == null || !schoolExtInfo.isScanMachineFlag()) {
                if (mesg.containsKey("jumpHttpsUrl")) {
                    mesg.remove("jumpHttpsUrl");
                }
                mesg.add("jumpHttpsUrl", ProductConfig.getMainSiteBaseUrl() + SSZ_REDIRECT_URL);
            }
        }

        return mesg;
    }

    // 教师切换手机号学号
    @RequestMapping(value = "mobileoraccount.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage mobileOrAccount() {
        String method = getRequestParameter("method", "MOBILE");
        if (!StringUtils.equals("ACCOUNT", method) && !StringUtils.equals("MOBILE", method))
            return MapMessage.errorMessage();
        asyncUserCacheServiceClient.getAsyncUserCacheService()
                .TeacherMobileOrAccountCacheManager_setMethod(currentUserId(), method)
                .awaitUninterruptibly();
        return MapMessage.successMessage();
    }

    // FIXME 待删!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    // 这些方法是因为消息中已经写死了地址，所以在这里中转跳转一下，过一段时间后当对应消息不再使用或看到后
    // 可以删除（比如半年？）

    @RequestMapping(value = "smartclazz/list.vpage")
    public String smartClazzRedirect() {
        return "redirect:" + ProductConfig.getMainSiteBaseUrl() + "/teacher/smartclazz/list.vpage";
    }

}
