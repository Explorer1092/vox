package com.voxlearning.utopia.service.surl.utils;

import com.voxlearning.alps.webmvc.support.context.UtopiaHttpRequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by xinxin on 15/1/2016.
 */
public class SurlRequestContext extends UtopiaHttpRequestContext {
    public SurlRequestContext(HttpServletRequest request, HttpServletResponse response) {
        super(request, response);
    }
}
