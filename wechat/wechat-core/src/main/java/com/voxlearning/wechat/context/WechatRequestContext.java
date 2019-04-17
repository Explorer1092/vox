/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.wechat.context;

import com.voxlearning.alps.annotation.meta.RoleType;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.webmvc.support.context.UtopiaHttpRequestContext;
import com.voxlearning.utopia.api.constant.OperationSourceType;
import com.voxlearning.utopia.core.cdn.CdnResourceUrlGenerator;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.footprint.client.AsyncFootprintServiceClient;
import com.voxlearning.utopia.service.user.api.constants.UserRecordMode;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserServiceClient;
import lombok.Setter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Xin Xin
 * @since 10/15/15
 */
public class WechatRequestContext extends UtopiaHttpRequestContext {

    @Setter
    private AsyncFootprintServiceClient asyncFootprintServiceClient;

    @Setter
    private UserLoaderClient userLoaderClient;
    @Setter
    private UserServiceClient userServiceClient;
    @Setter
    private TeacherLoaderClient teacherLoaderClient;

    public WechatRequestContext(HttpServletRequest request, HttpServletResponse response) {
        super(request, response);
    }

    public Optional<User> getCurrentUser() {
        Long userId = getUserId();
        if (null != userId) {
            return Optional.of(userLoaderClient.loadUser(userId));
        }
        return Optional.empty();
    }

    @Override
    public Object getRootModelAttribute(String key) {
        switch (key) {
            case "openId":
                return null == getAuthenticatedOpenId() ? "" : getAuthenticatedOpenId();
            case "currentUserId":
                return getUserId();
            case "currentCdnType":
                return CdnResourceUrlGenerator.formalizeCdnType(getRequest()).getKey();
            case "cdnDomainMapKeys":
                return ProductConfig.getCdnDomainMapKeys();
            case "currentUserType":
                Optional<User> user = getCurrentUser();
                return user.isPresent() ? user.get().getUserType() : UserType.ANONYMOUS.getType();
            case "currentSubject": {
                Teacher teacher = teacherLoaderClient.loadTeacher(getUserId());
                if (teacher == null || teacher.getSubject() == null) {
                    return Subject.UNKNOWN.name();
                } else {
                    return teacher.getSubject().name();
                }
                // return null == teacher ? Subject.UNKNOWN.name() : teacher.getSubject().name();
            }
            case "currentSubjectList": {
                Teacher teacher = teacherLoaderClient.loadTeacher(getUserId());
                if (teacher == null) {
                    return JsonUtils.toJson(Collections.emptyList());
                }
                if (CollectionUtils.isNotEmpty(teacher.getSubjects())) {
                    return JsonUtils.toJson(teacher.getSubjects().stream()
                            .map(s -> MiscUtils.m("name", s.name(), "value", s.getValue()))
                            .collect(Collectors.toList()));
                } else {
                    return JsonUtils.toJson(Collections.emptyList());
                }
            }
            case "scheme":
                return isHttpsRequest() ? "https" : "http";
        }

        return super.getRootModelAttribute(key);
    }

    public void saveAuthenticateState(int expire, Long userId, String saltPwd, String openId, RoleType... types) {
        super.saveAuthenticationStates(expire, userId, saltPwd, types);

        if (StringUtils.isNotBlank(openId)) {
            setAuthenticatedOpenId(openId, expire);
        }

        logLoginCount(userId);
    }

    public String getAuthenticatedOpenId() {
        return getCookieManager().getCookieDecrypt("openId", null);
    }

    public String getAuthenticatedChipsOpenId() {
        return getCookieManager().getCookieDecrypt("chips_openId", null);
    }


    public void setAuthenticatedOpenId(String openId, int expire) {
        getCookieManager().setCookieEncrypt("openId", openId, expire);
    }

    public void setAuthenticatedChipsOpenId(String openId, int expire) {
        getCookieManager().setCookieEncrypt("chips_openId", openId, expire);
    }

    // 记录登录次数
    public void logLoginCount(Long userId) {

        if (userId != null && userId > 0) {
            asyncFootprintServiceClient.getAsyncFootprintService().postUserLogin(userId,
                    getRealRemoteAddress(),
                    UserRecordMode.VALIDATE,// 这里应该是记错了，这块应该算VALIDATE
                    OperationSourceType.wechat,
                    true);
        }
    }

    public String getFullRequestUrl() {
        String url = getRequest().getRequestURL().toString();
        String queryString = getRequest().getQueryString();
        if (!StringUtils.isBlank(queryString)) {
            url = url + "?" + queryString;
        }
        if (isHttpsRequest() && url.startsWith("http://")) {
            url = "https://" + url.substring(7);
        }
        return url;
    }
}
