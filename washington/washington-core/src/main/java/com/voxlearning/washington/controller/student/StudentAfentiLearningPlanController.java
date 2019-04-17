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

package com.voxlearning.washington.controller.student;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.washington.controller.afenti.AfentiBaseController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.MessageFormat;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: tanguohong
 * Date: 13-9-11
 * Time: 下午9:11
 * To change this template use File | Settings | File Templates.
 * 2016-09-12上线新版本，新版本之后可以删除
 */
@Deprecated
@Controller
@RequestMapping("/student/afenti/learningplan")
public class StudentAfentiLearningPlanController extends AfentiBaseController {

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        Subject subject = Subject.of(getRequestString("subject"));
        String url = ProductConfig.getMainSiteBaseUrl() + "/resources/apps/hwh5/afenti/V2_0_0/index.html";
        url = UrlUtils.buildUrlQuery(url, MiscUtils.m("subject", subject.name(),
                "native_version", "2.7.0.0",
                "domain", ProductConfig.getMainSiteBaseUrl(),
                "img_domain", getCdnBaseUrlStaticSharedWithSep(),
                "server_type", RuntimeMode.current().getStageMode()));
        model.addAttribute("url", url);
        return "studentv3/afenti/learningplan/index";
    }

    @RequestMapping(value = "indexmobile.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage index_mobile() {
        return MapMessage.errorMessage("用新的吧");
    }


    @RequestMapping(value = "/afentiMath/indexMobile.vpage", method = RequestMethod.GET)
    public String afentiMathIndexMobile() {
        StudentDetail studentDetail = currentStudentDetail();
        String version = getRequestString("version");

        if (version == null || studentDetail == null) {
            return "redirect:/";
        }

        String version_h5;
        String url = "/resources/apps/hwh5/afenti/{0}/index.html";
        if (grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "AppH5Release", "testlist")) {
            version_h5 = "V0_0_0";
        } else {
            String str = getPageBlockContentGenerator().getPageBlockContentHtml("vendor_apps",
                    "vendor_apps_native_h5_version_mapping").replace("\r", "").replace("\n", "").replace("\t", "");
            Map<String, Object> native_h5_map = JsonUtils.fromJson(JsonUtils.toJson(JsonUtils.fromJson(str).get(OrderProductServiceType.AfentiMath.name())));
            version_h5 = SafeConverter.toString(native_h5_map.get("default"));
            for (String key : native_h5_map.keySet()) {
                if (version.matches(key)) {
                    version_h5 = SafeConverter.toString(native_h5_map.get(key));
                }
            }
        }
        url = cdnResourceVersionCollector.getVersionedUrlPath(MessageFormat.format(url, version_h5));
        url = UrlUtils.buildUrlQuery(url, MiscUtils.m(
                "domain", ProductConfig.getMainSiteBaseUrl(),
                "img_domain", getCdnBaseUrlStaticSharedWithSep(),
                "native_version", version,
                "subject", "MATH"));

        return "redirect:" + url;
    }

    @RequestMapping(value = "/afentiEnglish/indexMobile.vpage", method = RequestMethod.GET)
    public String afentiEnglishIndexMobile() {
        StudentDetail studentDetail = currentStudentDetail();
        String version = getRequestString("version");

        if (version == null || studentDetail == null) {
            return "redirect:/";
        }
        String version_h5;
        String url = "/resources/apps/hwh5/afenti/{0}/index.html";
        if (grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "AppH5Release", "testlist")) {
            version_h5 = "V0_0_0";
        } else {
            String str = getPageBlockContentGenerator().getPageBlockContentHtml("vendor_apps",
                    "vendor_apps_native_h5_version_mapping").replace("\r", "").replace("\n", "").replace("\t", "");
            Map<String, Object> native_h5_map = JsonUtils.fromJson(JsonUtils.toJson(JsonUtils.fromJson(str).get(OrderProductServiceType.AfentiExam.name())));
            version_h5 = SafeConverter.toString(native_h5_map.get("default"));
            for (String key : native_h5_map.keySet()) {
                if (version.matches(key)) {
                    version_h5 = SafeConverter.toString(native_h5_map.get(key));
                }
            }
        }
        url = cdnResourceVersionCollector.getVersionedUrlPath(MessageFormat.format(url, version_h5));
        url = UrlUtils.buildUrlQuery(url, MiscUtils.m(
                "domain", ProductConfig.getMainSiteBaseUrl(),
                "img_domain", getCdnBaseUrlStaticSharedWithSep(),
                "native_version", version,
                "subject", "ENGLISH"));

        return "redirect:" + url;
    }
}
