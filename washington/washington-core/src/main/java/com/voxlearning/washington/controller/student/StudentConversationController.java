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

package com.voxlearning.washington.controller.student;

import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.washington.support.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Student conversation related controller implementation.
 *
 * @author Xiaohai Zhang
 * @since 2013-11-28 21:33
 */
@Controller
@RequestMapping("/student/conversation")
public class StudentConversationController extends AbstractController {

    /**
     * Student conversation index (start page).
     */
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index() {
        return "redirect:" + ProductConfig.getUcenterUrl() + "/student/conversation/index.vpage";
    }

    /**
     * Student gets his/her own conversations.
     */
    @RequestMapping(value = "conversations.vpage", method = RequestMethod.GET)
    public String getConversation() {
        return "redirect:" + ProductConfig.getUcenterUrl() + "/student/conversation/conversations.vpage";
    }

}
