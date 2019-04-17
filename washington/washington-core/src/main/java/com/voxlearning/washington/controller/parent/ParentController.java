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

// $Id: ParentController.java 16245 2013-01-23 09:24:20Z xiaohai.zhang $
package com.voxlearning.washington.controller.parent;

import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.washington.support.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * ParentController.java
 *
 * @author Jingwei Dong
 * @author Lin Zhu
 * @author Yaoheng Wu
 * @author Guohong Tan
 * @author Liangliang Zhang
 * @author Xinqiang Wang
 * @author Rui Bao
 * @author Xiaohai Zhang
 * @author luwei li
 * @since 2011-08-26
 */
@Controller
@RequestMapping("/parent")
public class ParentController extends AbstractController {

    /**
     * 家长首页
     */
    @SuppressWarnings("ConstantConditions")
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model, @RequestParam(value = "redirectindex", required = false) String redirectindex, @RequestParam(value = "redirecturl", required = false) String redirecturl) {
        final User user = currentUser();
        if (user != null) {
            return "parent/index";
        } else {
            return "redirect:/login.vpage";
        }

    }

}
