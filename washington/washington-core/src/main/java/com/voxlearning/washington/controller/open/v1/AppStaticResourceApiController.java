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

package com.voxlearning.washington.controller.open.v1;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.core.helper.StringRegexUtils;
import com.voxlearning.washington.controller.open.AbstractApiController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * app静态资源
 * Created by Shuai Huan on 2016/4/11.
 */
@Controller
@RequestMapping(value = "/v1/staticresource")
@Slf4j
public class AppStaticResourceApiController extends AbstractApiController {

    @RequestMapping(value = "/get.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage get() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequestNoSessionKey();
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        String config = this.getConfig("app_static_resource");
        Map<String, Object> configMap = JsonUtils.fromJson(config);
        Map<String, Object> infoMap = new HashMap<>();
        if (configMap != null) {
            Map<String, Object> stageMap = (Map) configMap.get(getApiRequestApp().getAppKey());
            if (stageMap != null) {
                infoMap = (Map) stageMap.get(RuntimeMode.current().name());
                if (infoMap != null) {
                    infoMap.entrySet().stream()
                            .forEach(e -> e.setValue(generateVersionUrl(SafeConverter.toString(e.getValue()))));
                }
            }
        }
        resultMap.add(RES_RESOURCES, infoMap);
        //获取域名白名单配置
        String domainWhite = this.getConfig( "domain_white_list");
        if(StringUtils.isNotBlank(domainWhite)){
            List<String> domainWhites = JsonUtils.fromJsonToList(domainWhite, String.class);
            if(CollectionUtils.isNotEmpty(domainWhites)){
                resultMap.put(RES_DOMAIN_WHITE_LIST, domainWhites);
            }
        }
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }

    /**
     * 根据key获取配置信息,并过滤特殊字符
     *
     * @param key
     */
    private String getConfig(String key){
        String value = getPageBlockContentGenerator().getPageBlockContentHtml("client_app_publish", key);
        return StringUtils.isNotBlank(value) ? StringRegexUtils.normalizeC(value) : value;
    }

}
