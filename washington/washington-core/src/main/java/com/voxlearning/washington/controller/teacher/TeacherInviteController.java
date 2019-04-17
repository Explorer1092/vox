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


import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MobileRule;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.api.constant.AmbassadorLevel;
import com.voxlearning.utopia.api.constant.AmbassadorReportType;
import com.voxlearning.utopia.api.constant.UserServiceRecordOperationType;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.mapper.ActivateInfoMapper;
import com.voxlearning.utopia.mapper.ActivateMapper;
import com.voxlearning.utopia.service.ambassador.api.document.AmbassadorLevelDetail;
import com.voxlearning.utopia.service.ambassador.api.document.AmbassadorReportInfo;
import com.voxlearning.utopia.service.ambassador.api.document.AmbassadorSchoolRef;
import com.voxlearning.utopia.service.ambassador.client.AmbassadorLoaderClient;
import com.voxlearning.utopia.service.clazz.client.GroupLoaderClient;
import com.voxlearning.utopia.service.email.api.constants.EmailTemplate;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.footprint.client.UserActivityServiceClient;
import com.voxlearning.utopia.service.invitation.client.AsyncInvitationServiceClient;
import com.voxlearning.utopia.service.newhomework.api.entity.StudentHomeworkStat;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.user.api.constants.InvitationType;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupTeacherMapper;
import com.voxlearning.utopia.service.user.client.AsyncTeacherServiceClient;
import com.voxlearning.washington.mapper.UserMapper;
import com.voxlearning.washington.mapper.specialteacher.base.SpecialTeacherConstants;
import com.voxlearning.washington.support.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.alps.annotation.meta.AuthenticationState.SUCCESS;
import static com.voxlearning.utopia.api.constant.OperationSourceType.pc;
import static com.voxlearning.utopia.api.legacy.MemcachedKeyConstants.*;
import static com.voxlearning.utopia.service.user.api.constants.InvitationType.TEACHER_INVITE_TEACHER_SMS;
import static com.voxlearning.utopia.service.user.api.constants.UserActivityType.LAST_HOMEWORK_TIME;

@Controller
@RequestMapping("/teacher/invite")
public class TeacherInviteController extends AbstractController {

    @Inject private RaikouSDK raikouSDK;

    @Inject private AsyncInvitationServiceClient asyncInvitationServiceClient;
    @Inject private AsyncTeacherServiceClient asyncTeacherServiceClient;
    @Inject private UserActivityServiceClient userActivityServiceClient;
    @Inject private AmbassadorLoaderClient ambassadorLoaderClient;
    @Inject private EmailServiceClient emailServiceClient;
    @Inject private GroupLoaderClient groupLoaderClient;

    // ========================================================================
    // 老师邀请老师
    // ========================================================================

    // #63920 该区域已参加活动但未获得奖励的老师，不再计入奖励计算。
    private static final List<Integer> blackRegionCode = Arrays.asList(
            230102, 230103, 230104, 230106, 230108
    );

    @RequestMapping(value = "getCaptchaToken.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getCaptchaToken() {
        return MapMessage.successMessage().add("captchaToken", RandomUtils.randomString(24));
    }

