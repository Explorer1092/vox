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

package com.voxlearning.wechat.controller;

import com.voxlearning.alps.annotation.meta.RoleType;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.spi.core.HttpClientType;
import com.voxlearning.alps.spi.monitor.FlightRecorder;
import com.voxlearning.alps.webmvc.cookie.CookieManager;
import com.voxlearning.alps.webmvc.cookie.CookieManagerFactory;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.ai.api.ChipsWechatUserService;
import com.voxlearning.utopia.service.ai.constant.WechatUserType;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.wechat.constants.AuthType;
import com.voxlearning.wechat.constants.WechatInfoCode;
import com.voxlearning.wechat.constants.WechatRegisterEventType;
import com.voxlearning.wechat.support.WechatUserInfo;
import com.voxlearning.wechat.support.WechatUserInfoHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

/**
 * @author Xin Xin
 * @since 10/22/15
 */
@Controller
@Slf4j
@RequestMapping(value = "/")
public class OAuthController extends AbstractController {
    @ImportService(interfaceClass = ChipsWechatUserService.class)
    private ChipsWechatUserService chipsWechatUserService;

    //主动服务跳转url时拼接字符
    public static final String activeServiceRedirectUrlSeperator = ";";
    //微信家长通授权回调方法
    //用户点击授权链接后，微信回调本方法，传回code参数
    //如果用户没有绑定过，则跳转到登录绑定页;如果已绑定过，则跳转到学生列表页
    //openId写入cookie
    @RequestMapping(value = "/parent_auth.vpage", method = RequestMethod.GET)
    public String parentAuth(HttpServletRequest request, HttpServletResponse response, Model model) throws IOException {
        String code = request.getParameter("code");
        String state = request.getParameter("state");

        String openId = null;
        try {
            if (null == AuthType.of(state)) {
                return redirectWithMsg("未知类型回调", model);
            }

            FlightRecorder.dot("beforeGetOpenIdByCode");
            openId = getOpenIdByCode(code, WechatType.PARENT);
            FlightRecorder.dot("afterGetOpenIdByCode");
            if (null == openId) {
                return infoPage(WechatInfoCode.AUTH_FETCH_OPEN_ID_FROM_CODE_FAIL, model);
            }

            log(state, openId);

            return "redirect:" + auth(request, response, state, openId, "/signup/parent/login.vpage");
        } catch (Exception ex) {
            log.error("parent auth error,openId:{} code:{} state:{}", openId, code, state, ex);
        }
        return redirectWithMsg("授权认证失败", model);
    }

    @RequestMapping(value = "/teacher_auth.vpage", method = RequestMethod.GET)
    public String teacherAuth(HttpServletRequest request, HttpServletResponse response, Model model) {
        String code = request.getParameter("code");
        String state = request.getParameter("state");

        String openId = null;

        try {
            if (null == AuthType.of(state)) {
                return redirectWithMsg("未知类型回调", model);
            }

            FlightRecorder.dot("beforeGetOpenIdByCode");
            openId = getOpenIdByCode(code, WechatType.TEACHER);
            FlightRecorder.dot("afterGetOpenIdByCode");
            if (null == openId) {
                return infoPage(WechatInfoCode.AUTH_FETCH_OPEN_ID_FROM_CODE_FAIL, model);
            }

            log(state, openId);

            return "redirect:" + auth(request, response, state, openId, "/signup/teacher/login.vpage");
        } catch (Exception ex) {
            logger.error("Teacher OAuth error,openId:{}", openId, ex);
        }
        return redirectWithMsg("授权认证失败", model);
    }

