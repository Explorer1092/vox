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

import com.voxlearning.washington.support.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * 扬州电教馆JSE教育平台SSO对接
 *
 * @author Jia HuanYin
 * @since 2015/6/16
 */
@Controller
@RequestMapping("/yzedulogin")
@Deprecated
public class YzeduController extends AbstractController {

    private static final String SSO_URI = "/ssologin/yzedu.vpage?token=";

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String parseCode() {
        String code = getRequestString("code");
        return "redirect:" + SSO_URI + code;
    }
}
