package com.voxlearning.washington.controller.open.v1.teacher;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.config.api.entity.PageBlockContent;
import com.voxlearning.utopia.service.config.client.PageBlockContentServiceClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.washington.controller.open.AbstractTeacherApiController;
import com.voxlearning.washington.controller.open.ApiConstants;
import com.voxlearning.washington.controller.open.v1.util.InternalOffRewardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.voxlearning.washington.controller.open.ApiConstants.*;

@Controller
@RequestMapping(value = "/v1/teacher/app/config")
@Slf4j
public class TeacherAppConfigApiController extends AbstractTeacherApiController {

    @Inject
    private PageBlockContentServiceClient pageBlockContentServiceClient;
    @Inject
    private InternalOffRewardService internalOffRewardService;

    private static final String PRIMARY_TEACHER_APP_CENTER_PERSONAL = "primary_teacher_app_center_personal";

    private static final String PRIMARY_TEACHER_APP_CENTER = "primary_teacher_app_center";

    private static String TEST_HOST = "https://cdn-portrait.test.17zuoye.net";

    private static String ONLINE_HOST = "https://cdn-portrait.17zuoye.cn";

    @RequestMapping(value = "/personal.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage personal() {
        MapMessage resultMap = new MapMessage();
        try {
            boolean remove = false;
            User requestUser = getApiRequestUser();
            if (requestUser != null && Objects.equals(requestUser.getUserType(), UserType.TEACHER.getType())) {
                remove = true;
            }

            //获取小学老师APP个人心中的配置列表
            List<PageBlockContent> contents = pageBlockContentServiceClient.getPageBlockContentBuffer().findByPageName(PRIMARY_TEACHER_APP_CENTER);
            PageBlockContent configPageBlockContent = contents.stream()
                    .filter(p -> Objects.equals(PRIMARY_TEACHER_APP_CENTER_PERSONAL, p.getBlockName()))
                    .findFirst()
                    .orElse(null);
            if (null == configPageBlockContent || StringUtils.isEmpty(configPageBlockContent.getContent())) {
                resultMap.add(RES_RESULT, ApiConstants.RES_RESULT_INTERNAL_ERROR_CODE);
                resultMap.add(RES_MESSAGE, "加载老师个人中心配置列表失败");
                log.error("加载老师个人中心配置列表失败");
                return resultMap;
            }
            String content = configPageBlockContent.getContent();
            try {

                Map<String, Object> configMap = (Map<String, Object>) JsonUtils.fromJson(content, Map.class);
                List<Map<String, Object>> middleFeatureList = (List<Map<String, Object>>)configMap.get("middle_feature_list");
                List<Map<String, Object>> bottomFeatureList = (List<Map<String, Object>>)configMap.get("bottom_feature_list");

                if (remove) {
                    if (internalOffRewardService.offline(getCurrentTeacher())) {
                        bottomFeatureList.removeIf(next -> Objects.equals(next.get("name"), "教学用品中心"));
                    }
                }

                assembleUrl(middleFeatureList);
                assembleUrl(bottomFeatureList);
                configMap.get("bottom_feature_list");
                resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
                resultMap.add("featuer", configMap);
                return resultMap;
            } catch (Exception e) {
                resultMap.add(RES_RESULT, ApiConstants.RES_RESULT_INTERNAL_ERROR_CODE);
                resultMap.add(RES_MESSAGE, "加载老师个人中心配置列表失败");
                log.error("解析老师个人中心配置content中的json异常", e.getMessage(), e);
                return resultMap;
            }
        } catch (Exception e) {
            resultMap.add(RES_RESULT, ApiConstants.RES_RESULT_INTERNAL_ERROR_CODE);
            resultMap.add(RES_MESSAGE, "加载老师个人中心配置列表异常");
            log.error("加载老师个人中心配置列表异常", e.getMessage(), e);
            return resultMap;
        }
    }



    private void assembleUrl(List<Map<String, Object>> featureList) {
        String domain = this.fetchMainsiteUrlByCurrentSchema();
        String iconDomain = (RuntimeMode.isProduction() || RuntimeMode.isStaging()) ? ONLINE_HOST : TEST_HOST;
        if (CollectionUtils.isEmpty(featureList)) {
            return;
        }
        for (Map<String, Object> feature : featureList) {
            String url = SafeConverter.toString(feature.get("url"));
            if (StringUtils.isNotEmpty(url)) {
                feature.put("url", domain + url);
            }
            String icon = SafeConverter.toString(feature.get("icon"));
            if (StringUtils.isNotEmpty(url)) {
                feature.put("icon", iconDomain + icon);
            }
        }
    }
}
