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

package com.voxlearning.washington.controller;

import com.voxlearning.alps.annotation.meta.PasswordState;
import com.voxlearning.alps.annotation.meta.RoleType;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MobileRule;
import com.voxlearning.alps.core.util.PasswordRule;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.core.runtime.ProductDevelopment;
import com.voxlearning.utopia.service.config.api.entity.PageBlockContent;
import com.voxlearning.utopia.service.feedback.api.entities.ExamFeedback;
import com.voxlearning.utopia.service.feedback.client.FeedbackServiceClient;
import com.voxlearning.utopia.service.invitation.client.AsyncInvitationServiceClient;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.user.api.constants.InvitationType;
import com.voxlearning.utopia.service.user.api.entities.InviteHistory;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.NeonatalUser;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.washington.mapper.specialteacher.base.SpecialTeacherConstants;
import com.voxlearning.washington.support.AbstractController;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Project controller implementation.
 *
 * @author Yaoheng Wu
 * @author Jingwei Dong
 * @author Guohong Tan
 * @author Xiaohai Zhang
 * @since 2012-02-08
 */
@Controller
@RequestMapping("/project")
@NoArgsConstructor
public class ProjectController extends AbstractController {

    @Inject private FeedbackServiceClient feedbackServiceClient;
    @Inject private AsyncInvitationServiceClient asyncInvitationServiceClient;

    @RequestMapping(value = "{folder}/{page}.vpage", method = RequestMethod.GET)
    public String page(Model model,
                       @PathVariable("folder") String folder,
                       @PathVariable("page") String page,
                       @RequestParam(value = "p", required = false, defaultValue = "0") Integer p) {

        if (currentUserId() != null) {
            model.addAttribute("userId", currentUserId().toString());
        }

        //解决页面被删除了 但仍被调用的情况。
        if (folder.equals("riseschooltalent") || folder.equals("groupon") || folder.equals("eleutian")
                || page.equals("download") || folder.equals("motherday") || folder.equals("firstgrade")
                || folder.equals("happyLearning") || folder.equals("talent") || folder.equals("thanksgivingsend")
                || folder.equals("donateLove") || folder.equals("teachersday") || folder.equals("newwelcome")
                || folder.equals("thanksgiving") || folder.equals("preheat")) {
            return "redirect:/index.vpage";
        }

        if ("mingde".equals(folder)) {
            model.addAttribute("userId", getRequestParameter("userId", ""));
        }

        model.addAttribute("p", p);

        return "project/" + folder + "/" + page;
    }

    @RequestMapping(value = "{folder1}/{folder2}/{page}.vpage", method = RequestMethod.GET)
    public String page2(Model model, @PathVariable("folder1") String folder1, @PathVariable("folder2") String folder2,
                        @PathVariable("page") String page, @RequestParam(value = "p", required = false, defaultValue = "0") Integer p) {

        if (currentUserId() != null) {
            model.addAttribute("userId", currentUserId().toString());
        }
        //解决页面被删除了 但仍被调用的情况。
        if (page.equals("experience") || page.equals("prize") || folder2.equals("teacher")) {
            return "redirect:/index.vpage";
        }

        model.addAttribute("p", p);
        return "project/" + folder1 + "/" + folder2 + "/" + page;
    }

    /**
     * 应试反馈
     */
    @RequestMapping(value = "examfeedback.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendExamFeedback() {

        Long studentId = currentUserId();
        String content = getRequestParameter("content", "");
        Integer type = getRequestInt("feedbackType", 0);
        String examId = getRequestParameter("examId", "0");
        return feedbackServiceClient.getFeedbackService().sendExamFeedback(studentId, content, ExamFeedback.fetchExamFeedbackType(type), examId);
    }

    // 共用生成短地址接口
    @RequestMapping(value = "crt.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage shortLink() {
        try {
            String originalUrl = getRequestString("url");
            String apiUrl = "http://17zyw.cn/";
            if (ProductDevelopment.isDevEnv() || ProductDevelopment.isTestEnv()) {
                apiUrl = "http://d.test.17zuoye.net/";
            }

            String content = HttpRequestExecutor.defaultInstance().post(apiUrl + "crt")
                    .addParameter("url", originalUrl)
                    .execute()
                    .getResponseString();

            return MapMessage.successMessage().add("url", StringUtils.isBlank(content) ? originalUrl : apiUrl + content);
        } catch (Exception e) {
            return MapMessage.errorMessage();
        }
    }

    // 阿分题移动端预热专题
    @RequestMapping(value = "afentivote.vpage", method = RequestMethod.GET)
    public String aFenTiVote() {
        return "project/offline/index";
    }

    // 期末复习活动详情页
    @RequestMapping(value = "finalreview/index.vpage", method = RequestMethod.GET)
    public String finalReviewWarmUp(Model model) {
        if (isMobileRequest(getRequest())) {
            Long userId = getRequestLong("sid");
            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(userId);
            List<VendorApps> list = businessVendorServiceClient.getStudentMobileAvailableApps(studentDetail, null, null);
            list = list.stream().filter(o -> Objects.equals(o.getAppKey(), OrderProductServiceType.AfentiExam.name())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(list)) {
                model.addAttribute("afentiexam", false);
            } else {
                model.addAttribute("afentiexam", true);
            }
            return "project/finalreview/app";
        }
        return "project/finalreview/pc";
    }

