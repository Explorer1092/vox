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

package com.voxlearning.utopia.admin.controller.crm;

import com.voxlearning.alps.annotation.meta.Ktwelve;
import com.voxlearning.alps.annotation.meta.RoleType;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.MobileRule;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.admin.auth.AuthCurrentAdminUser;
import com.voxlearning.utopia.admin.cache.AdminCacheSystem;
import com.voxlearning.utopia.admin.data.UserDataAuthorityMapper;
import com.voxlearning.utopia.admin.service.crm.CrmAccountService;
import com.voxlearning.utopia.api.constant.UserServiceRecordOperationType;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.library.sensitive.SensitiveLib;
import com.voxlearning.utopia.service.email.api.constants.EmailTemplate;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.reward.constant.IdentificationCouponName;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.mappers.NeonatalUser;
import com.voxlearning.utopia.service.user.api.sms.DPUserSmsService;
import com.voxlearning.utopia.service.user.consumer.UserManagementClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author xin.xin
 * @since 2014-1-26
 */
@Controller
@RequestMapping(value = "crm/account")
public class CrmAccountController extends CrmAbstractController {

    @Inject private CrmAccountService crmAccountService;
    @Inject private UserManagementClient userManagementClient;
    @Inject private AdminCacheSystem adminCacheSystem;
    @Inject private EmailServiceClient emailServiceClient;

    @ImportService(interfaceClass = DPUserSmsService.class)
    private DPUserSmsService smsService;

    @RequestMapping(value = "getuserphone.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getUserPhone(@RequestParam Long userId) {

        // 先加次数
        AuthCurrentAdminUser currentAdminUser = getCurrentAdminUser();

        // FIXME 判断是否超限
        String desc = getRequestString("desc");
        MapMessage checkResult = checkViewUserPhoneLimit(getCurrentAdminUser(), desc);
        if (!checkResult.isSuccess()) {
            return checkResult;
        }

        adminCacheSystem.incViewUserPhoneCount(currentAdminUser.getFakeUserId());

        MapMessage mapMessage = new MapMessage();
        String phone = "";
        if (userId != null) {
            User user = userLoaderClient.loadUserIncludeDisabled(userId);
            if (user != null) {
                phone = sensitiveUserDataServiceClient.showUserMobile(userId, "CrmAccountCtl:getUserPhone", getCurrentAdminUser().getAdminUserName());
            }
        }
        addAdminLog("queryUserMobile", userId, StringUtils.mobileObscure(phone), "crm", "ID:" + userId + ", phone:" + StringUtils.mobileObscure(phone));
        mapMessage.setSuccess(true);
        mapMessage.add("phone", phone);
        return mapMessage;
    }

    private MapMessage checkViewUserPhoneLimit(AuthCurrentAdminUser currentAdminUser, String desc) {
        String userDepartment = currentAdminUser.getDepartmentName();
        long emailAlertCount = 100L;
        long popupAlertCount = 50L;
        if (userDepartment.contains("客服") || userDepartment.toLowerCase().startsWith("cs")) {
            emailAlertCount = 400L;
            popupAlertCount = 200L;
        }

        if (RuntimeMode.isDevelopment()) {
            emailAlertCount = 10L;
            popupAlertCount = 5L;
        }

        Long userId = currentAdminUser.getFakeUserId();
        long viewUserPhoneCount = adminCacheSystem.loadViewUserPhoneCount(userId);

        // 超过总量直接就不显示，提示错误
        if (Objects.equals(viewUserPhoneCount, emailAlertCount)) {
            Map<String, Object> content = new HashMap<>();
            content.put("info", currentAdminUser.getAdminUserName() + "@" + currentAdminUser.getDepartmentName() + ",触发数量:" + viewUserPhoneCount);
            emailServiceClient.createTemplateEmail(EmailTemplate.office)
                    .to("list.rmg@17zuoye.com")
                    .cc("zhilong.hu@17zuoye.com;lining.zhang@17zuoye.com")
                    .subject("【" + RuntimeMode.getCurrentStage() + "】admin手机号码查看-高")
                    .content(content)
                    .send();
        }

        // 弹窗警告逻辑
        if (Objects.equals(viewUserPhoneCount, popupAlertCount)) {

            // 为空，表示第一次触发，需要操作者填写10字以上描述，
            if (StringUtils.isBlank(desc) || desc.length() < 10) {
                return MapMessage.errorMessage().set("popup", true);
            }

            Map<String, Object> content = new HashMap<>();
            content.put("info", currentAdminUser.getAdminUserName() + "@" + currentAdminUser.getDepartmentName() + ",触发数量:" + viewUserPhoneCount + ",描述:" + desc);
            emailServiceClient.createTemplateEmail(EmailTemplate.office)
                    .to("list.rmg@17zuoye.com")
                    .cc("zhilong.hu@17zuoye.com;lining.zhang@17zuoye.com")
                    .subject("【" + RuntimeMode.getCurrentStage() + "】admin手机号码查看-低")
                    .content(content)
                    .send();
        }

        return MapMessage.successMessage();
    }


