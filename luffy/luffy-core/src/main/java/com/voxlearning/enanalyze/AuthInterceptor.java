package com.voxlearning.enanalyze;

import com.alibaba.fastjson.JSON;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.webmvc.RequestHandler;
import com.voxlearning.alps.spi.webmvc.ServletRequest;
import com.voxlearning.alps.spi.webmvc.ServletResponse;
import com.voxlearning.alps.webmvc.interceptor.AbstractRequestHandlerInterceptor;
import com.voxlearning.enanalyze.aggregate.UserAggregator;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

/**
 * 鉴权拦截器
 *
 * @author xiaolei.li
 * @version 2018/7/6
 */
public class AuthInterceptor extends AbstractRequestHandlerInterceptor {

    public final static String COOKIE_KEY_TOKEN = "TOKEN";

    @Resource
    private UserAggregator aggregator;

    @Override
    public boolean preHandle(ServletRequest request, ServletResponse response, RequestHandler handler) throws Exception {
        Cookie[] cookies = request.getServletRequest().getCookies();
        boolean isValid = false;
        if (null != cookies) {
            Cookie tokenCookie = Arrays.stream(cookies).filter(
                    cookie -> COOKIE_KEY_TOKEN.equals(cookie.getName()))
                    .findFirst().get();
            if (null != tokenCookie) {
                final String token = tokenCookie.getValue();
                isValid = aggregator.isValid(token);
                Session.setToken(token);
                Session.setOpenId(token);
            }
        }
        if (!isValid) {
            // 非法时，返回固定的错误信息
            HttpServletResponse servletResponse = response.getServletResponse();
            servletResponse.setContentType("application/json;charset=UTF-8");
            MapMessage message = ViewBuilder.error(ViewCode.SESSION_INVALID);
            servletResponse.getWriter().append(JSON.toJSONString(message));
        }
        return isValid;
    }

    @Override
    public void afterCompletion(ServletRequest request, ServletResponse response, RequestHandler handler, Exception ex) throws Exception {
        super.afterCompletion(request, response, handler, ex);
        Session.purge();
    }
}
