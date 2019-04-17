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

package com.voxlearning.washington.controller.open.v2.wonderland;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.washington.controller.open.AbstractApiController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;

import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * @author Ruib
 * @since 2017/5/25
 */
@Controller
@RequestMapping(value = "/v2/wonderland")
public class WonderlandApiV2Controller extends AbstractApiController {

    @RequestMapping(value = "/popups.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage fetchPopups() {
        MapMessage mesg = new MapMessage();

        try {
            validateRequest(REQ_ACTIVITY_ID);
        } catch (IllegalArgumentException e) {
            mesg.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            mesg.add(RES_MESSAGE, e.getMessage());
            return mesg;
        }

        mesg.add(RES_RESULT, RES_RESULT_SUCCESS);
        mesg.add(RES_POPUPS, new ArrayList<>());
        return mesg;

    }
}
