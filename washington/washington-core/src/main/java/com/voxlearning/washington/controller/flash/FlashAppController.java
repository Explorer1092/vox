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

package com.voxlearning.washington.controller.flash;

import com.voxlearning.utopia.business.net.frameworkapp.messages.InitInfoRequest;
import com.voxlearning.utopia.business.net.frameworkapp.types.response.BuyItemResponse;
import com.voxlearning.utopia.business.net.frameworkapp.types.response.InitInfoResponse;
import com.voxlearning.utopia.business.net.frameworkapp.types.response.RefreshInfoResponse;
import com.voxlearning.utopia.business.net.frameworkapp.types.response.UseItemResponse;
import com.voxlearning.washington.support.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by tanguohong on 14-7-9.
 */
@Controller
@RequestMapping("/flash")
public class FlashAppController extends AbstractController {

    @RequestMapping(value = "{subject}/app/api/initinfo.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String initInfo(@PathVariable("subject") String subject) {
        InitInfoResponse resp = InitInfoRequest.newResponse();
        logger.error("功能已下线");
        resp.success = false;
        return resp.toResponse();

    }

    @RequestMapping(value = "{subject}/app/api/buyitem.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String buyItem(@PathVariable("subject") String subject) {

        BuyItemResponse resp = new BuyItemResponse();
        logger.error("功能已下线");
        resp.success = false;
        return resp.toResponse();

    }


    @RequestMapping(value = "{subject}/app/api/useitem.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String useItem(@PathVariable("subject") String subject) {

        UseItemResponse resp = new UseItemResponse();
        logger.error("功能已下线");
        resp.success = false;
        return resp.toResponse();

    }

    @RequestMapping(value = "{subject}/app/api/refreshinfo.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String refreshInfo(@PathVariable("subject") String subject) {
        RefreshInfoResponse resp = new RefreshInfoResponse();
        logger.error("功能已下线");
        resp.success = false;
        return resp.toResponse();

    }
}
