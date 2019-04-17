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

package com.voxlearning.utopia.agent.controller.workspace;

import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.utopia.agent.bean.SchoolDetailBean;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.service.workspace.MarketToolService;
import com.voxlearning.utopia.service.user.api.entities.School;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.inject.Inject;
import java.util.List;

/**
 * @author shiwei.liao
 * @since 2015/9/7.
 */

@Controller
@RequestMapping(value = "/workspace/markettool")
public class SchoolSearchController extends AbstractAgentController {
    @Inject
    protected MarketToolService marketToolService;

    @RequestMapping(value = "school_search.vpage")
    String schoolSearch(Model model) {
        model.addAttribute("result", buildUserRegionJsonTree());
        return "workspace/markettool/school_search";
    }

    @RequestMapping(value = "region_school.vpage")
    String regionSchool(Model model) {
        model.addAttribute("cityName", getRequestString("cityName"));
        model.addAttribute("areaName", getRequestString("areaName"));
        int regionCode = getRequestInt("regionCode");
        if (regionCode <= 0) {
            logger.error("regionSchool - Illegal regionCode = {}", regionCode);
            return "workspace/markettool/school_list";
        }
        String key = getRequestString("key");
        List<School> schools = marketToolService.searchSchool(regionCode, key, getCurrentUserId());
        Page<SchoolDetailBean> result = marketToolService.buildSchoolDetail(schools, buildPageRequest());
        model.addAttribute("regionTree", buildUserRegionJsonTree());
        model.addAttribute("result", result);
        model.addAttribute("regionCode", regionCode);
        model.addAttribute("key", key);
        return "workspace/markettool/school_list";
    }

}
