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

package com.voxlearning.washington.controller.mobile.common;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.washington.controller.mobile.AbstractMobileController;
import lombok.extern.slf4j.Slf4j;
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
 * Created by jiangpeng on 16/6/16.
 */

@Controller
@RequestMapping(value = "/usermobile/region")
@Slf4j
public class MobileUserRegionController extends AbstractMobileController {

    @Inject private RaikouSystem raikouSystem;

    /**
     * 获取区域列表接口
     */
    @RequestMapping(value = "/children/get.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage getChildrenRegion() {
        addCrossHeaderForXdomain();

        User user = currentUser();
        if (user == null)
            return noLoginResult;

        List<Map<String, Object>> regionList = new LinkedList<>();
        Integer pcode = getRequestInt("region_pcode");
        List<ExRegion> regionAll = raikouSystem.getRegionBuffer().loadChildRegions(pcode);
        if (CollectionUtils.isNotEmpty(regionAll)) {
            for (ExRegion exRegion : regionAll) {
                Map<String, Object> region = new HashMap<>();
                region.put(RES_REGION_CODE, exRegion.getCode());
                region.put(RES_REGION_NAME, exRegion.getName());
                region.put(RES_REGION_TYPE, exRegion.fetchRegionType().getType());
                regionList.add(region);
            }
        }
        return MapMessage.successMessage().add("regionList", regionList);
    }
}
