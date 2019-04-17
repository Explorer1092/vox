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

package com.voxlearning.washington.interceptor;

import com.voxlearning.alps.core.util.ArrayUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.spi.webmvc.RequestHandler;
import com.voxlearning.alps.spi.webmvc.ServletRequest;
import com.voxlearning.alps.spi.webmvc.ServletResponse;
import com.voxlearning.alps.webmvc.interceptor.AbstractRequestHandlerInterceptor;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import com.voxlearning.washington.controller.open.AbstractOpenController;
import com.voxlearning.washington.data.OpenAuthContext;
import com.voxlearning.washington.service.OpenApiAuth;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class OpenAuthInterceptor extends AbstractRequestHandlerInterceptor {
    static public final String ATTRIBUTE_NAME_CONTEXT = "OpenAuthInterceptorContext";
    private static Logger logger = LoggerFactory.getLogger(OpenAuthInterceptor.class);
    @Inject
    UserLoaderClient userLoaderClient;
    @Inject
    private OpenApiAuth openApiAuth;

    private static boolean isAjax(HttpServletRequest request) {
        String header = request.getHeader("X-Requested-With");
        return header != null && "XMLHttpRequest".equals(header);
    }

    @Override
    public boolean preHandle(ServletRequest request, ServletResponse response, RequestHandler handler) throws Exception {
        boolean passed = false;
        Map<String, Object> params = new HashMap<>(0);
        try {
            String json = IOUtils.toString(request.getServletRequest().getInputStream(), "UTF-8");
            if (StringUtils.isNotBlank(json)) {
                params = JsonUtils.fromJson(json);
            }

            if (params == null) {
                return false;
            }

            // 排除login.vpage和logout.vpage
            // 这里好像不需要做反劫持处理？
            String[] anonymousUrlPaths = new String[]{
                    "/open/signup.vpage",
                    "/open/recoverpwd.vpage",
                    "/open/login.vpage",
                    "/open/logout.vpage",
                    "/open/test.vpage",
            };

            //2014-1-17 目前open接口只有商城在用
            //2014-04-16 微信公众号也使用了
            //2014-10-21 家长通也使用了
            if (!ArrayUtils.contains(anonymousUrlPaths, request.getServletRequest().getRequestURI())) {
                if ((params.get("uid") != null || params.get("_c") != null) && params.get("token") != null) {
                    Object uid = params.get("uid");
                    //微信python版里有些调用是不带uid的,会用_c,以免根据uid参数统计用户活跃时影响数据(2015-11-18)
                    if (null == uid) {
                        uid = params.get("_c").toString();
                    }
                    String accessToken = ConversionUtils.toString(params.get("token"));

                    //验证签名
                    if (openApiAuth.isSignValid(accessToken, uid.toString())) {
                        passed = true;
                    }
                }
            } else {
                // 登录和退出页不需要鉴权
                passed = true;
            }
        } catch (Exception e) {
            logger.error("应用授权错误", e);
        }

        logger.debug("OAuth result" + passed);
        if (passed) {
            // 鉴权通过 设置提交参数
            OpenAuthContext openAuthContext = new OpenAuthContext(params, "200", null);
            request.getServletRequest().setAttribute(ATTRIBUTE_NAME_CONTEXT, openAuthContext);

            Object bean = handler.getBean();
            if (bean instanceof AbstractOpenController) {
                // temp fix
//                List<User> users = userLoaderClient.loadUsers(SafeConverter.toString(params.get("uid")), null);
//                if (CollectionUtils.isNotEmpty(users)) {
//                    ((AbstractOpenController) bean).logLoginCount(request);
//                } else {
//                    logger.error("[OPEN API AUTH] no user info found. uid: {}, url: {}, token: {}",
//                            params.get("uid"), request.getRequestURI(), params.get("token"));
//                }
            }

            return super.preHandle(request, response, handler);
        } else {
            if (isAjax(request.getServletRequest())) {
                params.remove("password"); // 删除敏感数据
                OpenAuthContext openAuthContext = new OpenAuthContext(params, "403", "用户未授权，请重新登录。");
                response.getServletResponse().setContentType("application/json;charset=UTF-8");
                try {
                    response.getServletResponse().getWriter().write(JsonUtils.toJson(openAuthContext));
                } catch (IOException e) {
                    throw new ServletException(e);
                }
            } else {
                String redirect = "redirect:/login.vpage";
                logger.warn("OAuth not passed, redirect: " + redirect);
                try {
                    response.getServletResponse().sendRedirect(redirect);
                } catch (IOException e) {
                    throw new ServletException(e);
                }
            }
            return false;
        }
    }
}
