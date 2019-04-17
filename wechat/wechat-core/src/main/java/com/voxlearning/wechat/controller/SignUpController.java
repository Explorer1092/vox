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

package com.voxlearning.wechat.controller;

import com.voxlearning.alps.annotation.meta.RoleType;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MobileRule;
import com.voxlearning.utopia.api.constant.OperationSourceType;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.user.api.constants.CallName;
import com.voxlearning.utopia.service.user.api.constants.UserRecordMode;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.mappers.ClazzTeacher;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.wechat.constants.WechatInfoCode;
import com.voxlearning.wechat.constants.WechatRegisterEventType;
import com.voxlearning.wechat.support.utils.OAuthUrlGenerator;
import com.voxlearning.wechat.support.utils.TokenHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Xin Xin
 * @since 10/15/15
 */

@Controller
@Slf4j
@RequestMapping(value = "/signup")
public class SignUpController extends AbstractChipsController {

    @Inject private TokenHelper tokenHelper;

    @RequestMapping(value = "/index.vpage")
    public String index() {
        return "index";
    }

    //家长端使用学生帐号/手机号+密码登录
    @RequestMapping(value = "/parent/login.vpage", method = RequestMethod.GET)
    public String login(Model model) {
        String returnUrl = getRequestString("returnUrl");

        String openId = getOpenId();
        if (null == openId || openId.length() < 10) {
            return redirectWithMsg("无效的openId", model);
        }
        try {
            User user = wechatLoaderClient.loadWechatUser(openId);
            if (null != user) {
                UserAuthentication ua = userLoaderClient.loadUserAuthentication(user.getId());
                getRequestContext().saveAuthenticateState(24 * 60 * 60, user.getId(), ua.getPassword(), openId, RoleType.of(user.getUserType()));
                //userServiceClient.createUserRecordDaily(user.getId(), getRequestContext().getRealRemoteAddress(), UserRecordMode.LOGIN);
                asyncFootprintServiceClient.getAsyncFootprintService().postUserLogin(user.getId(), getRequestContext().getRealRemoteAddress(), UserRecordMode.LOGIN, OperationSourceType.wechat);
                return StringUtils.isBlank(returnUrl) ? "redirect:/parent/homework/index.vpage" : "redirect:/" + returnUrl;
            }
            model.addAttribute("source", "signUp");
            model.addAttribute("ref", getRequestString("ref"));
            return "/parent/signup/login";
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return redirectWithMsg("系统异常", model);
        }
    }

    @RequestMapping(value = "/parent/login.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loginPost() {
        String token = getRequestString("j_username");
        String password = getRequestString("j_password");

        if (StringUtils.isBlank(token) || StringUtils.isBlank(password)) {
            return MapMessage.errorMessage("参数错误");
        }

        String openId = getOpenId();
        if (null == openId || openId.length() < 10) {
            return MapMessage.errorMessage("openId无效");
        }

        try {
            List<User> users = userLoaderClient.loadUsers(token, null);
            Map userMap = users.stream().collect(Collectors.toMap(User::getUserType, Function.<User>identity(), (u, v) -> {
                throw new IllegalStateException("Duplicate key" + u);
            }, LinkedHashMap::new));

            User user = (User) userMap.get(UserType.STUDENT.getType());
            if (null == user) {
                return MapMessage.errorMessage("请使用学生帐号或手机号登录");
            }
            StudentDetail studentDetail = (user instanceof StudentDetail)
                    ? (StudentDetail) user : studentLoaderClient.loadStudentDetail(user.getId());
            if (studentDetail.isJuniorStudent()) {
                return MapMessage.errorMessage("微信端暂时只支持小学家长使用");
            }
            // 临时密码校验 xuesong.zhang 2015-12-3
            if (StringUtils.isBlank(password) || !StringUtils.equalsIgnoreCase(userLoaderClient.loadUserTempPassword(user.getId()), password)) {
                UserAuthentication ua = userLoaderClient.loadUserAuthentication(user.getId());
                if (!ua.fetchUserPassword().match(password)) {
                    return MapMessage.errorMessage("帐号或密码错误");
                }
            }

            saveCookie(user.getId(), null, null);

            return MapMessage.successMessage();
        } catch (Exception ex) {
            log.error("Login error,token:{},pwd:{}", token, password, ex);
            return MapMessage.errorMessage("学生帐号验证失败");
        }
    }