    /**
     * 邀请老师PC端
     */
    @RequestMapping(value = "inviteteacherbysms.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage teacherInviteTeacherBySmsNew() {

        String invitedTeacherName = getRequestString("invitedTeacherName");
        String invitedTeacherMobile = getRequestString("invitedTeacherMobile");
        String captchaToken = getRequestString("captchaToken");
        String captchaCode = getRequestString("captchaCode");

        TeacherDetail teacher = currentTeacherDetail();
        // 邀请小学数学或者语文老师活动
        if (teacher.isPrimarySchool()) {
            Long mainTeacherId = teacherLoaderClient.loadMainTeacherId(teacher.getId());
            if (mainTeacherId != null) {
                return MapMessage.errorMessage("活动只支持包班制主账号");
            }
        }
        // 初中英语老师邀请英语老师活动
        if (teacher.isJuniorTeacher()) {
            if (!teacher.isEnglishTeacher()) {
                return MapMessage.errorMessage("活动只支持英语老师参加");
            }
            if (blackRegionCode.contains(teacher.getRegionCode())) {
                return MapMessage.errorMessage("该活动不支持当前地区");
            }
        }

        if (!SpecialTeacherConstants.checkChineseName(invitedTeacherName, 6)) {
            return MapMessage.errorMessage("姓名只支持不超过五个字的中文和姓名符·");
        }

        // 检查验证码
        if (!consumeCaptchaCode(captchaToken, captchaCode)) {
            return MapMessage.errorMessage("验证码错误！");
        }

        if (!MobileRule.isMobile(invitedTeacherMobile)) {
            return MapMessage.errorMessage("手机号格式不正确！");
        }

        // 老师每天发送的邀请次数不能超过20次
        List<InviteHistory> histories = asyncInvitationServiceClient.getAsyncInvitationService()
                .queryTodayRecordByUserId(teacher.getId()).getUninterruptibly();
        if (histories.size() > 20) {
            return MapMessage.errorMessage("每天最多发出20个邀请，先去帮助已邀请的老师注册吧！");
        }

        // 验证此账号是否已在平台注册过
        UserAuthentication userAuthentication = userLoaderClient.loadMobileAuthentication(invitedTeacherMobile, UserType.TEACHER);
        if (Objects.nonNull(userAuthentication)) {
            return MapMessage.errorMessage("该手机号已注册一起作业，请换个老师邀请吧！");
        }

        InviteHistory history = asyncInvitationServiceClient.getAsyncInvitationService()
                .queryByUserIdInviteMobile(teacher.getId(), invitedTeacherMobile)
                .getUninterruptibly();
        if (Objects.nonNull(history)) {
            return MapMessage.errorMessage("您已邀请过该老师，试试邀请别人吧！");
        }

        // 向invite_history中添加邀请老师ID和被邀请老师手机号
        InviteHistory inviteHistory = InviteHistory.newInstance();
        inviteHistory.setUserId(teacher.getId());
        inviteHistory.setInviteSensitiveMobile(invitedTeacherMobile);
        inviteHistory.setInviteeTeacherName(invitedTeacherName);
        inviteHistory.setInvitationType(InvitationType.TEACHER_INVITE_TEACHER_SMS);
        Boolean flag = asyncInvitationServiceClient.getAsyncInvitationService()
                .createHistory(inviteHistory)
                .getUninterruptibly();
        if (!flag) {
            return MapMessage.errorMessage("系统错误，请稍后再试！");
        }

        String content = teacher.fetchRealname();
        if (content.endsWith("老师")) {
            content = content + "邀请您使用智能教学好助手一起作业（www.17zuoye.com），注册时输入邀请人ID：" + teacher.getId() + "，互联网精英教师就是您！";
        } else {
            content = content + "老师邀请您使用智能教学好助手一起作业（www.17zuoye.com），注册时输入邀请人ID：" + teacher.getId() + "，互联网精英教师就是您！";
        }

        smsServiceClient.createSmsMessage(invitedTeacherMobile).content(content).type(SmsType.TEACHER_SMS_INVITE_TEACHER.name()).send();
        return MapMessage.successMessage("已向该老师发送邀请短信，请当面提醒该老师，注册时记得填写您的ID哦！给予新老师帮助，可以更快得到邀请奖励！");
    }

    /**
     * 邀请老师App端
     */
    @RequestMapping(value = "inviteteacherbylink.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage teacherInviteTeacherByLinkNew() {
        TeacherDetail teacher = currentTeacherDetail();
        // 邀请小学数学/语文老师活动
        if (teacher.isPrimarySchool()) {
            Long mainTeacherId = teacherLoaderClient.loadMainTeacherId(teacher.getId());
            if (mainTeacherId != null) {
                return MapMessage.errorMessage("活动只支持包班制主账号");
            }
        }

        // 初中英语老师邀请英语老师活动
        if (teacher.isJuniorTeacher()) {
            if (!teacher.isEnglishTeacher()) {
                return MapMessage.errorMessage("活动只支持英语老师参加");
            }
        }

        // 老师每天发送的邀请次数不能超过20次
        List<InviteHistory> histories = asyncInvitationServiceClient.getAsyncInvitationService()
                .queryTodayRecordByUserId(teacher.getId()).getUninterruptibly();
        if (histories.size() > 20) {
            return MapMessage.errorMessage("每天最多发出20个邀请，先去帮助已邀请的老师注册吧！");
        }

        // 生成邀请地址
        String link = "";
        if (teacher.isPrimarySchool()) {
            // 数学老师邀请地址
            link = ProductConfig.getMainSiteBaseUrl() + "/view/mobile/teacher/activity/othercoming?param=" + teacher.getId();
            // 新增语文老师邀请地址
            if ("primaryChinese".equals(getRequestString("activeSign"))) {
                link = ProductConfig.getMainSiteBaseUrl() + "/view/mobile/teacher/activity/otherchinesecoming?param=" + teacher.getId();
            }
        } else if (teacher.isJuniorTeacher()) {
            link = ProductConfig.getMainSiteBaseUrl() + "/view/mobile/teacher/activity/junior/othercoming?param=" + teacher.getId();
        }

        return MapMessage.successMessage().add("link", link).add("realName", StringUtils.defaultString(teacher.getProfile().getRealname()));
    }


