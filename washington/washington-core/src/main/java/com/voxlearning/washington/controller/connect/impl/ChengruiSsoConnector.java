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

package com.voxlearning.washington.controller.connect.impl;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.service.user.api.constants.SsoConnections;
import com.voxlearning.washington.controller.connect.AbstractSsoConnector;
import org.apache.commons.codec.digest.DigestUtils;

import javax.inject.Named;
import java.text.MessageFormat;
import java.util.Map;

/**
 * @author Jia HuanYin
 * @since 2015/11/16
 */
@Named
@Deprecated
public class ChengruiSsoConnector extends AbstractSsoConnector {

    private static final String USER_INFO_API = "http://gxsy.ilaoshi.cc:7814/api/Login/GetUserInfoByToken?";
    private static final String APP_KEY = SsoConnections.Chengrui.getClientId();
    private static final String APP_SECRET = SsoConnections.Chengrui.getSecretId();
    private static final String SIGN_PATTERN = APP_KEY + ",{0}" + APP_SECRET;
    private static final String TEACHER_CODE = "2";
    private static final String STUDENT_CODE = "3";
    private static final String PARENT_CODE = "4";

    @Override
    public MapMessage validateToken(SsoConnections connectionInfo, String token) {
        if (StringUtils.isBlank(token)) {
            logger.error("Blank token");
            return MapMessage.errorMessage("Blank token");
        }
        String sig = sign(token);
        Map<String, Object> params = MiscUtils.m("token", token, "sig", sig);
        String URL = UrlUtils.buildUrlQuery(USER_INFO_API, params);
        String resp = HttpRequestExecutor.defaultInstance().get(URL).execute().getResponseString();
        Map<String, Object> jsonMap = JsonUtils.fromJson(resp);
        if (jsonMap == null) {
            logger.error("Illegal user info resp = {}", resp);
            return MapMessage.errorMessage("Illegal user info response");
        }
        Map userInfo = (Map) jsonMap.get("data");
        if (userInfo == null) {
            logger.error("Illegal user info resp = {}", resp);
            return MapMessage.errorMessage("Illegal user info response");
        }
        String userID = String.valueOf(userInfo.get("UserID"));
        if (StringUtils.isBlank(userID)) {
            logger.error("Blank userID with user info resp = {}", resp);
            return MapMessage.errorMessage("Got blank userID");
        }
        MapMessage message = MapMessage.successMessage();
        message.add("userId", userID);
        message.add("userName", userInfo.get("EmpName"));
        message.add("userCode", userCode(userInfo.get("UserType")));
        message.add("userMobile", userInfo.get("Mobile"));
        return message;
    }

    protected static String sign(String token) {
        String source = MessageFormat.format(SIGN_PATTERN, token);
        return DigestUtils.md5Hex(source).toUpperCase();
    }

    private static Integer userCode(Object code) {
        String iCode = String.valueOf(code);
        switch (iCode) {
            case TEACHER_CODE:
                return UserType.TEACHER.getType();
            case STUDENT_CODE:
                return UserType.STUDENT.getType();
            case PARENT_CODE:
                return UserType.PARENT.getType();
            default:
                return null;
        }
    }
}
