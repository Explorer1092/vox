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

package com.voxlearning.utopia.mizar.controller.hsb;

import com.google.code.kaptcha.Constants;
import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import com.google.code.kaptcha.util.Configurable;
import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.core.util.DigestUtils;
import com.voxlearning.alps.core.util.MobileRule;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.utopia.mizar.auth.HbsAuthUser;
import com.voxlearning.utopia.mizar.controller.AbstractMizarController;
import com.voxlearning.utopia.service.mizar.api.entity.hbs.HbsContestant;
import com.voxlearning.utopia.service.mizar.api.entity.hbs.HbsUser;
import com.voxlearning.utopia.service.mizar.consumer.loader.HbsLoaderClient;
import com.voxlearning.utopia.service.mizar.consumer.service.HbsServiceClient;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.sms.consumer.SmsServiceClient;
import lombok.SneakyThrows;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.Properties;

/**
 * 华数杯比赛：成绩查询 - Controller
 * Created by haitian.gan on 2017/2/13.
 */
@Controller
@RequestMapping("/hbs")
public class HbsMatchController extends AbstractMizarController {

    @Inject private HbsLoaderClient hbsLoader;
    @Inject private HbsServiceClient hbsService;
    @Inject private SmsServiceClient smsServiceClient;

    private static final String CAPTCHA_KEY_PREFIX = "HbsScoreLoginCaptcha";
    private static final int CAPTCHA_EXPIRE_TIME = 180;

    @RequestMapping(value = "/score/login.vpage", method = RequestMethod.GET)
    public String loginPage(Model model) {
        model.addAttribute("captchaToken", RandomUtils.randomString(24));
        return "hbs/login";
    }

    @RequestMapping(value = "/score/msm.vpage", method = RequestMethod.GET)
    public String msmPage(Model model) {
        // 如果已经登录的话，并且已经验证过了，则绑定手机
        HbsAuthUser user = getCurrentHbsUser();
        if (user != null) {
            HbsUser hbsUser = hbsLoader.loadUser(user.getUserId());
            if (hbsUser != null && MobileRule.isMobile(hbsUser.getUserName())) {
                model.addAttribute("phoneNumber", hbsUser.getUserName());
            }
        }

        model.addAttribute("captchaToken", RandomUtils.randomString(24));
        return "hbs/msm";
    }

    /**
     * 登录
     *
     * @return
     */
    @RequestMapping(value = "/score/login.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage login() {

        MapMessage resultMsg = new MapMessage();
        resultMsg.add("captchaToken", RandomUtils.randomString(24));

        String userName = getRequestString("username");
        resultMsg.add("userName", userName);

        if (StringUtils.isEmpty(userName)) {
            return resultMsg.setSuccess(false)
                    .setInfo("用户名不能为空")
                    .add("position", "home");
        }

        String password = getRequestString("password");
        if (StringUtils.isEmpty(password)) {
            return resultMsg.setSuccess(false)
                    .setInfo("密码不能为空")
                    .add("position", "home");
        }

        String verifyCode = getRequestString("verifyCode");
        if (StringUtils.isEmpty(verifyCode)) {
            return resultMsg.setSuccess(false)
                    .setInfo("验证码不能为空!")
                    .add("position", "home");
        }

        String captchaToken = getRequestString("captchaToken");
        if (StringUtils.isEmpty(captchaToken)) {
            return resultMsg.setSuccess(false)
                    .setInfo("非法的请求")
                    .add("position", "home");
        }

        if (!consumeCaptchaCode(captchaToken, verifyCode)) {
            return resultMsg.setSuccess(false)
                    .setInfo("验证码错误!")
                    .add("position", "home");
        }

        // 编码
        password = DigestUtils.md5Hex(password);

        HbsUser user;
        // 判断是否为手机号
        if (MobileRule.isMobile(userName)) {
            user = hbsLoader.loadUserByName(userName);
        } else {
            HbsContestant contestant = hbsLoader.findStudentByIdCardNo(userName);
            if (contestant == null) {
                return resultMsg.setSuccess(false)
                        .setInfo("用户不存在!")
                        .add("position", "home");
            } else {
                user = contestant.getUser();
                // 如果验证过手机号，则不再让用身份证登录
                if (MobileRule.isMobile(user.getUserName())) {
                    return resultMsg.setSuccess(false)
                            .setInfo("您已经验证过手机号，请用手机号登录!")
                            .add("position", "home");
                }
            }
        }

        if (user == null) {
            return resultMsg.setSuccess(false)
                    .setInfo("用户不存在!")
                    .add("position", "home");
        }

        if (!Objects.equals(user.getPassword(), password)) {
            return resultMsg.setSuccess(false)
                    .setInfo("密码错误!")
                    .add("position", "home");
        }

        // 判断是否为手机号
        if (MobileRule.isMobile(userName)) {
            setUserToCookie(SafeConverter.toString(user.getId()));
            return resultMsg.setSuccess(true).add("position", "result");
        }
        // 有可能是身份证号码
        else {
            setUserToCookie(SafeConverter.toString(user.getId()));
            return resultMsg.setSuccess(true)
                    .add("position", "msm");
        }
    }

