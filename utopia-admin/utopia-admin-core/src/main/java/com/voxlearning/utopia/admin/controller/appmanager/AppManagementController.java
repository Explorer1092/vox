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

package com.voxlearning.utopia.admin.controller.appmanager;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.math.NumberUtils;
import com.voxlearning.utopia.admin.controller.AbstractAdminSystemController;
import com.voxlearning.utopia.admin.service.appmanager.AppManagementService;
import com.voxlearning.utopia.admin.service.appmanager.AppResgManagementService;
import com.voxlearning.utopia.api.constant.OperationSourceType;
import com.voxlearning.utopia.service.vendor.api.entity.Vendor;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.crypto.KeyGenerator;
import javax.inject.Inject;
import java.security.Key;
import java.security.SecureRandom;

@Controller
@RequestMapping("/appmanager")

@Slf4j
public class AppManagementController extends AbstractAdminSystemController {
    @Inject private AppManagementService appManagementService;
    @Inject private AppResgManagementService appResgManagementService;

    @RequestMapping(value = "vendorindex.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String appVendorIndex(Model model) {
        model.addAttribute("appVendorList", appManagementService.getAppVendorList());
        return "apps/vendorlist";
    }

    @RequestMapping(value = "addvendor.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String addAppVendor(Model model) {
        Vendor appVendor;
        String vendorId = getRequestString("vendorId");
        if (StringUtils.isEmpty(vendorId) || !NumberUtils.isNumber(vendorId)) {
            appVendor = new Vendor();
            appVendor.setDisabled(false);
            appVendor.setId(0L);
        } else {
            appVendor = appManagementService.getAppVendor(Long.valueOf(vendorId));
            if (appVendor == null) {
                appVendor = new Vendor();
                appVendor.setDisabled(false);
                appVendor.setId(0L);
            }
        }
        model.addAttribute("appVendor", appVendor);
        return "apps/vendoredit";
    }

    @RequestMapping(value = "delvendor.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage delAppVendor(@RequestParam Long vendorId) {
        MapMessage message = new MapMessage();
        try {
            appManagementService.delAppVendor(vendorId);
            message.setSuccess(true);
            message.setInfo("操作成功");
        } catch (Exception ex) {
            message.setSuccess(false);
            message.setInfo("操作失败," + ex.getMessage());
        }
        return message;
    }

    @RequestMapping(value = "updatevendor.vpage", method = RequestMethod.POST)
    public String updateAppVendor(Model model) {
        try {
            Long vendorId = getRequestLong("vendorId");
            String vendorCname = getRequestString("vendorCname");
            String vendorEname = getRequestString("vendorEname");
            String vendorSname = getRequestString("vendorSname");
            String vendorAddress = getRequestString("vendorAddress");
            String webSite = getRequestString("webSite");
            String logoUrl = getRequestString("logoUrl");
            String contact1Name = getRequestString("contact1Name");
            String contact1Tel = getRequestString("contact1Tel");
            String contact1Mob = getRequestString("contact1Mob");
            String contact1Email = getRequestString("contact1Email");
            String contact2Name = getRequestString("contact2Name");
            String contact2Tel = getRequestString("contact2Tel");
            String contact2Mob = getRequestString("contact2Mob");
            String contact2Email = getRequestString("contact2Email");

            if (StringUtils.isEmpty(vendorCname) || StringUtils.isEmpty(contact1Name) || StringUtils.isEmpty(contact1Mob) || StringUtils.isEmpty(contact1Email)) {
                model.addAttribute("success", "false");
                model.addAttribute("message", "输入信息不全");
                return "apps/vendoredit";
            }

            vendorServiceClient.saveAppVendor(vendorId, vendorCname, vendorEname, vendorSname, vendorAddress, webSite, logoUrl, contact1Name, contact1Tel, contact1Mob, contact1Email, contact2Name, contact2Tel, contact2Mob, contact2Email);

            model.addAttribute("appVendorList", appManagementService.getAppVendorList());
            return "apps/vendorlist";
        } catch (Exception ex) {
            model.addAttribute("success", "false");
            model.addAttribute("message", "输入信息不全");
            return "apps/vendoredit";
        }
    }

    @RequestMapping(value = "appindex.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String appIndex(Model model) {
        Long vendorId = getRequestLong("searchVendorId");
        String keyWords = getRequestString("searchKeyWords");
        model.addAttribute("appVendorList", appManagementService.getAppVendorList());
        model.addAttribute("vendorAppsList", appManagementService.searchVendorApps(vendorId, keyWords));
        return "apps/applist";
    }

    @RequestMapping(value = "delvendorapp.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage delVendorApps(@RequestParam Long vendorAppId) {
        MapMessage message = new MapMessage();

        try {
            appManagementService.delVendorApp(vendorAppId);
            message.setSuccess(true);
            message.setInfo("操作成功");
        } catch (Exception ex) {
            message.setSuccess(false);
            message.setInfo("操作失败," + ex.getMessage());
        }
        return message;
    }

    @RequestMapping(value = "addvendorapp.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String addVendorApp(Model model) {
        VendorApps vendorApp;
        String vendorAppId = getRequestString("vendorAppId");
        if (StringUtils.isEmpty(vendorAppId) || !NumberUtils.isNumber(vendorAppId)) {
            vendorApp = new VendorApps();
            vendorApp.setDisabled(false);
            vendorApp.setId(0L);
        } else {
            vendorApp = appManagementService.getVendorApp(Long.valueOf(vendorAppId));
            if (vendorApp == null) {
                vendorApp = new VendorApps();
                vendorApp.setDisabled(false);
                vendorApp.setId(0L);
            }
        }
        model.addAttribute("appVendorList", appManagementService.getAppVendorList());
        model.addAttribute("vendorApp", vendorApp);
        return "apps/appedit";
    }

