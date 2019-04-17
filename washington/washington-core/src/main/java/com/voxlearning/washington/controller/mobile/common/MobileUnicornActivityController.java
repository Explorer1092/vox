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

package com.voxlearning.washington.controller.mobile.common;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.washington.controller.mobile.AbstractMobileController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by jiang wei on 2017/3/24.
 */
@Controller
@RequestMapping(value = "/usermobile/unicorn")
@Slf4j
public class MobileUnicornActivityController extends AbstractMobileController {

    /**
     * 获取当前时间是否在活动期间、学生是否有抽奖机会
     */
    @RequestMapping(value = "/activityPeriod.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getActivityStatus() {
        return activityExpiryMsg;
    }

    /**
     * 抽奖
     */
    @RequestMapping(value = "/getReward.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getReward() {
        return activityExpiryMsg;
    }

    /**
     * 获取当前学生的获奖记录接口
     */
    @RequestMapping(value = "/getRewardRecordByStudent.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getRewardRecordByStudent() {
        return activityExpiryMsg;
    }

    /**
     * 获取同校、同班学生的获奖记录接口
     */
    @RequestMapping(value = "/getRewardRecordByClazz.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getRewardRecordByClazz() {
        return activityExpiryMsg;
    }


    @RequestMapping(value = "/addFreeLotteryChance.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addFreeLotteryChance() {
        return activityExpiryMsg;
    }

}
