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

package com.voxlearning.ucenter.support.controller;

import com.voxlearning.alps.annotation.meta.Ktwelve;
import com.voxlearning.alps.annotation.meta.PasswordState;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.runtime.TopLevelDomain;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.utopia.api.constant.AppAuditAccounts;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.service.SeiueSyncDataService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * abstract controller for web access
 * use cookie to get request releated info
 *
 * @author changyuan.liu
 * @since 2015.12.6
 */
public class AbstractWebController extends AbstractController {

    @ImportService(interfaceClass = SeiueSyncDataService.class)
    protected SeiueSyncDataService seiueSyncDataService;

    /* ======================================================================================
       以下代码负责 captcha
       ====================================================================================== */
    protected void saveCaptchaCode(String token, String code) {
        ucenterWebCacheSystem.CBS.unflushable.set("Captcha:" + token, 600, code);
    }

    protected boolean consumeCaptchaCode(String token, String code) {
        if (StringUtils.isEmpty(code))
            return false;

        String cacheKey = "Captcha:" + token;
        CacheObject<String> cacheObject = ucenterWebCacheSystem.CBS.unflushable.get(cacheKey);
        if (cacheObject == null) {
            return false;
        }
        String except = cacheObject.getValue();
        boolean r = StringUtils.equals(StringUtils.trim(code), except);
        if (r) {
            ucenterWebCacheSystem.CBS.unflushable.delete(cacheKey);
        }
        return r;
    }




    /* ======================================================================================
       以下代码负责常用的跳转
       ====================================================================================== */

    /**
     * 生成跳转地址
     *
     * @param ktwelve
     * @param url
     * @return
     * @author changyuan.liu
     */
    protected String getPlatformWebRedirectStr(Ktwelve ktwelve, String url, boolean isKlxTeacher) {
        if (isKlxTeacher) {
            return "redirect:" + ProductConfig.getKuailexueUrl();
        }
        if (ktwelve == Ktwelve.PRIMARY_SCHOOL || ktwelve == Ktwelve.INFANT) {
            return "redirect:" + ProductConfig.getMainSiteBaseUrl() + url;
        } else if (ktwelve == Ktwelve.JUNIOR_SCHOOL) {
            return "redirect:" + ProductConfig.getJuniorSchoolUrl() + url;
        } else {
            return "redirect:" + ProductConfig.getMainSiteBaseUrl() + url;
        }
    }

    protected String getUserAvatarImgUrl(User user) {
        return getUserAvatarImgUrl(user.fetchImageUrl());
    }

    protected String getUserAvatarImgUrl(String imgFile) {
        String imgUrl;
        if (!StringUtils.isEmpty(imgFile)) {
            imgUrl = "gridfs/" + imgFile;
        } else {
            imgUrl = "upload/images/avatar/avatar_default.png";
        }

        return getCdnBaseUrlAvatarWithSep() + imgUrl;
    }

    protected String getCdnBaseUrlAvatarWithSep() {
        return cdnResourceUrlGenerator.getCdnBaseUrlAvatarWithSep(getWebRequestContext().getRequest());
    }

    protected String fetchMainsiteUrlByCurrentSchema() {
        if (getWebRequestContext().isHttpsRequest()) {
            return "https://www." + TopLevelDomain.getTopLevelDomain();
        }
        return "http://www." + TopLevelDomain.getTopLevelDomain();
    }

    /* ======================================================================================
       包班制支持
       一个老师多学科情况,拿到指定学科的老师
       ====================================================================================== */

    protected Long getSubjectSpecifiedTeacherId() {
        return getSubjectSpecifiedTeacherId(currentSubject());
    }

    protected Long getSubjectSpecifiedTeacherId(Subject subject) {
        Teacher teacher = currentTeacher();
        if (subject == null) {
            return teacher != null ? teacher.getId() : null;
        }

        // 多学科支持
        if (teacher != null) {
            if (subject != teacher.getSubject()) {// 此时需要根据学科切换当前老师的班级信息
                Long id = teacherLoaderClient.loadRelTeacherIdBySubject(teacher.getId(), subject);
                if (id != null) {
                    return id;
                }
            }
        }
        return teacher != null ? teacher.getId() : null;
    }

    protected Teacher getSubjectSpecifiedTeacher() {
        return getSubjectSpecifiedTeacher(currentSubject());
    }

    protected Teacher getSubjectSpecifiedTeacher(Subject subject) {
        Teacher teacher = currentTeacher();
        if (subject == null) {
            return teacher;
        }

        // 多学科支持
        if (teacher != null) {
            if (subject != teacher.getSubject()) {// 此时需要根据学科切换当前老师的班级信息
                Long id = teacherLoaderClient.loadRelTeacherIdBySubject(teacher.getId(), subject);
                Teacher t = teacherLoaderClient.loadTeacher(id);
                if (t != null) {
                    teacher = t;
                }
            }
        }
        return teacher;
    }

    protected TeacherDetail getSubjectSpecifiedTeacherDetail() {
        return getSubjectSpecifiedTeacherDetail(currentSubject());
    }

    protected TeacherDetail getSubjectSpecifiedTeacherDetail(Subject subject) {
        TeacherDetail teacher = currentTeacherDetail();
        if (subject == null) {
            return teacher;
        }

        // 多学科支持
        if (teacher != null) {
            if (subject != teacher.getSubject()) {// 此时需要根据学科切换当前老师的班级信息
                Long id = teacherLoaderClient.loadRelTeacherIdBySubject(teacher.getId(), subject);
                TeacherDetail t = teacherLoaderClient.loadTeacherDetail(id);
                if (t != null) {
                    teacher = t;
                }
            }
        }
        return teacher;
    }

    protected Subject currentSubject() {
        String subjectStr = getRequestString("subject");
        return StringUtils.isNotEmpty(subjectStr) ? Subject.valueOf(subjectStr) : null;
    }

    /**
     * 如果老师是通过批量注册的方式注册进来，而且没改过密码，直接去修改密码页面
     */
    protected boolean needForceModifyPassword(Long schoolId, User user) {
        if (AppAuditAccounts.isNoneForcePasswdUpdateSchool(schoolId) || !user.isBatchUser()) {
            return false;
        }

        UserAuthentication ua = userLoaderClient.loadUserAuthentication(user.getId());
        return ua != null && (ua.fetchPasswordState() == PasswordState.AUTO_GEN);
    }

    /**
     * 通过 CommonConfig 获取学校的映射关系
     * 测试 : 陈经纶中学(高中部)(414008)
     * 线上 : 陈经纶中学(高中部)(405492)
     */
    protected boolean isCJLSchool(Long schoolId) {
        if (schoolId == null) {
            return false;
        }
        try {
            String schoolMapConfig = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(
                    ConfigCategory.MIDDLE_PLATFORM_GENERAL.getType(), "CJL_SCHOOL_MAP"
            );

            Map<String, Long> schoolIdMap = new HashMap<>();
            Stream.of(schoolMapConfig.split(",")).forEach(pair -> {
                String[] split = pair.split(":");
                schoolIdMap.put(split[0], SafeConverter.toLong(split[1]));
            });
            return !schoolIdMap.isEmpty() && schoolIdMap.containsValue(schoolId);
        } catch (Exception ignored) {
            return false;
        }
    }

    /**
     * 希悦平台学校
     */
    protected boolean isSeiueSchool(Long schoolId) {
        return Objects.equals(seiueSyncDataService.loadSchoolId(), schoolId);
    }

}