    //选择身份页
    @RequestMapping(value = "/parent/selectparent.vpage", method = RequestMethod.GET)
    public String callName(Model model) {
        String openId = getOpenId();
        if (null == openId || openId.length() < 10) {
            return redirectWithMsg("openId无效", model);
        }

        Long studentId = null;
        try {
            Optional<Long> optional = getStudentIdFromCookie();
            if (!optional.isPresent()) {
                return infoPage(WechatInfoCode.PARENT_LOGIN_CACHE_EXPIRED, model);
            }
            studentId = optional.get();

            User user = userLoaderClient.loadUser(studentId);
            if (null != user) {
                model.addAttribute("name", user.getProfile().getRealname());
            }
            model.addAttribute("source", "signUp");
        } catch (Exception ex) {
            logger.error("Render choose call name failed,sid:{}", studentId, ex);
        }
        return "/parent/signup/selectparent";
    }

    //查询家长身份是否有帐号
    @RequestMapping(value = "/parent/callname.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage calllNamePost() {
        Integer callNameCode = getRequestInt("callNameCode");
        if (0 == callNameCode) {
            return MapMessage.errorMessage("未知身份");
        }

        CallName callName = CallName.of(callNameCode);
        if (null == callName) {
            return MapMessage.errorMessage("无效身份");
        }

        String openId = getOpenId();
        if (null == openId || openId.length() < 10) {
            return MapMessage.errorMessage("openId无效");
        }

        Long studentId = null;
        try {
            Optional<Long> optional = getStudentIdFromCookie();
            if (!optional.isPresent()) {
                return MapMessage.errorMessage("未查询到孩子帐号,请返回重试");
            }
            studentId = optional.get();

            Optional<Long> parentId = userService.getParentByCallName(studentId, callName.getValue());
            if (parentId.isPresent()) {
                saveCookie(studentId, parentId.get(), callName.getKey());
            } else {
                saveCookie(studentId, null, null);
            }
            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error("sid:{},callName:{}", studentId, callName, ex);
        }
        return MapMessage.errorMessage();
    }

    //选择身份后验证手机页
    //返回model:
    //  parentId不存在:选择的身份没有帐号
    //  parentId存在,mobile不存在:选择的身份有帐号,没绑手机
    //  parentId存在,mobile存在:选择的身份有帐号,已绑手机
    @RequestMapping(value = "/parent/verify.vpage", method = RequestMethod.GET)
    public String verifyWithCallName(Model model) {
        Integer callNameCode = getRequestInt("callNameCode");
        if (0 == callNameCode) {
            return redirectWithMsg("未知身份", model);
        }
        CallName callName = CallName.of(callNameCode);
        if (null == callName) {
            return redirectWithMsg("无效身份", model);
        }

        String openId = getOpenId();
        if (null == openId || openId.length() < 10) {
            return redirectWithMsg("openId无效", model);
        }

        Long studentId = null;
        Long parentId = null;
        try {
            Optional<Long> optional = getStudentIdFromCookie();
            if (!optional.isPresent()) {
                return infoPage(WechatInfoCode.PARENT_LOGIN_CACHE_EXPIRED, model); //studentId是必须有的
            }
            studentId = optional.get();
            optional = getParentIdFromCookie(callName.getKey());
            if (optional.isPresent()) { //选择的身份下是有帐号的
                parentId = optional.get();
                String userPhone = sensitiveUserDataServiceClient.loadUserMobileObscured(parentId);
                if (StringUtils.isNoneBlank(userPhone)) {
                    model.addAttribute("mobile", userPhone);
                }
            }

            User student = userLoaderClient.loadUser(studentId);
            if (null != student) {
                model.addAttribute("name", student.getProfile().getRealname());
            }

            model.addAttribute("cid", tokenHelper.generateContextId(getRequestContext()));
            model.addAttribute("callName", callName);
            model.addAttribute("source", "signUp");
        } catch (Exception ex) {
            logger.error("Verify with callName failed,pid:{},sid:{}", parentId, studentId, ex);
        }

        return "/parent/signup/verify";
    }

