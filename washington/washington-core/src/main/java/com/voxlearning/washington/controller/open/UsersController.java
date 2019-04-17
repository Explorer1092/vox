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
import lombok.extern.slf4j.Slf4j;
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
@RequestMapping(value = "/open")
@Slf4j
public class UsersController extends AbstractOpenController {
    @RequestMapping(value = "/test.vpage", method = RequestMethod.GET)
    public String test(Model model) {
        return "open/test";
    }

    /**
     * 用户登录
     */
    @RequestMapping(value = "/login.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext login(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setError("手机版功能升级中，请通过浏览器访问 www.17zuoye.com 完成操作");
        openAuthContext.add("successful", false);
        return openAuthContext;
    }

    /**
     * 家长获取孩子信息
     */
    @RequestMapping(value = "/childinfo.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext childinfo(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setError("手机版功能升级中，请通过浏览器访问 www.17zuoye.com 完成操作");
        openAuthContext.add("successful", false);
        return openAuthContext;
    }

    /**
     * 3.4 获取孩子成绩
     */
    @RequestMapping(value = "/homeworkresult.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext homeworkresult(HttpServletRequest request, Model model) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setError("手机版功能升级中，请通过浏览器访问 www.17zuoye.com 完成操作");
        openAuthContext.add("successful", false);
        return openAuthContext;
    }


    /**
     * 3.9　获得班级排名明细
     */
    @RequestMapping(value = "/classrankdetial.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext classrankdetial(HttpServletRequest request, Model model) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setError("手机版功能升级中，请通过浏览器访问 www.17zuoye.com 完成操作");
        openAuthContext.add("successful", false);
        return openAuthContext;
    }

    /**
     * 3.10 获得老师留言总数
     */
    @RequestMapping(value = "/lettercount.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext lettercount(HttpServletRequest request, Model model) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setError("手机版功能升级中，请通过浏览器访问 www.17zuoye.com 完成操作");
        openAuthContext.add("successful", false);
        return openAuthContext;

    }

    /**
     * 3.11获得老师留言
     */
    @RequestMapping(value = "/teacherletter.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext teacherletter(HttpServletRequest request, Model model) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setError("手机版功能升级中，请通过浏览器访问 www.17zuoye.com 完成操作");
        openAuthContext.add("successful", false);
        return openAuthContext;

    }

    /**
     * 3 获得家长留言总数
     */
    @RequestMapping(value = "/parentlettercount.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext parentlettercount(HttpServletRequest request, Model model) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setError("手机版功能升级中，请通过浏览器访问 www.17zuoye.com 完成操作");
        openAuthContext.add("successful", false);
        return openAuthContext;

    }

    /**
     * 3.12获得家长留言
     */
    @RequestMapping(value = "/parentletter.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext parentletter(HttpServletRequest request, Model model) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setError("手机版功能升级中，请通过浏览器访问 www.17zuoye.com 完成操作");
        openAuthContext.add("successful", false);
        return openAuthContext;

    }

    // 3.13用户反馈 (网站上暂时没有实现)
    // 3.14获取验证码(网站上暂时没有实现)

    /**
     * 用户自己修改密码
     */
    @RequestMapping(value = "/updatepwd.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext updatepwd(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setError("手机版功能升级中，请通过浏览器访问 www.17zuoye.com 完成操作");
        openAuthContext.add("successful", false);
        return openAuthContext;
    }

    /**
     * 用户修改头像
     */
    @RequestMapping(value = "/updateimage.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext updateimage(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setError("手机版功能升级中，请通过浏览器访问 www.17zuoye.com 完成操作");
        openAuthContext.add("successful", false);
        return openAuthContext;
    }

    /**
     * 家长与孩子建立关系.
     */
    @RequestMapping(value = "addchild.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext addchild(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setError("手机版功能升级中，请通过浏览器访问 www.17zuoye.com 完成操作");
        openAuthContext.add("successful", false);
        return openAuthContext;
    }

    /**
     * 3.18 注册家长帐号
     */
    @RequestMapping(value = "/signup.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext signup(HttpServletRequest request, Model model) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setError("手机版功能升级中，请通过浏览器访问 www.17zuoye.com 完成操作");
        openAuthContext.add("successful", false);
        return openAuthContext;
    }

    /**
     * 家长、老师通过邮件找回密码
     */
    @RequestMapping(value = "/recoverpwd.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext recoverpwd(HttpServletRequest request) throws Exception {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setError("手机版功能升级中，请通过浏览器访问 www.17zuoye.com 完成操作");
        openAuthContext.add("successful", false);
        return openAuthContext;
    }

    /**
     * 家长获得老师列表
     */
    @RequestMapping(value = "/teacherlist.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext teacherlist(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setError("手机版功能升级中，请通过浏览器访问 www.17zuoye.com 完成操作");
        openAuthContext.add("successful", false);
        return openAuthContext;
    }

    /**
     * 3.22给老师留言
     */
    @RequestMapping(value = "/sendLetter.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext sendletter(HttpServletRequest request, Model model) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setError("手机版功能升级中，请通过浏览器访问 www.17zuoye.com 完成操作");
        openAuthContext.add("successful", false);
        return openAuthContext;
    }

    /**
     * 3.23回复留言
     */
    @RequestMapping(value = "/replyLetter.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext replyLetter(HttpServletRequest request, Model model) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setError("手机版功能升级中，请通过浏览器访问 www.17zuoye.com 完成操作");
        openAuthContext.add("successful", false);
        return openAuthContext;
    }

    /**
     * 3.24勋章信息
     */
    @RequestMapping(value = "/medalinfo.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext medalinfo(HttpServletRequest request, Model model) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setError("手机版功能升级中，请通过浏览器访问 www.17zuoye.com 完成操作");
        openAuthContext.add("successful", false);
        return openAuthContext;
    }
}