    // gwcmodel
    @RequestMapping(value = "gmccontext/index.vpage", method = RequestMethod.GET)
    public String gwcModel() {
        if (isMobileRequest(getRequest())) {
            return "project/gmccontext/app";
        }
        return "project/gmccontext/pc";
    }

    @RequestMapping(value = "appoffline.vpage", method = RequestMethod.GET)
    public String appOffline(Model model) {
        model.addAttribute("type", getRequestString("type"));
        return "/apps/afenti/order/offlineinfomobile";
    }

    // 年会抽奖页面 2016
    @RequestMapping(value = "bizhong.vpage", method = RequestMethod.GET)
    public String biZhong(Model model) {
        // 加个权限
        User user = currentUser();
        if (user == null || (user.getId() != 1585006L && user.getId() != 121886L)) {
            return "redirect:/index.vpage";
        }
        return "project/bizhong/index";
    }

    /**
     * 年会执行抽奖获取中奖备选人
     */
    @RequestMapping(value = "loadluckymans.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage loadLuckyMans() {
        try {
            return atomicLockManager.wrapAtomic(miscServiceClient)
                    .expirationInSeconds(30)
                    .keyPrefix("2016_bizhong_do")
                    .proxy()
                    .loadLuckyMan();
        } catch (DuplicatedOperationException ignore) {
            return MapMessage.errorMessage("别着急，慢点儿点");
        }
    }

    /**
     * 年会执行抽奖翻牌
     */
    @RequestMapping(value = "bingo.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage bingo() {
        try {
            Long workNo = getRequestLong("workNo", -1);
            if (workNo < 0) {
                return MapMessage.errorMessage("亦飞，bug了，传过来的啥玩意儿");
            }
            return atomicLockManager.wrapAtomic(miscServiceClient)
                    .expirationInSeconds(30)
                    .keyPrefix("2016_bizhong_bingo")
                    .keys(workNo)
                    .proxy()
                    .bingo(workNo);
        } catch (DuplicatedOperationException ignore) {
            return MapMessage.errorMessage("别着急，慢点儿点");
        }
    }

    /**
     * 作业单分享详情接口
     */
    @RequestMapping(value = "offline/homework/detail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage offlineHomeworkDetail() {
        List<String> offlineHomeworkIds = StringUtils.toList(getRequestString("ohids"), String.class);
        if (CollectionUtils.isEmpty(offlineHomeworkIds)) {
            return MapMessage.errorMessage("作业单id不能为空");
        }
        String offlineHomeworkId = offlineHomeworkIds.get(0);
        return offlineHomeworkLoaderClient.loadOfflineHomeworkDetail(offlineHomeworkId);
    }

    /**
     * 自主考试报告分享接口
     */
    @RequestMapping(value = "newexam/independent/detail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage newExamIndependentDetail() {
        String newExamId = getRequestString("newExamId");
        if (StringUtils.isBlank(newExamId)) {
            return MapMessage.errorMessage("考试id错误");
        }
        return newExamReportLoaderClient.independentExamDetailForShare(newExamId);
    }

    @RequestMapping(value = "messagedoredirect.vpage", method = RequestMethod.GET)
    public String doRedirectByMessage() {
        String url = getRequestString("url");
        Long userId = currentUserId();
        Enumeration<String> parameterNames = getRequest().getParameterNames();
        String param = "parentId=" + userId;
        while (parameterNames.hasMoreElements()) {
            String name = parameterNames.nextElement();
            if ("url".equals(name)) {
                continue;
            }
            param += "&" + name + "=" + getRequestString(name);
        }
        url = url + "?" + param;
        return "redirect:" + url;
    }

    @RequestMapping(value = "/teacherreport/share.vpage", method = RequestMethod.GET)
    public String teacherReportShare() {
        return "/activity/teacherreport/share";
    }

    @RequestMapping(value = "/studentreport/share.vpage", method = RequestMethod.GET)
    public String studentReportShare() {
        return "/activity/studentreport/share";
    }

    // 2017秋季开学活动3-教师联盟，邀请有礼 redmine:50113

    /**
     * 获取老师姓名，url中拼中文着实蛋疼，故加此接口
     */
    @RequestMapping(value = "/invite/getTeacherName.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getTeacherName() {
        Long teacherId = getRequestLong("teacherId");
        Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
        String name = "";
        if (Objects.nonNull(teacher)) {
            name = teacher.fetchRealname();
        }
        return MapMessage.successMessage().add("teacherName", name).add("captchaToken", RandomUtils.randomString(24));
    }

