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

package com.voxlearning.washington.controller.open.v1;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.washington.controller.open.AbstractApiController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import static com.voxlearning.washington.controller.open.ApiConstants.RES_RESULT_ERROR_NEED_UPGRADE;

/**
 * @author shiwe.liao
 * @since 2016/4/26
 */
@Controller
@Slf4j
@RequestMapping(value = "/v1/user/jxt/")
public class UserJxtApiController extends AbstractApiController {

    /**
     * 聊天组成员列表
     */
    @RequestMapping(value = "/easemob_user_list.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getUserListByEaseMobId() {
        return failMessage(RES_RESULT_ERROR_NEED_UPGRADE);
    }


    /**
     * 用户在环信的用户ID和密码
     */
    @RequestMapping(value = "/user_easemob.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getUserEaseMob() {
        return failMessage(RES_RESULT_ERROR_NEED_UPGRADE);
    }

    /**
     * 创建环信组
     *
     * @return
     */
    @RequestMapping(value = "/createEaseMobGroup.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage createChatGroup() {
        return failMessage(RES_RESULT_ERROR_NEED_UPGRADE);
    }

    /**
     * 获取网址白名单
     *
     * @return
     */
    @RequestMapping(value = "/getEaseMobConfigUrlList.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getEseMobConfigUrlList() {
        return failMessage(RES_RESULT_ERROR_NEED_UPGRADE);
    }

}
