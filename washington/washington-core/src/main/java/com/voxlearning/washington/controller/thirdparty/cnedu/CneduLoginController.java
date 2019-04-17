package com.voxlearning.washington.controller.thirdparty.cnedu;

import com.voxlearning.alps.annotation.meta.Ktwelve;
import com.voxlearning.alps.annotation.meta.RoleType;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.OperationSourceType;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.core.utils.ObjectUtils;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.user.api.constants.UserRecordMode;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.NeonatalUser;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.washington.controller.thirdparty.base.VendorLoginController;
import com.voxlearning.washington.controller.thirdparty.base.VendorUserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * 中央电教馆第三方登录
 */
@Controller
@RequestMapping("/auth/vendor/cnedu")
@Slf4j
public class CneduLoginController extends VendorLoginController {

    @Inject private RaikouSystem raikouSystem;

    /**
     * 登录跳转
     * @return
     */
    @RequestMapping(value="redirect.vpage", method = RequestMethod.GET)
    public String redirect() {
        String token = getRequestString("token");
        String source = getRequestString("source");
        String appKey = getRequestString(REQ_APP_KEY);
        // 参数校验在user里面
        return "redirect:" +  ProductConfig.getMainSiteBaseUrl() +
                "/view/mobile/student/third_party_login/cnedu_middle_page.vpage?token="+token+"&source="+source+"&appKey="+appKey;
    }

    /**
     * 登录回调地址
     * @return
     */
    @RequestMapping(value="user.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage user() {
        VendorUserContext context = new VendorUserContext();
        userContext(context);
        return context.getMapMessage();
    }

    /**
     * 注册
     * @return
     */
    @RequestMapping(value="register.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage register() {
        // 手机号
        String mobile = getRequestString("user_mobile");
        // 验证码
        String code = getRequestString("code");
        // 姓名
        String userName = getRequestString("user_name");
        // userType
        UserType userType = ObjectUtils.get(() -> UserType.valueOf(getRequestString("user_type")));

        if (userType == null || userType == UserType.ANONYMOUS) {
            return failMessage("userType不存在");
        }
        // 第三方用户id
        String vendorUserId = getRequestString("vendor_user_id");
        if (StringUtils.isAnyBlank(mobile, code, userName, vendorUserId)) {
            return failMessage("参数错误");
        }
        VendorApps vendorApps = vendorApps();
        if (vendorApps == null || vendorApps.getAppKey() == null) {
            return failMessage("应用状态错误");
        }
        // 校验验证码
        SmsType smsType;
        if (userType == UserType.STUDENT) {
            smsType = SmsType.STUDENT_VERIFY_MOBILE_REGISTER_MOBILE;
        } else if (userType == UserType.TEACHER) {
            smsType = SmsType.TEACHER_VERIFY_MOBILE_REGISTER_MOBILE;
        } else {
            smsType = SmsType.PARENT_VERIFY_MOBILE_REGISTER_MOBILE;
        }
        MapMessage validateResult = smsServiceClient.getSmsService().verifyValidateCode(mobile, code, smsType.name(), false);
        if (!validateResult.isSuccess()) {
            return failMessage("手机验证码过期或者输入错误");
        }
        UserAuthentication userAuthentication = userLoaderClient.loadMobileAuthentication(mobile, userType);
        Long userId;
        if (userAuthentication != null) {
            // 校验学段
            userId = userAuthentication.getId();
            MapMessage mapMessage = checkUser(vendorApps.getAppKey(), userId, userType);
            if (!RES_RESULT_SUCCESS.equals(mapMessage.get(RES_RESULT))) {
                return mapMessage;
            }
        } else {
            // 注册17账号
            NeonatalUser neonatalUser = new NeonatalUser();
            neonatalUser.setPassword(mobile.substring(5));
            neonatalUser.setUserType(userType);
            neonatalUser.setRoleType(RoleType.of(userType.getType()));
            neonatalUser.setRealname(userName);
            neonatalUser.setMobile(mobile);
            neonatalUser.setCode(code);
            neonatalUser.setWebSource(appKey());
            MapMessage registerMessage = userServiceClient.registerUser(neonatalUser);
            if (!registerMessage.isSuccess()) {
                String errorMessage = registerMessage.getInfo();
                if (registerMessage.get("attributes") != null) {
                    errorMessage =  errorMessage + registerMessage.get("attributes");
                }
                return failMessage(errorMessage);
            }
            User user = (User) registerMessage.get("user");
            userId = user.getId();
        }
        // 绑定用户
        thirdPartyService.persistLandingSource(appKey(), vendorUserId, userName, userId);

        asyncFootprintServiceClient.getAsyncFootprintService().postUserLogin(userId,
                getWebRequestContext().getRealRemoteAddress(),
                UserRecordMode.LOGIN,
                OperationSourceType.app,
                false,
                getAppType());

        // 登录返回
        return successMessage().add(RES_SESSION_KEY, attachUser2RequestApp(userId));
    }