    //验证手机,并做一堆事情
    //  1 选择的身份没有帐号->注册新帐号,绑定手机,关联孩子->登录成功
    //  2 选择的身份有帐号,但未绑手机-> 绑定手机->登录成功
    //  3 选择的身份有帐号,已绑手机-> 直接登录成功
    @RequestMapping(value = "/parent/verify.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage verifySms() {
        Integer callNameCode = getRequestInt("callNameCode");
        if (0 == callNameCode) {
            return MapMessage.errorMessage("未知身份");
        }
        CallName callName = CallName.of(callNameCode);
        if (null == callName) {
            return MapMessage.errorMessage("未知身份");
        }

        String code = getRequestString("code");
        String mobile = getRequestString("mobile");
        if (StringUtils.isBlank(code) || StringUtils.isBlank(mobile) || !MobileRule.isMobile(mobile)) {
            return MapMessage.errorMessage("参数错误");
        }

        String openId = getOpenId();
        if (null == openId || openId.length() < 10) {
            return MapMessage.errorMessage("openId无效");
        }

        Long studentId = null;
        Long parentId = null;
        try {
            Optional<Long> optional = getStudentIdFromCookie();
            if (!optional.isPresent()) {
                return MapMessage.errorMessage("未查询到孩子帐号,请返回重试"); //studentId是必须有的
            }
            studentId = optional.get();


            MapMessage message = userService.verifySmsCode(mobile, code, WechatType.PARENT);
            if (!message.isSuccess()) {
                return message;
            }

            //老师和家长不能使用同一个手机号，如果家长号已经和孩子绑定了，就不做这个验证了
            if (!mobileAlreadyBindByParent(studentId, mobile)) {
                if (mobileAlreadyBindByTeacher(studentId, mobile)) {
                    return MapMessage.errorMessage("该手机号已被老师绑定");
                }
            }

            optional = getParentIdFromCookie(callName.getKey());
            User parent;
            if (!optional.isPresent()) {
                //选择的身份没有帐号,先检查一下手机号是否已被家长绑定,若没绑需要注册新帐号/绑定手机/关联孩子/绑定微信
                UserAuthentication uaParent = userLoaderClient.loadMobileAuthentication(mobile, UserType.PARENT);
                if (null != uaParent) {
                    parent = userLoaderClient.loadUser(uaParent.getId());
                    //检查一下这个帐号是否已和孩子关联
                    boolean alrealdyRef = false;
                    List<StudentParentRef> studentParentRefs = studentLoaderClient.loadStudentParentRefs(studentId);
                    if (!CollectionUtils.isEmpty(studentParentRefs)) {
                        for (StudentParentRef ref : studentParentRefs) {
                            if (Objects.equals(ref.getParentId(), parent.getId()) && !ref.getCallName().equals(callName.getValue())) {
                                if (StringUtils.isBlank(ref.getCallName())) {
                                    parentServiceClient.setParentCallName(ref.getParentId(), ref.getStudentId(), callName);
                                    alrealdyRef = true;
                                    break;
                                } else {
                                    return MapMessage.errorMessage("手机号已被其它身份的家长号占用");
                                }
                            }
                        }
                    }
                    //最多关联3个孩子
                    if (!alrealdyRef) {
                        List<User> children = studentLoaderClient.loadParentStudents(parent.getId());
                        if (!CollectionUtils.isEmpty(children) && children.size() >= 3) {
                            return MapMessage.errorMessage("此家长号已关联3个孩子,请联系客服关联更多孩子");
                        }
                    }
                } else {
                    MapMessage regMessage = userService.registParentByMobile(mobile);
                    if (!regMessage.isSuccess()) {
                        return regMessage;
                    }
                    parent = (User) regMessage.get("user");

                    log(callName, mobile, studentId, parent.getId());
                }

                userService.bindStudentToParent(studentId, parent.getId(), callName.getValue());
            } else {
                parentId = optional.get();
                //选择的身份有帐号,检查一下有没有绑手机
                List<UserAuthentication> authentications = userLoaderClient.loadMobileAuthentications(mobile);
                boolean mobileBinded = false;
                if (!CollectionUtils.isEmpty(authentications)) {
                    for (UserAuthentication authentication : authentications) {
                        if (Objects.equals(authentication.getId(), parentId)) {
                            mobileBinded = true;
                            break;
                        }
                    }
                }

                if (!mobileBinded) {
                    //还未绑手机,绑定之
                    MapMessage msg = userServiceClient.activateUserMobile(parentId, mobile);
                    if (!msg.isSuccess()) {
                        return MapMessage.errorMessage("手机绑定失败");
                    }
                }
                parent = userLoaderClient.loadUser(parentId);
            }

            if (null != parent) {
                userService.bindParentWithReward(parent.getId(), studentId, openId, "wechat_login");
                //设置登录cookie
                UserAuthentication ua = userLoaderClient.loadUserAuthentication(parent.getId());
                getRequestContext().saveAuthenticateState(24 * 60 * 60, parent.getId(), ua.getPassword(), openId, RoleType.ROLE_PARENT);
                //userServiceClient.createUserRecordDaily(parent.getId(), getRequestContext().getRealRemoteAddress(), UserRecordMode.LOGIN);
                asyncFootprintServiceClient.getAsyncFootprintService().postUserLogin(parent.getId(), getRequestContext().getRealRemoteAddress(), UserRecordMode.LOGIN, OperationSourceType.wechat);
                return MapMessage.successMessage();
            } else {
                return MapMessage.errorMessage("没有家长帐号");
            }
        } catch (Exception ex) {
            logger.error("Verify with callName failed,pid:{},sid:{},code:{},mobile:{}", parentId, studentId, code, mobile, ex);
            return MapMessage.errorMessage("系统异常");
        }
    }

