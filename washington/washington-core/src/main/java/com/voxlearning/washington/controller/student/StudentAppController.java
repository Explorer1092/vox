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

package com.voxlearning.washington.controller.student;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.runtime.collector.LogCollector;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.order.api.mapper.AppPayMapper;
import com.voxlearning.utopia.service.user.api.entities.StudentParent;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.utopia.service.vendor.api.entity.VendorAppsUserRef;
import com.voxlearning.utopia.service.vendor.client.AsyncVendorServiceClient;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.washington.support.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Student App controller implementation.
 *
 * @author Shuai Huan
 * @since 2014-06-12
 */
@Controller
@RequestMapping("/student")
public class StudentAppController extends AbstractController {

    @Inject private AsyncVendorServiceClient asyncVendorServiceClient;

    
    // 电脑端进入应用的调度页面
    @RequestMapping(value = "apps/index.vpage", method = RequestMethod.GET)
    public String appIndex(@RequestParam(value = "app_key", defaultValue = "", required = false) String appKey, Model model) {
        Long userId = currentUserId();

        VendorApps app = vendorLoaderClient.getExtension().loadVendorApp(appKey);
        if (app == null) {
            return "redirect:/";
        }

        if (OrderProductServiceType.AfentiExam.name().equals(appKey)) {
            return "redirect:/afenti/api/index.vpage";
        } else if (OrderProductServiceType.AfentiMath.name().equals(appKey)) {
            return "redirect:/afenti/api/index.vpage?subject=MATH";
        } else if (OrderProductServiceType.Walker.name().equals(appKey)) {
            return "redirect:/student/nekketsu/adventure.vpage";
        }

        if (app.isSuspend()) {
            model.addAttribute("appName", app.getCname());
            model.addAttribute("suspendMessage", app.getSuspendMessage());
            return "studentv3/apps/suspend";
        }

        MapMessage message = asyncVendorServiceClient.getAsyncVendorService()
                .registerVendorAppUserRef(appKey, userId)
                .getUninterruptibly();
        if (!message.isSuccess() || null == message.get("ref")) {
            return "redirect:/";
        }

        VendorAppsUserRef vendorAppsUserRef = (VendorAppsUserRef) message.get("ref");

        String sessionKey = vendorAppsUserRef.getSessionKey();
        model.addAttribute("sessionKey", sessionKey);

        AppPayMapper mapper = userOrderLoaderClient.getUserAppPaidStatus(app.getAppKey(), currentUserId());
        Map<String, Object> appPayInfo = new HashMap<>();
        appPayInfo.put("appStatus", mapper.getAppStatus());
        appPayInfo.put("dayToExpire", mapper.getDayToExpire());
        appPayInfo.put("expireTime", mapper.getExpireTime());
        appPayInfo.put("appKey", app.getAppKey());
        appPayInfo.put("appName", app.getCname());
        appPayInfo.put("appIcon", app.getAppIcon());
        appPayInfo.put("appUrl", app.getAppUrl());

        model.addAttribute("curapp", appPayInfo);

        return "studentv3/apps/index";
    }

    // 移动端进入应用的调度页面
    @RequestMapping(value = "apps/mobile/index.vpage", method = RequestMethod.GET)
    public String goApps(Model model) {
        Long studentId = currentUserId();

        String appKey = getRequestString("app_key");
        if (StringUtils.isBlank(appKey)) return "redirect:/";

        VendorApps app = vendorLoaderClient.getExtension().loadVendorApp(appKey);
        if (app == null) return "redirect:/";

        MapMessage message = asyncVendorServiceClient.getAsyncVendorService()
                .registerVendorAppUserRef(appKey, studentId)
                .getUninterruptibly();
        if (!message.isSuccess() || null == message.get("ref")) return "redirect:/";

        VendorAppsUserRef ref = (VendorAppsUserRef) message.get("ref");
        model.addAttribute("session_key", ref.getSessionKey());

        // temp code to find problem
        if (app.getAppmUrl().indexOf("?") > 0) {
            return "redirect:" + app.getAppmUrl() + "&session_key=" + ref.getSessionKey();
        } else {
            return "redirect:" + app.getAppmUrl() + "?session_key=" + ref.getSessionKey();
        }

        // return "redirect:" + app.getAppmUrl();
    }

    @RequestMapping(value = "chat/index.vpage", method = RequestMethod.GET)
    public String chatIndex(Model model) {
        if (currentUser() == null) {
            return "redirect:/";
        }
        final String appKey = "17chat";
        Long userId = currentUserId();
        VendorApps app = vendorLoaderClient.getExtension().loadVendorApp(appKey);
        if (app == null) {
            return "redirect:/";
        }

        MapMessage message = asyncVendorServiceClient.getAsyncVendorService()
                .registerVendorAppUserRef(appKey, userId)
                .getUninterruptibly();
        if (!message.isSuccess() || null == message.get("ref")) {
            return "redirect:/";
        }
        VendorAppsUserRef vendorAppsUserRef = (VendorAppsUserRef) message.get("ref");

        String sessionKey = vendorAppsUserRef.getSessionKey();
        model.addAttribute("sessionKey", sessionKey);

        model.addAttribute("userId", userId);

        StudentDetail detail = currentStudentDetail();
        if (detail != null && detail.getClazz() != null) {
            model.addAttribute("clazzId", detail.getClazz().getId());
        }

        return "redirect:" + app.getAppUrl();
    }
}
