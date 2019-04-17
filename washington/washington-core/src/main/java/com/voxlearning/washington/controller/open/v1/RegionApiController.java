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

package com.voxlearning.washington.controller.open.v1;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.washington.controller.open.AbstractApiController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * region api
 * Created by Shuai Huan on 2015/1/14.
 */
@Controller
@RequestMapping(value = "/v1/region")
public class RegionApiController extends AbstractApiController {

    @Inject private RaikouSystem raikouSystem;

    @RequestMapping(value = "/children/get.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage getAllRegion() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequiredNumber(REQ_REGION_PCODE, "区域PCODE");
            validateRequest(REQ_REGION_PCODE);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        List<Map<String, Object>> regionList = new LinkedList<>();
        Integer pcode = getRequestInt(REQ_REGION_PCODE);
        List<ExRegion> regionAll = raikouSystem.getRegionBuffer().loadChildRegions(pcode);
        if (CollectionUtils.isNotEmpty(regionAll)) {
            for (ExRegion exRegion : regionAll) {
                Map<String, Object> region = new HashMap<>();
                region.put(RES_REGION_CODE, exRegion.getCode());
                region.put(RES_REGION_NAME, exRegion.getName());
                regionList.add(region);
            }
        }
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        resultMap.add(RES_REGION_LIST, regionList);
        return resultMap;
    }

    @RequestMapping(value = "/getlist.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage getRegionList() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequiredNumber(REQ_REGION_PCODE, "区域PCODE");
            validateRequestNoSessionKey(REQ_REGION_PCODE);
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        List<Map<String, Object>> regionList = new LinkedList<>();
        Integer pcode = getRequestInt(REQ_REGION_PCODE);
        List<ExRegion> regionAll = raikouSystem.getRegionBuffer().loadChildRegions(pcode);
        if (CollectionUtils.isNotEmpty(regionAll)) {
            for (ExRegion exRegion : regionAll) {
                Map<String, Object> region = new HashMap<>();
                region.put(RES_REGION_CODE, exRegion.getCode());
                region.put(RES_REGION_NAME, exRegion.getName());
                region.put(RES_REGION_PARENT_CODE, exRegion.getPcode());
                regionList.add(region);
            }
        }
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        resultMap.add(RES_REGION_LIST, regionList);
        return resultMap;
    }
}
