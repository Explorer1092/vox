package com.voxlearning.washington.controller.specialteacher;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.MobileRule;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.UserServiceRecordOperationType;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.user.api.CrmSummaryService;
import com.voxlearning.utopia.service.user.api.VerificationService;
import com.voxlearning.utopia.service.user.api.constants.FindPasswordMethod;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.extension.ResearchStaff;
import com.voxlearning.utopia.service.user.api.mappers.UserSecurity;
import com.voxlearning.washington.mapper.specialteacher.base.SpecialTeacherConstants;
import com.voxlearning.washington.support.WashingtonRequestContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.List;

/**
 * 教务老师-个人中心相关
 */
@Controller
@RequestMapping("/specialteacher/center")
public class SpecialTeacherCenterController extends AbstractSpecialTeacherController {

    @Inject private RaikouSystem raikouSystem;

    @ImportService(interfaceClass = VerificationService.class)
    private VerificationService verificationService;

    @ImportService(interfaceClass = CrmSummaryService.class) private CrmSummaryService crmSummaryService;

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String teacherCenterManage() {
        if (currentSpecialTeacher() == null) {
            return "redirect: /";
        }
        return "specialteacherV2/teachercenter";
    }
    //========================================= 教务-老师个人中心 =========================================

    /**
     * 个人中心， 我的资料
     */
    @RequestMapping(value = "basicinfo.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage basicInfo() {
        ResearchStaff specialTeacher = currentSpecialTeacher();
        if (specialTeacher == null) {
            return MapMessage.errorMessage("请重新登录");
        }

        School school = currentSchool();
        ExRegion region = school == null ? null : raikouSystem.loadRegion(school.getRegionCode());

        String schoolName = school == null ? null : school.getCmainName();
        String schoolRegion = region == null ? null : region.formalizeCityCountyName();

        String mobile = sensitiveUserDataServiceClient.loadUserMobileObscured(specialTeacher.getId());

        return MapMessage.successMessage()
                .add("schoolName", schoolName)
                .add("schoolRegion", schoolRegion)
                .add("mobile", mobile);
    }

    /**
     * 个人中心， 账号安全
     */
    @RequestMapping(value = "securityinfo.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage securityInfo() {
        ResearchStaff specialTeacher = currentSpecialTeacher();
        if (specialTeacher == null) {
            return MapMessage.errorMessage("请重新登录");
        }

        String mobile = sensitiveUserDataServiceClient.loadUserMobileObscured(specialTeacher.getId());
        UserAuthentication ua = userLoaderClient.loadUserAuthentication(specialTeacher.getId());
        boolean password = StringUtils.isNotBlank(ua.getPassword()) && ua.getPwdState() != null && ua.getPwdState() == 0;
        return MapMessage.successMessage()
                .add("mobile", mobile)
                .add("password", password);
    }

    /**
     * 修改密码，发送短信验证码
     */
    @RequestMapping(value = "smsvalidatecode.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendChangePasswordCode() {
        ResearchStaff specialTeacher = currentSpecialTeacher();
        if (specialTeacher == null) {
            return MapMessage.errorMessage("请重新登录");
        }

        String am = sensitiveUserDataServiceClient.showUserMobile(currentUserId(), "/specilateacher/center", SafeConverter.toString(currentUserId()));
        if (am == null) {
            return MapMessage.errorMessage("请先绑定手机");
        }
        if (!MobileRule.isMobile(am)) {
            return MapMessage.errorMessage("错误的手机号");
        }

        return smsServiceClient.getSmsService().sendUnbindMobileVerificationCode(currentUserId(), am, SmsType.TEACHER_CHANGE_PASSWORD.name());
    }

