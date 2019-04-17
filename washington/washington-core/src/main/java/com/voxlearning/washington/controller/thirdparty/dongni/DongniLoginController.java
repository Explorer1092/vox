package com.voxlearning.washington.controller.thirdparty.dongni;

import com.voxlearning.alps.annotation.meta.RoleType;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.utopia.core.utils.LoggerUtils;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.mappers.NeonatalUser;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.washington.controller.thirdparty.base.VendorLoginController;
import com.voxlearning.washington.controller.thirdparty.base.VendorUserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Base64;
import java.util.Date;
import java.util.Map;

import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * 懂你登录
 * @author chongfeng.qi
 * @date 20181204
 *
 * 400 toast 信息
 * 401 密码错误
 */
@Controller
@RequestMapping("/auth/vendor/dongni")
@Slf4j
public class DongniLoginController extends VendorLoginController {

    private static final String APP_KEY = "DongNi";
    // 懂你登录页面
    private static final String AUTH_URL = "https://m.dongni100.com/api/auth/logout";
    private static final String CLIENT_ID = "17zuoye";
    private static final String CLIENT_SECRET = "0n1fRa3Z92d2";
    // 懂你回调地址
    private static final String REDIRECT_URI = ProductConfig.getMainSiteBaseUrl() + "/view/mobile/student/third_party_login/middle_page.vpage";
    // auto2 验证
    private static String AUTHORIZE;
    static {
        AUTHORIZE = "Basic " + Base64.getEncoder().encodeToString((CLIENT_ID + ":" + CLIENT_SECRET).getBytes());
    }

    /**
     * 懂你登录跳转
     *
     * @return
     *
     */
    @RequestMapping(value="redirect.vpage", method = RequestMethod.GET)
    public String redirect() {
        return "redirect:" + AUTH_URL + "?response_type=code&state=EJdsT&client_id=" + CLIENT_ID + "&scope=read&redirect_uri="
                + REDIRECT_URI;
//        return "redirect:" +  ProductConfig.getMainSiteBaseUrl() + "/view/mobile/student/third_party_login/offline.vpage";
    }

    /**
     * 登录回调地址
     * @return
     */
    @RequestMapping(value="user.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage user() {
        String code = getRequestString("code");
        if (StringUtils.isBlank(code)) {
            return failMessage("code为空");
        }
        VendorUserContext context = new VendorUserContext();
        context.setCode(code);
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
        // 密码
        String password = getRequestString("user_password");
        // 姓名
        String userName = getRequestString("user_name");
        // 第三方用户id
        String vendorUserId = getRequestString("vendor_user_id");
        if (StringUtils.isAnyBlank(mobile, code, password, userName, vendorUserId)) {
            return failMessage("参数错误");
        }
        VendorApps vendorApps = vendorApps();
        if (vendorApps == null || vendorApps.getAppKey() == null) {
            return failMessage("应用状态错误");
        }
        if (userLoaderClient.loadMobileAuthentication(mobile, UserType.STUDENT) != null) {
            return failMessage("手机号已经注册");
        }
        // 注册17账号
        NeonatalUser neonatalUser = new NeonatalUser();
        neonatalUser.setPassword(password);
        neonatalUser.setUserType(UserType.STUDENT);
        neonatalUser.setRoleType(RoleType.ROLE_STUDENT);
        neonatalUser.setRealname(userName);
        neonatalUser.setMobile(mobile);
        neonatalUser.setCode(code);
        neonatalUser.setWebSource(APP_KEY);
        MapMessage registerMessage = userServiceClient.registerUser(neonatalUser);
        if (!registerMessage.isSuccess()) {
            String errorMessage = registerMessage.getInfo();
            if (registerMessage.get("attributes") != null) {
                errorMessage =  errorMessage + registerMessage.get("attributes");
            }
            return failMessage(errorMessage);
        }
        User user = (User) registerMessage.get("user");
        // 绑定用户
        thirdPartyService.persistLandingSource(appKey(), vendorUserId, userName, user.getId());
        // 登录返回
        LoggerUtils.info("landing_login_"+ appKey(), user.getId(), new Date());
        return successMessage().add(RES_SESSION_KEY, attachUser2RequestApp(user.getId()));
    }