    @RequestMapping(value = "/chips_auth.vpage", method = RequestMethod.GET)
    public String chipsAuth(HttpServletRequest request, HttpServletResponse response, Model model) {
        String code = request.getParameter("code");
        String state = request.getParameter("state");
        String openId = null;

        try {
            if (null == AuthType.of(state)) {
                return redirectWithMsg("未知类型回调", model);
            }

            FlightRecorder.dot("beforeGetOpenIdByCode");
            openId = getOpenIdByCode(code, WechatType.CHIPS);
            FlightRecorder.dot("afterGetOpenIdByCode");
            if (null == openId) {
                return infoPage(WechatInfoCode.AUTH_FETCH_OPEN_ID_FROM_CODE_FAIL, model);
            }

            log(state, openId);
            return "redirect:" + auth(request, response, state, openId, "/signup/chips/verifiedlogin.vpage");
        } catch (Exception ex) {
            logger.error("Chips OAuth error,openId:{}", openId, ex);
        }
        return redirectWithMsg("授权认证失败", model);
    }

    /**
     * 用户基本信息授权
     * @param request
     * @param response
     * @param model
     * @return
     */
    @RequestMapping(value = "/chips_userauth.vpage", method = RequestMethod.GET)
    public String chipsUserAuth(HttpServletRequest request, HttpServletResponse response, Model model) {
        String code = request.getParameter("code");
        String state = request.getParameter("state");
        String openId = null;

        try {
            if (StringUtils.isBlank(state)) {
                return redirectWithMsg("类型为空", model);
            }
            String[] paramters = state.split("_");
            if (paramters == null || paramters.length <= 0) {
                return redirectWithMsg("类型为空", model);
            }
            AuthType type = AuthType.of(paramters[0]);
            if (type == null) {
                return redirectWithMsg("未知类型回调", model);
            }
            FlightRecorder.dot("beforeGetChipsOpenIdByCode");
            Map<String, Object> result = getAuthInfoByCode(code, WechatType.CHIPS);
            openId = SafeConverter.toString(result.get("openid"));
            FlightRecorder.dot("afterGetChipsOpenIdByCode");
            if (StringUtils.isBlank(openId)) {
                return infoPage(WechatInfoCode.AUTH_FETCH_OPEN_ID_FROM_CODE_FAIL, model);
            }

            return "redirect:" + authNoLogin(openId, type, paramters.length > 1 ? paramters[1] : "", SafeConverter.toString(result.get("access_token")));
        } catch (Exception ex) {
            logger.error("Chips user info OAuth error,openId:{}", openId, ex);
        }
        return redirectWithMsg("授权认证失败", model);
    }

    /**
     * 用户基本信息授权包括登录
     * @param request
     * @param response
     * @param model
     * @return
     */
    @RequestMapping(value = "/chips_userlogin.vpage", method = RequestMethod.GET)
    public String chipsUserLogin(HttpServletRequest request, HttpServletResponse response, Model model) {
        String code = request.getParameter("code");
        String state = request.getParameter("state");
        String openId = null;

        try {
            if (StringUtils.isBlank(state)) {
                return redirectWithMsg("类型为空", model);
            }
            String[] paramters = state.split("_");
            if (paramters == null || paramters.length <= 0) {
                return redirectWithMsg("类型为空", model);
            }
            AuthType type = AuthType.of(paramters[0]);
            if (type == null) {
                return redirectWithMsg("未知类型回调", model);
            }
            FlightRecorder.dot("beforeGetChipsOpenIdByCode");
            Map<String, Object> result = getAuthInfoByCode(code, WechatType.CHIPS);
            openId = SafeConverter.toString(result.get("openid"));
            FlightRecorder.dot("afterGetChipsOpenIdByCode");
            if (null == openId) {
                return infoPage(WechatInfoCode.AUTH_FETCH_OPEN_ID_FROM_CODE_FAIL, model);
            }
            // 设置cookies
            getRequestContext().setAuthenticatedOpenId(openId, 7 * 24 * 60 * 60);
            CookieManager cookieManager = CookieManagerFactory.newInstance(request, response);
            cookieManager.setCookieEncrypt("openId", openId, 7 * 24 * 60 * 60);

            return "redirect:" + authLogin(openId, type, paramters.length > 1 ? paramters[1] : "", SafeConverter.toString(result.get("access_token")));
        } catch (Exception ex) {
            logger.error("Chips user info OAuth error,openId:{}", openId, ex);
        }
        return redirectWithMsg("授权认证失败", model);
    }