    /*
     * 查询教研员（包含未认证）手机号
     */
    @RequestMapping(value = "getresearchstaffphone.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getresearchstaffphone(@RequestParam Long userId) {
        MapMessage mapMessage = new MapMessage();
        String phone = "";
        if (userId != null) {
            User user = userLoaderClient.loadUserIncludeDisabled(userId);
            if (user != null) {
                if (!user.isResearchStaff()) {
                    return MapMessage.errorMessage("请使用教研员账号");
                }
                phone = sensitiveUserDataServiceClient.showUserMobile(userId, "CrmAccountCtl:getresearchstaffphone", getCurrentAdminUser().getAdminUserName());
            }
        }

        addAdminLog("queryUserMobile", userId, StringUtils.mobileObscure(phone), "crm", "ID:" + userId + ", phone:" + StringUtils.mobileObscure(phone));
        mapMessage.setSuccess(true);
        mapMessage.add("phone", phone);
        return mapMessage;
    }

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String account(Model model) {
        return "/crm/account/account";
    }

    @RequestMapping(value = "index.vpage", method = RequestMethod.POST)
    public String account(@RequestParam String id,
                          @RequestParam String mobile,
                          @RequestParam String userType,
                          @RequestParam(defaultValue = "1") int currentPage,
                          Model model) {
        Map<String, String> pars = new HashMap<>();
        pars.put("id", id);
        pars.put("mobile", mobile);
        pars.put("user_Type", userType);
        Map<String, Object> map = crmAccountService.findAccounts(pars, currentPage, 20);

        pars.put("currentPage", String.valueOf(currentPage));//在查询完成后添加此参数，防止在查询时被当做查询条件
        model.addAttribute("totalPage", map.get("pageCount"));
        model.addAttribute("users", map.get("pageData"));
        model.addAttribute("conditions", pars);
        model.addAttribute("provinces", crmUserService.getAllProvince());
        return "/crm/account/account";
    }

    @RequestMapping(value = "register.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage register(@RequestParam String roleType,
                               @RequestParam String password,
                               @RequestParam String realname,
                               @RequestParam(value = "subject", required = false) String subject,
                               @RequestParam(value = "ktwelve", required = false) String ktwelve,
                               @RequestParam(value = "mobile", required = false) String mobile) {
        NeonatalUser user = new NeonatalUser();
        user.setRoleType(RoleType.of(Integer.valueOf(roleType)));
        switch (roleType.intern()) {
            case "7":
                user.setUserType(UserType.EMPLOYEE);
                break;
            case "9":
                user.setUserType(UserType.EMPLOYEE);
                break;
            case "10":
                user.setUserType(UserType.RESEARCH_STAFF);
                user.setSubject(subject);
                user.setKtwelve(StringUtils.isEmpty(ktwelve) ? Ktwelve.PRIMARY_SCHOOL : Ktwelve.of(ktwelve));
                break;
            case "11":
                user.setUserType(UserType.TEMPORARY);
                break;
            default:
                break;
        }
        user.setPassword(password);
        user.setRealname(realname);
        if(StringUtils.isNotBlank(mobile)){
            user.setMobile(mobile);
        }

        MapMessage message = new MapMessage();
        MapMessage userMessage = userServiceClient.registerUserAndSendMessage(user);
        if (!userMessage.isSuccess()) {
            logger.warn("添加帐号{}失败！", user);
            message.setSuccess(false);
            message.setInfo("添加帐号失败！");
            return message;
        }
        User newUser = (User) userMessage.get("user");

        message.setSuccess(true);
        message.setInfo("添加账号成功！");
        message.add("userId", newUser.getId());
        return message;
    }

    @RequestMapping(value = "getuserauthorityregion.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getUserAuthorityRegion(@RequestParam String userId) {
        MapMessage message = new MapMessage();
        try {
            List<UserDataAuthorityMapper> mappers = crmAccountService.getRstaffDataAuthor(Long.valueOf(userId));
            message.setSuccess(true);
            message.add("regions", mappers);
        } catch (Exception ex) {
            logger.info("获取教研员{}授权地区失败,msg:{}", userId, ex.getMessage());
            message.setSuccess(false);
            message.setInfo("查询教研员授权地区失败," + ex.getMessage());
        }
        return message;
    }

