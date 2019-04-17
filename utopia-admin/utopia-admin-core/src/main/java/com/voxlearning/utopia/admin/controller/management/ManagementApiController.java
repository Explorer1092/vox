/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.admin.controller.management;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.admin.persist.entity.AdminAppSystem;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: QJ
 * Date: 13-7-19
 * Time: 上午10:22
 * To change this template use File | Settings | File Templates.
 */
@Controller
@RequestMapping("/management/api")
public class ManagementApiController extends ManagementAbstractController{
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String api_index(Model model) {
        String adminName = getCurrentAdminUser().getAdminUserName();
        model.addAttribute("showAdmin", managementService.superAdmin(adminName));
        return "management/api/index";
    }

    @RequestMapping(value = "getUserAppPath.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    public String apiGetUserAppPath(Model model) {
        // FIXME COMMENT BY ZHAO REX FOR ret may not have been initialized.
        Map<String, Object> ret = null;
        String userName = getRequestParameter("userName", "");
        String appName = getRequestParameter("appName", "");
        String appKey = getRequestParameter("appKey", "");
        if(StringUtils.isNotEmpty(userName) && StringUtils.isNotEmpty(appName) && StringUtils.isNotEmpty(appKey)) {
            AdminAppSystem appInfo = managementService.getAppInfoByAppName(appName);
            //app_key 验证
            if(appInfo != null){
                if(DigestUtils.sha1Hex((userName + appName + appInfo.getAppKey()).getBytes(Charset.forName("UTF-8"))).equals(appKey)) {
                    addAdminLog("apiGetUserAppPath", userName, appName, Arrays.asList(userName, appName, appKey));
                    ret = managementService.apiGetUserAppPath(userName, appName);
                }
            }
        }

        // FIXME COMMENT BY ZHAO REX FOR ret may not have been initialized.
        model.addAttribute("pageRights", JsonUtils.toJson(ret));
        return "management/api/api";
    }
    @RequestMapping(value = "isHasUserAppPathRight.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    public String hasUserAppPathRight(Model model) {
        String userName = getRequestParameter("userName", "");
        String appName = getRequestParameter("appName", "");
        String pathName = getRequestParameter("pathName", "");
        String appKey = getRequestParameter("appKey", "");
        boolean flag = false;
        if(StringUtils.isNotEmpty(userName) && StringUtils.isNotEmpty(appName) && StringUtils.isNotEmpty(pathName)
                && StringUtils.isNotEmpty(appKey)) {
            AdminAppSystem appInfo = managementService.getAppInfoByAppName(appName);
            if(appInfo != null) {
                if(DigestUtils.sha1Hex((userName + appName + pathName + appInfo.getAppKey()).getBytes(Charset.forName("UTF-8"))).equals(appKey)) {
                    addAdminLog("apiIsHasUserAppPathRight", userName, appName+pathName, Arrays.asList(userName, appName, pathName, appKey));
                    flag = managementService.apiHasUserAppPathRight(userName, appName, pathName);
                }
            }
        }
        model.addAttribute("pageRights", JsonUtils.toJson(flag));
        return "management/api/api";

    }
}
