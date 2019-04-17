/*
 * VOX LEARNING TECHNOLOGY, INC. CONFIDENTIAL
 *
 * Copyright 2006-2013 Vox Learning Technology, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Vox Learning Technology, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Vox Learning
 * Technology, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Vox Learning Technology, Inc.
 */

package com.voxlearning.washington.controller.open;

import com.voxlearning.washington.data.OpenAuthContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Longlong Yu
 * @since 2013/05/07
 */
@Controller
@RequestMapping(value = "/open/student")
public class StudentOpenController extends AbstractOpenController {
    /**
     * 孩子获取孩子信息
     */
    @RequestMapping(value = "/childInfo.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext childInfo(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setError("手机版功能升级中，请通过浏览器访问 www.17zuoye.com 完成操作");
        openAuthContext.add("successful", false);
        return openAuthContext;
    }

    /**
     * 孩子获取教材
     */
    @RequestMapping(value = "bookList.vpage")
    @ResponseBody
    public OpenAuthContext bookList(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setError("手机版功能升级中，请通过浏览器访问 www.17zuoye.com 完成操作");
        openAuthContext.add("successful", false);
        return openAuthContext;
    }
}
