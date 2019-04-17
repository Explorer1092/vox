package com.voxlearning.washington.controller.open.v2;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.user.api.entities.StudentExtAttribute;
import com.voxlearning.utopia.service.user.api.entities.TeacherExtAttribute;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import static com.voxlearning.washington.controller.open.ApiConstants.*;

@Controller
@RequestMapping(value = "/v2/user")
@Slf4j
public class VerificationApi2Controller extends VerificationBaseController {
    private final static String RES_VERIFY_CODE = "verify_code";

    /**
     * 学生注册发送验证码
     * @return
     */
    @RequestMapping(value = "/student/register/verifycode/get.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage sendStudentRegisterVerifyCode() {
        return sendVerifyCode(SmsType.APP_STUDENT_VERIFY_MOBILE_REGISTER_MOBILE, mobile -> {
            if (userLoaderClient.loadMobileAuthentication(mobile, UserType.STUDENT) != null) {
                throw new IllegalArgumentException("该手机号码已经注册，请直接登录");
            }
        });
    }

    /**
     * 学生绑定手机号
     * @return
     */
    @RequestMapping(value = "/student/bindmobile/verifycode/get.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage sendStudentMobileCode() {
        return sendVerifyCode(SmsType.APP_STUDENT_VERIFY_MOBILE_CENTER, mobile -> {
            if (userLoaderClient.loadMobileAuthentication(mobile, UserType.STUDENT) != null) {
                throw new IllegalArgumentException(RES_RESULT_MOBILE_IS_BIND_ERROR_MSG);
            }
        });
    }

    /**
     * 学生通过验证码登录
     * @return
     */
    @RequestMapping(value = "/student/login/verifycode/get.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage sendStudentMobileCodeForLogin() {
        return sendVerifyCode(SmsType.APP_STUDENT_VERIFY_MOBILE_LOGIN_MOBILE, mobile -> {
            UserAuthentication ua = userLoaderClient.loadMobileAuthentication(mobile, UserType.STUDENT);
            if (ua == null) {
                throw new IllegalArgumentException(RES_RESULT_MOBILE_NOT_REGIST_ERROR_MSG);
            }
            StudentExtAttribute studentExtAttribute = studentLoaderClient.loadStudentExtAttribute(ua.getId());
            if (studentExtAttribute != null && studentExtAttribute.isForbidden()) {
                throw new IllegalArgumentException("用户被封禁啦");
            }
        });
    }


    /**
     * 老师注册发送验证码
     * @return
     */
    @RequestMapping(value = "/teacher/register/verifycode/get.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage sendTeacherRegisterVerifyCode() {
        return sendVerifyCode(SmsType.APP_TEACHER_VERIFY_MOBILE_REGISTER_MOBILE, mobile -> {
            if (userLoaderClient.loadMobileAuthentication(mobile, UserType.TEACHER) != null) {
                throw new IllegalArgumentException("该手机号码已经注册，请直接登录");
            }
        });
    }

    /**
     * 老师绑定手机号
     * @return
     */
    @RequestMapping(value = "/teacher/bindmobile/verifycode/get.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage sendTeacherMobileCode() {
        return sendVerifyCode(SmsType.APP_TEACHER_VERIFY_MOBILE_CENTER, mobile -> {
            if (userLoaderClient.loadMobileAuthentication(mobile, UserType.TEACHER) != null) {
                throw new IllegalArgumentException(RES_RESULT_MOBILE_IS_BIND_ERROR_MSG);
            }
        });
    }

    /**
     * 老师通过验证码登录
     * @return
     */
    @RequestMapping(value = "/teacher/login/verifycode/get.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage sendTeacherMobileCodeForLogin() {
        return sendVerifyCode(SmsType.APP_TEACHER_VERIFY_MOBILE_LOGIN_MOBILE, mobile -> {
            UserAuthentication ua = userLoaderClient.loadMobileAuthentication(mobile, UserType.TEACHER);
            if (ua == null) {
                throw new IllegalArgumentException(RES_RESULT_MOBILE_NOT_REGIST_ERROR_MSG);
            }
            TeacherExtAttribute teacherExtAttribute = teacherLoaderClient.loadTeacherExtAttribute(ua.getId());
            if (teacherExtAttribute != null && teacherExtAttribute.isForbidden()) {
                throw new IllegalArgumentException("用户被封禁啦");
            }
        });
    }

