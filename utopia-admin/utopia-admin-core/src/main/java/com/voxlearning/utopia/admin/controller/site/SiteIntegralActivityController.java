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

package com.voxlearning.utopia.admin.controller.site;


import com.voxlearning.alps.lang.util.MapMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 功能移动去运营管理节点 By Wyc 2016-04-28
 */
@Deprecated
@Controller
@Slf4j
@RequestMapping(value = "/site/integralactivity")
public class SiteIntegralActivityController extends SiteAbstractController {

    @RequestMapping(value = "activitylist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String activityList(Model model) {
        return "opmanager/notice";
    }

    @RequestMapping(value = "activitypage.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    private String activityPage(Model model) {
        return "opmanager/notice";
    }

    @RequestMapping(value = "activityinfo.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String editActivity(Model model) {
        return "opmanager/notice";
    }

    @RequestMapping(value = "saveactivity.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveIntegralActivity() {
        return MapMessage.errorMessage("该功能已经移至运营管理功能下");
    }

    @RequestMapping(value = "changestatus.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage changeActivityStatus() {
        return MapMessage.errorMessage("该功能已经移至运营管理功能下");
    }

    @RequestMapping(value = "saverule.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveActivityRule() {
        return MapMessage.errorMessage("该功能已经移至运营管理功能下");
    }

    @RequestMapping(value = "delrule.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage delActivityRule() {
        return MapMessage.errorMessage("该功能已经移至运营管理功能下");
    }

}