    // 获取openId 跳转广告页面
    @RequestMapping(value = "/chips_open.vpage", method = RequestMethod.GET)
    public String chipsOpen(HttpServletRequest request, HttpServletResponse response, Model model) {
        String code = request.getParameter("code");
        String openId = null;
        try {
            FlightRecorder.dot("beforeGetOpenIdByCode");
            openId = getOpenIdByCode(code, WechatType.CHIPS);
            FlightRecorder.dot("afterGetOpenIdByCode");
            if (null == openId) {
                return infoPage(WechatInfoCode.AUTH_FETCH_OPEN_ID_FROM_CODE_FAIL, model);
            }
            // 设置cookies
            getRequestContext().setAuthenticatedOpenId(openId, 7 * 24 * 60 * 60);
            String state = request.getParameter("state");
            if (StringUtils.isBlank(state)) {
                return "redirect:/signup/chips/verifiedlogin.vpage";
            }

            String redirectUrl = "/chips/center/index.vpage";
            long userId;
            if (state.contains(AuthType.CHIPS_CENTER.getType())) {
                userId = SafeConverter.toLong(state.replace(AuthType.CHIPS_CENTER.getType(), ""), 0L);
                redirectUrl = "/chips/center/robin.vpage?inviter=" + (Long.compare(userId, 0L) > 0 ? userId : "");
            } else if (state.contains(AuthType.CHIPS_OFFICIAL_PRODUCT_AD.getType())) {
                userId = SafeConverter.toLong(state.replace(AuthType.CHIPS_OFFICIAL_PRODUCT_AD.getType(), ""), 0L);
                redirectUrl = "/chips/center/robinnormal.vpage?inviter=" + (Long.compare(userId, 0L) > 0 ? userId : "");
            } else if (state.contains(AuthType.CHIPS_ROBIN_NEW.getType())) {
                String[] strings = SafeConverter.toString(state.replace(AuthType.CHIPS_ROBIN_NEW.getType(), ""), "").split("_");
                String productId = strings != null && strings.length > 0 ? strings[0] : "";
                String staffId = strings != null && strings.length > 1 ? strings[1] : "";
                redirectUrl = "/chips/order/create.vpage?productId="+ productId+ "&si=" + staffId;
            }else if (state.contains(AuthType.CHIPS_GROUP_SHOPPING.getType())) {
                String[] strings = state.split("_");
                String group = strings != null && strings.length > 0? strings[1] : "";

                String paramterKey = strings != null && strings.length > 1 ? strings[2] : "";
                String value = getPersistenceStringValue(paramterKey);
                String[] paramters = StringUtils.isNotBlank(value) ? value.split("_") : null;
                String productName = paramters != null && paramters.length > 0 ? paramters[0] : "";
                String productId = paramters != null && paramters.length > 1 ? paramters[1] : "";

                redirectUrl = "/chips/order/create.vpage?group=" + group + "&productName=" + productName + "&productId="+ productId ;
            } else if (AuthType.of(state) != null && AuthType.of(state) == AuthType.CHIPS_UGC) {
                redirectUrl = "/chips/ugc/collect.vpage";
            } else if (AuthType.of(state) != null && AuthType.of(state) == AuthType.CHIPS_UGC_ORAL) {
                redirectUrl = "/chips/ugc/oral_test.vpage";
            } else if (AuthType.of(state) != null && AuthType.of(state) == AuthType.CHIPS_UGC_MAIL) {
                redirectUrl = "/chips/center/emailQuestionnaire.vpage";
            } else if (state.contains(AuthType.CHIPS_ACTIVE_SERVICE.getType())) {
//                String[] strings = SafeConverter.toString(state.replace(AuthType.CHIPS_ACTIVE_SERVICE.getType(), ""), "").split("_");
                redirectUrl = buildActiveServiceRedirectUrl(AuthType.CHIPS_ACTIVE_SERVICE, "/chips/center/activeServicePreviewV2.vpage", state);
            } else if (state.contains(AuthType.CHIPS_OTHER_SERVICE.getType())) {
                redirectUrl = buildActiveServiceRedirectUrl(AuthType.CHIPS_OTHER_SERVICE, "/chips/center/otherServiceTypePreview.vpage", state);
            }

            return "redirect:/signup/chips/verifiedlogin.vpage?returnUrl=" + URLEncoder.encode(redirectUrl, "UTF-8");
        } catch (Exception ex) {
            logger.error("Chips OAuth error,openId:{}", openId, ex);
        }
        return redirectWithMsg("授权认证失败", model);
    }

