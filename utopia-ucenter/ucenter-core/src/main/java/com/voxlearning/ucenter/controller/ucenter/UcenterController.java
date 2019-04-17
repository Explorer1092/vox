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

package com.voxlearning.ucenter.controller.ucenter;

import com.voxlearning.alps.annotation.meta.RoleType;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.lang.util.RealnameRule;
import com.voxlearning.ucenter.service.user.AccountWebappService;
import com.voxlearning.ucenter.support.FileDownloader;
import com.voxlearning.ucenter.support.context.UcenterRequestContext;
import com.voxlearning.ucenter.support.controller.AbstractWebController;
import com.voxlearning.utopia.api.constant.UserServiceRecordOperationType;
import com.voxlearning.utopia.api.legacy.MemcachedKeyConstants;
import com.voxlearning.utopia.core.helper.StringRegexUtils;
import com.voxlearning.utopia.data.DownloadContent;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.entity.misc.UserPopup;
import com.voxlearning.utopia.service.footprint.client.ForgotPasswordDetailServiceClient;
import com.voxlearning.utopia.service.popup.client.LegacyPopupServiceClient;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.ResearchStaff;
import com.voxlearning.utopia.service.user.api.mappers.UserChangNameMapper;
import com.voxlearning.utopia.service.user.api.mappers.UserSecurity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author changyuan.liu
 * @since 2012.12.10
 */
@Slf4j
@Controller
@RequestMapping("/ucenter")
public class UcenterController extends AbstractWebController {

    @Inject private AccountWebappService accountWebappService;
    @Inject private LegacyPopupServiceClient legacyPopupServiceClient;
    @Inject private ForgotPasswordDetailServiceClient forgotPasswordDetailServiceClient;

    /**
     * 用户上传头像
     */
    @RequestMapping(value = "avatar.vpage", method = RequestMethod.GET)
    public String avatar(Model model) {
        User user = currentUser();
        if (null != user) {
            String avatarCancel = getRequestStringCleanXss("avatar_cancel");
            String avatarCallback = getRequestStringCleanXss("avatar_callback");

            model.addAttribute("avatar_callback", avatarCallback);
            model.addAttribute("avatar_cancel", avatarCancel);
            model.addAttribute("face", user.fetchImageUrl());
            model.addAttribute("userId", user.getId());
        }
        return "/ucenter/avatar";
    }

    @RequestMapping(value = "getuserpopups.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getUserPopups() {
        User user = currentUser();
        if (user != null) {
            List<UserPopup> userPopupList = legacyPopupServiceClient.getLegacyPopupService()
                    .currentAvailablePopups(user.toSimpleUser(), 3, 5 * 60 * 1000);
            List<String> htmlList = new ArrayList<>();
            for (UserPopup userPopup : userPopupList) {
                htmlList.add(userPopup.getContent());
            }
            return MapMessage.successMessage().add("htmlList", htmlList);
        }
        return MapMessage.errorMessage();
    }

    /**
     * 用户修改自己的密码
     */
    @RequestMapping(value = "resetmypw.vpage", method = RequestMethod.POST)
    @ResponseBody
    public Map resetmypw() {
        User user = currentUser();
        String currentPassword = getRequestParameter("current_password", "");
        String newPassword = getRequestParameter("new_password", "");
        try {
            MapMessage message = userServiceClient.changePassword(user, currentPassword, newPassword);
            if (!message.isSuccess()) {
                throw new RuntimeException(message.getInfo());
            }

            // 记录 UserServiceRecord
            UserServiceRecord userServiceRecord = new UserServiceRecord();
            userServiceRecord.setUserId(user.getId());
            userServiceRecord.setOperatorId(user.getId().toString());
            userServiceRecord.setOperationType(UserServiceRecordOperationType.用户信息变更.name());
            userServiceRecord.setOperationContent("用户重置密码");
            userServiceRecord.setComments("用户重置自己的密码，操作端[pc]");
            userServiceRecord.setAdditions("refer:UcenterController.resetmypw");
            userServiceClient.saveUserServiceRecord(userServiceRecord);

            //由于cookie中保存了加密后的密码，所以修改密码后需要更新cookie，否则会强制用户重新登录
            //由于不知道原来cookie是否存有“记住我”，无法确定当时设定的有效期，这里设定用户下次访问时重新登录
            resetAuthCookie(getWebRequestContext(), -1);

            // 重置密码后处理
            accountWebappService.onPasswordReset(user, newPassword);

            return MapMessage.successMessage("修改密码成功");
        } catch (Exception ex) {
            return MapMessage.errorMessage("修改密码失败。请输入正确的当前登录密码。");
        }
    }

