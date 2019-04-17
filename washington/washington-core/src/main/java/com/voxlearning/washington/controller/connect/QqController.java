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

package com.voxlearning.washington.controller.connect;

import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.washington.support.AbstractController;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author Rui.Bao
 * @since 2014-10-22 10:26
 */
@Controller
@RequestMapping("/qq")
@Slf4j
@NoArgsConstructor
@Deprecated
public class QqController extends AbstractController {

    @RequestMapping(value = "authorizecode.vpage", method = RequestMethod.GET)
    public String getAuthorizeCode() {
        return "redirect:" + ProductConfig.getUcenterUrl() + "/qq/authorizecode.vpage";
    }

    @RequestMapping(value = "authorize.vpage", method = RequestMethod.GET)
    public String authorize(Model model) {
        return "redirect:" + ProductConfig.getUcenterUrl() + "/qq/authorize.vpage";
    }
}