    // 已验证家长通过[手机+验证码]登录 by wyc 2015-12-23
    @RequestMapping(value = "/parent/verifiedlogin.vpage", method = RequestMethod.GET)
    public String verifiedLogin(Model model) {
        // 如果这个验证家长未验证，跳转到验证登录页面
        String returnUrl = getRequestString("returnUrl");

        String openId = getOpenId();
        if (null == openId || openId.length() < 10) {
            return redirectWithMsg("无效的openId", model);
        }

        try {
            User user = wechatLoaderClient.loadWechatUser(openId);
            if (null != user) {
                UserAuthentication ua = userLoaderClient.loadUserAuthentication(user.getId());
                getRequestContext().saveAuthenticateState(-1, user.getId(), ua.getPassword(), openId, RoleType.of(user.getUserType()));
                //userServiceClient.createUserRecordDaily(user.getId(), getRequestContext().getRealRemoteAddress(), UserRecordMode.LOGIN);
                asyncFootprintServiceClient.getAsyncFootprintService().postUserLogin(user.getId(), getRequestContext().getRealRemoteAddress(), UserRecordMode.LOGIN, OperationSourceType.wechat);
                return StringUtils.isBlank(returnUrl) ? "redirect:/parent/homework/index.vpage" : "redirect:/" + returnUrl;
            }
            model.addAttribute("cid", tokenHelper.generateContextId(getRequestContext()));
            model.addAttribute("source", "signUp");
            return "/parent/signup/mobilelogin";
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return redirectWithMsg("系统异常", model);
        }
    }

    // 已验证家长登录发送手机验证码 by wyc 2016-01-04
    @RequestMapping(value = "/parent/verifiedlogin/sendsmscode.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendSmsCode() {
        String mobile = getRequestString("mobile");
        String contextId = getRequestString("cid");

        try {
            if (!MobileRule.isMobile(mobile) || StringUtils.isBlank(contextId)) {
                return MapMessage.errorMessage("参数错误");
            }

            // 检查手机号是否已经认证
            UserAuthentication user = userLoaderClient.loadMobileAuthentication(mobile, UserType.PARENT);
            if (null == user) {
                return MapMessage.errorMessage("请家长先绑定手机号");
            }

            if (!tokenHelper.verifyContextId(contextId)) {
                return MapMessage.errorMessage("您发送的太频繁了,请稍侯再试~");
            }

            return smsServiceClient.getSmsService().sendUnbindMobileVerificationCode(
                    mobile,
                    SmsType.PARENT_VERIFY_MOBILE_WEIXIN_REGISTER.name(),
                    false);
        } catch (Exception ex) {
            log.error("Send sms code for parent mobile bind failed,mobile:{},contextId:{}", mobile, contextId, ex);
            return MapMessage.errorMessage("发送失败");
        }
    }

