package com.voxlearning.ucenter.controller.connect;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.spi.core.RuntimeModeLoader;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.ucenter.controller.connect.impl.CneduSsoConnector;
import com.voxlearning.ucenter.support.context.UcenterRequestContext;
import com.voxlearning.ucenter.support.controller.AbstractWebController;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.user.api.constants.SsoConnections;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/**
 * 中央电教馆云平台对接处理
 * Created by Alex on 15-3-10.
 */
@Controller
@RequestMapping("/")
@Slf4j
@NoArgsConstructor
public class CneduController extends AbstractWebController {

    private static final String SOURCE_CNEDU = "Cnedu";
    private static final String QR_ACCESS_URL = "/auth/vendor/cnedu/redirect.vpage";

    @Inject private CneduSsoConnector cneduSsoConnector;


    @RequestMapping(value = "cnedulogin/index.vpage", method = RequestMethod.GET)
    public String gotoHomePage(Model model) {
        String ticket = getRequestString("ticket");
        return "redirect:/ssologin/cnedu.vpage?token=" + ticket;
    }

    @RequestMapping(value = "cnedulogin/pteapc.vpage", method = RequestMethod.GET)
    public String gotoPteapcHomePage(Model model) {
        String ticket = getRequestString("ticket");

        MapMessage userInfoMap = mockRequest(UserType.TEACHER);
        if (userInfoMap == null) {
            userInfoMap = cneduSsoConnector.validateToken(SsoConnections.CneduTeacher, ticket);
        }

        if (userInfoMap.isSuccess()) {
            String userCode = SafeConverter.toString(userInfoMap.get("userCode"));
            if ("TEACHER".equals(userCode)) {
                String token = generateUserDataToken(userInfoMap);
                String url = buildQrurl("a17teacher://platform.17zuoye.client/third_login", token);

                model.addAttribute("qrurl", url);
            } else {
                model.addAttribute("info", "本应用暂不支持老师以外用户访问");
            }
        }

        model.addAttribute("application", "17teacher");

        return "sso/cnedu";
    }

    @RequestMapping(value = "cnedulogin/pteamob.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage gotoPteaMobHomePage() {
        // 设置跨域
        addCros();

        String ticket = getRequestString("ticket");

        MapMessage userInfoMap = mockRequest(UserType.TEACHER);
        if (userInfoMap == null) {
            userInfoMap = cneduSsoConnector.validateToken(SsoConnections.CneduTeacher, ticket);
        }

        if (userInfoMap.isSuccess()) {
            String userCode = SafeConverter.toString(userInfoMap.get("userCode"));
            if ("TEACHER".equals(userCode)) {
                String token = generateUserDataToken(userInfoMap);
                String url = buildQrurl("a17teacher://platform.17zuoye.client/third_login", token);
                return MapMessage.successMessage()
                        .add("qrurl", url)
                        .add("application", "17teacher");
            } else {
                return MapMessage.errorMessage("本应用暂不支持老师以外用户访问");
            }
        } else {
            return MapMessage.errorMessage("无效的ticket，请刷新页面重新进入");
        }
    }

    @RequestMapping(value = "cnedulogin/pstupc.vpage", method = RequestMethod.GET)
    public String gotoPstuPcHomePage(Model model) {
        String ticket = getRequestString("ticket");

        MapMessage userInfoMap = mockRequest(UserType.STUDENT);
        if (userInfoMap == null) {
            userInfoMap = cneduSsoConnector.validateToken(SsoConnections.CneduStudent, ticket);
        }

        if (userInfoMap.isSuccess()) {
            String userCode = SafeConverter.toString(userInfoMap.get("userCode"));
            if ("STUDENT".equals(userCode)) {
                String token = generateUserDataToken(userInfoMap);
                String url = buildQrurl("a17zuoye://platform.17zuoye.client/third_login", token);
                model.addAttribute("qrurl", url);
            } else {
                model.addAttribute("info", "本应用暂不支持学生以外用户访问");
            }
        }

        model.addAttribute("application", "17student");

        return "sso/cnedu";
    }

