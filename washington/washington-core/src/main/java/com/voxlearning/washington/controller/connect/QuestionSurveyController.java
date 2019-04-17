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

package com.voxlearning.washington.controller.connect;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.entity.questionsurvey.QuestionSurveyResult;
import com.voxlearning.utopia.service.business.consumer.MiscServiceClient;
import com.voxlearning.washington.support.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/**
 * 问卷调查回调接口
 * Created by yaguang.wang on 2016/4/21.
 */

@RequestMapping(value = "/survey")
@Controller
public class QuestionSurveyController extends AbstractController {

    @Inject MiscServiceClient miscServiceClient;

    @RequestMapping(value = "callback.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage callback(@RequestBody String userAnswer) {
        try {
            Map<String, Object> surveyMap = JsonUtils.fromJson(userAnswer);

            if (surveyMap == null || !surveyMap.containsKey("activity") || !surveyMap.containsKey("sojumpparm")) {
                //logger.warn("Illegal survey result received. result json:{}", userAnswer);
                return MapMessage.errorMessage("Illegal survey result!");
            }

            QuestionSurveyResult questionSurvey = new QuestionSurveyResult();
            questionSurvey.setUserId(ConversionUtils.toLong(surveyMap.get("sojumpparm")));
            questionSurvey.setActivityId(ConversionUtils.toString(surveyMap.get("activity")));
            surveyMap.remove("sojumpparm");
            surveyMap.remove("activity");
            HashMap<String, String> questionAnswerMap = new HashMap<>();
            surveyMap.keySet().forEach(p -> questionAnswerMap.put(p, ConversionUtils.toString(surveyMap.get(p))));
            questionSurvey.setQuestionAnswerMap(questionAnswerMap);
            miscServiceClient.saveQuestionSurveyResult(questionSurvey);

            return MapMessage.successMessage();
        }catch (Exception e){
            logger.error("Process survey callback error.", e);
            return MapMessage.errorMessage("Illegal survey result!");
        }
    }

    @RequestMapping(value = "go.vpage", method = RequestMethod.GET)
    public String goSurvey() {
        String surveyUrl = getRequestString("survey");
        if (StringUtils.isBlank(surveyUrl)) {
            return "redirect:/";
        }

        if (currentUser() == null) {
            String ucenterLoginUrl = ProductConfig.getUcenterUrl() + "/login.vpage";
            String returnUrl = ProductConfig.getMainSiteBaseUrl() + "/survey/go.vpage?survey=" + surveyUrl;
            Map paramMap = new HashMap<>();
            paramMap.put("returnURL", returnUrl);

            return "redirect:" + UrlUtils.buildUrlQuery(ucenterLoginUrl, paramMap);
        }

        Long userId = currentUserId();
        surveyUrl = surveyUrl + "?sojumpparm=" + userId;
        return "redirect:" + surveyUrl;
    }
}