    @RequestMapping(value = "/parent/verifiedlogin.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage verifiedLoginPost() {
        String mobileNumber = getRequestString("mobile");
        String verifyCode = getRequestString("code");

        String openId = getOpenId();
        if (null == openId || openId.length() < 10) {
            return MapMessage.errorMessage("openId无效");
        }

        if (!MobileRule.isMobile(mobileNumber) || StringUtils.isBlank(verifyCode)) {
            return MapMessage.errorMessage("请输入正确的手机号与验证码");
        }

        try {
            // 检查手机号是否已经认证
            UserAuthentication user = userLoaderClient.loadMobileAuthentication(mobileNumber, UserType.PARENT);
            if (null == user) {
                return MapMessage.errorMessage("请家长先绑定手机号");
            }

            // 验证短信验证码
            MapMessage verifySmsMessage = userService.verifySmsCode(mobileNumber, verifyCode, WechatType.PARENT);
            if (!verifySmsMessage.isSuccess()) {
                return verifySmsMessage;
            }

            User parent = userLoaderClient.loadUser(user.getId());

            if (null != parent) {
                wechatServiceClient.bindUserAndWechat(parent.getId(), openId, "parent_mobile_login", WechatType.PARENT.getType());
                //设置登录cookie
                UserAuthentication ua = userLoaderClient.loadUserAuthentication(parent.getId());
                getRequestContext().saveAuthenticateState(24 * 60 * 60, parent.getId(), ua.getPassword(), openId, RoleType.ROLE_PARENT);
                //userServiceClient.createUserRecordDaily(user.getId(), getRequestContext().getRealRemoteAddress(), UserRecordMode.LOGIN);
                asyncFootprintServiceClient.getAsyncFootprintService().postUserLogin(user.getId(), getRequestContext().getRealRemoteAddress(), UserRecordMode.LOGIN, OperationSourceType.wechat);
                return MapMessage.successMessage();
            } else {
                return MapMessage.errorMessage("没有家长帐号");
            }
        } catch (Exception ex) {
            log.error("Verified Login Failed, mobile:{},code:{}", mobileNumber, verifyCode, ex);
            return MapMessage.errorMessage("登录失败");
        }
    }

    private void log(CallName callName, String mobile, Long studentId, Long parentId) {
        Map<String, String> log = new HashMap<>();
        log.put("module", "signup");
        log.put("op", "regist");
        log.put("parentId", String.valueOf(parentId));
        log.put("studentId", studentId.toString());
        log.put("callName", callName.getValue());
        log.put("mobile", mobile);
        super.log(log);
    }

    //发送手机登录验证码（家长端)
    @RequestMapping(value = "/parent/sendsmscode.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendCode() {
        String mobile = getRequestString("mobile");
        String contextId = getRequestString("cid");

        if (!MobileRule.isMobile(mobile) || StringUtils.isBlank(contextId)) {
            return MapMessage.errorMessage("参数错误");
        }

        try {
            Optional<Long> studentId = getStudentIdFromCookie();
            if (!studentId.isPresent()) {
                return MapMessage.errorMessage("未查询到学生帐号");
            }

            if (!tokenHelper.verifyContextId(contextId)) {
                return MapMessage.errorMessage("发送失败，请稍候再试");
            }

            //老师和家长不能使用同一个手机号，如果家长号已经和孩子绑定了，就不做这个验证了
            if (!mobileAlreadyBindByParent(studentId.get(), mobile)) {
                if (mobileAlreadyBindByTeacher(studentId.get(), mobile)) {
                    return MapMessage.errorMessage("该手机号已被老师绑定");
                }
            }
            return smsServiceClient.getSmsService().sendUnbindMobileVerificationCode(
                    mobile,
                    SmsType.PARENT_VERIFY_MOBILE_WEIXIN_REGISTER.name(),
                    false
            );
        } catch (Exception ex) {
            log.error("Send code for mobile login failed,mobile:{},contextId:{}", mobile, contextId, ex);
            return MapMessage.errorMessage("发送失败");
        }
    }

    /**
     * 判断一下手机号是否已被此学生的老师绑定过
     *
     * @return true 手机号已被老师绑定; false 手机号未被老师绑定
     */
    private boolean mobileAlreadyBindByTeacher(Long studentId, String mobile) {
        Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(studentId);
        if (null == clazz) return false;

        List<UserAuthentication> userAuthentications = userLoaderClient.loadMobileAuthentications(mobile);
        if (CollectionUtils.isEmpty(userAuthentications)) return false;

        Optional<UserAuthentication> teacherAuthentication = userAuthentications.stream().filter(ua -> ua.getUserType() == UserType.TEACHER).findFirst();
        if (!teacherAuthentication.isPresent()) return false;

        List<ClazzTeacher> teachers = teacherLoaderClient.loadClazzTeachers(clazz.getId());
        if (CollectionUtils.isEmpty(teachers)) return false;

        Optional<ClazzTeacher> clazzTeacher = teachers.stream().filter(t -> Objects.equals(t.getTeacher().getId(), teacherAuthentication.get().getId())).findFirst();
        return clazzTeacher.isPresent();
    }

