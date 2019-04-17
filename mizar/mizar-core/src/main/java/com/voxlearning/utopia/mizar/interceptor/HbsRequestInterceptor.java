package com.voxlearning.utopia.mizar.interceptor;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.spi.webmvc.RequestHandler;
import com.voxlearning.alps.spi.webmvc.ServletRequest;
import com.voxlearning.alps.spi.webmvc.ServletResponse;
import com.voxlearning.alps.webmvc.interceptor.AbstractRequestHandlerInterceptor;
import com.voxlearning.alps.webmvc.support.DefaultContext;
import com.voxlearning.utopia.mizar.auth.HbsAuthUser;
import com.voxlearning.utopia.service.mizar.api.entity.hbs.HbsUser;
import com.voxlearning.utopia.service.mizar.consumer.loader.HbsLoaderClient;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 华杯赛的请求拦截器
 * Created by haitian.gan on 2017/2/15.
 */
public class HbsRequestInterceptor extends AbstractRequestHandlerInterceptor {

    private static final Set<String> AUTH_IGNORE_URI;    // 直接放过的URI

    @Inject private MizarCookieHelper mizarCookieHelper;

    @Inject private HbsLoaderClient hbsLoaderClient;

    static {
        AUTH_IGNORE_URI = new HashSet<>();
        AUTH_IGNORE_URI.add("/hbs/score/captcha.vpage");
        AUTH_IGNORE_URI.add("/hbs/score/login.vpage");
        AUTH_IGNORE_URI.add("/hbs/score/msm.vpage");
        AUTH_IGNORE_URI.add("/hbs/score/sendsms.vpage");
        AUTH_IGNORE_URI.add("/hbs/score/verify.vpage");
    }

    @Override
    public boolean preHandle(ServletRequest servletRequest,
                             ServletResponse servletResponse,
                             RequestHandler handler) throws Exception {

        MizarHttpRequestContext context = (MizarHttpRequestContext) DefaultContext.get();
        HttpServletRequest request = servletRequest.getServletRequest();
        HttpServletResponse response = servletResponse.getServletResponse();

        // 请求的相对路径
        String relativeUriPath = request.getRequestURI().substring(request.getContextPath().length());

        // 从cookie当中，取得登录信息
        Long userId = 0L;
        Map<String,Object> requestParams = mizarCookieHelper.getCookMapFromCookie(request);
        if(requestParams != null){
            userId = SafeConverter.toLong(requestParams.get("userId"));
            // 取缓存
            String userCacheKey = HbsAuthUser.ck_user(userId);
            HbsAuthUser hbsAuthUser = CacheSystem.CBS.getCache("unflushable").load(userCacheKey);
            if(hbsAuthUser == null){

                HbsUser user = hbsLoaderClient.loadUser(userId);
                if(user != null){
                    hbsAuthUser = new HbsAuthUser();
                    hbsAuthUser.setUserId(userId);

                    CacheSystem.CBS.getCache("unflushable").set(userCacheKey, DateUtils.getCurrentToDayEndSecond(), hbsAuthUser);
                }
            }

            if(hbsAuthUser != null){
                context.setHbsAuthUser(hbsAuthUser);
            }
        }

        if(AUTH_IGNORE_URI.contains(relativeUriPath) || context.isLoggedIn()){
            return super.preHandle(servletRequest, servletResponse, handler);
        }

        if (requestParams == null || userId == 0L) {
            response.sendRedirect("redirect:" + context.getWebAppContextPath() + "/hbs/score/login.vpage");
            return false;
        }

        return true;
    }

}