    /**
     * FIXME: 本来此方法只是给老师用, 后来中学生也有这个需求,所以加一个user_type区分
     * user_type 1 或者空代表老师 其他代表学生
     * 用户通过手机验证码的方式重置密码
     *
     * @return
     * @author changyuan.liu
     */
    @RequestMapping(value = "resetpwbycode.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage resetPasswordUsingVerifyCode() {
        User user = currentUser();
        String verifyCode = getRequestString("verify_code");
        String newPassword = getRequestString("new_password");
        String userType = getRequestString("user_type");

        UserType userTypeEnum = UserType.TEACHER;
        if (StringUtils.isNotEmpty(userType)) {
            userTypeEnum = UserType.of(Integer.valueOf(userType));
        }

        try {
            MapMessage verifyResult = null;
            if (userTypeEnum == UserType.TEACHER) {
                verifyResult = smsServiceClient.getSmsService().verifyValidateCode(user.getId(), verifyCode, SmsType.TEACHER_CHANGE_PASSWORD.name());
            } else {
                verifyResult = smsServiceClient.getSmsService().verifyValidateCode(user.getId(), verifyCode, SmsType.PC_JUNIOR_STUDENT_VERIFY_RESET_PASS.name());
            }
            if (!verifyResult.isSuccess()) {
                return MapMessage.errorMessage("修改密码失败。请确认验证码是否正确");
            }

            // 修改密码
            userServiceClient.setPassword(user, newPassword);

            // 记录 UserServiceRecord
            UserServiceRecord userServiceRecord = new UserServiceRecord();
            userServiceRecord.setUserId(user.getId());
            userServiceRecord.setOperatorId(user.getId().toString());
            userServiceRecord.setOperationType(UserServiceRecordOperationType.用户信息变更.name());
            userServiceRecord.setOperationContent("用户重置密码");
            userServiceRecord.setComments("用户通过手机验证的方式重置自己的密码，操作端[pc]");
            userServiceRecord.setAdditions("referer:UcenterController.resetpwbycode");
            userServiceClient.saveUserServiceRecord(userServiceRecord);
            //由于cookie中保存了加密后的密码，所以修改密码后需要更新cookie，否则会强制用户重新登录
            //由于不知道原来cookie是否存有“记住我”，无法确定当时设定的有效期，这里设定用户下次访问时重新登录
            resetAuthCookie(getWebRequestContext(), -1);

            // 重置密码后处理
            accountWebappService.onPasswordReset(user, newPassword);

            return MapMessage.successMessage("修改密码成功");
        } catch (Exception ex) {
            return MapMessage.errorMessage("修改密码失败。请确认验证码是否正确");
        }
    }


