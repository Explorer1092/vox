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

package com.voxlearning.washington.controller.open.wechat.teacher;

import com.voxlearning.washington.controller.open.AbstractOpenController;
import com.voxlearning.washington.data.OpenAuthContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * @author RuiBao
 * @version 0.1
 * @since 6/19/2015
 */
@Controller
@RequestMapping(value = "/open/wechat/vh")
@Slf4j
public class WechatVacationHomeworkController extends AbstractOpenController {

    @RequestMapping(value = "bavhsc.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext batchAssignVacationHomework_SelectClazz(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("功能已下线");
        return openAuthContext;
    }

    @RequestMapping(value = "bavhgr.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext batchAssignVacationHomework_GetRecommendation(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("功能已下线");
        return openAuthContext;
    }

    @RequestMapping(value = "bavhsd.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext batchAssignVacationHomework_SelectDate(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("功能已下线");
        return openAuthContext;
    }

    @RequestMapping(value = "bavh.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext batchAssignVacationHomework(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("功能已下线");
        return openAuthContext;
    }

    @RequestMapping(value = "list.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext list(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("功能已下线");
        return openAuthContext;
    }

    @RequestMapping(value = "clazzvhscore.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext clazzVacationHomeworkScore(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("功能已下线");
        return openAuthContext;
    }

    @RequestMapping(value = "wvhc.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext writeVacationHomeworkComment(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("功能已下线");
        return openAuthContext;
    }

    @RequestMapping(value = "abts.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext awardBeanToStudent(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("功能已下线");
        return openAuthContext;
    }
}