    /**
     * 邀请人查看活动进度
     */
    @RequestMapping(value = "getActivityProcess.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getActivityProcess() {
        MapMessage message = MapMessage.successMessage();

        Teacher teacher = currentTeacher();
        message.add("authenticate", teacher.fetchCertificationState().name());

        List<InviteHistory> histories = asyncInvitationServiceClient.getAsyncInvitationService()
                .queryByUserId(teacher.getId()).getUninterruptibly();

        int invitedTeacherNum = 0;
        int inviteFailedNum = 0;
        int totalAwardNum;
        List<Map<String, String>> successList = new ArrayList<>();
        List<Map<String, String>> failureList = new ArrayList<>();
        int awardNum = 0;

        if (histories != null && !histories.isEmpty()) {
            message.add("inviteDate", DateUtils.dateToString(histories.get(histories.size() - 1).getCreateTime(), "yyyy-MM-dd HH:mm"));
        }
        String activeSign = getRequestString("activeSign");
        for (InviteHistory history : histories) {
            Map<String, String> map = new HashMap<>();
            map.put("inviteDate", DateUtils.dateToString(history.getCreateTime(), "yyyy-MM-dd HH:mm"));
            map.put("inviteTeacherName", history.getInviteeTeacherName());
            String sensitiveMobile = sensitiveUserDataServiceClient.loadInviteMobileObscured(history.getId());
            map.put("inviteTeacherMobile", sensitiveMobile);
            if (Objects.nonNull(history.getInviteeUserId())) {
                Teacher t = teacherLoaderClient.loadTeacher(history.getInviteeUserId());
                map.put("inviteTeacherName", t.fetchRealname());// 如果已经注册了则取注册时的姓名
                if ((t.isPrimarySchool() && (t.isMathTeacher() || t.isChineseTeacher())) || (t.isJuniorTeacher() && t.isEnglishTeacher())) {//老师认证的是小学数学老师#60601 或初中英语老师#61859
                    map.put("invitedAuthenticate", t.fetchCertificationState() == AuthenticationState.SUCCESS ? "已认证" : "未认证");
                    int finishHomeworkStudentNum = getFinishHomeworkStudentNum(history.getInviteeUserId());
                    map.put("finishHomeworkStudentNum", ConversionUtils.toString(finishHomeworkStudentNum));
                    // 邀请语文老师的认证时间是15天
                    int lastDays = this.getLastDays(history.getCreateTime(), "primaryChinese".equals(activeSign) ? 15 : 30);
                    map.put("lastDays", ConversionUtils.toString(lastDays));
                    if (history.getIsChecked() == 9) {
                        map.put("award", "20元");
                        awardNum++;
                    } else {
                        map.put("award", "0元");
                    }

                    invitedTeacherNum++;
                    successList.add(map);
                } else {
                    map.put("inviteStatus", "未补全信息");
                    if (t.isPrimarySchool()) {
                        if ("primaryChinese".equals(activeSign)) {
                            map.put("inviteStatus", "被邀请老师注册为非小学语文老师");
                        }
                    } else if (t.isJuniorTeacher()) {
                        map.put("inviteStatus", "被邀请老师注册为非初中英语老师");
                    }

                    inviteFailedNum++;
                    failureList.add(map);
                }
            } else {
                UserAuthentication userAuthentication = userLoaderClient.loadMobileAuthentication(history.getInviteSensitiveMobile(), UserType.TEACHER);
                if (Objects.nonNull(userAuthentication)) {
                    map.put("inviteStatus", "邀请失败！<br/>（未填写邀请ID）");
                    Teacher t = teacherLoaderClient.loadTeacher(userAuthentication.getId());
                    Teacher teacher1 = teacherLoaderClient.loadTeacher(history.getUserId());
                    map.put("inviteTeacherName", t.fetchRealname());// 如果已经注册了则取注册时的姓名
                    if (teacher1.isPrimarySchool() && !t.isPrimarySchool() && (!t.isMathTeacher() || t.isChineseTeacher())) {
                        map.put("inviteStatus", "被邀请老师注册为非小学数学老师");
                        if ("primaryChinese".equals(getRequestString("activeSign"))) {
                            map.put("inviteStatus", "被邀请老师注册为非小学语文老师");
                        }
                    } else if (teacher1.isJuniorTeacher() && !t.isJuniorTeacher() && !t.isEnglishTeacher()) {
                        map.put("inviteStatus", "被邀请老师注册为非初中英语老师");
                    }
                } else {
                    map.put("inviteStatus", "未注册");
                }

                inviteFailedNum++;
                failureList.add(map);
            }
        }

        totalAwardNum = awardNum * 20;

        return message.add("invitedTeacherNum", invitedTeacherNum)
                .add("totalAwardNum", totalAwardNum)
                .add("inviteFailedNum", inviteFailedNum)
                .add("successList", successList)
                .add("failureList", failureList);
    }

