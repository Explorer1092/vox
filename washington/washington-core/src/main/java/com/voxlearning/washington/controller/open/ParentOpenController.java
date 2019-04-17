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
@RequestMapping(value = "/open/parent")
public class ParentOpenController extends AbstractOpenController {
    /**
     * 家长-孩子-作业成绩
     */
    @RequestMapping(value = "index.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext index(HttpServletRequest request, Model model) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setError("手机版功能升级中，请通过浏览器访问 www.17zuoye.com 完成操作");
        openAuthContext.add("successful", false);
        return openAuthContext;
    }

    /**
     * 家长-孩子-成绩排名
     */
    @RequestMapping(value = "ranking.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext ranking(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setError("手机版功能升级中，请通过浏览器访问 www.17zuoye.com 完成操作");
        openAuthContext.add("successful", false);
        return openAuthContext;
    }

    /**
     * 家长-孩子-知识点报告
     */
    @RequestMapping(value = "report.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext report(HttpServletRequest request, Model model) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setError("手机版功能升级中，请通过浏览器访问 www.17zuoye.com 完成操作");
        openAuthContext.add("successful", false);
        return openAuthContext;
    }

    /**
     * 家长-孩子-最新作业列表
     */
    @RequestMapping(value = "homework.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext homework(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setError("手机版功能升级中，请通过浏览器访问 www.17zuoye.com 完成操作");
        openAuthContext.add("successful", false);
        return openAuthContext;
    }

    /**
     * 家长-孩子-历史作业列表
     */
    @RequestMapping(value = "homeworkchip.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext homeworkchip(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setError("手机版功能升级中，请通过浏览器访问 www.17zuoye.com 完成操作");
        openAuthContext.add("successful", false);
        return openAuthContext;
    }

    @RequestMapping(value = "getUnreadNum.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext getUnreadNum(HttpServletRequest request, Model model) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setError("手机版功能升级中，请通过浏览器访问 www.17zuoye.com 完成操作");
        openAuthContext.add("successful", false);
        return openAuthContext;
    }


    @RequestMapping(value = "getnotice.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext getnotice(HttpServletRequest request, Model model) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setError("手机版功能升级中，请通过浏览器访问 www.17zuoye.com 完成操作");
        openAuthContext.add("successful", false);
        return openAuthContext;
    }

    /**
     * 家长获得老师对他（她）的留言
     */
    @RequestMapping(value = "getteacherletter.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext getTeacherLetter(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setError("手机版功能升级中，请通过浏览器访问 www.17zuoye.com 完成操作");
        openAuthContext.add("successful", false);
        return openAuthContext;
    }

    /**
     * 家长对老师发送留言
     */
    @RequestMapping(value = "sendLetter.vpage")
    @ResponseBody
    public OpenAuthContext sendLetter(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setError("手机版功能升级中，请通过浏览器访问 www.17zuoye.com 完成操作");
        openAuthContext.add("successful", false);
        return openAuthContext;
    }

    /**
     * 对家长显示老师回复
     */
    @RequestMapping(value = "getreply.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext GetReply(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setError("手机版功能升级中，请通过浏览器访问 www.17zuoye.com 完成操作");
        openAuthContext.add("successful", false);
        return openAuthContext;
    }

}