    /**
     * 判断一下手机号是否已被此孩子的家长绑定过了
     *
     * @return true 手机号已被家长绑定; false 手机号未被家长绑定
     */
    private boolean mobileAlreadyBindByParent(Long studentId, String mobile) {
        List<StudentParentRef> parentRefs = studentLoaderClient.loadStudentParentRefs(studentId);
        if (CollectionUtils.isEmpty(parentRefs)) return false;
        List<Long> parentIds = parentRefs.stream().map(StudentParentRef::getParentId).collect(Collectors.toList());
        UserAuthentication authentication = userLoaderClient.loadMobileAuthentication(mobile, UserType.PARENT);
        if (authentication == null) return false;
        if (parentIds.contains(authentication.getId())) {
            return true;
        }
        return false;
    }

    //*****************************************************************薯条英语公众号部分*************************************************************//

    // 薯条英语 手机号+验证码 登录 or 注册
    @RequestMapping(value = "/chips/verifiedlogin.vpage", method = RequestMethod.GET)
    public String chipsVerifiedLogin(Model model) {
        String returnUrl = getRequestString("returnUrl");

        String openId = getOpenId();
        if (StringUtils.isBlank(openId)) {
            return redirectWithMsg("无效的openId，请在微信客户端打开链接", model);
        }

        try {
            model.addAttribute("cid", tokenHelper.generateContextId(getRequestContext()));
            model.addAttribute("returnUrl", StringUtils.isBlank(returnUrl) ? "/chips/center/index.vpage" : returnUrl);
            model.addAttribute("source", "signUp");
            return "/parent/chips/mobilelogin";
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return redirectWithMsg("系统异常", model);
        }
    }

    @RequestMapping(value = "/chips/verifiedlogin/sendsmscode.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage chipsSendSmsCode() {
        String mobile = getRequestString("mobile");
        String contextId = getRequestString("cid");

        try {
            if (!MobileRule.isMobile(mobile) || StringUtils.isBlank(contextId)) {
                return MapMessage.errorMessage("参数错误");
            }

            if (!tokenHelper.verifyContextId(contextId)) {
                return MapMessage.errorMessage("您发送的太频繁了,请稍侯再试~");
            }

            return smsServiceClient.getSmsService().sendUnbindMobileVerificationCode(
                    mobile,
                    SmsType.PARENT_VERIFY_MOBILE_CHIPS_WEIXIN_REGISTER.name(),
                    false);
        } catch (Exception ex) {
            log.error("Send sms code for chips login failed,mobile:{},contextId:{}", mobile, contextId, ex);
            return MapMessage.errorMessage("发送失败");
        }
    }

    @RequestMapping(value = "/chips/verifiedlogin.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage chipsVerifiedLoginPost() {
        String mobileNumber = getRequestString("mobile");
        String verifyCode = getRequestString("code");

        String openId = getOpenId();
        if (null == openId || openId.length() < 10) {
            return MapMessage.errorMessage("openId无效");
        }

        if (!MobileRule.isMobile(mobileNumber) || StringUtils.isBlank(verifyCode)) {
            return MapMessage.errorMessage("请输入正确的手机号与验证码");
        }

        try {
            // 验证短信验证码
            MapMessage message = userService.verifySmsCode(mobileNumber, verifyCode, WechatType.CHIPS);
            if (!message.isSuccess()) {
                return message;
            }

            // 检查手机号是否已经认证
            UserAuthentication userAus = userLoaderClient.loadMobileAuthentication(mobileNumber, UserType.PARENT);
            Long userId;
            if (null == userAus) {
                // 注册一个
                message = userService.registParentByMobile(mobileNumber);
                if (!message.isSuccess()) {
                    return message;
                }
                userId = ((User) message.get("user")).getId();
            } else {
                userId = userAus.getId();
            }

            User parent = userLoaderClient.loadUser(userId);
            messageHelper.sendRegister(openId, WechatType.CHIPS, WechatRegisterEventType.LOGIN);
            if (null != parent) {
                wechatServiceClient.bindUserAndWechat(parent.getId(), openId, "parent_chips_wechat_login", WechatType.CHIPS.getType());
                // 设置登录cookie
                UserAuthentication ua = userLoaderClient.loadUserAuthentication(parent.getId());
                getRequestContext().saveAuthenticateState(7 * 24 * 60 * 60, parent.getId(), ua.getPassword(), openId, RoleType.ROLE_PARENT);
                asyncFootprintServiceClient.getAsyncFootprintService().postUserLogin(userId, getRequestContext().getRealRemoteAddress(), UserRecordMode.LOGIN, OperationSourceType.wechat);
                return MapMessage.successMessage();
            } else {
                return MapMessage.errorMessage("没有家长帐号");
            }
        } catch (Exception ex) {
            log.error("Verified Login Failed, mobile:{},code:{}", mobileNumber, verifyCode, ex);
            return MapMessage.errorMessage("登录失败");
        }
    }