    private String buildActiveServiceRedirectUrl(AuthType authType, String url, String state) {
        String[] split = SafeConverter.toString(state.replace(authType.getType(), ""), "").split(activeServiceRedirectUrlSeperator);
        boolean firstFlag = true;
        StringBuffer sb = new StringBuffer();
        sb.append(url);
        for (String s : split) {
            if (firstFlag) {
                sb.append("?");
                firstFlag = false;
            } else {
                sb.append("&");
            }
            sb.append(s);
        }
        return sb.toString();
    }

    private String auth(HttpServletRequest request, HttpServletResponse response, String state, String openId, String loginUrl) throws IOException, ServletException {
        // 设置openId cookie
        getRequestContext().setAuthenticatedOpenId(openId, 7 * 24 * 60 * 60);
        //检查openId是否已被绑定
        User user = wechatLoaderClient.loadWechatUser(openId);
        if (null != user) {//已绑定
            //设置登录cookie
            UserAuthentication ua = userLoaderClient.loadUserAuthentication(user.getId());
            getRequestContext().saveAuthenticateState(7 * 24 * 60 * 60, user.getId(), ua.getPassword(), openId, RoleType.of(user.getUserType()));
            return redirect(AuthType.of(state));
        } else {
            return loginUrl + "?returnUrl=" + URLEncoder.encode(redirect(AuthType.of(state)), "UTF-8");
        }
    }
    private String authNoLogin(String openId, AuthType type, String paramKey, String snsAccessToken) throws IOException, ServletException {
        Long userId = null;
        // 设置openId cookie
        getRequestContext().setAuthenticatedOpenId(openId, 7 * 24 * 60 * 60);
        //检查openId是否已被绑定
        User user = wechatLoaderClient.loadWechatUser(openId);
        if (null != user) {//已绑定
            //设置登录cookie
            UserAuthentication ua = userLoaderClient.loadUserAuthentication(user.getId());
            getRequestContext().saveAuthenticateState(7 * 24 * 60 * 60, user.getId(), ua.getPassword(), openId, RoleType.of(user.getUserType()));
            userId = user.getId();
        }
        WechatUserInfo info = WechatUserInfoHelper.getSNSUserInfo(snsAccessToken, openId);
        String nick = Optional.ofNullable(info)
                .map(WechatUserInfo::getNickname)
                .orElse("");
        String avatar = Optional.ofNullable(info)
                .map(WechatUserInfo::getHeadimgurl)
                .orElse("");
        chipsWechatUserService.register(openId, WechatUserType.CHIPS_OFFICIAL_ACCOUNTS.name(), nick, avatar, userId);
        String val = StringUtils.isNoneBlank(paramKey) ? getPersistenceStringValue(paramKey) : "";
        return getAuthRedirectUrl(type) + (StringUtils.isNotBlank(val) ? ("?" + val ) : "");
    }

