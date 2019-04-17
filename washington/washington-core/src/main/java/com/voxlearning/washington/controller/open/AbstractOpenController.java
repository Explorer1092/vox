/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.washington.controller.open;


import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.washington.data.OpenAuthContext;
import com.voxlearning.washington.interceptor.OpenAuthInterceptor;
import com.voxlearning.washington.support.AbstractController;

import javax.servlet.http.HttpServletRequest;

public class AbstractOpenController extends AbstractController {


    public static OpenAuthContext getOpenAuthContext(HttpServletRequest request) {
        return (OpenAuthContext) request.getAttribute(OpenAuthInterceptor.ATTRIBUTE_NAME_CONTEXT);
    }

    protected String getOpenAuthParameter(String key, String def) {
        String v = (String) (getOpenAuthContext(getRequest()).getParams().get(key));
        return v == null ? def : v;
    }

    protected String getOpenAuthString(String key) {
        String v = (String) (getOpenAuthContext(getRequest()).getParams().get(key));
        return v == null ? "" : v;
    }

    protected boolean getOpenAuthBool(String name) {
        return ConversionUtils.toBool(getOpenAuthContext(getRequest()).getParams().get(name));
    }

    protected long getOpenAuthLong(String name, long def) {
        return ConversionUtils.toLong(getOpenAuthContext(getRequest()).getParams().get(name), def);
    }

    protected long getOpenAuthLong(String name) {
        return ConversionUtils.toLong(getOpenAuthContext(getRequest()).getParams().get(name));
    }

    protected int getOpenAuthInt(String name, int def) {
        return ConversionUtils.toInt(getOpenAuthContext(getRequest()).getParams().get(name), def);
    }

    protected int getOpenAuthInt(String name) {
        return ConversionUtils.toInt(getOpenAuthContext(getRequest()).getParams().get(name));
    }

    protected double getOpenAuthDouble(String name) {
        return ConversionUtils.toDouble(getRequest().getParameter(name), 0D);
    }

    protected static OpenAuthContext successContext(OpenAuthContext context) {
        context.setCode("200");
        return context;
    }

    protected static OpenAuthContext errorContext(OpenAuthContext context, String error) {
        return errorContext(context, "400", error);
    }

    protected static OpenAuthContext errorContext(OpenAuthContext context, String code, String error) {
        context.setCode(code);
        context.setError(error);
        return context;
    }

    // 记录登录次数
//    public void logLoginCount(HttpServletRequest request) {
//        OpenAuthContext context = getOpenAuthContext(request);
//        Long userId = ConversionUtils.toLong(context.getParams().get("uid"));
//        if (userId > 0) {
//            long count = userCacheClient.cookieLoginCountManager.increase(userId);
//            if (count == 1) {
//                // the first time cookie authentication within today
//                userServiceClient.createUserRecord(userId, getWebRequestContext().getRealRemoteAddr());
//            }
//        }
//    }

    /* *****************************************
        包班制支持
       ***************************************** */

    protected Long getTeacherIdBySubject(OpenAuthContext openAuthContext) {
        Long userId = SafeConverter.toLong(openAuthContext.getParams().get("uid"));
        Object subjectObj = openAuthContext.getParams().get("subject");
        String subject = SafeConverter.toString(subjectObj);
        if (StringUtils.isBlank(subject)) {
            return userId;
        } else {
            return teacherLoaderClient.loadRelTeacherIdBySubject(userId, Subject.of(subject));
        }
    }

}
