package com.voxlearning.washington.filter;

import com.voxlearning.alps.annotation.meta.RoleType;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.webmvc.support.DefaultContext;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.core.runtime.ProductDevelopment;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.washington.support.WashingtonAuthenticationHandler;
import com.voxlearning.washington.support.WashingtonRequestContext;
import org.springframework.beans.BeansException;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

/**
 * 这个filter是为了屏蔽中学老师访问小学老师页面的
 * 逐步增多点功能，跨域支持也先放这吧
 *
 * @author changyuan.liu
 * @since 2016.1.7
 */
public class WashingtonFilter extends OncePerRequestFilter {

    private TeacherLoaderClient teacherLoaderClient;
    private LinkedHashMap<String, List<RoleType>> limitedUrls;

    // 跨域配置
    private Set<String> allowedDomain;
    private List<String> needCorsPath = new ArrayList<String>() {
        {
            add("/student/homework/");
            add("/student/vacation/homework/");
            add("/flash/loader/");
            add("/appdata/");
            add("/student/exam/");
            add("/exam/flash/");
        }
    };

    @Override
    protected void initFilterBean() throws ServletException {
        super.initFilterBean();

        WebApplicationContext wac = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
        if (wac != null) {
            try {
                teacherLoaderClient = wac.getBean(TeacherLoaderClient.class);
                WashingtonAuthenticationHandler washingtonAuthenticationHandler = wac.getBean(WashingtonAuthenticationHandler.class);

                Field field = WashingtonAuthenticationHandler.class.getSuperclass().getDeclaredField("limitedUrls");
                field.setAccessible(true);
                limitedUrls = (LinkedHashMap<String, List<RoleType>>)field.get(washingtonAuthenticationHandler);
            } catch (BeansException ex) {
                throw new ServletException("no TeacherLoaderClient bean found", ex);
            } catch (NoSuchFieldException ex) {
                throw new ServletException("no such field exception", ex);
            } catch (IllegalAccessException ex) {
                throw new ServletException("illegal access exception", ex);
            }
        }

        allowedDomain = new HashSet<>();
        if (ProductDevelopment.isProductionEnv()) {
            allowedDomain.add("cdn-cnc.17zuoye.cn");
            allowedDomain.add("cdn-bsy.17zuoye.cn");
            allowedDomain.add("cdn-ali.17zuoye.cn");
            allowedDomain.add("cdn.17zuoye.com");

            allowedDomain.add("17zuoye.com");
            allowedDomain.add("api.17zuoye.com");
        } else if (ProductDevelopment.isStagingEnv()) {
            allowedDomain.add("staticshared.staging.17zuoye.net");
            allowedDomain.add("cdn-cnc.staging.17zuoye.net");
            allowedDomain.add("cdn-cc.staging.17zuoye.net");

            allowedDomain.add("www.staging.17zuoye.net");
            allowedDomain.add("api.staging.17zuoye.net");
        } else {
            allowedDomain.add("cdn-static-shared.test.17zuoye.net");
            allowedDomain.add("cdn-cnc.test.17zuoye.net");
            allowedDomain.add("cdn-cc.test.17zuoye.net");
            allowedDomain.add("cdn-cnc.content.17zuoye.net");

            allowedDomain.add("www.test.17zuoye.net");
            allowedDomain.add("api.test.17zuoye.net");
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        WashingtonRequestContext context = (WashingtonRequestContext) DefaultContext.get();
        if (context != null) {
            boolean isAuthUrl = false;// 是否是需要身份验证的url
            for (Map.Entry<String, List<RoleType>> rule : limitedUrls.entrySet()) {
                if (context.getRequest().getServletPath().startsWith(rule.getKey())) {
                    isAuthUrl = true;
                }
            }
            if (isAuthUrl
                    && !context.getRequest().getServletPath().contains("feedback")
                    && !context.getRequest().getServletPath().startsWith("/tts/")
                    && !context.getRequest().getServletPath().startsWith("/reward/")
                    && !context.getRequest().getServletPath().startsWith("/campaign/")
                    && !context.getRequest().getServletPath().startsWith("/teacher/newexam/")
                    && !context.getRequest().getServletPath().startsWith("/teacher/activity/term2017")
                    && !context.getRequest().getServletPath().startsWith("/teacher/invite/")// FIXME: 2018/3/1 中学英语老师邀请英语老师活动加上的
                    && !context.getRequest().getServletPath().startsWith("/teacher/activity/term2018")// FIXME: 2018/3/5 中学英语老师抽奖加上的#61861
                    && !context.getRequest().getServletPath().startsWith("/teacher/activity/")
                    && !context.getRequest().getServletPath().startsWith("/connect/survey/callback")) {// 中小学共用
                if (CollectionUtils.isNotEmpty(context.getRoleTypes())
                        && context.getRoleTypes().contains(RoleType.ROLE_TEACHER)) {
                    Teacher teacher = teacherLoaderClient.loadTeacher(context.getUserId());
                    if (teacher != null && teacher.isJuniorTeacher()) {
                        context.getResponse().sendRedirect(ProductConfig.getUcenterUrl());
                        return;
                    }
                }
            }

            String origin = request.getHeader(HttpHeaders.ORIGIN);
            if (StringUtils.isNotEmpty(origin)) {
                int ind = origin.indexOf("://");
                if (ind > 0 && (ind + 3) < origin.length()) {
                    String originHost = origin.substring(ind + 3);
                    if (allowedDomain.contains(originHost)) {
                        for (String path : needCorsPath) {
                            if (context.getRequest().getServletPath().startsWith(path)) {
                                response.addHeader("Access-Control-Allow-Origin", origin);
                                // 允许的方法名
                                response.addHeader("Access-Control-Allow-Methods", "POST, GET");
                                // 允许服务端访问的客户端请求头，多个请求头用逗号分割
                                response.addHeader("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With");
                                // 预检验请求时间30分钟
                                response.addHeader("Access-Control-Max-Age", "3600");
                                response.addHeader("Access-Control-Allow-Credentials", "true");
                                break;
                            }
                        }
                    }
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
