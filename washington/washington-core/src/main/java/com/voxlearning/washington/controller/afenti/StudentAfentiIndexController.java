/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.washington.controller.afenti;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.base.gray.StudentGrayFunctionManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;
import java.text.MessageFormat;
import java.util.Set;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.config.api.constant.ConfigCategory.PRIMARY_PLATFORM_STUDENT;

/**
 * @author peng.zhang.a
 * @since 16-8-30
 */
@Controller
@RequestMapping("/afenti/api")
public class StudentAfentiIndexController extends StudentAfentiBaseController {

    @Inject private CommonConfigServiceClient commonConfigServiceClient;

    //电脑端入口
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        StudentDetail studentDetail = getStudent();
        Subject subject = getSubject();
        if (studentDetail == null || subject == null) {
            return "redirect:/";
        }

        String url = ProductConfig.getMainSiteBaseUrl() + "/resources/apps/hwh5/afenti/" + version_pc(studentDetail) + "/index.html";
        url = UrlUtils.buildUrlQuery(url, MiscUtils.m("subject", subject.name(),
                "native_version", "2.7.0.0",
                "domain", ProductConfig.getMainSiteBaseUrl(),
                "img_domain", getCdnBaseUrlStaticSharedWithSep(),
                "server_type", RuntimeMode.current().getStageMode(),
                "refer", getRequestParameter("refer", "300003")));
        model.addAttribute("url", url);
        model.addAttribute("sid", studentDetail.getId());
        return "studentv3/afenti/learningplan/index";
    }

    @RequestMapping(value = "/afentiMath/indexMobile.vpage", method = RequestMethod.GET)
    public String afentiMathIndexMobile() {
        String errorPage = "redirect:/";

        StudentDetail studentDetail = getStudent();
        String version = getRequestString("version");
        String module = getRequestString("module");
        String ref = getRequestString("refer");

        if (version == null || studentDetail == null) {
            return errorPage;
        }

        String version_h5 = version_mobile(studentDetail);
        String url = "/resources/apps/hwh5/afenti/{0}/index.html";
        url = cdnResourceVersionCollector.getVersionedUrlPath(MessageFormat.format(url, version_h5));
        url = UrlUtils.buildUrlQuery(url, MiscUtils.m(
                "domain", ProductConfig.getMainSiteBaseUrl(),
                "img_domain", getCdnBaseUrlStaticSharedWithSep(),
                "native_version", version,
                "subject", "MATH",
                "sid", studentDetail.getId(),
                "ref", ref,
                "refer", ref));
        if (StringUtils.isNotBlank(module)) url += ("#" + module);

        return "redirect:" + url;
    }

    @RequestMapping(value = "/afentiEnglish/indexMobile.vpage", method = RequestMethod.GET)
    public String afentiEnglishIndexMobile() {
        StudentDetail studentDetail = getStudent();
        String version = getRequestString("version");
        String module = getRequestString("module");
        String ref = getRequestString("refer");

        if (version == null || studentDetail == null) {
            return "redirect:/";
        }
        String version_h5 = version_mobile(studentDetail);
        String url = "/resources/apps/hwh5/afenti/{0}/index.html";
        // 中学afenti英语进入路径
        if (studentDetail.isJuniorStudent()) {
            url = "/resources/apps/hwh5/afentimiddle/V1_0_0/index.html";
        }
        url = cdnResourceVersionCollector.getVersionedUrlPath(MessageFormat.format(url, version_h5));
        url = UrlUtils.buildUrlQuery(url, MiscUtils.m(
                "domain", ProductConfig.getMainSiteBaseUrl(),
                "img_domain", getCdnBaseUrlStaticSharedWithSep(),
                "native_version", version,
                "subject", "ENGLISH",
                "sid", studentDetail.getId(),
                "ref", ref,
                "refer", ref));
        if (StringUtils.isNotBlank(module)) url += ("#" + module);

        return "redirect:" + url;
    }

    @RequestMapping(value = "/afentiChinese/indexMobile.vpage", method = RequestMethod.GET)
    public String afentiChineseIndexMobile() {
        StudentDetail studentDetail = getStudent();
        String version = getRequestString("version");
        String module = getRequestString("module");
        String ref = getRequestString("refer");

        if (version == null || studentDetail == null) {
            return "redirect:/";
        }
        String version_h5 = version_mobile(studentDetail);
        String url = "/resources/apps/hwh5/afenti/{0}/index.html";
        url = cdnResourceVersionCollector.getVersionedUrlPath(MessageFormat.format(url, version_h5));
        url = UrlUtils.buildUrlQuery(url, MiscUtils.m(
                "domain", ProductConfig.getMainSiteBaseUrl(),
                "img_domain", getCdnBaseUrlStaticSharedWithSep(),
                "native_version", version,
                "subject", "CHINESE",
                "sid", studentDetail.getId(),
                "ref", ref,
                "refer", ref));
        if (StringUtils.isNotBlank(module)) url += ("#" + module);

        return "redirect:" + url;
    }

    private StudentDetail getStudent() {
        User user = currentUser();
        if (user == null || (!user.isParent() && !user.isStudent())) return null;

        if (user.isStudent()) {
            if (user instanceof StudentDetail) {
                return (StudentDetail) user;
            } else {
                return currentStudentDetail();
            }
        } else {
            Long sid = getRequestLong("sid");
            if (sid == 0) sid = Long.parseLong(getCookieManager().getCookie("sid", "0"));
            Set<Long> childrenIds = parentLoaderClient.loadParentStudentRefs(user.getId()).stream()
                    .map(StudentParentRef::getStudentId)
                    .collect(Collectors.toSet());
            if (!childrenIds.contains(sid)) return null;
            return studentLoaderClient.loadStudentDetail(sid);
        }
    }

    private String version_pc(StudentDetail student) {
        StudentGrayFunctionManager sgfm = grayFunctionManagerClient.getStudentGrayFunctionManager();
        String version_h5 = "V3_0_0";
        if (sgfm.isWebGrayFunctionAvailable(student, "Afenti", "VXXX"))
            version_h5 = StringUtils.defaultString(commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(
                    PRIMARY_PLATFORM_STUDENT.name(), "afenti_version").trim());
        return version_h5;

    }

    private String version_mobile(StudentDetail student) {
        StudentGrayFunctionManager sgfm = grayFunctionManagerClient.getStudentGrayFunctionManager();

        String version_h5;
        if (sgfm.isWebGrayFunctionAvailable(student, "AppH5Release", "testlist")) {
            version_h5 = "V0_0_0";
        } else if (sgfm.isWebGrayFunctionAvailable(student, "Afenti", "VXXX")) {
            version_h5 = StringUtils.defaultString(commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(
                    PRIMARY_PLATFORM_STUDENT.name(), "afenti_version").trim());
        } else {
            version_h5 = "V3_0_0";
        }
        return version_h5;
    }
}