    /**
     * 不做手机号校验
     * @param mobile
     * @return
     */
    @Override
    public UserAuthentication loadMobileAuthentication(String mobile) {
        return null;
    }

    /**
     * @param context
     *
     */
    @Override
    public void token(VendorUserContext context) {
        String token = getRequestString("token");
        if (StringUtils.isBlank(token)) {
            context.setMapMessage(failMessage("token信息获取失败"));
        }
        context.setToken(token);
    }

    @Override
    public void initUser(VendorUserContext context) {
        String token = context.getToken();
        Map<String, Object> user = ObjectUtils.get(() -> (Map<String, Object>) washingtonCacheSystem.CBS.unflushable.get(token).getValue());
        if (user == null) {
            context.setMapMessage(failMessage("二维码已过期，请重新刷新页面，再次扫描"));
            return;
        }
        context.setVendorUserId(SafeConverter.toString(user.get("userId")));
        context.setUserName(SafeConverter.toString(user.get("userName")));
        UserType userType = ObjectUtils.get(() -> UserType.valueOf(SafeConverter.toString(user.get("userType"))));
        if (userType == null || userType == UserType.ANONYMOUS) {
            context.setMapMessage(failMessage("用户身份未确定"));
        }
        context.setUserType(userType);
    }

    @Override
    public void result(VendorUserContext context) {
        if (context.fetchIsBand()) {
            User user = raikouSystem.loadUser(context.getUserId());
            if (user == null) {
                context.setMapMessage(failMessage("没有用户"));
                return;
            }
            MapMessage mapMessage = checkUser(context.getAppKey(), user.getId(), context.getUserType());
            if (!RES_RESULT_SUCCESS.equals(mapMessage.get(RES_RESULT))) {
                context.setMapMessage(mapMessage);
                return;
            }
            context.setMapMessage(successMessage().add(RES_SESSION_KEY, attachUser2RequestApp(context.getUserId())));
            return;
        }
        MapMessage mapMessage = successMessage();
        mapMessage.add("isRegister", context.fetchIsRegister())
                .add("vendorUserId", context.getVendorUserId())
                .add("userName", context.getUserName())
                .add("userType", context.getUserType().name());
        context.setMapMessage(mapMessage);
    }

    @Override
    public String appKey() {
        return "Cnedu";
    }

    /**
     * 校验用户
     * @param appKey
     * @param userId
     * @param userType
     * @return
     */
    private MapMessage checkUser(String appKey, Long userId, UserType userType) {
        List<Ktwelve> ktwelves = null;
        switch (appKey) {
            case "17JuniorStu":
            case "17JuniorTea":
                ktwelves = Arrays.asList(Ktwelve.JUNIOR_SCHOOL, Ktwelve.SENIOR_SCHOOL);
                break;
            case "17Student":
            case "17Teacher":
                ktwelves = Collections.singletonList(Ktwelve.PRIMARY_SCHOOL);
                break;
            default:
                break;
        }
        if (ktwelves == null) {
            return failMessage("暂不支持该应用");
        }
        User user = raikouSystem.loadUser(userId);
        if (user == null) {
            return successMessage();
        }
        String desc = null;
        if (userType ==  UserType.STUDENT) {
            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(userId);
            if (studentDetail != null && studentDetail.getClazz() != null && !ktwelves.contains(studentDetail.getClazz().getEduSystem().getKtwelve())) {
                desc = ((studentDetail.getClazz().getEduSystem().getKtwelve() == Ktwelve.PRIMARY_SCHOOL)?"小学":"中学") + "学生";
            }
        } else if (userType ==  UserType.TEACHER) {
            Teacher teacher = teacherLoaderClient.loadTeacher(userId);
            if (teacher != null && teacher.getKtwelve() != null && !ktwelves.contains(teacher.getKtwelve())) {
                desc = (teacher.isPrimarySchool()?"小学":"中学") + "老师";
            }
        } else {
            return failMessage("暂不支持用户类型:" + user.fetchUserType().getDescription());
        }
        if (desc == null) {
            return successMessage();
        }
        return failMessage("401", String.format("检测到您已注册一起%s账号，请使用一起%sAPP操作，或联系客服处理：4001601717", desc, desc));
    }
}
