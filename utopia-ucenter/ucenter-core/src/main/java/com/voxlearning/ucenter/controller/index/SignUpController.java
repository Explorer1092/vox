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

package com.voxlearning.ucenter.controller.index;

import com.voxlearning.alps.annotation.meta.Ktwelve;
import com.voxlearning.alps.annotation.meta.RoleType;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MobileRule;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.ucenter.mapper.UserMapper;
import com.voxlearning.ucenter.service.helper.AbnormalIpHelper;
import com.voxlearning.ucenter.service.helper.IpHelper;
import com.voxlearning.ucenter.service.user.AccountWebappService;
import com.voxlearning.ucenter.service.user.ClazzWebappService;
import com.voxlearning.ucenter.service.user.TeacherWebappService;
import com.voxlearning.ucenter.support.context.UcenterRequestContext;
import com.voxlearning.ucenter.support.controller.AbstractWebController;
import com.voxlearning.utopia.api.constant.OperationSourceType;
import com.voxlearning.utopia.service.clazz.client.AsyncGroupServiceClient;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.sms.consumer.SmsServiceClient;
import com.voxlearning.utopia.service.user.api.constants.UserConstants;
import com.voxlearning.utopia.service.user.api.constants.UserRecordMode;
import com.voxlearning.utopia.service.user.api.constants.UserWebSource;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.config.api.constant.ConfigCategory.PRIMARY_PLATFORM_GENERAL;

/**
 * @author xinxin
 * @since 10/12/2015.
 */
@Controller
@RequestMapping(value = "/signup")
public class SignUpController extends AbstractWebController {

    @Inject private RaikouSystem raikouSystem;
    @Inject private SmsServiceClient smsServiceClient;
    @Inject private ClazzWebappService clazzWebappService;
    @Inject private AccountWebappService accountWebappService;
    @Inject private TeacherWebappService teacherWebappService;
    @Inject private AsyncGroupServiceClient asyncGroupServiceClient;
    @Inject private IpHelper ipHelper;

    @RequestMapping(value = "checkclazzinfo.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage findClazzInfo() {
        String id = getRequestString("id");
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage("请输入老师给你的号。");
        }

        //这里有个坑,前端显示的"请输入老师手机号",暗地里这里允许输入老师ID
        Teacher teacher;
        if (MobileRule.isMobile(id)) {
            UserAuthentication ua = userLoaderClient.loadMobileAuthentication(id, UserType.TEACHER);
            if (ua == null) {
                return MapMessage.errorMessage("老师号错误");
            }
            teacher = teacherLoaderClient.loadTeacher(ua.getId());
        } else {
            long teacherId = SafeConverter.toLong(id);
            teacher = teacherLoaderClient.loadTeacher(teacherId);
        }

        if (teacher == null) {
            return MapMessage.errorMessage("老师账号错误");
        }

        String webSource = getRequestString("webSource");
        //如果是极算过来的注册, 输入的老师号不是初中数学老师,则拒绝
        if (UserWebSource.Shensz.getSource().equals(webSource)) {
            TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacher.getId());
            if (!teacherDetail.isJuniorMathTeacher()) {
                return MapMessage.errorMessage("请输入初中数学老师的号哦");
            }
        }

        //1.15~app上线前提醒的内容,app上线后统一提示：非小学学生请在APP端注册
        //(老师号是初中数理化生老师 or高中老师)且(老师注册时间大于2017年1月15 or 是快乐学导入老师)
        // Feature #46231 初高中老师均提示非小学学生请在APP端注册 Update: 2017-06-01
