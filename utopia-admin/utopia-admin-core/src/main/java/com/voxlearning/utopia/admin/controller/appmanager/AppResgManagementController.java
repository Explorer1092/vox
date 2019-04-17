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

package com.voxlearning.utopia.admin.controller.appmanager;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.admin.controller.AbstractAdminSystemController;
import com.voxlearning.utopia.admin.service.appmanager.AppResgManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;

@Controller
@RequestMapping("/appmanager")
@Slf4j
public class AppResgManagementController extends AbstractAdminSystemController {
    @Inject AppResgManagementService appResgManagementService;

    @RequestMapping(value = "resgindex.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String resgIndex(Model model) {
        model.addAttribute("resgList", appResgManagementService.getResgList());
        return "apps/resglist";
    }

    @RequestMapping(value = "getresg.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getResg(@RequestParam Long resgId) {
        MapMessage message = new MapMessage();
        try {
            message.add("resg", appResgManagementService.getResg(resgId));
            message.setSuccess(true);
            message.setInfo("操作成功");
        } catch (Exception ex) {
            message.setSuccess(false);
            message.setInfo("操作失败," + ex.getMessage());
        }
        return message;
    }

    @RequestMapping(value = "saveresg.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveResg(@RequestParam Long resgId, @RequestParam String cname, @RequestParam String ename, @RequestParam String desc) {
        MapMessage message;
        try {
            message = vendorServiceClient.saveVendorResg(resgId, cname, ename, desc);
        } catch (Exception ex) {
            logger.error("Failed to save vendor resg", ex);
            message = MapMessage.errorMessage();
        }
        return message.setInfo(message.isSuccess() ? "操作成功" : "操作失败");
    }

    @RequestMapping(value = "delresg.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteResg(@RequestParam Long resgId) {
        MapMessage message = new MapMessage();
        try {
            appResgManagementService.deleteResg(resgId);

            message.setSuccess(true);
            message.setInfo("操作成功");
        } catch (Exception ex) {
            message.setSuccess(false);
            message.setInfo("操作失败," + ex.getMessage());
        }
        return message;
    }

    @RequestMapping(value = "editresgcontent.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String toEditResgContent(Model model) {
        model.addAttribute("resg", appResgManagementService.getResg(getRequestLong("resgId")));
        return "apps/resgedit";
    }

    @RequestMapping(value = "getresgcontent.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getResgContent(@RequestParam Long resgContentId) {
        MapMessage message = new MapMessage();
        try {
            message.add("resgContent", appResgManagementService.getResgContent(resgContentId));
            message.setSuccess(true);
            message.setInfo("操作成功");
        } catch (Exception ex) {
            message.setSuccess(false);
            message.setInfo("操作失败," + ex.getMessage());
        }
        return message;
    }

    @RequestMapping(value = "saveresgcontent.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveResgContent(@RequestParam Long resgId, @RequestParam Long resgContentId, @RequestParam String resName) {
        MapMessage message;
        try {
            message = vendorServiceClient.saveVendorResgContent(resgId, resgContentId, resName);
        } catch (Exception ex) {
            logger.error("Failed to save vendor resg content", ex);
            message = MapMessage.errorMessage();
        }
        return message.setInfo(message.isSuccess() ? "操作成功" : "操作失败");
    }

    @RequestMapping(value = "delresgcontent.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteResgContent(@RequestParam Long resgContentId) {
        MapMessage message;
        try {
            message = vendorServiceClient.deleteVendorResgContent(resgContentId);
        } catch (Exception ex) {
            logger.error("Failed to delete vendor resg content", ex);
            message = MapMessage.errorMessage();
        }
        return message.setInfo(message.isSuccess() ? "操作成功" : "操作失败");
    }
}