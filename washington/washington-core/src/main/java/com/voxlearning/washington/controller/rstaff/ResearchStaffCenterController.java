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

package com.voxlearning.washington.controller.rstaff;

import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.washington.support.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author Longlong Yu
 * @since 下午7:24,13-9-17.
 */
@Controller
@RequestMapping("/rstaff/center")
public class ResearchStaffCenterController extends AbstractController {

    /**
     *  NEW 教研员个人中心
     */
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    String index(Model model) {
        return "redirect:" + ProductConfig.getUcenterUrl() + "/rstaff/center/index.vpage";
    }

    /**
     *  NEW 教研员个人信息编辑
     */
    @RequestMapping(value = "edit.vpage", method = RequestMethod.GET)
    String edit(Model model) {
        return "redirect:" + ProductConfig.getUcenterUrl() + "/rstaff/center/edit.vpage";
    }

    /**
     * NEW 教研员重置密码
     *
     */
    @RequestMapping(value = "editPassword.vpage", method = RequestMethod.GET)
    String editPassword() {
        return "redirect:" + ProductConfig.getUcenterUrl() + "/rstaff/center/editPassword.vpage";
    }

}