    /**
     * 用户通过手机验证码的方式重置密码
     */
    @RequestMapping(value = "resetpassword.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage resetPassword() {
        ResearchStaff specialTeacher = currentSpecialTeacher();
        if (specialTeacher == null) {
            return MapMessage.errorMessage("请重新登录");
        }

        Long userId = specialTeacher.getId();

        String verifyCode = getRequestString("verifyCode");
        String newPassword = getRequestString("newPassword");
        try {
            MapMessage verifyResult = smsServiceClient.getSmsService().verifyValidateCode(userId, verifyCode, SmsType.TEACHER_CHANGE_PASSWORD.name());
            if (!verifyResult.isSuccess()) {
                return MapMessage.errorMessage("修改密码失败。请确认验证码是否正确");
            }

            // 修改密码
            userServiceClient.setPassword(specialTeacher, newPassword);

            // 用户自己修改密码记录一下
            saveForgotPwdRecord(userId, FindPasswordMethod.MODIFY_PASSWORD);
            // 由于cookie中保存了加密后的密码，所以修改密码后需要更新cookie，否则会强制用户重新登录
            // 由于不知道原来cookie是否存有“记住我”，无法确定当时设定的有效期，这里设定用户下次访问时重新登录
            resetAuthCookie(getWebRequestContext(), -1);

            return MapMessage.successMessage("修改密码成功");
        } catch (Exception ex) {
            logger.error("Failed change password for special teacher. id={}", userId, ex);
            return MapMessage.errorMessage("修改密码失败。请确认验证码是否正确");
        }
    }

    private void saveForgotPwdRecord(Long userId, FindPasswordMethod method) {
        if (userId == null) {
            return;
        }
        UserServiceRecord userServiceRecord = new UserServiceRecord();
        userServiceRecord.setUserId(userId);
        userServiceRecord.setOperationType(UserServiceRecordOperationType.用户信息变更.name());
        userServiceRecord.setOperationContent("修改密码");
        userServiceRecord.setComments(method.getDescription());

        userServiceClient.saveUserServiceRecord(userServiceRecord);
    }

    /**
     * 发送手机验证码
     */
    @RequestMapping(value = "sendmobilecode.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendMobileCode() {
        ResearchStaff specialTeacher = currentSpecialTeacher();
        if (specialTeacher == null) {
            return MapMessage.errorMessage("请重新登录");
        }

        String mobile = getRequestString("mobile");
        try {
            return smsServiceClient.getSmsService().sendUnbindMobileVerificationCode(currentUserId(), mobile, SmsType.TEACHER_VERIFY_MOBILE_CENTER.name());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage("验证码发送失败");
        }
    }

    /**
     * 重新绑定手机号 验证手机号
     */
    @RequestMapping(value = "validatemobile.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage submitRebindMobile() {
        ResearchStaff specialTeacher = currentSpecialTeacher();
        if (specialTeacher == null) {
            return MapMessage.errorMessage("请重新登录");
        }

        String code = getRequestString("code");
        try {
            return verificationService.verifyMobile(specialTeacher.getId(), code, SmsType.TEACHER_VERIFY_MOBILE_CENTER.name());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return MapMessage.errorMessage("手机验证失败");
        }
    }

    /**
     * 个人中心 更新我的资料
     */
    @RequestMapping(value = "modifyprofile.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage modifyProfile() {
        ResearchStaff specialTeacher = currentSpecialTeacher();
        if (specialTeacher == null) {
            return MapMessage.errorMessage("请重新登录");
        }

        String newName = getRequestString("name");

        if (!SpecialTeacherConstants.checkChineseName(newName)) {
            return MapMessage.errorMessage("姓名仅支持十个字以内中文、间隔符·");
        }

        if (badWordCheckerClient.containsUserNameBadWord(newName)) {
            return MapMessage.errorMessage("姓名请不要使用敏感词汇!");
        }

        // 更新名字
        if (!StringUtils.equals(newName, specialTeacher.fetchRealname())) {
            // Feature #54929
//            if (ForbidModifyNameAndPortrait.check()) {
//                return ForbidModifyNameAndPortrait.errorMessage;
//            }
            if (!userServiceClient.changeName(specialTeacher.getId(), newName).isSuccess()) {
                return MapMessage.errorMessage("个人信息更新失败！");
            }
        }

        return MapMessage.successMessage("更新个人信息成功！");
    }

    /**
     * 重置cookie
     */
    private void resetAuthCookie(WashingtonRequestContext context, int expire) {
        List<UserSecurity> securities = userLoaderClient.loadUserSecurities(context.getCurrentUser().getId().toString(), context.getCurrentUser().fetchUserType());
        UserSecurity userSecurity = MiscUtils.firstElement(securities);
        if (null != userSecurity) {
            context.saveAuthenticationStates(expire, userSecurity);
        }
    }

}
