package com.voxlearning.washington.controller.mobile;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.washington.controller.mobile.parent.AbstractMobileParentController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author malong
 * @since 2018/4/11
 */
@Controller
@Slf4j
@RequestMapping(value = "parentMobile/redirector")
public class MobileParentRedirectController extends AbstractMobileParentController {
    @Inject
    private CommonConfigServiceClient commonConfigServiceClient;

    @RequestMapping(value = "tab/selfStudy.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    public String selfStudyTab() {
        String redirectUrl = ProductConfig.getMainSiteBaseUrl() + "/view/mobile/parent/album/index.vpage";
        String redirectConfig = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.name(), "PARENT_TAB_URL_CHANGE_TIME");
        if (StringUtils.isNotBlank(redirectConfig)) {
            Map<String, Object> configMap = JsonUtils.fromJson(redirectConfig);
            String redirectTime = SafeConverter.toString(configMap.get("redirectTime"), "");
            String configUrl = SafeConverter.toString(configMap.get("redirectUrl"), "");
            Date redirectDate = null;
            if (StringUtils.isNotBlank(redirectTime)) {
                redirectDate = DateUtils.stringToDate(redirectTime);
            }
            Date now = new Date();
            if (redirectDate != null && now.after(redirectDate) && StringUtils.isNotBlank(configUrl)) {
                redirectUrl = ProductConfig.getMainSiteBaseUrl() + configUrl;
            }
        }

        return "redirect:" + UrlUtils.buildUrlQuery(redirectUrl, getParameter());
    }

    @RequestMapping(value = "/tab/17shuo.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    public String talkTab() {
        String redirectUrl = ProductConfig.getMainSiteBaseUrl() + "/view/mobile/parent/17shuo/list.vpage";
        String redirectConfig = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.name(), "PARENT_TAB_URL_17TALK");
        if (StringUtils.isNotBlank(redirectConfig)) {
            Map<String, Object> configMap = JsonUtils.fromJson(redirectConfig);
            String configUrl = SafeConverter.toString(configMap.get("redirectUrl"), "");

            if (StringUtils.isNotEmpty(configUrl)) {
                redirectUrl = configUrl.toLowerCase().startsWith("https")
                        ? configUrl
                        : ProductConfig.getMainSiteBaseUrl() + configUrl;
            }
        }
        return "redirect:" + UrlUtils.buildUrlQuery(redirectUrl, getParameter());
    }

    @RequestMapping(value = "/tab/studyresource/training.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String training() {
        String redirectUrl = ProductConfig.getMainSiteBaseUrl() + "/view/mobile/parent/17xue_train/index.vpage";
        String redirectConfig = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.name(), "PARENT_STUDY_RESOURCE_TRAINING");
        if (StringUtils.isNotBlank(redirectConfig)) {
            Map<String, Object> configMap = JsonUtils.fromJson(redirectConfig);
            String configUrl = SafeConverter.toString(configMap.get("redirectUrl"), "");
            redirectUrl = ProductConfig.getMainSiteBaseUrl() + configUrl;
        }
        return "redirect:" + UrlUtils.buildUrlQuery(redirectUrl, getParameter());
    }

    private Map<String, String> getParameter() {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("app_version", getRequestString("app_version"));
        paramMap.put("client_type", getRequestString("client_type"));
        paramMap.put("client_name", getRequestString("client_name"));
        paramMap.put("app_product_id", getRequestString("app_product_id"));
        paramMap.put("imei", getRequestString("imei"));
        paramMap.put("env", getRequestString("env"));
        Long sid = getRequestLong("sid");
        if (sid > 0L) {
            paramMap.put("sid", SafeConverter.toString(sid));
        } else {
            User user = currentUser();
            if (user != null && user.fetchUserType() == UserType.PARENT) {
                List<User> students = studentLoaderClient.loadParentStudents(user.getId());
                if (CollectionUtils.isNotEmpty(students)) {
                    paramMap.put("sid", SafeConverter.toString(students.get(0).getId()));
                } else {
                    paramMap.put("sid", "0");
                }
            }
        }

        return paramMap;
    }
}
