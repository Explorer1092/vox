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

package com.voxlearning.washington.controller.open.wechat.o2o;

import com.voxlearning.washington.controller.open.AbstractOpenController;
import com.voxlearning.washington.data.OpenAuthContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author by shiwei.liao
 * @since 2015/6/23.
 */
@Controller
@RequestMapping("/open/wechat/o2o/tutor")
@Slf4j
public class WechatO2OTutorController extends AbstractOpenController {

    /**
     * 教辅材料的章节选择列表
     */
    @RequestMapping(value = "partlist.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext getPartListOfTutor(HttpServletRequest request) {
        OpenAuthContext context = getOpenAuthContext(request);
        context.setCode("200");
        context.setError("该活动已下线");
        context.add("partList", null);
        return context;
    }

    /**
     * 签到列表页
     */
    @RequestMapping(value = "signindex.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext signIndex(HttpServletRequest request) {
        OpenAuthContext context = getOpenAuthContext(request);
        String paperName = "";
        List<Map<String, Object>> signList = new ArrayList<>();

        context.setCode("200");
        context.setError("该活动已下线");
        context.add("signList", signList);
        context.add("paperName", paperName);
        return context;
    }

    /**
     * 签到
     */
    @RequestMapping(value = "sign.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext sign(HttpServletRequest request) {
        OpenAuthContext context = getOpenAuthContext(request);
        context.setCode("200");

        context.add("description", "该活动已下线");
        context.add("success", false);
        return context;

    }

    /**
     * 校验是否是天津的学校&&系统自建班级
     */
    @RequestMapping(value = "istianjin.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext validateIsTianJinSchool(HttpServletRequest request) {
        OpenAuthContext context = getOpenAuthContext(request);
        context.setCode("200");
        context.setError("该活动已下线");
        context.add("isShowTutor", false);
        return context;
    }

}
