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

package com.voxlearning.washington.controller;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.user.api.mappers.AbtestMapper;
import com.voxlearning.washington.athena.SearchEngineServiceClient;
import com.voxlearning.washington.support.AbstractController;
import com.voxlearning.washington.support.UserAbtestLoaderClientHelper;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;

/**
 * <p>
 * abtest测试 没有角色限制
 */
@Controller
@Slf4j
@NoArgsConstructor
@RequestMapping("/abtest")
public class AbtestController extends AbstractController {

    @Inject private SearchEngineServiceClient searchEngineServiceClient;

    @Inject private UserAbtestLoaderClientHelper userAbtestLoaderClientHelper;


    @RequestMapping(value = "generateuserabtestinfo.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage generateUserAbtestInfo() {
        long userId = getRequestLong("userId");
        String experimentId = getRequestString("experimentId");
        //阿分题购买页面全部显示90天
        if (StringUtils.isNotBlank(experimentId) && experimentId.equals("585252821e346b56e210aaf9")) {
            AbtestMapper abtestMapper = AbtestMapper.newInstance("585252821e346b56e210aaf9", "阿分题购买页默认天数实验", null, null, null, "B", true, null);
            return MapMessage.successMessage().add("abtest", abtestMapper);
        }
        try {
            AbtestMapper abtestMapper = userAbtestLoaderClientHelper.generateUserAbtestInfo(userId, experimentId);
            return MapMessage.successMessage().add("abtest", abtestMapper);
        } catch (Exception exp) {
            return MapMessage.errorMessage("加载abtest信息失败");
        }
    }

    @RequestMapping(value = "testabtest.vpage", method = RequestMethod.GET)
    public String testabtest(Model model) {
        model.addAttribute("labelTree", JsonUtils.toJson(searchEngineServiceClient.getLabelTree()));
        return "abtest/testabtest";
    }

    @RequestMapping(value = "example.vpage", method = RequestMethod.GET)
    public String example() {
        return "abtest/example";
    }
}
