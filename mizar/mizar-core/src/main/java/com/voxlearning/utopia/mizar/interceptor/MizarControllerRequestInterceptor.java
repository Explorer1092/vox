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

package com.voxlearning.utopia.mizar.interceptor;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.spi.webmvc.RequestHandler;
import com.voxlearning.alps.spi.webmvc.ServletRequest;
import com.voxlearning.alps.spi.webmvc.ServletResponse;
import com.voxlearning.alps.webmvc.interceptor.AbstractRequestHandlerInterceptor;
import com.voxlearning.alps.webmvc.support.DefaultContext;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.mizar.auth.MizarAuthUser;
import com.voxlearning.utopia.service.mizar.api.constants.MizarUserRoleType;
import com.voxlearning.utopia.service.mizar.api.entity.user.MizarUser;
import com.voxlearning.utopia.service.mizar.api.entity.user.MizarUserOfficialAccounts;
import com.voxlearning.utopia.service.mizar.api.entity.user.MizarUserSchool;
import com.voxlearning.utopia.service.mizar.consumer.loader.MizarSystemConfigLoaderClient;
import com.voxlearning.utopia.service.mizar.consumer.loader.MizarUserLoaderClient;
import com.voxlearning.utopia.service.mizar.consumer.loader.MizarUserSchoolLoaderClient;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class MizarControllerRequestInterceptor extends AbstractRequestHandlerInterceptor {

    private static final Set<String> AUTH_IGNORE_URI;    // 直接放过的URI
    private static final Set<String> AUTH_IGNORE_ROOT;   // 直接放过的一级功能

    @Inject private MizarUserLoaderClient mizarUserLoaderClient;
    @Inject private MizarUserSchoolLoaderClient mizarUserSchoolLoaderClient;
    @Inject private MizarSystemConfigLoaderClient mizarSystemConfigLoaderClient;

    private static final String INDEX_URL = "/index.vpage";
    private static final String FA_URL = "/auth/fa.vpage";

    @Inject
    private MizarCookieHelper mizarCookieHelper;

    static {
        AUTH_IGNORE_URI = new HashSet<>();
        AUTH_IGNORE_URI.add("/index.vpage");
        AUTH_IGNORE_URI.add("/auth/login.vpage");
        AUTH_IGNORE_URI.add("/auth/logout.vpage");
        AUTH_IGNORE_URI.add("/auth/isLogin.vpage");
        AUTH_IGNORE_URI.add("/auth/getSMSCode.vpage");
        AUTH_IGNORE_URI.add("/auth/getBackPass.vpage");
        AUTH_IGNORE_URI.add("/auth/resetPassword.vpage");
        AUTH_IGNORE_URI.add("/auth/fa.vpage");
        AUTH_IGNORE_URI.add("/hbs/score/captcha.vpage");
        AUTH_IGNORE_URI.add("/hbs/score/login.vpage");
        AUTH_IGNORE_URI.add("/hbs/score/msm.vpage");
        AUTH_IGNORE_URI.add("/hbs/score/result.vpage");
        AUTH_IGNORE_URI.add("/hbs/score/sendsms.vpage");
        AUTH_IGNORE_URI.add("/hbs/score/verify.vpage");
        AUTH_IGNORE_URI.add("/activity/xqb/check_region.vpage");
        AUTH_IGNORE_URI.add("/activity/xqb/sign_up.vpage");
        AUTH_IGNORE_URI.add("/activity/xqb/upload_works.vpage");
        AUTH_IGNORE_URI.add("/activity/xqb/get_region.vpage");
        AUTH_IGNORE_URI.add("/activity/xqb/export_data.vpage");
        AUTH_IGNORE_URI.add("/activity/xqb/get_wechat_config.vpage");

        AUTH_IGNORE_ROOT = new HashSet<>();
        AUTH_IGNORE_ROOT.add("auth");
        AUTH_IGNORE_ROOT.add("common");
        AUTH_IGNORE_ROOT.add("hbs");
        AUTH_IGNORE_ROOT.add("activity");
    }

    @Override
    public boolean preHandle(ServletRequest servletRequest,
                             ServletResponse servletResponse,
                             RequestHandler handler) throws Exception {
        HttpServletRequest request = servletRequest.getServletRequest();
        HttpServletResponse response = servletResponse.getServletResponse();
        MizarHttpRequestContext context = (MizarHttpRequestContext) DefaultContext.get();

        // 设置网站URL的根路径
        context.setWebAppContextPath(HttpRequestContextUtils.getWebAppContextPath(request));
        // 请求的相对路径
        String relativeUriPath = request.getRequestURI().substring(request.getContextPath().length());
        context.setRelativeUriPath(relativeUriPath);

        // 从cookie获取当前登录用户
        String userId = "";
        Map<String, Object> map = mizarCookieHelper.getCookMapFromCookie(request);
        if (map != null) {
            userId = SafeConverter.toString(map.get("userId"));
            String cacheKey = MizarAuthUser.ck_user(userId);
            MizarAuthUser mizarAuthUser = CacheSystem.CBS.getCache("unflushable").load(cacheKey);
            if (mizarAuthUser == null) {
                MizarUser mizarUser = mizarUserLoaderClient.getRemoteReference().loadUser(userId);
                if (mizarUser != null) {
                    mizarAuthUser = new MizarAuthUser();
                    mizarAuthUser.setRealName(mizarUser.getRealName());
                    mizarAuthUser.setAccountName(mizarUser.getAccountName());
                    mizarAuthUser.setMobile(mizarUser.getMobile());
                    mizarAuthUser.setUserId(userId);

                    // 获得用户在所有组下面的角色汇总
                    List<String> belongRoleGroupIds = mizarUserLoaderClient.loadUserRolesInAllDepartments(mizarUser.getId());

                    // 获得所属组的所有角色列表
                    List<Integer> roleList = belongRoleGroupIds.stream()
                            .filter(StringUtils::isNotEmpty)
                            .map(id -> {
                                String[] idParts = id.split("-");
                                if (idParts.length >= 2 && StringUtils.isNumeric(idParts[1])) {
                                    return Integer.parseInt(idParts[1]);
                                }
                                return -1;
                            }).sorted(Integer::compare)
                            .distinct()
                            .collect(Collectors.toList());

                    // 切换新的权限实现，初始化超级管理员用
                    if (CollectionUtils.isEmpty(belongRoleGroupIds) && mizarUser.getAccountName().equals("Mizar")) {
                        mizarAuthUser.setRoleList(Collections.singletonList(MizarUserRoleType.MizarAdmin.getId()));
                        mizarAuthUser.setAuthPathList(Arrays.asList("/config/syspath", "/config/user", "/config/group"));
                    } else {
                        mizarAuthUser.setRoleList(roleList);
                        mizarAuthUser.setAuthPathList(mizarSystemConfigLoaderClient.loadRolePaths(belongRoleGroupIds));
                    }

                    // 加载机构信息
                    if (mizarAuthUser.isShopOwner() || mizarAuthUser.isBD()) {
                        mizarAuthUser.setShopList(mizarUserLoaderClient.loadUserShopId(userId));
                    }

                    // 加载学校信息
                    if (mizarAuthUser.isInfantOp() || mizarAuthUser.isTangramJury()) {
                        List<Long> schoolIds = mizarUserSchoolLoaderClient.loadByUserId(userId)
                                .stream()
                                .map(MizarUserSchool::getSchoolId)
                                .collect(Collectors.toList());
                        mizarAuthUser.setSchoolList(schoolIds);
                    }

                    // 加载公众号信息
                    if (mizarAuthUser.isAdmin() || mizarAuthUser.isOperator()) {
                        List<String> oaList = mizarUserLoaderClient.loadUserOfficialAccounts(userId)
                                .stream()
                                .map(MizarUserOfficialAccounts::getAccountsKey)
                                .collect(Collectors.toList());
                        mizarAuthUser.setOfficialAccountKeyList(oaList);
                    }

                    CacheSystem.CBS.getCache("unflushable").set(cacheKey, DateUtils.getCurrentToDayEndSecond(), mizarAuthUser);
                }
            }
            if (mizarAuthUser != null) {
                context.setMizarAuthUser(mizarAuthUser);
                // 记录用户操作
                Map<String, String[]> paramMap = request.getParameterMap();
                Map<String, String> logInfo = new HashMap<>();
                logInfo.put("app", "mizar");
                logInfo.put("env", RuntimeMode.current().getStageMode());
                logInfo.put("user_ip", context.getRealRemoteAddress());
                logInfo.put("userAgent", request.getHeader("User-Agent"));
                logInfo.put("url", relativeUriPath);
                logInfo.put("requestMethod", request.getMethod());
                logInfo.put("requestTime", DateUtils.dateToString(new Date(), "yyyyMMddHHmmss.SSS"));
                logInfo.put("userId", mizarAuthUser.getUserId() == null ? "" : String.valueOf(mizarAuthUser.getUserId()));
                logInfo.put("userName", mizarAuthUser.getRealName());
                StringBuilder params = new StringBuilder();
                paramMap.forEach((k, v) -> params.append(k).append("=").append(StringUtils.join(v, ",")).append("&"));
                logInfo.put("params", params.toString());
                LogCollector.info("mizar_user_access_log", logInfo);
            }
        }

        if (AUTH_IGNORE_URI.contains(relativeUriPath) || (context.isLoggedIn() && relativeUriPath.equals(INDEX_URL))) {
            return super.preHandle(servletRequest, servletResponse, handler);
        }

        if (map == null || StringUtils.isEmpty(userId)) {
            response.sendRedirect("redirect:" + context.getWebAppContextPath() + INDEX_URL);
            return false;
        }

        Date date = new Date(SafeConverter.toLong(map.get("time")));
        if (DateUtils.dayDiff(date, new Date()) != 0) {
            response.sendRedirect("redirect:" + context.getWebAppContextPath() + INDEX_URL);
            return false;
        }

        // check the auth right
        if (context.isLoggedIn()) {
            // 有登录用户，判断用户权限,若用户没有访问权限，跳转到首页，并提示;若有访问权限，什么也不做
            // 获取systemName和pathName
            Pattern pattern = Pattern.compile("[/]?([^/]+)/");
            Matcher matcher = pattern.matcher(relativeUriPath);
            if (matcher.find()) {
                String[] path = relativeUriPath.split("/");
                String functionName = path[1];
                String pathName = path[2];
                if (!AUTH_IGNORE_ROOT.contains(functionName) && !context.getMizarAuthUser().checkSysAuth(functionName, pathName)) {
                    // 判断是否是ajax请求
                    String header = request.getHeader("X-Requested-With");
                    if (header != null && "XMLHttpRequest".equals(header)) {
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write(JsonUtils.toJson(MapMessage.errorMessage("您没有权限进行此操作")));
                    } else {
                        response.sendRedirect("redirect:" + context.getWebAppContextPath() + FA_URL);
                    }
                }
            }
        }

        return super.preHandle(servletRequest, servletResponse, handler);
    }

    @Override
    public void afterCompletion(ServletRequest request, ServletResponse response, RequestHandler handler, Exception ex) throws Exception {

    }

}
