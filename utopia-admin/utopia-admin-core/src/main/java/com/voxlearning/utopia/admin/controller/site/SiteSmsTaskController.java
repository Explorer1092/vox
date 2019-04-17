/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 功能移动去运营管理节点 By Wyc 2016-04-28
 */
@Deprecated
@Controller
@Slf4j
@RequestMapping(value = "/site/smstask")
public class SiteSmsTaskController extends SiteAbstractController {

    @RequestMapping(value = "index.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String smsTaskList(Model model) {
        return "opmanager/notice";
    }

    @RequestMapping(value = "smsdetail.vpage", method = RequestMethod.GET)
    public String editSms(Model model) {
        return "opmanager/notice";
    }

    @RequestMapping(value = "savesms.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveSmsTask() {
        return MapMessage.errorMessage("该功能已经移至运营管理功能下");
    }

    @RequestMapping(value = "deletesms.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteSmsTask(@RequestParam Long smsId) {
        return MapMessage.errorMessage("该功能已经移至运营管理功能下");
    }

    @RequestMapping(value = "sumbitsms.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage submitSms() {
        return MapMessage.errorMessage("该功能已经移至运营管理功能下");
    }

    @RequestMapping(value = "approvesms.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage approveSms() {
        return MapMessage.errorMessage("该功能已经移至运营管理功能下");
    }

    @RequestMapping(value = "rejectsms.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage rejectSms() {
        return MapMessage.errorMessage("该功能已经移至运营管理功能下");
    }

    @RequestMapping(value = "raiseup.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage raiseUp() {
        return MapMessage.errorMessage("该功能已经移至运营管理功能下");
    }

    @RequestMapping(value = "tracesms.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage traceSmsFlow(@RequestParam Long smsId) {
        return MapMessage.errorMessage("该功能已经移至运营管理功能下");
    }

}