    /**
     * 登录
     * @return
     */
    @RequestMapping(value="login.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage login() {
        String mobile = getRequestString("user_mobile");
        String password = getRequestString("user_password");
        String userName = getRequestString("user_name");
        String vendorUserId = getRequestString("vendor_user_id");
        if (StringUtils.isAnyBlank(mobile, password, userName, vendorUserId)) {
            return failMessage("参数错误, 请重新输入");
        }
        VendorApps vendorApps = vendorApps();
        if (vendorApps == null || vendorApps.getAppKey() == null) {
            return failMessage("应用状态错误");
        }
        UserAuthentication userAuthentication = userLoaderClient.loadMobileAuthentication(mobile, UserType.STUDENT);
        if (userAuthentication == null) {
            return failMessage("手机号未注册账号");
        }
        if (!userAuthentication.verifyPassword(password)) {
            return failMessage("401", "密码输入有误，请重试，如忘记密码请返回至登录页找回密码或联系客服处理：4001601717");
        }
        StudentDetail student = studentLoaderClient.loadStudentDetail(userAuthentication.getId());
        if (student.getClazz() != null && student.isPrimaryStudent()) {
            return failMessage("402", "检测到您已注册一起小学学生账号，如需换至中学请联系客服处理后再进行绑定：4001601717");
        }
        // 绑定用户
        thirdPartyService.persistLandingSource(appKey(), vendorUserId, userName, userAuthentication.getId());
        // 登录返回
        LoggerUtils.info("landing_login_"+ appKey(), userAuthentication.getId(), new Date());
        return successMessage().add(RES_SESSION_KEY, attachUser2RequestApp(userAuthentication.getId()));
    }


    @Override
    public UserAuthentication loadMobileAuthentication(String mobile) {
        return  userLoaderClient.loadMobileAuthentication(mobile, UserType.STUDENT);
    }

    @Override
    public void token(VendorUserContext context) {
        String responseString = HttpRequestExecutor.defaultInstance()
                .post("https://m.dongni100.com/api/auth/oauth/token")
                .addParameter(MapUtils.map("grant_type", "authorization_code", "code", context.getCode(), "client_id", CLIENT_ID, "redirect_uri", REDIRECT_URI))
                .headers(MapUtils.map("Content-Type", "application/x-www-form-urlencoded", "Authorization", AUTHORIZE)).execute().getResponseString();

        Map<String, Object> tokenMap = JsonUtils.fromJson(responseString);
        if (tokenMap == null) {
            context.setMapMessage(failMessage("token信息获取失败"));
            return;
        }
        String accessToken = SafeConverter.toString(tokenMap.get("access_token"));
        if (StringUtils.isBlank(accessToken)) {
            context.setMapMessage(failMessage("token不存在"));
            return;
        }
        context.setToken(accessToken);
    }

    @Override
    public void initUser(VendorUserContext context) {
        /**
         * {"active":true,
         * "exp":1542720744,
         * "user_name":"admins",
         * "client_id":"17zuoye",
         * "scope":["all"],
         * "userInfo":{"userId":123456,
         *  "userName":"啦啦啦德玛西亚",
         *  "phone":"13800138000",
         *  },
         * }
         */
        String requestUser = HttpRequestExecutor.defaultInstance()
                .get("https://m.dongni100.com/api/auth/oauth/check_token?token=" + context.getToken())
                .execute().getResponseString();
        Map<String, Object> userMap = JsonUtils.fromJson(requestUser);
        if (userMap == null) {
            context.setMapMessage(failMessage("获取用户异常"));
            return;
        }
        Map<String, Object> userInfo = JsonUtils.safeConvertObjectToMap(userMap.get("userInfo"));
        if (userInfo == null) {
            context.setMapMessage(failMessage("获取用户信息异常"));
            return;
        }
        context.setVendorUserId(SafeConverter.toString(userInfo.get("userId")));
        context.setMobile(SafeConverter.toString(userInfo.get("phone")));
        context.setUserName(SafeConverter.toString(userInfo.get("userName")));
    }

    @Override
    public void result(VendorUserContext context) {
        // 如果已经绑定了，直接登录
        if (context.fetchIsBand()) {
            context.setMapMessage(successMessage().add(RES_SESSION_KEY, attachUser2RequestApp(context.getUserId())));
            return;
        }
        // 注册的时候给页面cid，发验证码的时候要用
        MapMessage mapMessage = successMessage();
        if (!context.fetchIsRegister()) {
            String contextId = RandomUtils.randomString(10);
            washingtonCacheSystem.CBS.unflushable.set("VrfCtxIp_" + contextId, 10 * 60, getWebRequestContext().getRealRemoteAddress());
            mapMessage.add("cid", contextId);
        }
        mapMessage.add("isRegister", context.fetchIsRegister()).add("vendorUserId", context.getVendorUserId()).add("mobile", context.getMobile()).add("userName", context.getUserName());
        context.setMapMessage(mapMessage);
    }

    @Override
    public String appKey() {
        return APP_KEY;
    }
}