    @RequestMapping(value = "updatevendorapp.vpage", method = RequestMethod.POST)
    public String updateVendorApp(Model model) {
        try {
            Long vendorAppId = getRequestLong("vendorAppId");
            Long vendorId = getRequestLong("vendorId");
            String appCname = getRequestString("appCname");
            String appEname = getRequestString("appEname");
            String appSname = getRequestString("appSname");
            Integer vitalityType = getRequestInt("vitalityType");
            Integer integralType = getRequestInt("integralType");
            String appUrl = getRequestString("appUrl");
            String appIcon = getRequestString("appIcon");
            String appKey = getRequestString("appKey");
            String secretKey = getRequestString("secretKey");
            if (StringUtils.isEmpty(secretKey)) {
                secretKey = generateNewSecretKey();
            }
            String callbackUrl = getRequestString("callbackUrl");
            String purchaseUrl = getRequestString("purchaseUrl");
            Long dayMaxAccess = getRequestLong("dayMaxAccess");
            Integer dayMaxPk = getRequestInt("dayMaxPk");
            Integer dayMaxIntegral = getRequestInt("dayMaxIntegral");
            String status = getRequestString("status");
            Integer runtimeMode = getRequestInt("runtimeMode");
            String serverIps = getRequestString("serverIps");
            String suspendMessage = getRequestString("suspendMessage");
            Integer rank = getRequestInt("rank");
            Boolean isPaymentFree = getRequestBool("isPaymentFree");
            Boolean isEduApp = getRequestBool("isEduApp");
            String playSources = getRequestString("playSources");
            String appmUrl = getRequestString("appmUrl");
            String version = getRequestString("version");
            String appmIcon = getRequestString("appmIcon");
            Boolean wechatBuyFlag = getRequestBool("wechatBuyFlag");
            String subhead = getRequestString("subhead");
            String orientation = getRequestString("orientation");
            String clazzLevel = getRequestString("clazzLevel");
            String description = getRequestString("description");
            Boolean virtualItemExist = getRequestBool("virtualItemExist");
            String browser = getRequestString("browser");
            String versionAndroid = getRequestString("versionAndroid");
            String iosParentVersion = getRequestString("iosParentVersion");
            String androidParentVersion = getRequestString("androidParentVersion");

            // 校验此字段是否正确
            if (vendorId != 0L) {
                VendorApps vendorApp = appManagementService.getVendorApp(vendorAppId);
                model.addAttribute("vendorApp", vendorApp);
                model.addAttribute("appVendorList", appManagementService.getAppVendorList());
            }

            if (StringUtils.isNotBlank(playSources)) {
                String[] sourceArr = StringUtils.split(playSources, ",");
                for (String source : sourceArr) {
                    OperationSourceType type = OperationSourceType.ofWithUnknown(source);
                    if (type == OperationSourceType.unknown) {
                        getAlertMessageManager().addMessageError("请正确填写【学生在哪端可以使用】");
                        return "apps/appedit";
                    }
                    if (type == OperationSourceType.app && (StringUtils.isBlank(version) || StringUtils.isBlank(versionAndroid))) {
                        getAlertMessageManager().addMessageError("请正确填写【壳版本号】");
                        return "apps/appedit";
                    }
                }
            }

            appManagementService.saveVendorApp(vendorAppId, vendorId, appCname, appEname, appSname, vitalityType, integralType,
                    appUrl, appIcon, appKey, secretKey, callbackUrl, purchaseUrl, dayMaxAccess, dayMaxPk,
                    dayMaxIntegral, status, runtimeMode, serverIps, suspendMessage, rank, isPaymentFree, isEduApp, playSources, appmUrl
                    , version, appmIcon, wechatBuyFlag, subhead, orientation, clazzLevel, description, virtualItemExist, browser,
                    versionAndroid, iosParentVersion, androidParentVersion);

            model.addAttribute("appVendorList", appManagementService.getAppVendorList());
            model.addAttribute("vendorAppsList", appManagementService.searchVendorApps(0L, null));
            return "apps/applist";
        } catch (Exception ex) {
            getAlertMessageManager().addMessageError("输入信息不全");
            return "apps/appedit";
        }
    }

    private String generateNewSecretKey() {
        try {
            KeyGenerator keygen = KeyGenerator.getInstance("AES");
            SecureRandom random = new SecureRandom();
            keygen.init(random);
            Key key = keygen.generateKey();
            String base64Key = Base64.encodeBase64String(key.getEncoded());
            if (base64Key.length() > 12) {
                base64Key = base64Key.substring(0, 12);
            }
            return base64Key;

        } catch (Exception e) {
            throw new RuntimeException("Generate New Secret Key Error!", e);
        }
    }

    @RequestMapping(value = "editappresg.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String editAppResg(Model model) {
        model.addAttribute("vendorApp", appManagementService.getVendorApp(getRequestLong("appId")));
        model.addAttribute("resgList", appResgManagementService.getResgList());
        model.addAttribute("appRestList", appManagementService.getAppResgList(getRequestLong("appId")));
        return "apps/appresgedit";
    }

    @RequestMapping(value = "saveappresg.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveAppResg(@RequestParam Long appId, @RequestParam String resgList) {
        MapMessage message;
        try {
            message = vendorServiceClient.saveVendorAppResg(appId, resgList);
        } catch (Exception ex) {
            logger.error("Failed to save vendor app resg", ex);
            message = MapMessage.errorMessage();
        }
        return message.setInfo(message.isSuccess() ? "操作成功" : "操作失败");
    }
}
