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

package com.voxlearning.washington.controller;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.crm.api.loader.agent.AgentUserLoader;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentUserLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.washington.support.AbstractController;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


@Controller
@RequestMapping("/help")
@NoArgsConstructor
public class HelpController extends AbstractController {

    // 七鱼网站端接入的
    private static final String QIYU_KF_SCRIPT = "https://qiyukf.com/script/f10a2349a4bead156114e00f9084177c.js";

    @RequestMapping(value = "{page}.vpage", method = RequestMethod.GET)
    public String page(@PathVariable("page") String page, Model model, HttpServletRequest request,
                       @RequestParam(value = "p", required = false, defaultValue = "0") Integer p) {
        if (currentUserId() != null) {
            model.addAttribute("userId", currentUserId().toString());
        }
        //解决页面被删除了 但仍被调用的情况。
        if (page.equals("pfcampaign") || page.equals("aboutus-1") || page.contains("forgetstudentpassword") || page.equals("childjf")) {
            return "redirect:/index.vpage";
        }

        // FIXME 做一下错误兼容
        if (page.equals("downloadstudentapp")) {
            page = "download-student-app";
        }

        model.addAttribute("p", p);
        return "help/" + page;
    }

    @RequestMapping(value = "{folder}/{page}.vpage", method = RequestMethod.GET)
    public String page(Model model, HttpServletRequest request, @PathVariable("folder") String folder, @PathVariable("page") String page,
                       @RequestParam(value = "p", required = false, defaultValue = "0") Integer p) {

        if (currentUserId() != null) {
            model.addAttribute("userId", currentUserId().toString());
        }

        model.addAttribute("p", p);
        model.addAttribute("scriptPath",QIYU_KF_SCRIPT);

        return "help/" + folder + "/" + page;
    }

    @RequestMapping(value = "checkup.vpage", method = RequestMethod.GET)
    public String checkup(Model model, HttpServletRequest request,
                          @RequestParam(value = "auto", required = false, defaultValue = "0") Integer auto) {
        model.addAttribute("auto", auto);
        model.addAttribute("currentDate", new Date());
        return "help/checkup";
    }

    // 下载
    @RequestMapping(value = "downloadApp.vpage", method = RequestMethod.GET)
    public String downloadCommon(Model model, HttpServletRequest request, @RequestParam(value = "auto", required = false, defaultValue = "0") Integer auto) {
        model.addAttribute("refrerer", getRequestString("refrerer"));
        return "help/download_app/common";
    }

}