    private String authLogin(String openId, AuthType type, String paramKey, String snsAccessToken) throws IOException, ServletException {
        WechatUserInfo info = WechatUserInfoHelper.getSNSUserInfo(snsAccessToken, openId);
        String nick = Optional.ofNullable(info)
                .map(WechatUserInfo::getNickname)
                .orElse("");
        String avatar = Optional.ofNullable(info)
                .map(WechatUserInfo::getHeadimgurl)
                .orElse("");

        String val = StringUtils.isNoneBlank(paramKey) ? getPersistenceStringValue(paramKey) : "";
        String returnUrl = getAuthRedirectUrl(type) + (StringUtils.isNotBlank(val) ? ("?" + val ) : "");
        // 设置openId cookie
        getRequestContext().setAuthenticatedOpenId(openId, 7 * 24 * 60 * 60);
        //检查openId是否已被绑定
        User user = wechatLoaderClient.loadWechatUser(openId);
        if (null != user) {//已绑定
            //设置登录cookie
            UserAuthentication ua = userLoaderClient.loadUserAuthentication(user.getId());
            getRequestContext().saveAuthenticateState(7 * 24 * 60 * 60, user.getId(), ua.getPassword(), openId, RoleType.of(user.getUserType()));
            chipsWechatUserService.register(openId, WechatUserType.CHIPS_OFFICIAL_ACCOUNTS.name(), nick, avatar, user.getId());
            return returnUrl;
        }
        chipsWechatUserService.register(openId, WechatUserType.CHIPS_OFFICIAL_ACCOUNTS.name(), nick, avatar, null);
        return "/signup/chips/verifiedlogin.vpage?returnUrl=" + URLEncoder.encode(returnUrl, "UTF-8");
    }

    private void log(String state, String openId) {
        List<String> logInfo = getLogInfo(AuthType.of(state));
        if (!CollectionUtils.isEmpty(logInfo)) {
            Map<String, String> log = new HashMap<>();
            log.put("module", logInfo.get(0));
            log.put("op", logInfo.get(1));
            log.put("s0", openId);
            super.log(log);
        }
    }

    private String getOpenIdByCode(String code, WechatType type) {
        Map<String, Object> result = getAuthInfoByCode(code, type);
        return SafeConverter.toString(result.get("openid"), null);
    }

    private Map<String, Object> getAuthInfoByCode(String code, WechatType type) {
        String url = StringUtils.formatMessage("https://api.weixin.qq.com/sns/oauth2/access_token?appid={}&secret={}&code={}&grant_type=authorization_code", ProductConfig.get(type.getAppId()), ProductConfig.get(type.getAppSecret()), code);
        AlpsHttpResponse response = HttpRequestExecutor.instance(HttpClientType.POOLING).get(url).execute();
        if (null == response.getResponseString()) {
            logger.warn("Get openId by code from weixin failed, response nothing.");
            return Collections.emptyMap();
        }

        Map<String, Object> result = JsonUtils.fromJson(response.getResponseString());
        if (!MapUtils.isEmpty(result) && !result.keySet().contains("errcode")) {
           return result;
        } else {
           logger.error("Get openId by code from oauth failed,code:{},response:{}", code, response.getResponseString());
        }
        return Collections.emptyMap();
    }

    private String redirect(AuthType type) throws IOException, ServletException {
        String queryString = getRequestContext().getRequest().getQueryString();
        String url = getAuthRedirectUrl(type);
        if (!StringUtils.isBlank(queryString)) {
            if (url.contains("?")) {
                url = url + "&" + queryString;
            } else {
                url = url + "?" + queryString;
            }
        }
        return url;
    }

