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

package com.voxlearning.washington.controller.teacher;

import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.washington.support.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @deprecated 相关controller已移至ucenter
 */
@Controller
@RequestMapping("/teacher/center")
@Deprecated
public class TeacherCenterController extends AbstractController {
    // 2014暑期改版 -- 教师个人中心 -- 基本信息
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index() {
        return "redirect:" + ProductConfig.getUcenterUrl() + "/teacher/center/index.vpage";
    }

    // 2014暑期改版 -- 教师个人中心 -- 我的资料
    @RequestMapping(value = "myprofile.vpage", method = RequestMethod.GET)
    public String myProfile() {
        return "redirect:" + ProductConfig.getUcenterUrl() + "/teacher/center/index.vpage#/teacher/center/myprofile.vpage";
    }

    // 2014暑期改版 -- 教师个人中心 -- 账号安全
    @RequestMapping(value = "securitycenter.vpage", method = RequestMethod.GET)
    public String securityInformation() {
        return "redirect:" + ProductConfig.getUcenterUrl() + "/teacher/center/index.vpage#/teacher/center/securitycenter.vpage";
    }

    // 2014暑期改版 -- 教师个人中心 -- 我的等级
    @RequestMapping(value = "mylevel.vpage", method = RequestMethod.GET)
    public String myLevel() {
        return "redirect:" + ProductConfig.getUcenterUrl() + "/teacher/center/index.vpage#/teacher/center/mylevel.vpage";
    }

    // 2014暑期改版 -- 教师个人中心 -- 我的金币
    @RequestMapping(value = "mygold.vpage", method = RequestMethod.GET)
    public String integral() {
        return "redirect:" + ProductConfig.getUcenterUrl() + "/teacher/center/index.vpage#/teacher/center/mygold.vpage";
    }

    @RequestMapping(value = "mygoldchip.vpage", method = RequestMethod.GET)
    public String integralChip() {
        return "redirect:" + ProductConfig.getUcenterUrl() + "/teacher/center/mygoldchip.vpage";
    }

    // 2014暑期改版 -- 教师个人中心 -- 我的认证
    @RequestMapping(value = "myauthenticate.vpage", method = RequestMethod.GET)
    public String myAuthenticate() {
        return "redirect:" + ProductConfig.getUcenterUrl() + "/teacher/center/index.vpage#/teacher/center/myauthenticate.vpage";
    }

    // 2014暑期改版 -- 教师个人中心 -- 提交认证确认页面
    @RequestMapping(value = "authenticatechip.vpage", method = RequestMethod.GET)
    public String authenticateChip() {
        return "redirect:" + ProductConfig.getUcenterUrl() + "/teacher/center/authenticatechip.vpage";
    }
}