    /**
     * app端被邀请人接受到消息查看活动进度
     */
    @RequestMapping(value = "getInvitedTeacherAcProcess.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getInvitedTeacherAcProcess() {

        Teacher teacher = currentTeacher();
        String authenticate = teacher.fetchCertificationState().name();
        int finishHomeworkStudentNum = getFinishHomeworkStudentNum(teacher.getId());
        return MapMessage.successMessage().add("authenticate", authenticate)
                .add("finishHomeworkStudentNum", finishHomeworkStudentNum);

    }

    private int getFinishHomeworkStudentNum(Long teacherId) {
        Set<Long> sids = newHomeworkLoaderClient.getStudentHomeworkStatByTeacherId(teacherId)
                .stream()
                .filter(source -> source.getNormalHomeworkCount() >= 3)
                .map(StudentHomeworkStat.DataMapper::getStudentId)
                .collect(Collectors.toSet());
        return sids.size();
    }

    private int getLastDays(Date date, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int day = calendar.get(Calendar.DAY_OF_YEAR);
        int endDay = day + days;
        Date now = new Date();
        calendar.setTime(now);
        int today = calendar.get(Calendar.DAY_OF_YEAR);
        return (endDay - today) < 0 ? 0 : (endDay - today);
    }

    private boolean isSameClazzSharedGroup(InviteHistory history) {
        // 被邀请人与邀请人在同一个clazz
        List<Long> groupIds = raikouSDK.getClazzClient().getGroupTeacherTupleServiceClient().getTeacherGroupIds(history.getUserId());
        List<Long> invitedGroupIds = raikouSDK.getClazzClient().getGroupTeacherTupleServiceClient().getTeacherGroupIds(history.getInviteeUserId());

        Set<String> groupParents = groupLoaderClient.getGroupLoader().loadGroups(groupIds).getUninterruptibly()
                .values().stream().filter(p -> p.getGroupParent() != null && !Objects.equals(p.getGroupParent(), ""))
                .map(Group::getGroupParent).collect(Collectors.toSet());
        Set<String> invitedGroupParents = groupLoaderClient.getGroupLoader().loadGroups(invitedGroupIds).getUninterruptibly()
                .values().stream().filter(p -> p.getGroupParent() != null && !Objects.equals(p.getGroupParent(), ""))
                .map(Group::getGroupParent).collect(Collectors.toSet());
        groupParents.retainAll(invitedGroupParents);
        if (invitedGroupParents.size() <= 0) {
            return false;
        }

        return true;
    }

    /**
     * 老师邀请首页
     */
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String teacherInviteTeacherDistributor(Model model) {
        TeacherDetail teacher = currentTeacherDetail();
        if (teacher.getSubject() == null || teacher.getKtwelve() == null || asyncTeacherServiceClient.getAsyncTeacherService()
                .loadTeacherSchool(teacher.getId()).getUninterruptibly() == null) {
            return "redirect:/teacher/index.vpage";
        }

        model.addAttribute("captchaToken", RandomUtils.randomString(24));
        model.addAttribute("allowInviteChinese", true);
        return "teacherv3/invite/indexupgrade";
    }

