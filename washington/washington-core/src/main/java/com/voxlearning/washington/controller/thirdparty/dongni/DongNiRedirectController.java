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

package com.voxlearning.washington.controller.thirdparty.dongni;

import com.voxlearning.washington.controller.thirdparty.base.VendorRedirectController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * 懂你接入
 *
 * @author Wenlong Meng
 * @version 1.0.0
 * @date 2018-10-09
 */
@Controller
@RequestMapping("/redirector/dongni")
@Slf4j
public class DongNiRedirectController extends VendorRedirectController {

    private static final String APP_KEY = "DongNi";
    private static final String REPORT_LIST_PAGE = "https://m.dongni100.com/auth/17zuoye";

    /**
     * 懂你报告列表页
     *
     * @return
     */
    @RequestMapping(value="list.vpage", method = RequestMethod.GET)
    public String list() {
        String redirect = this.redirect(REPORT_LIST_PAGE);
        log.info("{}: {}", appKey(), redirect);
        return redirect;
    }

    /**
     * 获取appKey
     *
     * @return
     */
    public String appKey(){
        return APP_KEY;
    }
}