    @RequestMapping(value = "cnedulogin/pstumob.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage gotoPstuMobHomePage() {
        // 设置跨域
        addCros();

        String ticket = getRequestString("ticket");

        MapMessage userInfoMap = mockRequest(UserType.STUDENT);
        if (userInfoMap == null) {
            userInfoMap = cneduSsoConnector.validateToken(SsoConnections.CneduStudent, ticket);
        }

        if (userInfoMap.isSuccess()) {
            String userCode = SafeConverter.toString(userInfoMap.get("userCode"));
            if ("STUDENT".equals(userCode)) {
                String token = generateUserDataToken(userInfoMap);
                String url = buildQrurl("a17zuoye://platform.17zuoye.client/third_login", token);

                return MapMessage.successMessage()
                        .add("qrurl", url)
                        .add("application", "17student");
            } else {
                return MapMessage.errorMessage("本应用暂不支持学生以外用户访问");
            }
        } else {
            return MapMessage.errorMessage("无效的ticket，请刷新页面重新进入");
        }
    }

//    @RequestMapping(value = "cnedulogin/pparpc.vpage", method = RequestMethod.GET)
//    public String gotoPparPcHomePage(Model model) {
//        String ticket = getRequestString("ticket");
//
//        String url = buildQrurl("a17parent://platform.17zuoye.client/third_login", ticket);
//
//        model.addAttribute("qrurl", url);
//        model.addAttribute("app", "17Parent");
//
//        return "sso/cnedu";
//    }

    @RequestMapping(value = "cnedulogin/mteapc.vpage", method = RequestMethod.GET)
    public String gotoMteaPcHomePage(Model model) {
        String ticket = getRequestString("ticket");

        MapMessage userInfoMap = mockRequest(UserType.TEACHER);
        if (userInfoMap == null) {
            userInfoMap = cneduSsoConnector.validateToken(SsoConnections.CneduJuniorTea, ticket);
        }

        if (userInfoMap.isSuccess()) {
            String userCode = SafeConverter.toString(userInfoMap.get("userCode"));
            if ("TEACHER".equals(userCode)) {
                String token = generateUserDataToken(userInfoMap);
                String url = buildQrurl("a17middleteacher://platform.17zuoye.client/third_login", token);

                model.addAttribute("qrurl", url);
            } else {
                model.addAttribute("info", "本应用暂不支持老师以外用户访问");
            }
        }

        model.addAttribute("application", "17juniorteacher");

        return "sso/cnedu";
    }

    @RequestMapping(value = "cnedulogin/mteamob.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage gotoMteaMobHomePage() {
        // 设置跨域
        addCros();

        String ticket = getRequestString("ticket");

        MapMessage userInfoMap = mockRequest(UserType.TEACHER);
        if (userInfoMap == null) {
            userInfoMap = cneduSsoConnector.validateToken(SsoConnections.CneduJuniorTea, ticket);
        }

        if (userInfoMap.isSuccess()) {
            String userCode = SafeConverter.toString(userInfoMap.get("userCode"));
            if ("TEACHER".equals(userCode)) {
                String token = generateUserDataToken(userInfoMap);
                String url = buildQrurl("a17middleteacher://platform.17zuoye.client/third_login", token);

                return MapMessage.successMessage()
                        .add("qrurl", url)
                        .add("application", "17juniorteacher");
            } else {
                return MapMessage.errorMessage("本应用暂不支持老师以外用户访问");
            }
        } else {
            return MapMessage.errorMessage("无效的ticket，请刷新页面重新进入");
        }
    }

    @RequestMapping(value = "cnedulogin/mstupc.vpage", method = RequestMethod.GET)
    public String gotoMstuPcHomePage(Model model) {
        String ticket = getRequestString("ticket");

        MapMessage userInfoMap = mockRequest(UserType.STUDENT);
        if (userInfoMap == null) {
            userInfoMap = cneduSsoConnector.validateToken(SsoConnections.CneduJuniorStu, ticket);
        }

        if (userInfoMap.isSuccess()) {
            String userCode = SafeConverter.toString(userInfoMap.get("userCode"));
            if ("STUDENT".equals(userCode)) {
                String token = generateUserDataToken(userInfoMap);
                String url = buildQrurl("a17middlestudent://platform.17zuoye.client/third_login", token);

                model.addAttribute("qrurl", url);
            } else {
                model.addAttribute("info", "本应用暂不支持学生以外用户访问");
            }
        }

        model.addAttribute("application", "17juniorstudent");

        return "sso/cnedu";
    }