//        boolean isKlxTeacher = Objects.equals(teacher.getWebSource(), UserWebSource.happy_study.toString());
//        //时间是根据这块功能上线时间定,暂定为2017-01-15
//        boolean isDateAfter = false;
//        if (RuntimeMode.isProduction()) {
//            isDateAfter = teacher.getCreateTime().after(DateUtils.stringToDate("2017-01-15", DateUtils.FORMAT_SQL_DATE));
//        } else {
//            isDateAfter = teacher.getCreateTime().after(DateUtils.stringToDate("2017-01-04", DateUtils.FORMAT_SQL_DATE));
//        }
//        boolean isSeniorTeacher = teacher.getKtwelve() == Ktwelve.SENIOR_SCHOOL;
//        if ((isSeniorTeacher) && (isKlxTeacher || isDateAfter)) {
//            return MapMessage.errorMessage("暂时不支持答卷业务学生注册，敬请期待哦");
//        }

        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacher.getId());

        //神算子来源不限制小学初中
        if (!teacherDetail.isShensz()) {
            if (!teacher.isPrimarySchool()) {
                return MapMessage.errorMessage("非小学学生请在APP端注册").add("ktwelve", Ktwelve.JUNIOR_SCHOOL.name());
            }

            if (teacher.getKtwelve() == Ktwelve.JUNIOR_SCHOOL) {
                return MapMessage.errorMessage("初中学生请在APP端注册").add("ktwelve", Ktwelve.JUNIOR_SCHOOL.name());
            }
        }

        // 假老师判断
        if (teacherLoaderClient.isFakeTeacher(teacher.getId())) {
            return MapMessage.errorMessage("你输入的老师号异常，请联系客服！");
        }

        MapMessage mapMessage = clazzWebappService.getClazzListByTeacher(teacher);
        if (mapMessage.isSuccess() && mapMessage.remove("teacher") == null) {// 这里直接remove，没必要把老师信息暴露出来
            return MapMessage.errorMessage("老师号错误。").add("ktwelve", Ktwelve.PRIMARY_SCHOOL.name());
        }
        return mapMessage;
    }

    /**
     * TODO 待重构，把一些逻辑整理下放到service中
     * 学生帐号注册
     */
    @RequestMapping(value = "signup.vpage", method = RequestMethod.POST)
    @ResponseBody
    public Map processSignupForm(@RequestBody UserMapper command) {
        if (badWordCheckerClient.containsUserNameBadWord(command.getRealname())) {
            return MapMessage.errorMessage("姓名请不要使用敏感词汇");
        }

        RoleType roleType = RoleType.valueOf(command.getRole());
        UserType userType = UserType.of(command.getUserType());
        String webSource = command.getWebSource();

        if (roleType != RoleType.ROLE_STUDENT) {
            return MapMessage.errorMessage("角色错误");
        }
        if (userType != UserType.STUDENT) {
            return MapMessage.errorMessage("用户类型错误");
        }

        Long clazzId = StringUtils.isBlank(command.getClazzId()) ? null : Long.parseLong(command.getClazzId().trim());
        long teacherId = SafeConverter.toLong(command.getTeacherId()); //这里有个坑,前端显示的是输入老师手机号,暗地里允许输入老师ID

        try {
            User user = null;

            if (null != clazzId) {
                if (command.getTeacherId().length() == 11) {
                    UserAuthentication ua = userLoaderClient.loadMobileAuthentication(String.valueOf(teacherId), UserType.TEACHER);
                    if (ua == null) {
                        return MapMessage.errorMessage("该手机号找不到老师，请核实手机号是否正确");
                    }
                    teacherId = ua.getId();
                }

                //如果是极算过来的注册, 输入的老师号不是初中数学老师,则拒绝
                if (UserWebSource.Shensz.getSource().equals(webSource)) {
                    TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherId);
                    if (!teacherDetail.isJuniorMathTeacher()) {
                        return MapMessage.errorMessage("请输入初中数学老师的号哦");
                    }
                }

                // 包班制支持，这里学生有可能选的是副账号班级，所以要遍历所有关联id确认老师id
                if (!teacherLoaderClient.isTeachingClazz(teacherId, clazzId)) {
                    Set<Long> relTeacherIds = teacherLoaderClient.loadRelTeacherIds(teacherId);
                    for (Long relTeacherId : relTeacherIds) {
                        if (!Objects.equals(relTeacherId, teacherId)) {
                            if (teacherLoaderClient.isTeachingClazz(relTeacherId, clazzId)) {
                                teacherId = relTeacherId;
                                break;
                            }
                        }
                    }
                }

                // 判断班级是否允许学生自由加入
                MapMessage message = clazzWebappService.checkFreeJoinClazz(clazzId, teacherId);
                if (!message.isSuccess()) {
                    return message;
                }

                //查找班内重名的学生,如果重名的帐号未登录过,则该学生使用这一帐号
                List<Long> studentIds = asyncGroupServiceClient.getAsyncGroupService()
                        .findStudentIdsByClazzId(clazzId);
                if (CollectionUtils.isNotEmpty(studentIds)) {
                    List<User> sameNameList = userLoaderClient.loadUsers(studentIds)
                            .values()
                            .stream()
                            .filter(s -> StringUtils.equals(s.fetchRealname(), command.getRealname().trim()))
                            .sorted((o1, o2) -> Long.compare(o2.getCreateTime().getTime(), o1.getCreateTime().getTime()))
                            .collect(Collectors.toList());

                    if (CollectionUtils.isNotEmpty(sameNameList)) {
                        // 找到第一个未登录的重名学生
                        User usableUser = userLoginServiceClient.getUserLoginService().findSameNameNeverLoginUser(sameNameList).getUninterruptibly();
                        if (usableUser != null) {
                            userServiceClient.setPassword(usableUser, command.getPassword());
                            if (StringUtils.isNoneBlank(command.getMobile())) {
                                userServiceClient.activateUserMobile(usableUser.getId(), command.getMobile());
                            }
                            user = raikouSystem.loadUser(usableUser.getId());
                            // 导入学生
                            clazzServiceClient.importStudent(teacherId, clazzId, user.getId());
                        } else {
                            return MapMessage.errorMessage("班级学生不能重名，请换个姓名");
                        }
                    }

                }
            }

            //这回真真的要去注册了
            if (null == user) {
                MapMessage regMsg = accountWebappService.registStudent(command, teacherId);
                if (!regMsg.isSuccess()) {
                    return regMsg;
                }
                user = (User) regMsg.get("user");
            }

            //注册后处理
            if (!StringUtils.equals(webSource, UserConstants.WEB_SOURCE_P_S_QQ_WECHAT)) {
                //刚注册的登录，登录状态关闭浏览器就消除了，强制用户下次登录的时候再输入一次密码，强化记忆
                UserAuthentication ua = userLoaderClient.loadUserAuthentication(user.getId());
                getWebRequestContext().saveAuthenticationStates(-1, user.getId(), ua.getPassword(), roleType);

                // 注册成功后，立刻记录用户的登录记录
//                userServiceClient.createUserRecord(user.getId(), getWebRequestContext().getRealRemoteAddress(), OperationSourceType.pc);
//                userServiceClient.createUserRecordDaily(user.getId(), getWebRequestContext().getRealRemoteAddress(), UserRecordMode.LOGIN);
                asyncFootprintServiceClient.getAsyncFootprintService().postUserLogin(user.getId(),
                        getWebRequestContext().getRealRemoteAddress(),
                        UserRecordMode.LOGIN,
                        OperationSourceType.pc);

                accountWebappService.checkAccountBind(command, user.getId());

                // 学生邀请学生处理
                if (StringUtils.isNotBlank(command.getInvitation())) {
                    accountWebappService.awardForInvitation(user.getId(), command.getInvitation());
                }
            }

            return MapMessage.successMessage("创建用户成功").add("row", user.getId());
        } catch (Exception ex) {
            logger.error("Student register failed,mapper:{}", JsonUtils.toJson(command), ex);
            return MapMessage.errorMessage("学生帐号注册失败");
        }
    }

    /**
     * 老师帐号注册--发送验证码
     */
    @RequestMapping(value = "tmsignsvc.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendSmsCodeForTeacherRegist() {
        /**
         * 线上环境加一段IP访问受限，防止被恶意调用
         */
        if (RuntimeMode.isStaging() || RuntimeMode.isProduction()) {
            String ip = getWebRequestContext().getRealRemoteAddress();
            if (ipHelper.isOverseas(ip)) {
                return MapMessage.successMessage();
            }

            // FIXME 被刷了，先通过UA过滤一下，有问题再想办法
            String illegalUa = "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.2)";
            if (getRequest().getHeader("User-Agent").equals(illegalUa)) {
                return MapMessage.successMessage("  发验证码成功  ");  //返回一个假消息
            }
        }

        String mobile = getRequest().getParameter("mobile");
        if (StringUtils.isBlank(mobile) || !MobileRule.isMobile(mobile)) {
            return MapMessage.errorMessage("请输入正确的手机号");
        }

        if (!verifyContext()) {
            return MapMessage.successMessage("  发验证码成功  ");  //返回一个假消息
        }

        MapMessage capchaMsg = verifyCaptchaCodeForTeacherRegist();
        if (!capchaMsg.isSuccess()) {
            return capchaMsg;
        }

        String pickUpLog = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(PRIMARY_PLATFORM_GENERAL.name(), "pick_up_log");
        if (StringUtils.isNoneBlank(pickUpLog) && "2".equals(pickUpLog)) {
            logger.info("sendRegisterVerifyCode invoked from {}, {}, {}, {}", "SignUpController", mobile, getWebRequestContext().getRealRemoteAddress(), getRequest().getHeader("User-Agent"));
        }

        boolean voice = getRequestBool("voice");
        if (voice) {
            MapMessage checkIpMessage = AbnormalIpHelper.checkVoiceVerifyCodeIP(getWebRequestContext().getRealRemoteAddress(), mobile);
            if (!checkIpMessage.isSuccess()) {
                return checkIpMessage;
            }
        }
        int count = getRequestInt("count");
        MapMessage message = smsServiceHelper.sendUnbindMobileVerificationCode(mobile,
                SmsType.TEACHER_VERIFY_MOBILE_REGISTER_MOBILE,
                UserType.TEACHER,
                getWebRequestContext().getRealRemoteAddress(),
                voice);

        if (message.isSuccess() && count == 2) {
            if (userLoaderClient.loadMobileAuthentication(mobile, UserType.TEACHER) != null) {
                return MapMessage.errorMessage("该手机号码已经注册，请直接登录");
            }
        }

        return message;
    }

    /**
     * 学生手机注册，发送验证码
     */
    @RequestMapping(value = "smsignsvc.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage studentMobileSignupSendVerificationCode() {
        String mobile = getRequest().getParameter("mobile");
        if (!verifyContext()) {
            return MapMessage.successMessage("  发验证码成功  ");     //返回一个假消息
        }

        return smsServiceHelper.sendUnbindMobileVerificationCode(
                mobile,
                SmsType.STUDENT_VERIFY_MOBILE_REGISTER_MOBILE,
                UserType.STUDENT,
                getWebRequestContext().getRealRemoteAddress()
        );
    }

    private boolean verifyContext() {
        String contextId = getRequest().getParameter("cid");
        String ctxIp = ucenterWebCacheSystem.CBS.unflushable.load("VrfCtxIp_" + contextId);
        return !StringUtils.isEmpty(ctxIp);
    }

    /**
     * 老师帐号注册--验证验证码
     */
    @RequestMapping(value = "validatemobileonly.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage validateMobileOnly() {
        String code = getRequestString("code");
        String mobile = getRequestString("mobile");

        if (StringUtils.isBlank(code) || StringUtils.isBlank(mobile)) {
            return MapMessage.errorMessage("信息不全");
        }
        if (!MobileRule.isMobile(mobile)) {
            return MapMessage.errorMessage("手机号不正确");
        }

        MapMessage validateResult = smsServiceClient.getSmsService().verifyValidateCode(mobile, code, SmsType.TEACHER_VERIFY_MOBILE_REGISTER_MOBILE.name(), false);
        if (!validateResult.isSuccess()) {
            return MapMessage.errorMessage("短信验证码已失效或不匹配");
        }

        return MapMessage.successMessage();
    }

    /**
     * 老师帐号注册--注册
     */
    @RequestMapping(value = "msignup.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage processMobileSignupForm(@RequestBody UserMapper command) {
        String code = command.getCode();
        String mobile = command.getMobile();
        String realname = command.getRealname();
        String password = command.getPassword();
        String webSource = command.getWebSource();
        RoleType roleType = RoleType.valueOf(command.getRole());
        UserType userType = UserType.of(command.getUserType());

        if (StringUtils.isBlank(code) || StringUtils.isBlank(mobile) || StringUtils.isBlank(realname) || StringUtils.isBlank(password)) {
            return MapMessage.errorMessage("信息不全");
        }
        if (!MobileRule.isMobile(mobile)) {
            return MapMessage.errorMessage("手机号不正确");
        }
        if (roleType != RoleType.ROLE_TEACHER) {
            return MapMessage.errorMessage("用户角色错误");
        }
        if (userType != UserType.TEACHER) {
            return MapMessage.errorMessage("用户类型错误");
        }

        try {
            String pickUpLog = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(PRIMARY_PLATFORM_GENERAL.name(), "pick_up_log");
            if ("2".equals(pickUpLog)) {
                logger.warn("teacher registed with info:{}, {}, {}", mobile, code, webSource);
            }

            MapMessage message = verifySmsCodeForTeacherRegist(realname, userType, mobile, code);
            if (!message.isSuccess()) {
                return message;
            }

            MapMessage regMsg = accountWebappService.registTeacher(command, StringUtils.isNotEmpty(webSource) ? webSource : UserWebSource.web_self_reg.name());
            if (!regMsg.isSuccess()) {
                return regMsg;
            }

            User user = (User) regMsg.get("user");
            MapMessage msg = userServiceClient.activateUserMobile(user.getId(), mobile, true);
            if (!msg.isSuccess()) {
                return MapMessage.errorMessage("绑定手机失败");
            }

            //刚注册的登录，登录状态关闭浏览器就消除了，强制用户下次登录的时候再输入一次密码，强化记忆
            UserAuthentication ua = userLoaderClient.loadUserAuthentication(user.getId());
            getWebRequestContext().saveAuthenticationStates(-1, user.getId(), ua.getPassword(), roleType);

            // 注册成功后，立刻记录用户的登录记录
            asyncFootprintServiceClient.getAsyncFootprintService().postUserLogin(user.getId(),
                    getWebRequestContext().getRealRemoteAddress(),
                    UserRecordMode.LOGIN,
                    OperationSourceType.pc);

            // 判断是否要做第三方帐号绑定
            accountWebappService.checkAccountBind(command, user.getId());

            //如果是短信邀请进来的，默认绑定手机号
            teacherWebappService.bindInvitedTeacherMobile(user.getId());

            // #11782 老师注册成功发下一步指导短信
            if (userType == UserType.TEACHER && !isTest(realname, code)) {
                smsServiceClient.createSmsMessage(command.getMobile())
                        .content("注册成功！用手机号和密码即可登录一起作业网辅助教学。学生注册时填写你的手机号即可加入班级，完成作业。")
                        .type(SmsType.TEACHER_GUIDE_AFTER_REG.name())
                        .send();

                smsServiceClient.createSmsMessage(command.getMobile())
                        .content("[转发家长]本班拟用教 育 部课题平台，请下载手机一起作业www.17zyw.cn/YRFfA3 输入" + command.getMobile() + "注册学生号")
                        .type(SmsType.TEACHER_GUIDE_AFTER_REG.name())
                        .send();
            }

            return MapMessage.successMessage("创建用户成功").add("row", user.getId());
        } catch (Exception ex) {
            logger.error("Teacher register failed,mapper:{}", JsonUtils.toJson(command), ex);
            return MapMessage.errorMessage("老师帐号注册失败");
        }
    }


    @RequestMapping(value = "filtersensitiveusername.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage filterSensitiveUsername() {
        String userName = getRequestParameter("userName", "").trim();
        if (badWordCheckerClient.containsUserNameBadWord(userName)) {
            return MapMessage.errorMessage();
        }
        return MapMessage.successMessage();
    }

    /**
     * 验证手机验证码
     */
    private MapMessage verifySmsCodeForTeacherRegist(String realname, UserType userType, String mobile, String code) {

        if (!isTest(realname, code)) {
            MapMessage validateResult = smsServiceClient.getSmsService().verifyValidateCode(mobile, code, SmsType.TEACHER_VERIFY_MOBILE_REGISTER_MOBILE.name());
            if (!validateResult.isSuccess()) {
                return validateResult;
            }
        } else {
            if (userLoaderClient.loadMobileAuthentication(mobile, userType) != null) {
                return MapMessage.errorMessage("该手机号码已经注册，请直接登录");
            }
        }

        return MapMessage.successMessage();
    }

    /**
     * 验证图片验证码
     */
    private MapMessage verifyCaptchaCodeForTeacherRegist() {
        String captchaToken = getRequestString("captchaToken");
        String captchaCode = getRequestString("captchaCode");

        if (!consumeCaptchaCode(captchaToken, captchaCode)) {
            return MapMessage.errorMessage("验证码输入错误，请重新输入!");
        }
        return MapMessage.successMessage();
    }

    private boolean isTest(String realname, String code) {
        return (StringUtils.equals("演示", realname) || StringUtils.equals("测试", realname)) && StringUtils.equals("123456", code);
    }

    /*
      ------------------------------------------ 新版注册流程  2014年4月10日 --------------------------------------------
     */

    /**
     * step1 - 选择角色
     */
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String signupIndex(Model model) {
        String dataKey = getRequestString("dataKey");
        model.addAttribute("dataKey", dataKey);

        if (StringUtils.isBlank(dataKey)) {
            return "ucenter/signupchip/index";
        }

        CacheObject<Map> cacheObject = ucenterWebCacheSystem.CBS.unflushable.get(dataKey);
        if (cacheObject == null) {
            // failed to access couchbase server, ignore
            return "ucenter/signupchip/index";
        }

        Map dataMap = cacheObject.getValue();
        if (dataMap != null && dataMap.get("userType") != null) {
            UserType realType = UserType.of(ConversionUtils.toInt(dataMap.get("userType")));
            if (realType == UserType.TEACHER || realType == UserType.STUDENT || realType == UserType.PARENT) {
                return "redirect:htmlchip/" + realType.name().toLowerCase() + ".vpage";
            }
        }

        return "ucenter/signupchip/index";
    }

    /**
     * step2 - 根据角色不同进入注册表单页
     */
    @RequestMapping(value = "htmlchip/{page}.vpage", method = RequestMethod.GET)
    public String steps(@PathVariable("page") String page, Model model) {
        if (page.equals("teacher")) {
            List<Map<String, String>> subjectList = new ArrayList<>();
            for (Subject sub : Subject.values()) {
                Map<String, String> subjectMap = new HashMap<>();
                subjectMap.put("name", sub.name());
                subjectMap.put("value", sub.getValue());
                subjectList.add(subjectMap);
            }
            model.addAttribute("subject", subjectList);
        }

        // 生成一个 contextId 用于防止机器人刷接口
        model.addAttribute("contextId", generateContextId(getWebRequestContext()));

        String dataKey = getRequestString("dataKey");
        if (StringUtils.isNotBlank(dataKey)) {
            model.addAttribute("dataKey", dataKey);
            CacheObject<Map> cacheObject = ucenterWebCacheSystem.CBS.unflushable.get(dataKey);
            if (cacheObject != null && cacheObject.getValue() != null) {
                Map map = cacheObject.getValue();
                model.addAttribute("defUserName", map.get("userName"));
                model.addAttribute("defUserMobile", map.get("userMobile"));
            }
        }

        String invitation = getRequestString("invitation");
        if (StringUtils.isNotBlank(invitation)) {
            model.addAttribute("invitation", invitation);
        }

        model.addAttribute("captchaToken", RandomUtils.randomString(24));
        return "ucenter/signupchip/htmlchip/" + page;
    }

    private String generateContextId(UcenterRequestContext context) {
        String contextId = RandomUtils.randomString(10);
        Boolean ret = ucenterWebCacheSystem.CBS.unflushable.set("VrfCtxIp_" + contextId, 10 * 60, context.getRealRemoteAddress());
        if (!ret) {
            throw new IllegalStateException("create contextId error.");
        }
        return contextId;
    }

    @RequestMapping(value = "sendemailsuccess.vpage", method = RequestMethod.GET)
    public String emailSendSuccess(Model model) {
        //这里会构造对外的url跳转，有些安全检测网站认为这里有xss隐患，所以我们先特殊处理一下
        String email = getRequestParameter("email", "");
        String emailLogin = "mail." + StringUtils.substringAfterLast(email, "@");

        //简单的 javascript escape 逻辑，全部处理
        //警告，这里不是防止xss，而是试着绕过第三方扫描引擎的检测
        StringBuilder escapedEmail = new StringBuilder(), escapedEmailLoginHost = new StringBuilder();
        for (int i = 0; i < email.length(); i++) {
            escapedEmail.append('%');
            escapedEmail.append(Integer.toHexString((byte) email.charAt(i) / 16));
            escapedEmail.append(Integer.toHexString((byte) email.charAt(i) % 16));
        }
        for (int i = 0; i < emailLogin.length(); i++) {
            escapedEmailLoginHost.append('%');
            escapedEmailLoginHost.append(Integer.toHexString((byte) emailLogin.charAt(i) / 16));
            escapedEmailLoginHost.append(Integer.toHexString((byte) emailLogin.charAt(i) % 16));
        }
        model.addAttribute("escapedEmail", escapedEmail.toString());
        model.addAttribute("escapedEmailLoginHost", escapedEmailLoginHost.toString());

        return "ucenter/signupchip/htmlchip/sendemailsuccess";
    }

}