    /**
     * 家长注册或者登录发送验证码
     * @return
     */
    @RequestMapping(value = "/parent/verifycode/get.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage sendParentRegisterVerifyCode() {
        return sendVerifyCode(SmsType.APP_PARENT_VERIFY_MOBILE_REGISTER_MOBILE, mobile -> {});
    }

    /**
     * 家长绑定手机号
     * @return
     */
    @RequestMapping(value = "/parent/bindmobile/verifycode/get.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage sendParentMobileCode() {
        return sendVerifyCode(SmsType.APP_PARENT_VERIFY_MOBILE_CENTER, mobile -> {
            if (userLoaderClient.loadMobileAuthentication(mobile, UserType.PARENT) != null) {
                throw new IllegalArgumentException(RES_RESULT_MOBILE_IS_BIND_ERROR_MSG);
            }
        });
    }

    /**
     * 家长通过验证码登录
     * @return
     */
    @RequestMapping(value = "/parent/login/verifycode/get.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage sendParentMobileCodeForLogin() {
        return sendVerifyCode(SmsType.APP_PARENT_VERIFY_MOBILE_LOGIN_MOBILE, mobile -> {
            UserAuthentication ua = userLoaderClient.loadMobileAuthentication(mobile, UserType.PARENT);
            if (ua == null) {
                throw new IllegalArgumentException(RES_RESULT_MOBILE_NOT_REGIST_ERROR_MSG);
            }
        });
    }

    /**
     * 家长ID获取验证码
     * @return
     */
    @RequestMapping(value = "/parent/id/verifycode/get.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage sendParentMobileCodeById() {
        return sendVerifyCodeByUserId(SmsType.APP_PARENT_VERIFY_MOBILE_CENTER, null);
    }

    /**
     * 用户忘记密码
     * @return
     */
    @RequestMapping(value = "/forgotpassword/verifycode/get.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage sendForgotPasswordVerifyCode() {
        return sendVerifyCode(SmsType.FORGOT_PASSWORD_SEND_VERIFY_CODE, mobile -> {
            if (CollectionUtils.isEmpty(userLoaderClient.loadMobileAuthentications(mobile))) {
                throw new IllegalArgumentException(RES_RESULT_MOBILE_NOT_REGIST_ERROR_MSG);
            }
        });
    }

    /**
     * 通过id 从缓存中load 验证码, 拿不到返回空
     * @return
     */
    @RequestMapping(value = "/cacheVerifyCode/id/get.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage cacheVerifyCodeByUserId() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequiredNumber(REQ_USER_ID, "用户ID");
            if (hasSessionKey())
                validateRequest( REQ_USER_ID);
            else
                validateRequestNoSessionKey(REQ_USER_ID);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        } catch (Exception e) {
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_INTERNAL_ERROR_MSG);
            return resultMap;
        }
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        resultMap.add(RES_VERIFY_CODE, smsServiceClient.getSmsService().loadCacheVerifyCode(SmsType.NO_CATEGORY.name(), getRequestLong(REQ_USER_CODE)).getUninterruptibly());
        return resultMap;
    }

    /**
     * 通过mobile 从缓存中load 验证码, 拿不到返回空
     * @return
     */
    @RequestMapping(value = "/cacheVerifyCode/mobile/get.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage cacheVerifyCodeByMobile() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequiredNumber(REQ_USER_CODE, "手机号码");
            if (hasSessionKey())
                validateRequest( REQ_USER_CODE);
            else
                validateRequestNoSessionKey(REQ_USER_CODE);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        } catch (Exception e) {
            resultMap.add(RES_RESULT, RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_INTERNAL_ERROR_MSG);
            return resultMap;
        }
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        resultMap.add(RES_VERIFY_CODE,
                !getRequestString(REQ_USER_CODE).startsWith("117") ? "" :
                smsServiceClient.getSmsService().loadCacheVerifyCode(SmsType.NO_CATEGORY.name(), getRequestString(REQ_USER_CODE)).getUninterruptibly());
        return resultMap;
    }
}
