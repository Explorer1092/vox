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
import java.util.concurrent.TimeUnit;

/**
 * 微信测验，布置测验、检查测验
 *
 * @author Jia HuanYin
 * @since 2015/8/27
 */
@Controller
@RequestMapping(value = "/open/wechat/quiz")
@Slf4j
public class WechatQuizController extends AbstractOpenController {

    private static final long MIN_QUIZ_MILLS = TimeUnit.MINUTES.toMillis(5);
    private static final String QUIZ_HISTORY_START_DATE = "2015-03-01 00:00:00";

    /**
     * 可布置测验的班级
     */
    @RequestMapping(value = "clazzes.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext clazzes(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("功能已下线");
        return openAuthContext;
    }

    /**
     * 测验试卷
     */
    @RequestMapping(value = "papers.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext papers(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("功能已下线");
        return openAuthContext;
    }

    /**
     * 布置测验
     */
    @RequestMapping(value = "assign.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext assign(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("功能已下线");
        return openAuthContext;
    }

    /**
     * 测验列表
     */
    @RequestMapping(value = "list.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext list(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("功能已下线");
        return openAuthContext;
    }

    /**
     * 检查测验
     */
    @RequestMapping(value = "check.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext check(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("功能已下线");
        return openAuthContext;
    }

    /**
     * 删除测验
     */
    @RequestMapping(value = "delete.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext delete(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("功能已下线");
        return openAuthContext;
    }

    /**
     * 测验报告
     */
    @RequestMapping(value = "report.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext report(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("功能已下线");
        return openAuthContext;
    }

    /**
     * 测验历史
     */
    @RequestMapping(value = "history.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext history(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("功能已下线");
        return openAuthContext;
    }

    private boolean nonTeachingClazz(Long teacherId, Long clazzId) {
        return clazzId != null && !teacherLoaderClient.isTeachingClazz(teacherId, clazzId);
    }
}
