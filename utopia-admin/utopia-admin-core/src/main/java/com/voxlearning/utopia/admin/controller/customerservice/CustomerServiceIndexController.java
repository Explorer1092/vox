/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.admin.controller.customerservice;

import com.voxlearning.utopia.admin.controller.AbstractAdminSystemController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author Longlong Yu
 * @since 下午7:53,13-11-13.
 * 功能移动去运营管理节点 By Wyc 2016-04-28
 */
@Deprecated
@Controller
@RequestMapping("/customerservice")
public class CustomerServiceIndexController extends AbstractAdminSystemController {

    @RequestMapping(value = "customerserviceindex.vpage", method = RequestMethod.GET)
    public String customerServiceIndex(Model model) {
        return "opmanager/notice";
    }

}