    @RequestMapping(value = "updateuserauthorityregion.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateUserAuthorityRegion(@RequestParam Boolean setSchoolMaster,
                                                @RequestParam String userId,
                                                @RequestParam String regions,
                                                @RequestParam String regionTypes) {
        regions = regions.replace("[", "").replace("]", "");
        regionTypes = regionTypes.replace("[", "").replace("]", "");
        MapMessage message = new MapMessage();
        if (regions.length() == 0) {
            message.setSuccess(false);
            message.setInfo("请选择用户授权地区！");
            return message;
        }

        try {
            crmAccountService.updateRstaffAuthorityRegion(Long.valueOf(userId), regions, regionTypes);

            message.setSuccess(true);
            message.setInfo("用户权限设置成功！");
        } catch (Exception ex) {
            logger.warn("用户{}权限设置失败，region:{},msg:{}", userId, regions, ex.getMessage());
            message.setSuccess(false);
            message.setInfo("用户权限设置失败," + ex.getMessage());
        }

        if (setSchoolMaster) {//setSchoolMaster表示:教研员设置为校长
            if (!message.isSuccess()) {
                return message;
            }
            if (regions.split(",").length != 1) {
                return MapMessage.errorMessage("校长必须只能选择一个学校!!");
            }
            MapMessage tempMessage = userManagementClient.setSchoolMaster(SafeConverter.toLong(userId));
            if (!tempMessage.isSuccess()) {
                return tempMessage;
            }
        }

        return message;
    }

    @RequestMapping(value = "getuserinfo.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getUserInfo(@RequestParam Long userId) {
        //此处需要把disable的用户也找出来，所以不用userLoader加载
        User user = userLoaderClient.loadUserIncludeDisabled(userId);
        MapMessage mapMessage = new MapMessage();
        if (null == user) {
            mapMessage.setSuccess(false);
            mapMessage.setInfo("未找到用户！");
            logger.info("未找到用户" + userId);
        } else {
            mapMessage.setSuccess(true);
            mapMessage.setInfo("查询用户信息成功！");
            mapMessage.add("user", user);
        }
        return mapMessage;
    }

    @RequestMapping(value = "updateuserinfo.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateUserInfo(
            @RequestParam Long id,
            @RequestParam String realName,
            @RequestParam String mobile) {
        Map<String, Object> parMap = new HashMap<>();
        parMap.put("id", id);
        parMap.put("realName", realName);
        parMap.put("mobile", mobile);

        MapMessage message = new MapMessage();
        try {
            crmAccountService.updateUserInfo(parMap, getCurrentAdminUser().getFakeUserId(), getCurrentAdminUser().getAdminUserName());

            //operation log
            addAdminLog("修改用户信息", id, "会员管理-修改用户信息", parMap);

            message.setSuccess(true);
            message.setInfo("更新成功！");
        } catch (Exception ex) {
            logger.warn("更新失败！info:{},msg:{}", parMap, ex.getMessage());
            message.setSuccess(false);
            message.setInfo("更新失败," + ex.getMessage());
        }
        return message;
    }

    /**
     * crm 更换学生手机号
     * @return
     */
    @RequestMapping(value = "changeStudentPhone.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage changeStudentPhone() {
        Long studentId = getRequestLong("studentId");
        String changePhone = getRequestString("changePhone");
        String changePhoneReason = getRequestString("changePhoneReason");
        if (studentId <= 0L) {
            return MapMessage.errorMessage("学生ID不能为空");
        }
        if (!MobileRule.isMobile(changePhone)) {
            return MapMessage.errorMessage("请输入正确的手机号");
        }
        if (StringUtils.isEmpty(changePhoneReason)) {
            return MapMessage.errorMessage("请输入备注");
        }
        UserAuthentication userAuthentication = userLoaderClient.loadUserAuthentication(studentId);
        if (userAuthentication == null) {
            return MapMessage.errorMessage("学生账号不存在");
        }
        String oPhone = SensitiveLib.encodeMobile(userAuthentication.getSensitiveMobile());
        if (Objects.equals(oPhone, changePhone)) {
            return MapMessage.errorMessage("输入手机号和学生原手机号相同");
        }
        if (userLoaderClient.loadMobileAuthentication(changePhone, UserType.STUDENT) != null) {
            return MapMessage.errorMessage("手机号已经绑定，请更换");
        }
        MapMessage mapMessage = userServiceClient.updateEmailMobile(studentId, null, changePhone);
        if (mapMessage.isSuccess()) {
            UserServiceRecord userServiceRecord = new UserServiceRecord();
            userServiceRecord.setUserId(studentId);
            userServiceRecord.setOperatorId(getCurrentAdminUser().getAdminUserName());
            userServiceRecord.setOperationType(UserServiceRecordOperationType.用户信息变更.name());
            userServiceRecord.setOperationContent(changePhoneReason);
            userServiceRecord.setComments("修改学生手机号, 原手机号["+ SensitiveLib.encodeMobile(oPhone) +"]");
            userServiceClient.saveUserServiceRecord(userServiceRecord);
        }
        return mapMessage;
    }
}
