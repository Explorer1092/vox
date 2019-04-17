package com.voxlearning.luffy.interceptor;

import com.alibaba.fastjson.JSON;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.webmvc.RequestHandler;
import com.voxlearning.alps.spi.webmvc.ServletRequest;
import com.voxlearning.alps.spi.webmvc.ServletResponse;
import com.voxlearning.alps.webmvc.interceptor.AbstractRequestHandlerInterceptor;
import com.voxlearning.luffy.support.SessionHelper;
import com.voxlearning.utopia.service.ai.constant.ChipsErrorType;

import javax.inject.Inject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;


public class ChipsAuthInterceptor extends AbstractRequestHandlerInterceptor {

    private final static String COOKIE_KEY_TOKEN = "CHIPSMINITOKEN";

    @Inject
    private SessionHelper sessionHelper;

    @Override
    public boolean preHandle(ServletRequest request, ServletResponse response, RequestHandler handler) throws Exception {
        Cookie[] cookies = request.getServletRequest().getCookies();
        boolean isValid = false;
        if (null != cookies) {
            Cookie tokenCookie = Arrays.stream(cookies).filter(
                    cookie -> COOKIE_KEY_TOKEN.equals(cookie.getName()))
                    .findFirst().orElse(null);
            if (null != tokenCookie) {
                final String token = tokenCookie.getValue();
                isValid = sessionHelper.validToken(token);
                request.getServletRequest().setAttribute("openId", token);
            }
        }

        if (!isValid) {
            HttpServletResponse servletResponse = response.getServletResponse();
            servletResponse.setContentType("application/json;charset=UTF-8");
            MapMessage message = MapMessage.errorMessage().setErrorCode(ChipsErrorType.NEED_LOGIN.getInfo()).setInfo(ChipsErrorType.NEED_LOGIN.getCode());
            servletResponse.getWriter().append(JSON.toJSONString(message));
        }
        return isValid;
    }

    @Override
    public void afterCompletion(ServletRequest request, ServletResponse response, RequestHandler handler, Exception ex) throws Exception {
        super.afterCompletion(request, response, handler, ex);
        request.getServletRequest().removeAttribute("openId");
    }
}