    /**
     * 下载账号
     *
     * @param response
     */
    @RequestMapping(value = "fetchaccount.vpage", method = RequestMethod.GET)
    @ResponseBody
    public void downloadPassword(HttpServletResponse response) {
        User user = currentUser();
        if (user == null) {
            int code = 403;
            String message = "not allowed to get account";
            response.setStatus(code);
            try {
                response.getWriter().write(code + ":" + message);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
            return;
        }

        MapMessage message = sensitiveUserDataServiceClient.downloadPassword(user);
        if (!message.isSuccess()) {
            logger.error("Failed to download user '{}' password", user.getId());
        } else {
            byte[] content = ((DownloadContent) message.get("password")).getContent();
            String filename = user.fetchRealname() + "的一起作业账号.txt";
            try {
                FileDownloader.downloadText(filename, content, getRequest(), response);
            } catch (Exception ex) {
                logger.error("Failed to download user '{}' password, error: {}", user.getId(), ex.getMessage());
            }
        }
    }

    @RequestMapping(value = "authorize/selectuser.vpage", method = RequestMethod.GET)
    public String selectUser(Model model) {
        String key = getRequestParameter("key", "");
        if (StringUtils.isBlank(key)) {
            return "redirect:/login.vpage#error=adult";
        }
        try {
            Map<String, Object> map = ucenterWebCacheSystem.CBS.unflushable.load(MemcachedKeyConstants.MULTI_USER_LOGIN_PREFIX + key);
            // 如果东西没有拿到，登陆失败，重新登陆
            if (map == null || !map.containsKey("candidates")) {
                return "redirect:/login.vpage#error=adult";
            }
            List<Map<String, Object>> result = new ArrayList<>();
            // noinspection unchecked
            List<UserSecurity> securities = (List<UserSecurity>) map.get("candidates");
            for (UserSecurity security : securities) {
                Map<String, Object> each = new HashMap<>();
                each.put("useId", security.getUserId());
                each.put("realname", security.getRealname());
                RoleType roleType = security.getRoleTypes().get(0);
                // FIXME 教研员的类型居然没办法转换？？
                each.put("userType", UserType.of(roleType.getType()));
                // 检查教务老师
                if (RoleType.ROLE_RESEARCH_STAFF == roleType) {
                    ResearchStaff staff = researchStaffLoaderClient.loadResearchStaff(security.getUserId());
                    each.put("affairTeacher", staff != null && staff.isAffairTeacher());
                    each.put("userType", UserType.RESEARCH_STAFF);
                }
                result.add(each);
            }
            model.addAttribute("candidates", result);
            model.addAttribute("key", key);

            Object dataKey = map.get("dataKey");
            if (dataKey != null) {
                model.addAttribute("dataKey", dataKey);
            }

        } catch (Exception e) {
            return "redirect:/login.vpage#error=adult";
        }
        return "ucenter/authorize/selectuser";
    }


    @RequestMapping(value = "changName.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage changName(@RequestBody final UserChangNameMapper command) {
        User user = currentUser();

        if(StringRegexUtils.isNotRealName(command.getUserName())){
            return MapMessage.errorMessage("姓名不可用！");
        }

        if (badWordCheckerClient.containsUserNameBadWord(command.getUserName())) {
            return MapMessage.errorMessage("姓名请不要使用敏感词汇！");
        }

        // 更新名字
        if (!StringUtils.equals(command.getUserName(), user.fetchRealname())) {
            String userName = StringUtils.cleanXSS(RealnameRule.removeInvalidRealNameChars(command.getUserName()));
            if (userName.length() > 10) {
                return MapMessage.errorMessage("姓名不能大于10个字符");
            }
            MapMessage mapMessage = userServiceClient.changeName(user.getId(), userName);
            if (!mapMessage.isSuccess()) {
                return MapMessage.errorMessage(mapMessage.getInfo());
            }
        }

        return MapMessage.successMessage("更新姓名成功！");
    }

    //////////////////////////////////////////private methods///////////////////////////////////////////////

    /**
     * 重置cookie
     *
     * @param context
     * @param expire
     */
    private void resetAuthCookie(UcenterRequestContext context, int expire) {
        List<UserSecurity> securities = userLoaderClient.loadUserSecurities(context.getCurrentUser().getId().toString(), context.getCurrentUser().fetchUserType());
        UserSecurity userSecurity = MiscUtils.firstElement(securities);
        if (null != userSecurity) {
            context.saveAuthenticationStates(expire, userSecurity);
        }
    }

}
