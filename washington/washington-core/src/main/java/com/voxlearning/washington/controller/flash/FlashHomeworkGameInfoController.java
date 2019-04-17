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

package com.voxlearning.washington.controller.flash;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.service.content.api.entity.PracticeType;
import com.voxlearning.washington.support.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/flash")
public class FlashHomeworkGameInfoController extends AbstractController {

    @RequestMapping(value = "gameinfo.vpage", method = RequestMethod.GET)
    @ResponseBody
    public Map gameinfo() {
        String name = getRequestParameter("name", "");

        Map<String, Object> m = new LinkedHashMap<>();
        Map<String, Object> mapParams = new LinkedHashMap<>();

        //兼容之前flash的命名规则
        mapParams.put("domain", HttpRequestContextUtils.getWebAppBaseUrl(getRequest()) + "/");
        mapParams.put("imgDomain", cdnResourceUrlGenerator.getCdnBaseUrlWithSep(getRequest()));
        PracticeType practiceType = practiceLoaderClient.loadNamedPractice(name);
        if (practiceType == null) {
            return MapMessage.errorMessage("flash name is error " + name);
        }
        String file = StringUtils.isNotBlank(practiceType.getFrameType()) ? practiceType.getFrameType() : practiceType.getFilename();
        gameFlashLoaderConfigManager.setupFlashV1Url(mapParams, getRequest(), file, name);
        m.put("params", mapParams);

        return m;
    }

    @RequestMapping(value = "gameurl.vpage", method = RequestMethod.GET)
    @ResponseBody
    public Map getGameUrl() {
        String names = getRequestString("names");

        List<String> nameArr = JsonUtils.fromJsonToList(names, String.class);

        Map<String, Object> map = new HashMap<>();
        if (nameArr == null) {
            return map;
        }

        for (String name : nameArr) {
            //英语迁移新体系兼容课外乐园走遍美国、通天塔、沃克大冒险（奇幻探险）flash的路径偶都改成flashv1
            map.put(name, gameFlashLoaderConfigManager.getFlashUrl(getRequest(), StringUtils.replace(name,"flash", "flashv1")));
        }

        return map;
    }

    /**
     * 获取资源版本号，不会强制转换到flashv1，具体有问题请找。
     */
    @RequestMapping(value = "resourceversion.vpage", method = RequestMethod.GET)
    @ResponseBody
    public Map getResourceVersion() {
        String names = getRequestString("names");
        List<String> nameArr = JsonUtils.fromJsonToList(names, String.class);
        Map<String, Object> map = new HashMap<>();
        if (nameArr == null) {
            return map;
        }
        for (String name : nameArr) {
            map.put(name, gameFlashLoaderConfigManager.getFlashUrl(getRequest(), name));
        }
        return map;
    }
}

