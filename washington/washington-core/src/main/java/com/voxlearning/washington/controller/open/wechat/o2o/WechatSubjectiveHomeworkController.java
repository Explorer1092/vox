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

package com.voxlearning.washington.controller.open.wechat.o2o;

import com.voxlearning.utopia.core.runtime.ProductDevelopment;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.client.GrayFunctionManagerClient;
import com.voxlearning.washington.controller.open.AbstractOpenController;
import com.voxlearning.washington.data.OpenAuthContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

/**
 * @author admin
 * @since 2015/7/27
 */
@Controller
@RequestMapping(value = "/open/wechat/o2o/sh")
public class WechatSubjectiveHomeworkController extends AbstractOpenController {

    @Inject private GrayFunctionManagerClient grayFunctionManagerClient;

    /**
     * 学生端->获取主观作业
     */
    @RequestMapping(value = "gethomework.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext getSubjectiveHomework() {
        HttpServletRequest request = getRequest();
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("功能已下线");
        return openAuthContext;
    }

    @Deprecated
    @RequestMapping(value = "uploadresult.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext uploadSubjectiveHomework() {
        HttpServletRequest request = getRequest();
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("功能已下线");
        return openAuthContext;
    }

    @RequestMapping(value = "uploadimages.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext uploadSubjectiveHomework2() {
        HttpServletRequest request = getRequest();
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("功能已下线");
        return openAuthContext;
    }

    @RequestMapping(value = "assignhomework.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext assignSubjectiveHomework() {
        HttpServletRequest request = getRequest();
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("功能已下线");
        return openAuthContext;


    }

    @RequestMapping(value = "savehomework.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext saveSubjectiveHomework() {
        HttpServletRequest request = getRequest();

        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("功能已下线");
        return openAuthContext;

    }

    @RequestMapping(value = "history/checkstudetail.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext getStudentSubjectiveHWDetail() {
        HttpServletRequest request = getRequest();
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("功能已下线");
        return openAuthContext;
    }

    @RequestMapping(value = "removehomework.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext removeSubjectiveHomework() {
        HttpServletRequest request = getRequest();
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("功能已下线");
        return openAuthContext;
    }

    @RequestMapping(value = "uncheckedhomeworks.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext getUncheckedSubjectiveHomework() {
        HttpServletRequest request = getRequest();
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("功能已下线");
        return openAuthContext;
    }

    @RequestMapping(value = "checkhomwork.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext checkSubjectiveHomework() {
        HttpServletRequest request = getRequest();
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("功能已下线");
        return openAuthContext;
    }

    @RequestMapping(value = "history.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext subjectiveHomeworkHistory() {
        HttpServletRequest request = getRequest();
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("功能已下线");
        return openAuthContext;
    }

    @RequestMapping(value = "historydetail.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext historyDetail() {
        HttpServletRequest request = getRequest();
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("功能已下线");
        return openAuthContext;
    }

    @RequestMapping(value = "writecomment.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext writeComment() {
        HttpServletRequest request = getRequest();
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("功能已下线");
        return openAuthContext;
    }

    private boolean isTeacherSubjectiveHWAvailable(TeacherDetail teacher) {
        if (teacher == null || teacher.getRootRegionCode() == null) {
            return false;
        }
        return (ProductDevelopment.isTestEnv()
                || ProductDevelopment.isDevEnv()
                || teacher.getRootRegionCode().equals(320000)
                || teacher.getRootRegionCode().equals(440000)
                || teacher.getRootRegionCode().equals(370000)
                || grayFunctionManagerClient.getTeacherGrayFunctionManager().isWebGrayFunctionAvailable(teacher, "SubjectiveHW", "Teacher"));
    }
}