    /**
     * 老师邀请老师--短信邀请
     */
    @RequestMapping(value = "sms.vpage", method = RequestMethod.POST)
    @ResponseBody
    public Map teacherInviteTeacherBySms() throws Exception {
        try {
            TeacherDetail teacher = currentTeacherDetail();

            String mobile = getRequestParameter("mobile", "");
            String realname = getRequestParameter("realname", "");
            String captchaToken = getRequestParameter("captchaToken", "");
            String captchaCode = getRequestParameter("captchaCode", "");
            Subject subject = Subject.ofWithUnknown(getRequestString("subject"));
            Set<Long> groupIds = Arrays.stream(StringUtils.split(getRequestString("groupIds"), ",")).map(ConversionUtils::toLong).collect(Collectors.toSet());
            String schoolName = getRequestString("schoolName");

            // 检查验证码
            if (!consumeCaptchaCode(captchaToken, captchaCode)) {
                return MapMessage.errorMessage("验证码输入错误，请重新输入。").set("value", "codeFalse");
            }

            return atomicLockManager.wrapAtomic(deprecatedInvitationServiceClient)
                    .keyPrefix("TEACHER_INVITE_TEACHER_SMS")
                    .keys(teacher.getId(), mobile, realname)
                    .proxy()
                    .invite(teacher, mobile, realname, subject, groupIds, schoolName, TEACHER_INVITE_TEACHER_SMS, pc);
        } catch (DuplicatedOperationException e) {
            return MapMessage.successMessage("邀请成功");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return MapMessage.errorMessage("邀请失败");
    }

    @RequestMapping(value = "groups.vpage", method = RequestMethod.POST)
    @ResponseBody
    public Map loadInivterGroups() throws Exception {
        Long teacherId = currentUserId();

        List<GroupTeacherMapper> groups = deprecatedGroupLoaderClient.loadTeacherGroups(teacherId, false);
        Set<Long> clazzIds = groups.stream().map(GroupTeacherMapper::getClazzId).collect(Collectors.toSet());
        Map<Long, Clazz> clazzs = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazzs(clazzIds)
                .stream()
                .collect(Collectors.toMap(Clazz::getId, Function.identity()));

        List<Map<String, Object>> results = new ArrayList<>();
        for (GroupTeacherMapper group : groups) {
            if (!group.isTeacherGroupRefStatusValid(teacherId)) continue;

            Clazz clazz = clazzs.get(group.getClazzId());
            if (clazz == null) continue;
            if (clazz.isTerminalClazz()) continue;

            Map<String, Object> map = new HashMap<>();
            map.put("clazzName", clazz.formalizeClazzName());
            map.put("groupId", group.getId());
            results.add(map);
        }

        return MapMessage.successMessage().add("clazzs", results);
    }

    /**
     * 老师邀请老师--链接邀请
     */
    @RequestMapping(value = "web.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage teacherInviteTeacherByLink() throws Exception {
        try {
            Teacher teacher = currentTeacher();
            String subject = getRequest().getParameter("subject");
            // 检查是不是老师
            if (UserType.of(teacher.getUserType()) != UserType.TEACHER) {
                return MapMessage.errorMessage("此功能只针对老师开放");
            }
            // 生成邀请地址
            String link = ProductConfig.getMainSiteBaseUrl() + "/ucenter/titlink.vpage?url=" + businessTeacherServiceClient.encryptCodeGenerator(teacher.getId(), null, subject);
            return MapMessage.successMessage().add("link", link).add("realName", StringUtils.defaultString(teacher.getProfile().getRealname()));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage();
        }
    }

    /**
     * 老师邀请老师--邮件邀请
     */
    @RequestMapping(value = "email.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage teacherInviteTeacherByEmail(@RequestBody UserMapper command) throws Exception {
        MapMessage mesg = new MapMessage();
        try {
            User teacher = currentUser();
            // 检查是不是老师
            if (UserType.of(teacher.getUserType()) != UserType.TEACHER) {
                return MapMessage.errorMessage("此功能只针对老师开放");
            }
            // 检查被邀请老师姓名
            mesg = businessTeacherServiceClient.nameValidator(command.getRealname());
            if (!mesg.isSuccess()) {
                return mesg;
            }
            // 验证邮箱
            mesg = businessTeacherServiceClient.emailValidator(command.getEmail());
            if (!mesg.isSuccess()) {
                return mesg;
            }
            // 验证学科
            if (StringUtils.isBlank(command.getSubject()) || (Subject.of(command.getSubject()) != Subject.ENGLISH && Subject.of(command.getSubject()) != Subject.MATH)) {
                return MapMessage.errorMessage("请正确填写您所邀请教师的学科");
            }
            // 生成邀请地址
            String link = ProductConfig.getMainSiteBaseUrl() + "/ucenter/titemail.vpage?url=" + businessTeacherServiceClient.encryptCodeGenerator(teacher.getId(), command.getEmail(), command.getSubject());
            // 发送邮件
            String subject = StringUtils.defaultString(teacher.getProfile().getRealname()) + "邀请您免费使用一起作业";
            Map<String, Object> content = new LinkedHashMap<>();
            content.put("inviterName", teacher.getProfile().getRealname());
            content.put("inviterId", teacher.getId());
            content.put("inviteeName", command.getRealname());
            content.put("url", link);
            content.put("date", DateUtils.dateToString(new Date(), "yyyy年MM月dd日 HH时"));

            emailServiceClient.createTemplateEmail(EmailTemplate.emailmodle)
                    .to(command.getEmail())
                    .subject(subject)
                    .content(content)
                    .send();

            mesg.setSuccess(true);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            mesg.setSuccess(false);
        }
        return mesg;
    }

    /**
     * 发送提醒认证短信
     */
    @RequestMapping(value = "notify.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage notifyAuthencate() {
        return businessTeacherServiceClient.sendAuthenticateNotify(currentUser(), getRequestLong("inviteeId"));
    }

    // ========================================================================
    // 老师激活老师
    // ========================================================================

    /**
     * 2015暑假改版 -- 有奖互助   大使专区已经转移
     */
    @RequestMapping(value = "activateteacher.vpage", method = RequestMethod.GET)
    public String schoolAmbassadorActivateTeacherIndex(Model model) {
        TeacherDetail teacher = currentTeacherDetail();
        try {
            /************************************** 普通教师和校园大使共用部分 *******************************************/

            School school = asyncTeacherServiceClient.getAsyncTeacherService()
                    .loadTeacherSchool(teacher.getId())
                    .getUninterruptibly();
            if (school == null) {
                return "redirect:/teacher/index.vpage";
            }
            model.addAttribute("school", school);
            AmbassadorSchoolRef ref = ambassadorLoaderClient.getAmbassadorLoader().findSameSubjectAmbassadorInSchool(teacher.getSubject(), school.getId());
            model.addAttribute("hasAmbassador", ref != null);

            MapMessage mesg = businessTeacherServiceClient.personalStatisticOfTeacherActivateTeacher(teacher.getId());
            if (mesg.isSuccess()) {
                model.addAttribute("pcount", mesg.get("pcount"));
                model.addAttribute("icount", mesg.get("icount"));
            }

            // 缓存1小时，当执行激活操作，删除激活操作，检查作业操作时清除缓存
            model.addAttribute("teacherList", washingtonCacheSystem.CBS.flushable
                    .wrapCache(businessTeacherServiceClient)
                    .expiration(3600)
                    .keyPrefix(TEACHER_ACTIVATE_TEACHER)
                    .keys(teacher.getId())
                    .proxy()
                    .getPotentialTeacher(teacher));
            if (teacher.isSchoolAmbassador()) {
                //校园大使级别
                AmbassadorLevelDetail levelDetail = ambassadorLoaderClient.getAmbassadorLoader().loadAmbassadorLevelDetail(teacher.getId());
                if (levelDetail == null) {
                    levelDetail = new AmbassadorLevelDetail();
                    levelDetail.setAmbassadorId(currentUserId());
                    levelDetail.setLevel(AmbassadorLevel.SHI_XI);
                }
                model.addAttribute("ambassadorLevel", levelDetail);
            } else {
                model.addAttribute("activatingCount", businessTeacherServiceClient.getActivatingCount(teacher.getId()));
            }
        } catch (Exception e) {
            logger.error("Error occurs in teacher activate teacher index, ", e);
        }

        return "teacherv3/invite/activateteacher";
    }

    /**
     * 唤醒老师
     */
    @RequestMapping(value = "teacheractivateteacher.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage teacherActivateTeacher(@RequestBody ActivateMapper mapper) {
        try {
            TeacherDetail teacher = currentTeacherDetail();
            // 目前一次只能选择一个
            if (null == mapper || null == mapper.getUserList() || mapper.getUserList().size() != 1) {
                return MapMessage.errorMessage("请重新选择教师。");
            }
            Long targetId = mapper.getUserList().get(0).getUserId();
            try {
                return atomicLockManager.wrapAtomic(businessTeacherServiceClient)
                        .keys(targetId)
                        .proxy()
                        .activateTeacher(teacher, mapper);
            } catch (Exception ex) {
                if (ex instanceof DuplicatedOperationException) {
                    return MapMessage.successMessage("正在处理，请不要重复提交");
                }
                logger.error("ERROR OCCURS WHEN ACTIVATING TEACHER {}, THE ERROR MESSAGE IS: {}", targetId, ex.getMessage(), ex);
                return MapMessage.errorMessage("发送邀请失败，请重新选择教师");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage("发送邀请失败，请重新选择教师");
        }
    }

    // 认证教师举报老师
    @RequestMapping(value = "reportTeacher.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage reportTeacher() {
        TeacherDetail teacher = currentTeacherDetail();
        if (teacher == null || teacher.fetchCertificationState() != SUCCESS) {
            return MapMessage.errorMessage("没有权限");
        }
        Long teacherId = getRequestLong("teacherId");
        String teacherName = getRequestParameter("teacherName", "");
        String reason = getRequestParameter("reason", "");
        Integer type = getRequestInt("type");
        if (StringUtils.isBlank(teacherName) || StringUtils.isBlank(reason)) {
            return MapMessage.errorMessage("参数错误");
        }
        if (AmbassadorReportType.typeOf(type) == null) {
            return MapMessage.errorMessage("类型错误");
        }
        if (Objects.equals(teacher.getId(), teacherId)) {
            return MapMessage.errorMessage("不能举报自己");
        }
        // 7日之内不能举报该老师
        Date beginDate = DateUtils.calculateDateDay(new Date(), -7);
        List<AmbassadorReportInfo> reportInfos = ambassadorLoaderClient.getAmbassadorLoader().loadAmbassadorReportInfoIgnoreDisabled(teacher.getId(), teacherId, beginDate);
        if (CollectionUtils.isNotEmpty(reportInfos)) {
            return MapMessage.errorMessage("您已申请过，7天后才能再次申请！");
        }
        try {
            MapMessage message = atomicLockManager.wrapAtomic(businessTeacherServiceClient)
                    .keys(teacher.getId(), teacherId)
                    .proxy()
                    .reportTeacher(teacher, teacherId, teacherName, reason, AmbassadorReportType.typeOf(type));
            if (message.isSuccess()) {
                // 记录 UserServiceRecord
                UserServiceRecord userServiceRecord = new UserServiceRecord();
                userServiceRecord.setUserId(teacherId);
                userServiceRecord.setOperatorId(teacher.getId().toString());
                userServiceRecord.setOperationType(UserServiceRecordOperationType.举报老师.name());
                userServiceRecord.setOperationContent("举报老师");
                userServiceRecord.setComments("老师[" + teacher.getId() + "]举报[" + teacherId + "]，举报类型[" + AmbassadorReportType.typeOf(type).getDescription() + "]");
                userServiceClient.saveUserServiceRecord(userServiceRecord);
            }
            return message;
        } catch (Exception ex) {
            if (ex instanceof DuplicatedOperationException) {
                return MapMessage.successMessage("正在处理，请不要重复提交");
            }
            logger.error("ERROR OCCURS WHEN REPORT TEACHER {}, THE ERROR MESSAGE IS: {}", teacherId, ex.getMessage());
            return MapMessage.errorMessage("提交失败，请重新操作");
        }
    }

    /**
     * 取消激活老师
     */
    @RequestMapping(value = "delete.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage delete() {
        String historyId = getRequestString("historyId");
        try {
            return atomicLockManager.wrapAtomic(businessTeacherServiceClient)
                    .proxy()
                    .deleteTeacherActivateTeacherHistory(currentUserId(), historyId);
        } catch (Exception ex) {
            if (ex instanceof DuplicatedOperationException) {
                return MapMessage.successMessage("正在处理，请不要重复提交");
            }
            logger.error("ERROR OCCURS WHEN DELETE ACTIVATING TEACHER HISTORY {}, THE ERROR MESSAGE IS: {}", historyId, ex.getMessage(), ex);
            return MapMessage.errorMessage("删除失败，请重新操作");
        }
    }

    /**
     * 正在唤醒中的教师列表
     */
    @RequestMapping(value = "findactivatingteacher.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage findactivatingteacher() {
        Long teacherId = currentUserId();
        try {
            // 缓存半小时，当执行激活操作，删除激活操作，检查作业操作时清除缓存
            List<ActivateInfoMapper> mappers = washingtonCacheSystem.CBS.flushable
                    .wrapCache(businessTeacherServiceClient)
                    .expiration(1800)
                    .keyPrefix(ACTIVATING_TEACHER_LIST)
                    .keys(teacherId)
                    .proxy()
                    .getActivatingTeacher(teacherId);
            return MapMessage.successMessage("获取被唤醒老师成功").add("teacherList", mappers);
        } catch (Exception ex) {
            logger.error("获取被唤醒老师失败，[teacherId:{},msg:{}]", teacherId, ex.getMessage(), ex);
            return MapMessage.errorMessage("获取被唤醒老师失败");
        }
    }

    /**
     * 唤醒成功的教师列表
     */
    @RequestMapping(value = "findactivatedteacher.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage findactivatedteacher() {
        Long teacherId = currentUserId();
        try {
            // 缓存半小时，检查作业操作时清除缓存
            List<ActivateInfoMapper> mappers = washingtonCacheSystem.CBS.flushable
                    .wrapCache(businessTeacherServiceClient)
                    .expiration(1800)
                    .keyPrefix(ACTIVATED_TEACHER_LIST)
                    .keys(teacherId)
                    .proxy()
                    .getActivatedTeacher(teacherId);
            return MapMessage.successMessage("获取被唤醒成功老师完成").add("teacherList", mappers);
        } catch (Exception ex) {
            logger.error("获取被唤醒老师都是失败，[teacherId:{},msg:{}]", teacherId, ex.getMessage(), ex);
            return MapMessage.errorMessage("获取被唤醒老师失败");
        }
    }

    @RequestMapping(value = "findteacher.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage findUnauthenticatedTeacherInTheSameSchool() {
        try {
            TeacherDetail teacher = currentTeacherDetail();
            if (!teacher.isSchoolAmbassador()) {
                return MapMessage.errorMessage("您不是校园大使。");
            }
            String token = getRequest().getParameter("token").trim();
            if (StringUtils.isBlank(token)) {
                return MapMessage.errorMessage("请输入您要查找的教师姓名或者学号。");
            }
            return businessTeacherServiceClient.findUnauthenticatedTeacherInTheSameSchoolByIdOrName(teacher.getId(), token);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage("您查找的教师不存在。");
        }
    }

    @RequestMapping(value = "recommendteacher.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage recommendTeacher() {
        try {
            TeacherDetail teacher = currentTeacherDetail();
            if (!teacher.isSchoolAmbassador()) {
                return MapMessage.errorMessage("您不是校园大使。");
            }
            return businessTeacherServiceClient.recommendTeacherAuthentication(teacher.getId(), getRequestLong("recommendedTeacherId"));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage("推荐失败");
        }
    }

    // 邀请详情
    @RequestMapping(value = "progressjmstjmst.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage fetchInvitationProgress() {
        Long inviterId = currentUserId();
        int pageNumber = getRequestInt("pn", 1);
        Pageable pageable = new PageRequest(pageNumber - 1, 5);

        return MapMessage.successMessage().add("pn", pageNumber)
                .add("pagination", newInvitationServiceClient.fetchInvitationProgress(inviterId, pageable));
    }

    @RequestMapping(value = "getinvitationprocess.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getInvitationProcess() {
        Set<Long> inviteeIds = asyncInvitationServiceClient.loadByInviter(currentUserId())
                .originalLocationsAsList()
                .stream()
                .filter(t -> t.getInviteeId() != 0)
                .sorted((o1, o2) -> Long.compare(o2.getCreateTime(), o1.getCreateTime()))
                .map(InviteHistory.Location::getInviteeId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Long, User> invitees = userLoaderClient.loadUsers(inviteeIds);
        Map<Long, UserAuthentication> uaus = userLoaderClient.loadUserAuthentications(inviteeIds);
        Map<Long, List<Clazz>> clazzs = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(inviteeIds);
        Map<Long, UserActivity> uacm = userActivityServiceClient.getUserActivityService()
                .findUserActivities(inviteeIds)
                .getUninterruptibly()
                .values()
                .stream()
                .map(t -> t.stream()
                        .filter(a -> a.getActivityType() == LAST_HOMEWORK_TIME)
                        .sorted((o1, o2) -> {
                            long a1 = SafeConverter.toLong(o1.getActivityTime());
                            long a2 = SafeConverter.toLong(o2.getActivityTime());
                            return Long.compare(a2, a1);
                        })
                        .findFirst()
                        .orElse(null))
                .filter(t -> t != null)
                .collect(Collectors.toMap(UserActivity::getUserId, t -> t));
        List<Map<String, Object>> historyList = new LinkedList<>();
        for (Long inviteeId : inviteeIds) {
            User invitee = invitees.get(inviteeId);
            if (invitee == null) {
                continue;
            }
            Map<String, Object> map = new HashMap<>();
            map.put("userId", inviteeId);
            map.put("mobile", sensitiveUserDataServiceClient.loadUserMobileObscured(inviteeId));
            map.put("userName", invitee.getProfile().getRealname());
            map.put("isCertificated", invitee.fetchCertificationState() == SUCCESS);
            //只查认证30天以内的老师
            if (invitee.fetchCertificationState() == SUCCESS &&
                    invitee.getLastAuthDate() != null && DateUtils.dayDiff(new Date(), invitee.getLastAuthDate()) <= 30) {
                map.put("studentCount", businessTeacherServiceClient.studentsFinishedHomeworkCount(inviteeId, invitee.getCreateTime()));
            } else {
                map.put("studentCount", 0);
            }
            map.put("hasClazz", CollectionUtils.isNotEmpty(clazzs.get(inviteeId)));
            map.put("checkedHomework", uacm.get(inviteeId) != null);
            historyList.add(map);
        }
        //排序 认证的在前面
        historyList = historyList.stream().sorted((o1, o2) -> {
            int i1 = SafeConverter.toBoolean(o1.get("isCertificated")) ? 1 : 0;
            int i2 = SafeConverter.toBoolean(o2.get("isCertificated")) ? 1 : 0;
            return Integer.compare(i2, i1);
        }).collect(Collectors.toList());
        return MapMessage.successMessage().add("historyList", historyList);
    }

}
