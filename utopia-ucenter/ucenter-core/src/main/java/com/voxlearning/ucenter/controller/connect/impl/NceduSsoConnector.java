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

package com.voxlearning.ucenter.controller.connect.impl;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.ucenter.controller.connect.AbstractSsoConnector;
import com.voxlearning.utopia.service.user.api.constants.SsoConnections;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.codec.digest.DigestUtils;

import javax.inject.Named;
import java.text.MessageFormat;
import java.util.List;

/**
 * 南昌教育平台SSO对接
 *
 * @author Jia HuanYin
 * @since 2015/9/8
 */
@Named
public class NceduSsoConnector extends AbstractSsoConnector {

    private static final String USER_INFO_API = "http://cloud.ncedu.gov.cn/Sites/SSO/YQZuoyeAPI.aspx";
    private static final String CLIENT_ID = SsoConnections.Ncedu.getClientId();
    private static final String CLIENT_SECRET = SsoConnections.Ncedu.getSecretId();
    private static final String SIGN_SOURCE_PATTERN = "app_key=" + CLIENT_ID + "&token={0}" + CLIENT_SECRET;
    private static final String TEACHER_CODE = "1";
    private static final String STUDENT_CODE = "2";
    private static final String PARENT_CODE = "3";

    @Override
    public MapMessage validateToken(SsoConnections connectionInfo, String token) {
        if (StringUtils.isBlank(token)) {
            logger.error("Blank token");
            return MapMessage.errorMessage("Blank token");
        }
        String sig = sign(token);
        String resp = HttpRequestExecutor.defaultInstance().post(USER_INFO_API)
                .addParameter("token", token)
                .addParameter("sig", sig)
                .execute().getResponseString();
        List<UserInfo> respList = JsonUtils.fromJsonToList(resp, UserInfo.class);
        if (respList == null || respList.isEmpty()) {
            logger.error("Illegal user info resp = {}", resp);
            return MapMessage.errorMessage("Illegal user info response");
        }
        UserInfo userInfo = respList.get(0);
        String userid = userInfo.getUserid();
        if (StringUtils.isBlank(userid)) {
            logger.error("Blank userid with user info resp = {}", resp);
            return MapMessage.errorMessage("Got blank userid");
        }
        MapMessage message = MapMessage.successMessage();
        message.add("userId", userid);
        message.add("userName", userInfo.getXm());
        message.add("userCode", userCode(userInfo.getSf()));
        message.add("userMobile", userInfo.getSjh());
        return message;
    }

    private static String sign(String token) {
        String source = MessageFormat.format(SIGN_SOURCE_PATTERN, token);
        return DigestUtils.md5Hex(source);
    }

    private static Integer userCode(String code) {
        switch (code) {
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

    @Getter
    @Setter
    @NoArgsConstructor
    private static class UserInfo {
        private String Code;
        private String userid;
        private String xm;
        private String sf;
        private String sjh;
    }
}