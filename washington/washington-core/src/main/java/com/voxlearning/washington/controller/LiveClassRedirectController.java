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

package com.voxlearning.washington.controller;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.util.DigestUtils;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.alps.webmvc.cookie.CookieManager;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.washington.support.AbstractController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

import static com.voxlearning.utopia.service.config.api.constant.ConfigCategory.PRIMARY_PLATFORM_GENERAL;

/**
 * Created by xiang.lv on 2016/10/26.
 *
 * @author xiang.lv
 * @date 2016/10/26   16:20
 */
@Controller
@RequestMapping("/redirector")
@Slf4j
public class LiveClassRedirectController extends AbstractController {

    private static final String CLASS_URL_CONFIG = "live_class_ohwit_url";
    private static final String PLAY_BACK_URL_CONFIG = "live_class_ohwit_play_back_url";

    //默认用户在cookie中的key
    private static final String DEFAULT_USER_ID_COOK_KEY = "ohwit_user_id";

    @Inject private CommonConfigServiceClient commonConfigServiceClient;

    @RequestMapping(value = "ohwit.vpage", method = RequestMethod.GET)
    public String goOhwitClass(Model model) {
        //1.用户帐号： request里面获取 sid ／ 家长的第一个孩子id／家长自己的id
        String classroom = getRequestString("classroom");
        if (StringUtils.isBlank(classroom)) {
            return "redirect:/";
        }
        String url = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(PRIMARY_PLATFORM_GENERAL.name(), CLASS_URL_CONFIG);
        return "redirect:" + UrlUtils.buildUrlQuery(url, getParamMap(classroom));
    }

    @RequestMapping(value = "ohwitframe.vpage", method = RequestMethod.GET)
    public String goOhwitClassFrame(Model model) {
        //1.用户帐号： request里面获取 sid ／ 家长的第一个孩子id／家长自己的id
        String classroom = getRequestString("classroom");
        if (StringUtils.isBlank(classroom)) {
            return "redirect:/";
        }
        String url = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(PRIMARY_PLATFORM_GENERAL.name(), CLASS_URL_CONFIG);
        model.addAttribute("url", UrlUtils.buildUrlQuery(url, getParamMap(classroom)));

        // add text and link url
        String text = getRequestString("text");
        String linkUrl = getRequestString("link");
        if (StringUtils.isNoneBlank(text) && StringUtils.isNoneBlank(linkUrl)) {
            model.addAttribute("text", text);
            model.addAttribute("linkUrl", linkUrl);
        }

        //跳转在ftl页面
        return "/ohwit/ohwitframe";
    }


    @RequestMapping(value = "ohwitplayback.vpage", method = RequestMethod.GET)
    public String goOhwitPlayBack(Model model) {
        String classroom = getRequestString("classroom");
        String epid = getRequestString("epid");
        if (StringUtils.isBlank(classroom) || StringUtils.isBlank(epid)) {
            return "redirect:/";
        }
        //epid={$课时id}
        String url = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(PRIMARY_PLATFORM_GENERAL.name(), PLAY_BACK_URL_CONFIG);
        Map<String, String> paramMap = getParamMap(classroom);
        paramMap.put("epid", epid);
        return "redirect:" + UrlUtils.buildUrlQuery(url, paramMap);
    }

    @RequestMapping(value = "ohwitplaybackframe.vpage", method = RequestMethod.GET)
    public String goOhwitPlayBackFrame(Model model) {
        String classroom = getRequestString("classroom");
        String epid = getRequestString("epid");
        if (StringUtils.isBlank(classroom) || StringUtils.isBlank(epid)) {
            return "redirect:/";
        }

        //epid={$课时id}
        String url = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(PRIMARY_PLATFORM_GENERAL.name(), PLAY_BACK_URL_CONFIG);
        Map<String, String> paramMap = getParamMap(classroom);
        paramMap.put("epid", epid);
        model.addAttribute("url", UrlUtils.buildUrlQuery(url, paramMap));

        // add text and link url
        String text = getRequestString("text");
        String linkUrl = getRequestString("link");
        if (StringUtils.isNoneBlank(text) && StringUtils.isNoneBlank(linkUrl)) {
            model.addAttribute("text", text);
            model.addAttribute("linkUrl", linkUrl);
        }

        //跳转在ftl页面
        return "/ohwit/ohwitframe";
    }

    private Map<String, String> getParamMap(final String classroom) {
        User useUser = null;
        // 如果有sid，使用sid对应的user
        Long userId = getRequestLong("sid");
        if (userId > 0) {
            useUser = studentLoaderClient.loadStudent(userId);
        }
        if (useUser == null) {
            User curUser = currentUser();
            if (curUser != null && curUser.fetchUserType() == UserType.PARENT) {
                useUser = MiscUtils.firstElement(studentLoaderClient.loadParentStudents(curUser.getId()));
            }
            if (useUser == null) {
                useUser = curUser;
            }
        }

        Long userNo = 0L;
        String nickName = "";
        int userType = 3;

        if (useUser != null) {
            userNo = useUser.getId();
            nickName = useUser.fetchRealname();
            userType = useUser.fetchUserType().getType();
        } else {
            // 优先查cookie
            CookieManager cookieManager = getCookieManager();
            String randomUserId = cookieManager.getCookie(DEFAULT_USER_ID_COOK_KEY, null);
            if (StringUtils.isBlank(randomUserId)) {
                randomUserId = RandomUtils.randomNumeric(12);
                cookieManager.setCookie(DEFAULT_USER_ID_COOK_KEY, randomUserId, 30 * 24 * 60 * 60);
            }

            userNo = SafeConverter.toLong(randomUserId);
            nickName = "游客" + randomUserId;
        }
        if (StringUtils.isBlank(nickName)) {
            nickName = String.valueOf(userNo);//从request中取到了sid,则昵称直接用sid
        }

        String customer = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(PRIMARY_PLATFORM_GENERAL.name(), "live_class_ohwit_customer");
        String secretKey = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(PRIMARY_PLATFORM_GENERAL.name(), "live_class_ohwit_secretkey");
        long timestamp = System.currentTimeMillis();
        // k值:  md5(customer + timestamp + secretKey+学生账号+ 直播Id)
        StringBuilder md5Value = new StringBuilder();
        md5Value.append(customer).append(timestamp).append(secretKey).append(userNo).append(classroom);
        String key = DigestUtils.md5Hex(md5Value.toString());

        // p = k值|timestamp|账号|昵称|用户类型
        StringBuilder sbP = new StringBuilder(key);
        sbP.append("|").append(timestamp).append("|").append(userNo).append("|").append(nickName).append("|").append(userType);
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("liveClassroomId", classroom);//参数名称叫什么？
        paramMap.put("p", sbP.toString());
        paramMap.put("customer", customer);
        paramMap.put("customerType", "weiketang");
        paramMap.put("sp", "0");
        return paramMap;
    }

}
