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

// $Id: TeacherOpenController.java 16032 2013-01-16 08:27:03Z xiaohai.zhang $
package com.voxlearning.washington.controller.open;

import com.voxlearning.washington.data.OpenAuthContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/*
public final class StatusCode {

    // public static int SUCCESS = 200;

    public static String ERROR_CODE_20201 = "20201";
    public static String ERROR_CONTENT_20201 = "登录用户未找到。";
    public static String ERROR_CODE_20202 = "20202";
    public static String ERROR_CONTENT_20202 = "定单号为空。";
    public static String ERROR_CODE_20203 = "20203";
    public static String ERROR_CONTENT_20203 = "上传头像时异常。";
    public static String ERROR_CODE_20204 = "20204";
    public static String ERROR_CONTENT_20204 = "图片流有问题。";
    public static String ERROR_CODE_20205 = "20205";
    public static String ERROR_CONTENT_20205 = "金币不足。";
    public static String ERROR_CODE_20206 = "20206";
    public static String ERROR_CONTENT_20206 = "银币不足。";


}
*/
@Controller
@RequestMapping(value = "/open/teacher")
public class TeacherOpenController extends AbstractOpenController {
    @RequestMapping(value = "/info.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext info(HttpServletRequest request, Model model) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setError("手机版功能升级中，请通过浏览器访问 www.17zuoye.com 完成操作");
        openAuthContext.add("successful", false);
        return openAuthContext;
    }

    @RequestMapping(value = "/gethomeworklist.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext gethomeworklist(HttpServletRequest request, Model model) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setError("手机版功能升级中，请通过浏览器访问 www.17zuoye.com 完成操作");
        openAuthContext.add("successful", false);
        return openAuthContext;
    }

    @RequestMapping(value = "/checkhomework.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext checkhomework(HttpServletRequest request, Model model) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setError("手机版功能升级中，请通过浏览器访问 www.17zuoye.com 完成操作");
        openAuthContext.add("successful", false);
        return openAuthContext;
    }

    @RequestMapping(value = "/gethomeworkhistorylist.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext gethomeworkhistorylist(HttpServletRequest request, Model model) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setError("手机版功能升级中，请通过浏览器访问 www.17zuoye.com 完成操作");
        openAuthContext.add("successful", false);
        return openAuthContext;
    }

    @RequestMapping(value = "/gethomeworkdetailclasstotal.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext gethomeworkdetailclasstotal(HttpServletRequest request, Model model) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setError("手机版功能升级中，请通过浏览器访问 www.17zuoye.com 完成操作");
        openAuthContext.add("successful", false);
        return openAuthContext;
    }

    @RequestMapping(value = "/gethomeworkdetailstudentrank.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext gethomeworkdetailstudentrank(HttpServletRequest request, Model model) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setError("手机版功能升级中，请通过浏览器访问 www.17zuoye.com 完成操作");
        openAuthContext.add("successful", false);
        return openAuthContext;
    }


    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/gethomeworkdetailstudenttotal.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext gethomeworkdetailstudenttotal(HttpServletRequest request, Model model) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setError("手机版功能升级中，请通过浏览器访问 www.17zuoye.com 完成操作");
        openAuthContext.add("successful", false);
        return openAuthContext;
    }

    @RequestMapping(value = "/getparentletter.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext getparentletter(HttpServletRequest request, Model model) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setError("手机版功能升级中，请通过浏览器访问 www.17zuoye.com 完成操作");
        openAuthContext.add("successful", false);
        return openAuthContext;
    }

    @RequestMapping(value = "/getstudentletter.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext getstudentletter(HttpServletRequest request, Model model) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setError("手机版功能升级中，请通过浏览器访问 www.17zuoye.com 完成操作");
        openAuthContext.add("successful", false);
        return openAuthContext;
    }

    @RequestMapping(value = "/getreply.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext getreply(HttpServletRequest request, Model model) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setError("手机版功能升级中，请通过浏览器访问 www.17zuoye.com 完成操作");
        openAuthContext.add("successful", false);
        return openAuthContext;
    }

    @RequestMapping(value = "/sendletter.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext sendletter(HttpServletRequest request, Model model) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setError("手机版功能升级中，请通过浏览器访问 www.17zuoye.com 完成操作");
        openAuthContext.add("successful", false);
        return openAuthContext;
    }

    @RequestMapping(value = "/sendreply.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext sendreply(HttpServletRequest request, Model model) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setError("手机版功能升级中，请通过浏览器访问 www.17zuoye.com 完成操作");
        openAuthContext.add("successful", false);
        return openAuthContext;
    }

    @RequestMapping(value = "/getnotice.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext getnotice(HttpServletRequest request, Model model) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setError("手机版功能升级中，请通过浏览器访问 www.17zuoye.com 完成操作");
        openAuthContext.add("successful", false);
        return openAuthContext;
    }

    @RequestMapping(value = "/getclasslist.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext getclasslist(HttpServletRequest request, Model model) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setError("手机版功能升级中，请通过浏览器访问 www.17zuoye.com 完成操作");
        openAuthContext.add("successful", false);
        return openAuthContext;
    }

    @RequestMapping(value = "/getstudentlist.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext getstudentlist(HttpServletRequest request, Model model) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setError("手机版功能升级中，请通过浏览器访问 www.17zuoye.com 完成操作");
        openAuthContext.add("successful", false);
        return openAuthContext;
    }

    @RequestMapping(value = "/getparentlist.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext getparentlist(HttpServletRequest request, Model model) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setError("手机版功能升级中，请通过浏览器访问 www.17zuoye.com 完成操作");
        openAuthContext.add("successful", false);
        return openAuthContext;
    }

    @RequestMapping(value = "/updatepwd.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext updatePassword(HttpServletRequest request, Model model) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setError("手机版功能升级中，请通过浏览器访问 www.17zuoye.com 完成操作");
        openAuthContext.add("successful", false);
        return openAuthContext;
    }

    @RequestMapping(value = "/updateimg.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext updateImg(HttpServletRequest request, Model model) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setError("手机版功能升级中，请通过浏览器访问 www.17zuoye.com 完成操作");
        openAuthContext.add("successful", false);
        return openAuthContext;
    }

    @RequestMapping(value = "/updatename.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext updateName(HttpServletRequest request, Model model) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setError("手机版功能升级中，请通过浏览器访问 www.17zuoye.com 完成操作");
        openAuthContext.add("successful", false);
        return openAuthContext;
    }

    @RequestMapping(value = "/deletehomework.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext deleteHomework(HttpServletRequest request, Model model) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setError("手机版功能升级中，请通过浏览器访问 www.17zuoye.com 完成操作");
        openAuthContext.add("successful", false);
        return openAuthContext;
    }

    /* --------------------------------------------------------
     * 用于英语老师为英语作业写评语
     * -------------------------------------------------------- */
    @RequestMapping(value = "/savenote.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext savenote(HttpServletRequest request, Model model) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setError("手机版功能升级中，请通过浏览器访问 www.17zuoye.com 完成操作");
        openAuthContext.add("successful", false);
        return openAuthContext;
    }
}
