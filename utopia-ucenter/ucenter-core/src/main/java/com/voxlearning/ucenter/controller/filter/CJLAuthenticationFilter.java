package com.voxlearning.ucenter.controller.filter;

import com.voxlearning.utopia.core.runtime.ProductConfig;
import org.jasig.cas.client.authentication.AuthenticationFilter;

import javax.servlet.*;
import java.io.IOException;

/**
 * Created by Yuechen.Wang on 2017/7/25.
 */
public class CJLAuthenticationFilter implements Filter {

    private FilterConfig filterConfig;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        AuthenticationFilter authFilter = new AuthenticationFilter();
        authFilter.init(filterConfig);

        authFilter.setServerName(ProductConfig.getUcenterUrl());

        authFilter.doFilter(request, response, chain);
    }

    @Override
    // empty implementation as most filters won't need this.
    public void destroy() {
        // nothing to do
    }
}