    private String getAuthRedirectUrl(AuthType type) {
        String url;
        switch (type) {
            case CHIPS_CENTER:
                url = "/chips/center/index.vpage";
                break;
            case CHIPS_CRATE_ORDER:
                url = "/chips/center/reservepay.vpage";
                break;
            case CHIPS_INVITATION:
                url = "/chips/center/invite.vpage";
                break;
            case CHIPS_STUDY_REWARD:
                url = "/chips/center/reward.vpage";
                break;
            case CHIPS_STUDY_SUMMARY:
                url = "/chips/center/planmethod.vpage";
                break;
            case CHIPS_STUDY_FINISH:
                url = "/chips/center/report.vpage";
                break;
            case CHIPS_STUDY_LIST:
                url = "/chips/center/travelcatalog.vpage";
                break;
            case CHIPS_STUDY_CERTIFICATE:
                url = "/chips/center/getcertificate.vpage";
                break;
            case CHIPS_OFFICIAL_PRODUCT_AD:
                url = "/chips/center/robinnormal.vpage";
                break;
            case CHIPS_STUDY_FINAL_REPORT:
                url = "/chips/center/report.vpage";
                break;
            case CHIPS_FORMAL_AD_1:
                url = "/chips/center/1/formal_robin.vpage";
                break;
            case CHIPS_FORMAL_AD_2:
                url = "/chips/center/2/formal_robin.vpage";
                break;
            case CHIPS_FORMAL_AD_3:
                url = "/chips/center/3/formal_robin.vpage";
                break;
            case CHIPS_FORMAL_AD_4:
                url = "/chips/center/be_normal.vpage";
                break;
            case CHIPS_FORMAL_AD_5_1:
                url = "/chips/center/prod3/formal_3.vpage";
                break;
            case CHIPS_FORMAL_AD_5_2:
                url = "/chips/center/prod4/formal_3.vpage";
                break;
            case CHIPS_OPEN_AD_6_COUNCIL_SCHOOL:
                url = ProductConfig.getMainSiteBaseUrl() + "/view/mobile/parent/parent_ai/be_s6_council_school";
                break;
            case CHIPS_OPEN_AD_7_GRADE:
                url = ProductConfig.getMainSiteBaseUrl() + "/view/mobile/parent/parent_ai/be/f_index";
                break;
            case CHIPS_OPEN_ADDRESS_CHECK:
                url = ProductConfig.getMainSiteBaseUrl() + "/view/mobile/parent/parent_ai/get_address";
                break;
            case CHIPS_UGC:
                url = "/chips/ugc/collect.vpage";
                break;
            case CHIPS_UGC_ORAL:
                url = "/chips/ugc/oral_test.vpage";
                break;
            case CHIPS_UGC_MAIL:
                url = "/chips/center/emailQuestionnaire.vpage";
                break;
            case CHIPS_ACTIVE_SERVICE:
                url = "/chips/center/activeServicePreviewV2.vpage";
                break;
            case CHIPS_OTHER_SERVICE:
                url = "/chips/center/otherServiceTypePreview.vpage";
                break;
            case CHIPS_GROUP_SHOPPING:
                url = "/chips/center/formal_group_buy.vpage";
                break;
            case CHIPS_INVITATION2:
                url = "/chips/center/invite_award_activity.vpage";
                break;
            case CHIPS_INVITATION_PIC:
                url = "/chips/center/invite_award_activity.vpage";
                break;
            case CHIPS_INVITATION_BE:
                url = "/chips/center/invite_be.vpage";
                break;
            case CHIPS_CRATE_ORDERV2:
                url = "/chips/order/create.vpage";
                break;
            case CHIPS_PERSONAL_REWARD:
                url = "/chips/center/invite_personal_center.vpage";
                break;
            case CHIPS_STUDY_INFORMATION:
                url = "/chipsv2/center/study_information.vpage";
                break;
            case CHIPS_REPORT_V2:
                url = "/chips/center/reportV2.vpage";
                break;
            case CHIPS_RENEW:
                url = "/chips/center/otherServiceTypePreview.vpage";
                break;
            case CHIPS_ACTIVITY_LEAD:
                url = "/chips/activity/waiting_for_you.vpage";
                break;
            case CHIPS_DRAWING_TASK_JOIN:
                url = "/chips/task/drawing_update.vpage";
                break;
            default:
                url = "/chips/center/index.vpage";
                break;
        }
        return url;
    }
    private List<String> getLogInfo(AuthType type) {
        List<String> info = new LinkedList<>();
        switch (type) {
            case HOMEWORK:
                info.add("homework");
                info.add("homework_wechat_menu");
                break;
            case REPORT:
                info.add("report");
                info.add("report_wechat_menu");
                break;
            case UCENTER:
                info.add("ucenter");
                info.add("ucenter_wechat_menu");
                break;
            case PARADISE:
                info.add("paradise");
                info.add("paradise_wechat_menu");
                break;
            case SMART:
                info.add("smart");
                info.add("smart_wechat_menu");
                break;
            case STAR_REWARD:
                info.add("ucenter");
                info.add("ucenter_starreward_wechat_menu");
                break;
            case PARENT_REWARD:
                info.add("parentreward");
                info.add("parentreward_wechat_menu");
                break;
            default:
                break;
        }
        return info;
    }
}