    // 重新登录  解绑当前微信
    @RequestMapping(value = "/chips/logout.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage logout() {
        User user = currentChipsUser();
        if (user == null) {
            return MapMessage.errorMessage("请重新登录后操作");
        }
        wechatServiceClient.getWechatService().unbindUserAndWechatWithUserIdAndType(user.getId(), WechatType.CHIPS.getType());
        // 同时清除cookie
        removeUserAndOpenIdFromCookie();
        return MapMessage.successMessage().add("returnUrl", OAuthUrlGenerator.generatorLoginCenterUrlForChips());
    }


    @RequestMapping(value = "/teacher/login.vpage", method = RequestMethod.GET)
    public String teacherLogin() {
        return "/teacher/signup/login";
    }

    @RequestMapping(value = "/teacher/login.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage teacherLogin_Post() {
        String token = getRequestString("token");
        String password = getRequestString("pwd");

        if (StringUtils.isBlank(token) || StringUtils.isBlank(password)) {
            return MapMessage.errorMessage("参数错误");
        }

        try {
            MapMessage msg = authentication(token, password);
            if (!msg.isSuccess()) return msg;

            User user = (User) msg.get("user");
            String openId = getOpenId();
            if (StringUtils.isNotBlank(openId)) { //由wap登录进来是没有openId的
                wechatServiceClient.bindUserAndWechat(user.getId(), openId, "teacher_mobile_login", WechatType.TEACHER.getType());
            }

            //设置登录cookie
            UserAuthentication ua = userLoaderClient.loadUserAuthentication(user.getId());
            getRequestContext().saveAuthenticateState(24 * 60 * 60, user.getId(), ua.getPassword(), openId, RoleType.ROLE_TEACHER);
            //userServiceClient.createUserRecordDaily(user.getId(), getRequestContext().getRealRemoteAddress(), UserRecordMode.LOGIN);
            asyncFootprintServiceClient.getAsyncFootprintService().postUserLogin(user.getId(), getRequestContext().getRealRemoteAddress(), UserRecordMode.LOGIN, OperationSourceType.wechat);
            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error("Teacher login error,token:{},p:{}", token, password, ex);
            return MapMessage.errorMessage("登录失败");
        }
    }

    private MapMessage authentication(String token, String password) {
        List<User> users = userLoaderClient.loadUsers(token, null);
        Map userMap = users.stream().collect(Collectors.toMap(User::getUserType, Function.<User>identity(), (u, v) -> {
            throw new IllegalStateException("Duplicate key" + u);
        }, LinkedHashMap::new));

        User user = (User) userMap.get(UserType.TEACHER.getType());
        if (null == user) {
            return MapMessage.errorMessage("请使用老师帐号或手机号登录");
        }
        School school = asyncTeacherServiceClient.getAsyncTeacherService()
                .loadTeacherSchool(user.getId())
                .getUninterruptibly();
        if (null == school || school.getLevel() != 1) {
            return MapMessage.errorMessage("微信端暂时只支持小学老师使用");
        }

        // 临时密码校验 xuesong.zhang 2015-12-3
        if (StringUtils.isBlank(password) || !StringUtils.equalsIgnoreCase(userLoaderClient.loadUserTempPassword(user.getId()), password)) {
            UserAuthentication ua = userLoaderClient.loadUserAuthentication(user.getId());
            if (!ua.fetchUserPassword().match(password)) {
                return MapMessage.errorMessage("帐号或密码错误");
            }
        }

        return MapMessage.successMessage().add("user", user);
    }
}
