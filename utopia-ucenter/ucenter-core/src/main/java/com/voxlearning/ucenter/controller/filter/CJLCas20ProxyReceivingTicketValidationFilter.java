package com.voxlearning.ucenter.controller.filter;

import com.voxlearning.utopia.core.runtime.ProductConfig;
import org.jasig.cas.client.util.AbstractConfigurationFilter;
import org.jasig.cas.client.validation.Cas20ProxyReceivingTicketValidationFilter;

import javax.servlet.*;
import java.io.IOException;

/**
 * Created by Yuechen.Wang on 2017/7/25.
 */
public class CJLCas20ProxyReceivingTicketValidationFilter extends AbstractConfigurationFilter {

    private FilterConfig filterConfig;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        Cas20ProxyReceivingTicketValidationFilter proxyFilter = new Cas20ProxyReceivingTicketValidationFilter();
        proxyFilter.init(filterConfig);

        proxyFilter.setServerName(ProductConfig.getUcenterUrl());

        proxyFilter.doFilter(request, response, chain);
    }

    @Override
    // empty implementation as most filters won't need this.
    public void destroy() {
        // nothing to do
    }

}