    @RequestMapping(value = "/score/captcha.vpage", method = RequestMethod.GET)
    @SneakyThrows
    public void captcha(HttpServletResponse resp) {
        String token = getRequestString("token");

        Properties properties = new Properties();
        properties.setProperty(Constants.KAPTCHA_TEXTPRODUCER_CHAR_STRING, "0123456789");
        properties.setProperty(Constants.KAPTCHA_TEXTPRODUCER_CHAR_LENGTH, "4");
        properties.setProperty(Constants.KAPTCHA_IMAGE_WIDTH, "80");
        properties.setProperty(Constants.KAPTCHA_IMAGE_HEIGHT, "30");
        properties.setProperty(Constants.KAPTCHA_TEXTPRODUCER_FONT_SIZE, "25");
        properties.setProperty(Constants.KAPTCHA_BORDER, "no");
        properties.setProperty(Constants.KAPTCHA_OBSCURIFICATOR_IMPL, "com.google.code.kaptcha.impl.FishEyeGimpy");

        Producer kaptchaProducer = new DefaultKaptcha();
        ((Configurable) kaptchaProducer).setConfig(new Config(properties));

        // Set to expire far in the past.
        resp.setDateHeader("Expires", 0);
        // Set standard HTTP/1.1 no-cache headers.
        resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        // Set IE extended HTTP/1.1 no-cache headers (use addHeader).
        resp.addHeader("Cache-Control", "post-check=0, pre-check=0");
        // Set standard HTTP/1.0 no-cache header.
        resp.setHeader("Pragma", "no-cache");

        // return a jpeg
        resp.setContentType("image/jpeg");

        // create the text for the image
        String capText = kaptchaProducer.createText();

        // create the image with the text
        BufferedImage bi = kaptchaProducer.createImage(capText);

        // write the data out
        ImageIO.write(bi, "jpg", resp.getOutputStream());
        CacheSystem.CBS.getCache("unflushable").set(CAPTCHA_KEY_PREFIX + ":" + token, CAPTCHA_EXPIRE_TIME, capText);
    }

    private boolean consumeCaptchaCode(String token, String code) {
        if (StringUtils.isEmpty(code))
            return false;

        String cacheKey = CAPTCHA_KEY_PREFIX + ":" + token;
        CacheObject<String> cacheObject = CacheSystem.CBS.getCache("unflushable").get(cacheKey);
        if (cacheObject == null)
            return false;

        String exceptValue = cacheObject.getValue();
        if (StringUtils.equals(exceptValue, StringUtils.trim(code))) {
            CacheSystem.CBS.getCache("unflushable").delete(cacheKey);
            return true;
        } else
            return false;
    }

    /**
     * 发送手机验证码
     *
     * @return
     */
    @RequestMapping(value = "/score/sendsms.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage sendSms() {

       /* HbsAuthUser user = getCurrentHbsUser();
        if(user == null){
            return MapMessage.errorMessage("请重新登录!");
        }*/

        String phoneNumber = getRequestString("phoneNumber");
        if (StringUtils.isEmpty(phoneNumber) || !MobileRule.isMobile(phoneNumber)) {
            return MapMessage.errorMessage("手机号不正确!");
        }

        return smsServiceClient.getSmsService().sendUnbindMobileVerificationCode(
                phoneNumber,
                SmsType.HBS_RESULT_QUERY.name(),
                false);
    }