    @RequestMapping(value = "cnedulogin/mstumob.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage gotoMstuMobHomePage() {
        // 设置跨域
        addCros();

        String ticket = getRequestString("ticket");

        MapMessage userInfoMap = mockRequest(UserType.STUDENT);
        if (userInfoMap == null) {
            userInfoMap = cneduSsoConnector.validateToken(SsoConnections.CneduJuniorStu, ticket);
        }

        if (userInfoMap.isSuccess()) {
            String userCode = SafeConverter.toString(userInfoMap.get("userCode"));
            if ("STUDENT".equals(userCode)) {
                String token = generateUserDataToken(userInfoMap);
                String url = buildQrurl("a17middlestudent://platform.17zuoye.client/third_login", token);

                return MapMessage.successMessage()
                        .add("qrurl", url)
                        .add("application", "17juniorstudent");
            } else {
                return MapMessage.errorMessage("本应用暂不支持学生以外用户访问");
            }
        } else {
            return MapMessage.errorMessage("无效的ticket，请刷新页面重新进入");
        }
    }

//    @RequestMapping(value = "cnedulogin/mparpc.vpage", method = RequestMethod.GET)
//    public String gotoMparPcHomePage(Model model) {
//        String ticket = getRequestString("ticket");
//
//        String url = buildQrurl("a17middleparent://platform.17zuoye.client/third_login", ticket);
//
//        model.addAttribute("qrurl", url);
//        model.addAttribute("app", "17JuniorPar");
//
//        return "sso/cnedu";
//    }

    private String generateUserDataToken(MapMessage userInfoMap) {
        Map<String, String> userDataMap = new HashMap<>();
        userDataMap.put("userId", SafeConverter.toString(userInfoMap.get("userId")));
        userDataMap.put("userName", SafeConverter.toString(userInfoMap.get("name")));
        userDataMap.put("gender", SafeConverter.toString(userInfoMap.get("gender")));
        userDataMap.put("userType", SafeConverter.toString(userInfoMap.get("userCode")));

        String mckey = "c_sso_" + RandomUtils.randomString(16);
        ucenterWebCacheSystem.CBS.unflushable.set(mckey, 1800, userDataMap);
        return mckey;
    }

    private String buildQrurl(String baseUrl, String token) {

        Map<String, String> urlParams = new HashMap<>();
        urlParams.put("source", SOURCE_CNEDU);
        urlParams.put("token", token);

        String qraccess = UrlUtils.buildUrlQuery(ProductConfig.getMainSiteBaseUrl() + QR_ACCESS_URL, urlParams);

        urlParams = new HashMap<>();
        urlParams.put("url", qraccess);

//        String m = UrlUtils.buildUrlQuery(baseUrl, urlParams);
//        String qrbase = ProductConfig.getMainSiteBaseUrl() + "/qrcode";
//
//        urlParams = new HashMap<>();
//        urlParams.put("m", m);

        return UrlUtils.buildUrlQuery(baseUrl, urlParams);
    }

    private void addCros() {
        // 设置跨域
        UcenterRequestContext context = getWebRequestContext();
        if (RuntimeModeLoader.getInstance().isProduction()) {
            context.getResponse().addHeader("Access-Control-Allow-Origin", "https://www.17zuoye.com");
        } else if (RuntimeModeLoader.getInstance().isStaging()) {
            context.getResponse().addHeader("Access-Control-Allow-Origin", "https://www.staging.17zuoye.net");
        } else if (RuntimeModeLoader.getInstance().isTest()) {
            context.getResponse().addHeader("Access-Control-Allow-Origin", "https://www.test.17zuoye.net");
        }
        context.getResponse().addHeader("Access-Control-Allow-Methods", "GET, POST");
        context.getResponse().addHeader("Access-Control-Allow-Headers", "x-requested-with");
        context.getResponse().addHeader("Access-Control-Max-Age", "1800");
        context.getResponse().addHeader("Access-Control-Allow-Credentials", "true");
    }

    private MapMessage mockRequest(UserType userType) {
        boolean test = StringUtils.isNotBlank(getRequestString("test"));
        if (!test) {
            return null;
        }


        return MapMessage.successMessage().add("userId", "mock_user_"+ (getRequestString("userId") != null ?getRequestString("userId"):""))
                .add("name", "测试" + userType.getDescription())
                .add("gender", "N")
                .add("userCode", userType.name());
    }

    public static void main(String[] args) {
        System.out.println(new CneduController().buildQrurl("a17middlestudent://platform.17zuoye.client/third_login", "xxxaa"));
    }

}
