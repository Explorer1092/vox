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

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.washington.controller.open.AbstractApiController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.MessageFormat;
import java.util.*;

import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * Created by XiaoPeng.Yang on 15-1-26.
 */
@Controller
@RequestMapping(value = "/v1/user/learning")
public class UserLearningApiController extends AbstractApiController {

    @RequestMapping(value = "/saveresult.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage saveResult() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_QUESTION_RESULTS, "答题记录");
            validateRequest();
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        User curUser = getApiRequestUser();
        VendorApps vendorApps = getApiRequestApp();
        //校验数据
        String resultJson = getRequestString(REQ_QUESTION_RESULTS);
        Map<String, Object> objMap = JsonUtils.fromJson(resultJson);
        if (objMap == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, "json格式错误");
            return resultMap;
        }
        String studyType = ConversionUtils.toString(objMap.get("studyType"));
        Long userId = ConversionUtils.toLong(objMap.get("userId"));
        if (!vendorApps.getAppKey().equals(studyType) || !curUser.getId().equals(userId)) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, "appKey或者用户ID不匹配");
            return resultMap;
        }

        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }

    @RequestMapping(value = "/saveappresult.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage saveAppResult() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_APP_RESULTS, "答题记录");
            validateRequest();
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        User curUser = getApiRequestUser();
        VendorApps vendorApps = getApiRequestApp();
        //校验数据
        String resultJson = getRequestString(REQ_APP_RESULTS);
        Map<String, Object> objMap = JsonUtils.fromJson(resultJson);
        if (objMap == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, "json格式错误");
            return resultMap;
        }

        String studyType = ConversionUtils.toString(objMap.get("studyType"));
        // userId
        Long userId = ConversionUtils.toLong(objMap.get("userId"));

        if (!vendorApps.getAppKey().equals(studyType) || !curUser.getId().equals(userId)) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, "appKey或者用户ID不匹配");
            return resultMap;
        }

        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        return resultMap;
    }
}