    /**
     * 验证手机号码，并更新信息
     *
     * @return
     */
    @RequestMapping(value = "/score/verify.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage verifyPhoneNumber() {

        String phoneNumber = getRequestString("phoneNumber");
        if (StringUtils.isEmpty(phoneNumber) || !MobileRule.isMobile(phoneNumber)) {
            return MapMessage.errorMessage("手机号不正确!");
        }

        HbsUser hbsUser = hbsLoader.loadUserByName(phoneNumber);

        HbsAuthUser user = getCurrentHbsUser();
        if (user != null) {
            // 如果是已经登录的情况
            // 校验该手机号码是否已经存在
            if (hbsUser != null && !Objects.equals(hbsUser.getId(), user.getUserId())) {
                return MapMessage.errorMessage("手机号已经被占用!");
            }
        } else {
            if (hbsUser == null) {
                return MapMessage.errorMessage("手机号码不存在或者未经过验证!");
            }

            user = new HbsAuthUser();
            user.setUserId(hbsUser.getId());
        }


        String verifyCode = getRequestString("verifyCode");
        if (StringUtils.isEmpty(verifyCode)) {
            return MapMessage.errorMessage("验证码不能为空!");
        }

        String captchaToken = getRequestString("captchaToken");
        if (StringUtils.isEmpty(captchaToken)) {
            return MapMessage.errorMessage("非法的请求");
        }

        if (!consumeCaptchaCode(captchaToken, verifyCode)) {
            return MapMessage.errorMessage("验证码错误");
        }

        String code = getRequestString("smsCode");
        if (StringUtils.isEmpty(code)) {
            return MapMessage.errorMessage("短信验证码不能为空!");
        }

        String newPwd = getRequestString("newPwd");
        String confirmPwd = getRequestString("confirmPwd");
        if (!Objects.equals(newPwd, confirmPwd)) {
            return MapMessage.errorMessage("两次输入密码不一致!");
        }

        MapMessage validateResult = smsServiceClient.getSmsService().verifyValidateCode(phoneNumber, code, SmsType.HBS_RESULT_QUERY.name());
        if (!validateResult.isSuccess()) {
            return validateResult;
        }

        // 更新手机号
        MapMessage resultMsg = hbsService.updateUserPhoneNumber(user.getUserId(), phoneNumber);
        if (resultMsg.isSuccess()) {
            // 更新密码
            hbsService.updateUserPwd(user.getUserId(), newPwd);
            if (resultMsg.isSuccess())
                return resultMsg.add("mobile", phoneNumber);
            else
                return resultMsg;
        } else {
            return resultMsg;
        }

    }

    /**
     * 成绩查询页面
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "/score/result.vpage")
    public String queryScore(Model model) {
        // 验证登录
        HbsAuthUser user = getCurrentHbsUser();
        if (user == null) {
            return "hbs/result";
        }

        HbsContestant contestant = hbsLoader.loadContestant(user.getUserId());
        if (contestant != null) {
            contestant.setPhoneNumber(contestant.getUser().getUserName());
        } else {
            HbsUser hbsUser = hbsLoader.loadUser(user.getUserId());
            // 如果只有用户信息，没有成绩信息，容下错
            contestant = new HbsContestant();
            contestant.setPhoneNumber(hbsUser.getUserName());
            contestant.setFinalContestResult("——");

            model.addAttribute("info", contestant);
            return "hbs/result";
        }

        if (!StringUtils.isEmpty(contestant.getFinalContestResult())) {
            contestant.setPreContestResult("通过");
        }

        if ("不通过".equals(contestant.getPreContestResult())) {
            contestant.setFinalContestResult("——");
        } else if ("通过".equals(contestant.getPreContestResult())
                && StringUtils.isEmpty(contestant.getFinalContestResult())) {
            contestant.setFinalContestResult(null);
        }

        if (StringUtils.isEmpty(contestant.getPreContestResult())) {
            contestant.setFinalContestResult("——");
        }

        model.addAttribute("info", contestant);
        return "hbs/result";
    }

}
