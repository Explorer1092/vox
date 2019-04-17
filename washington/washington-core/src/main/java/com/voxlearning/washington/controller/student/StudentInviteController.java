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

import com.voxlearning.washington.support.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/student/invite")
public class StudentInviteController extends AbstractController {

//    @Inject private EmailServiceClient emailServiceClient;
//    @Inject private SmsServiceClient smsServiceClient;
//
//    // ========================================================================
//    // 学生邀请老师
//    // ========================================================================
//
//    /**
//     * 链接邀请老师
//     */
//    @RequestMapping(value = "register.vpage", method = RequestMethod.GET)
//    public String inviteRegister(Model model) {
//        try {
//            User student = currentStudent();
//
//            // 学生所在班级的老师 ∃! 一个
//            List<Teacher> teacherList = userAggregationLoaderClient.loadStudentTeachers(student.getId()).stream().map(ClazzTeacher::getTeacher).collect(Collectors.toList());
//            boolean hasTeachers = teacherList.size() > 1;
//            model.addAttribute("hasTeachers", hasTeachers);
//            if (hasTeachers || teacherList.size() == 0) {
//                return "redirect:activate.vpage";
//            }
//
//            Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(student.getId());
//            List<ClazzTeacher> clazzTeachers = teacherLoaderClient.loadClazzTeachers(clazz.getId());
//            boolean hasEnglishTeacher = clazzTeachers.stream()
//                    .filter(t -> t.getTeacher() != null)
//                    .filter(t -> t.getTeacher().getSubject() == Subject.ENGLISH)
//                    .count() > 0;
//            boolean hasMathTeacher = clazzTeachers.stream()
//                    .filter(t -> t.getTeacher() != null)
//                    .filter(t -> t.getTeacher().getSubject() == Subject.MATH)
//                    .count() > 0;
//            model.addAttribute("hasEnglishTeacher", hasEnglishTeacher);
//            model.addAttribute("hasMathTeacher", hasMathTeacher);
//
//            Subject subject = teacherList.get(0).getSubject() == Subject.ENGLISH ? Subject.MATH : Subject.ENGLISH;
//            // 生成邀请地址
//            String link = ProductConfig.getMainSiteBaseUrl() + "/ucenter/titlink.vpage?url=" + businessTeacherServiceClient.encryptCodeGenerator(student.getId(), null, subject.toString());
//            model.addAttribute("link", link);
//            //验证码
//            model.addAttribute("captchaToken", RandomUtils.randomString(24));
//
//        } catch (Exception e) {
//            logger.error(e.getMessage(), e);
//        }
//        return "studentv3/invite/invitation/register";
//    }
//
//    /**
//     * 手机号邀请老师(数据提交)
//     */
//    @RequestMapping(value = "register.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage smsInvitation() {
//        try {
//            User student = currentStudent();
//            String teacherName = getRequest().getParameter("teacherName");
//            String teacherMobile = getRequest().getParameter("teacherMobile");
//            String captchaToken = getRequest().getParameter("captchaToken");
//            String captchaCode = getRequest().getParameter("captchaCode");
//
//            if (!consumeCaptchaCode(captchaToken, captchaCode)) {
//                return MapMessage.errorMessage("验证码输入错误，请重新输入。").set("value", "codeFalse");
//            }
//
//            // 检查被邀请老师姓名
//            MapMessage mesg = businessTeacherServiceClient.nameValidator(teacherName);
//            if (!mesg.isSuccess()) {
//                return mesg;
//            }
//            // 验证手机
//            mesg = businessTeacherServiceClient.mobileValidator(teacherMobile);
//            if (!mesg.isSuccess()) {
//                return mesg;
//            }
//
//            String payload = "尊敬的" + teacherName + "老师，" + student.getProfile().getRealname() + "，邀您加入一起作业（17zuoye.com）给同学们布置作业，邀请人填" + student.getId().toString();
//            sendSmsToTeacher(teacherMobile, payload, mesg, SmsType.STUDENT_SMS_INVITE_TEACHER);
//            return mesg;
//
//        } catch (Exception e) {
//            logger.error(e.getMessage(), e);
//        }
//        return MapMessage.errorMessage("手机邀请失败");
//    }
//
//    /**
//     * 邮件邀请老师
//     */
//    @RequestMapping(value = "activate.vpage", method = RequestMethod.POST)
//    @ResponseBody
//    public MapMessage emailInvitation() {
//        try {
//            User student = currentStudent();
//            String teacherName = getRequest().getParameter("teacherName");
//            String teacherEmail = getRequest().getParameter("teacherEmail");
//
//            // 学生所在班级的老师
//            List<Teacher> teacherList = userAggregationLoaderClient.loadStudentTeachers(student.getId()).stream().map(ClazzTeacher::getTeacher).collect(Collectors.toList());
////            List<Teacher> teacherList = teacherLoaderClient.getExtension().loadStudentTeachers(student.getId());
//            if (teacherList.size() != 1) {
//                throw new RuntimeException("邀请注册异常，学生所在班级的老师有" + teacherList.size() + "位");
//            } else {
//                Subject subject = teacherList.get(0).getSubject() == Subject.ENGLISH ? Subject.MATH : Subject.ENGLISH;
//                // 检查被邀请老师姓名
//                MapMessage mesg = businessTeacherServiceClient.nameValidator(teacherName);
//                if (!mesg.isSuccess()) {
//                    return mesg;
//                }
//
//                // 验证邮箱
//                mesg = businessTeacherServiceClient.emailValidator(teacherEmail);
//                if (!mesg.isSuccess()) {
//                    return mesg;
//                }
//
//                // 生成邀请地址
//                String link = ProductConfig.getMainSiteBaseUrl() + "/ucenter/titemail.vpage?url=" + businessTeacherServiceClient.encryptCodeGenerator(student.getId(), teacherEmail, subject.toString());
//
//                // 发送邮件
//                String emailSubject = StringUtils.defaultString(student.getProfile().getRealname()) + "邀请您免费使用一起作业";
//                Map<String, Object> content = new LinkedHashMap<>();
//                content.put("inviterName", student.getProfile().getRealname());
//                content.put("inviteeName", teacherName);
//                content.put("url", link);
//                content.put("date", DateUtils.dateToString(new Date(), "yyyy年MM月dd日 HH时"));
//                emailServiceClient.createTemplateEmail(EmailTemplate.studentinviteteacher)
//                        .to(teacherEmail)
//                        .subject(emailSubject)
//                        .content(content)
//                        .send();
//
//                return MapMessage.successMessage();
//            }
//
//        } catch (Exception e) {
//            logger.error(e.getMessage(), e);
//        }
//
//        return MapMessage.errorMessage("邮件邀请失败");
//    }
//
//    // ========================================================================
//    // private method
//    // ========================================================================
//
//    private void sendSmsToTeacher(String teacherMobile, String payload, MapMessage mesg, SmsType smsType) {
//        Date startDate = DateUtils.nextDay(new Date(), -7);
//        String teacherSensitiveMobile = sensitiveUserDataServiceClient.encodeMobile(teacherMobile);
//
//        // 如果学生一周之内发送过短信，则返回
//        List<StudentInviteSendLog> studentLogList = businessStudentServiceClient.loadStudentInviteSendLogBySenderIdAndCreateDatetime(currentUserId(), startDate);
//        if (studentLogList.size() > 0) {
//            mesg.setSuccess(false);
//            mesg.setInfo("您本周已发送过邀请短信，请下周再发");
//            return;
//        }
//
//        // 如果老师一周之内接收过短信，则返回
//        List<StudentInviteSendLog> teacherLogList = businessStudentServiceClient.loadStudentInviteSendLogByMobileAndCreateDatetime(teacherSensitiveMobile, startDate);
//        if (teacherLogList.size() > 0) {
//            mesg.setSuccess(false);
//            mesg.setInfo("您邀请的老师本周已收到过邀请短信了, 您可以选择给其他老师发送邀请。");
//            return;
//        }
//
//        smsServiceClient.createSmsMessage(teacherMobile)
//                .content(payload)
//                .type(smsType)
//                .send();
//    }
//
//    @RequestMapping(value = "myinvitation.vpage", method = RequestMethod.GET)
//    @ResponseBody
//    public MapMessage getMyInvitationUrl() {
//        User user = currentUser();
//        if (user == null) {
//            return MapMessage.errorMessage("用户未登录");
//        }
//
//        Long userId = user.getId();
//        String defaultDesKey = ConfigManager.instance().getCommonConfig().getConfigs().get("default_des_key");
//        if (defaultDesKey == null) {
//            throw new ConfigurationException("No 'default_des_key' configured");
//        }
//        String inviteCode = DesUtils.encryptHexString(defaultDesKey, userId.toString());
//
//        String myUrl = ProductConfig.getMainSiteBaseUrl() + "/ucenter/invite.vpage?invitation=" + inviteCode;
//        // 新浪短链接无法被QQ分享，无语
////        String shortUrl = HttpUtils.generateSinaShortUrl(myUrl);
////        if (shortUrl == null) {
////            logger.warn("Generate sina short url failed, using original url:" + myUrl);
////        } else {
////            myUrl = shortUrl;
////        }
//
//        return MapMessage.successMessage().add("inviteUrl", myUrl);
//    }
}