    /**
     * App端被邀请人点开链接接受邀请
     */
    @RequestMapping(value = "/invite/acceptInvite.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage acceptInvite() {
        Long teacherId = getRequestLong("param");
        Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
        if (Objects.isNull(teacher)) {
            return MapMessage.errorMessage("邀请人不存在");
        }

        String invitedTeacherMobile = getRequestString("invitedTeacherMobile");
        String verifyCode = getRequestString("verifyCode");
        if (!MobileRule.isMobile(invitedTeacherMobile)) {
            return MapMessage.errorMessage("请输入正确的手机号码");
        }

        MapMessage message = smsServiceClient.getSmsService().verifyValidateCode(invitedTeacherMobile, verifyCode, SmsType.TEACHER_SMS_INVITE_TEACHER.name());
        if (!message.isSuccess()) {
            return message;
        }

        // 验证此账号是否已在平台注册过
        UserAuthentication userAuthentication = userLoaderClient.loadMobileAuthentication(invitedTeacherMobile, UserType.TEACHER);
        if (Objects.nonNull(userAuthentication)) {
            return MapMessage.errorMessage("该手机号已注册过一起作业！");
        }

        InviteHistory history = asyncInvitationServiceClient.getAsyncInvitationService()
                .queryByUserIdInviteMobile(teacherId, invitedTeacherMobile)
                .getUninterruptibly();
        if (Objects.nonNull(history)) {
            return MapMessage.successMessage();
        }

        // 向invite_history中添加邀请老师ID和被邀请老师手机号
        InviteHistory inviteHistory = InviteHistory.newInstance();
        inviteHistory.setUserId(teacherId);
        inviteHistory.setInviteSensitiveMobile(invitedTeacherMobile);
        inviteHistory.setInvitationType(InvitationType.TEACHER_INVITE_TEACHER_LINK);
        Boolean flag = asyncInvitationServiceClient.getAsyncInvitationService()
                .createHistory(inviteHistory)
                .getUninterruptibly();
        if (!flag) {
            return MapMessage.errorMessage("系统错误，请稍后再试！");
        }

        // 老师邀请活动优化，接受邀请页面添加注册逻辑
        String invitedTeacherName = getRequestString("name");
        String password = getRequestString("pwd");
        if (Objects.nonNull(invitedTeacherName) && Objects.nonNull(password)) {

            if (!SpecialTeacherConstants.checkChineseName(invitedTeacherName, 6)) {
                return MapMessage.errorMessage("姓名只支持不超过五个字的中文和姓名符·");
            }
            try {
                PasswordRule.validatePassword(password);
            } catch (Exception e) {
                return MapMessage.errorMessage(e.getMessage());
            }

            NeonatalUser neonatalUser = new NeonatalUser();
            neonatalUser.setRoleType(RoleType.ROLE_TEACHER);
            neonatalUser.setUserType(UserType.TEACHER);
            neonatalUser.setMobile(invitedTeacherMobile);
            neonatalUser.setRealname(invitedTeacherName);
            neonatalUser.setPassword(password);
            neonatalUser.setWebSource("INVITE_TEACHER_ACTIVITY");
            neonatalUser.attachPasswordState(PasswordState.USER_SET);

            MapMessage regResult = userServiceClient.registerUser(neonatalUser);
            if (!regResult.isSuccess()) {
                return regResult;
            }
        }

        return MapMessage.successMessage();
    }


    /**
     * 接受邀请获取短信验证码
     */
    @RequestMapping(value = "/invite/getVerifyCode.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getVerifyCode() {
        String mobile = getRequestString("mobile");
//        String captchaToken = getRequestString("captchaToken");
//        String captchaCode = getRequestString("captchaCode");
        if (!MobileRule.isMobile(mobile)) {
            return MapMessage.errorMessage("请输入正确的手机号码");
        }
        // 检查验证码
//        if (!consumeCaptchaCode(captchaToken, captchaCode)) {
//            return MapMessage.errorMessage("验证码错误！");
//        }

        // 验证此账号是否已在平台注册过
        UserAuthentication userAuthentication = userLoaderClient.loadMobileAuthentication(mobile, UserType.TEACHER);
        if (Objects.nonNull(userAuthentication)) {
            return MapMessage.errorMessage("您已注册过一起作业，请直接登录使用");
        }

        return smsServiceClient.getSmsService().sendUnbindMobileVerificationCode(mobile, SmsType.TEACHER_SMS_INVITE_TEACHER.name(), false);
    }

    @RequestMapping(value = "getpageblockcontent.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getPageBlockContent() {
        String pageName = getRequestString("pageName");
        String blockName = getRequestString("blockName");
        if (StringUtils.isEmpty(pageName) || StringUtils.isEmpty(blockName)) {
            return MapMessage.errorMessage("error param");
        }
        PageBlockContent pageBlockContent = pageBlockContentServiceClient.getPageBlockContentBuffer().findByPageName(pageName)
                .stream().filter(p -> StringUtils.isNotBlank(p.getBlockName()) && StringUtils.equals(p.getBlockName(), blockName))
                .findFirst().orElse(null);
        if (pageBlockContent != null) {
            return MapMessage.successMessage().add("content", pageBlockContent.getContent());
        }
        return MapMessage.errorMessage("no data");
    }
}


