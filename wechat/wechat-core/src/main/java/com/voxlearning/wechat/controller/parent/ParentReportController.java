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

package com.voxlearning.wechat.controller.parent;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.wechat.controller.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Xin Xin
 * @since 11/10/15
 * 微信单元报告
 */
@Controller
@RequestMapping(value = "/parent/homework/report")
public class ParentReportController extends AbstractController {

    @RequestMapping(value = "/{page}.vpage", method = RequestMethod.GET)
    public String index(@PathVariable("page") String page,Model model) {
        return "redirect:/parent/homework/index.vpage";
    }

    @RequestMapping(value = "/unitreport.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage unitReport() {
        return MapMessage.errorMessage("功能已下线");
    }

    @RequestMapping(value = "/unitreportdetail.vpage", method = RequestMethod.GET)
    public String unitReportDetail(Model model) {
        return "redirect:/parent/homework/index.vpage";
    }
}
