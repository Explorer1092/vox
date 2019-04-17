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

package com.voxlearning.utopia.admin.service.appmanager;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.admin.service.AbstractAdminService;
import com.voxlearning.utopia.service.vendor.api.entity.Vendor;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.utopia.service.vendor.api.entity.VendorAppsResgRef;
import com.voxlearning.utopia.service.vendor.api.entity.VendorResg;
import com.voxlearning.utopia.service.vendor.client.VendorAppsResgRefServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Named
public class AppManagementService extends AbstractAdminService {

    @Inject private VendorAppsResgRefServiceClient vendorAppsResgRefServiceClient;

    public List<Vendor> getAppVendorList() {
        return vendorLoaderClient.loadVendorsIncludeDisabled().values()
                .stream()
                .filter(t -> !t.isDisabledTrue())
                .collect(Collectors.toList());
    }

    public Vendor getAppVendor(Long vendorId) {
        Vendor vendor = vendorLoaderClient.loadVendorsIncludeDisabled().get(vendorId);
        if (vendor != null && vendor.isDisabledTrue()) {
            vendor = null;
        }
        return vendor;
    }

    public void delAppVendor(Long vendorId) {
        if (vendorId <= 0) {
            return;
        }

        Vendor vendor = vendorLoaderClient.loadVendorsIncludeDisabled().get(vendorId);
        if (vendor != null && vendor.isDisabledTrue()) {
            vendor = null;
        }
        if (vendor != null) {
            vendor.setDisabled(true);
            vendorServiceClient.deleteVendor(vendor.getId());
        }
    }

    public VendorApps getVendorApp(Long vendorAppId) {
        if (vendorAppId == null) {
            return null;
        }
        return vendorLoaderClient.$loadVendorAppsList().stream()
                .filter(e -> Objects.equals(vendorAppId, e.getId()))
                .findFirst()
                .orElse(null);
    }

    public List<VendorApps> searchVendorApps(final Long appVendorId, final String keyWords) {
        return vendorLoaderClient.$loadVendorAppsList()
                .stream()
                .filter(t -> !t.isDisabledTrue())
                .filter(t -> appVendorId == null || appVendorId <= 0 || Objects.equals(appVendorId, t.getVendorId()))
                .filter(t -> StringUtils.isEmpty(keyWords) || StringUtils.startsWith(t.getCname(), keyWords)
                        || StringUtils.startsWith(t.getEname(), keyWords))
                .collect(Collectors.toList());
    }

    public void delVendorApp(Long vendorAppId) {
        if (vendorAppId <= 0) {
            return;
        }
        vendorServiceClient.deleteVendorApp(vendorAppId);
    }

    public Long saveVendorApp(Long vendorAppId, Long vendorId, String cname, String ename, String sname, Integer vitalityType, Integer integralType,
                              String appUrl, String appIcon, String appKey, String secretKey, String callbackUrl, String purchaseUrl, Long dayMaxAccess,
                              Integer dayMaxPk, Integer dayMaxIntegral, String status, Integer runtimeMode, String serverIps, String suspendMessage,
                              Integer rank, Boolean isPaymentFree, Boolean isEduApp, String playSources, String appmUrl, String version, String appmIcon,
                              Boolean wechatBuyFlag, String subhead, String orientation, String clazzLevel, String desctription,
                              Boolean virtualItemExist, String browser, String versionAndroid, String iosParentVersion, String androidParentVersion) {
        VendorApps vendorApps;
        if (vendorAppId > 0) {
            vendorApps = vendorLoaderClient.$loadVendorAppsList().stream()
                    .filter(e -> Objects.equals(vendorAppId, e.getId()))
                    .findFirst()
                    .orElse(null);
            if (vendorApps == null) {
                throw new RuntimeException("VendorApp " + vendorAppId + " not exist!");
            }
        } else {
            vendorApps = new VendorApps();
            vendorApps.setDisabled(false);
        }
        vendorApps.setVendorId(vendorId);
        vendorApps.setCname(cname);
        vendorApps.setEname(ename);
        vendorApps.setShortName(sname);
        vendorApps.setVitalityType(vitalityType);
        vendorApps.setIntegralType(integralType);
        vendorApps.setAppUrl(appUrl);
        vendorApps.setAppIcon(appIcon);
        vendorApps.setAppKey(appKey);
        vendorApps.setSecretKey(secretKey);
        vendorApps.setCallBackUrl(callbackUrl);
        vendorApps.setPurchaseUrl(purchaseUrl);
        vendorApps.setDayMaxAccess(dayMaxAccess);
        vendorApps.setDayMaxAddPK(dayMaxPk);
        vendorApps.setDayMaxAddIntegral(dayMaxIntegral);
        vendorApps.setStatus(status);
        vendorApps.setRuntimeMode(runtimeMode);
        vendorApps.setServerIps(serverIps);
        vendorApps.setSuspendMessage(suspendMessage);
        vendorApps.setRank(rank);
        vendorApps.setIsPaymentFree(isPaymentFree);
        vendorApps.setIsEduApp(isEduApp);
        vendorApps.setPlaySources(playSources);
        vendorApps.setAppmUrl(appmUrl);
        vendorApps.setVersion(version);
        vendorApps.setAppmIcon(appmIcon);
        vendorApps.setWechatBuyFlag(wechatBuyFlag);
        vendorApps.setSubhead(subhead);
        vendorApps.setOrientation(orientation);
        vendorApps.setClazzLevel(clazzLevel);
        vendorApps.setDescription(desctription);
        vendorApps.setVirtualItemExist(virtualItemExist);
        vendorApps.setBrowser(browser);
        vendorApps.setVersionAndroid(versionAndroid);
        vendorApps.setIosParentVersion(iosParentVersion);
        vendorApps.setAndroidParentVersion(androidParentVersion);

        MapMessage message;
        try {
            message = vendorServiceClient.persistVendorApp(vendorApps);
        } catch (Exception ex) {
            message = MapMessage.errorMessage();
        }
        if (!message.isSuccess()) {
            throw new RuntimeException("Failed to persist vendor app");
        }
        return (Long) message.get("id");
    }

    public List<VendorAppsResgRef> getAppResgList(Long appId) {
        Collection<VendorAppsResgRef> appsResgRefList = vendorAppsResgRefServiceClient.getVendorAppsResgRefService()
                .loadAllVendorAppsResgRefsFromDB()
                .getUninterruptibly()
                .stream()
                .filter(e -> Objects.equals(appId, e.getAppId()))
                .collect(Collectors.toList());
        if (appsResgRefList != null && appsResgRefList.size() > 0) {
            for (VendorAppsResgRef appsResgRef : appsResgRefList) {
                VendorResg resg = vendorLoaderClient.getExtension().loadVendorResg(appsResgRef.getResgId());
                appsResgRef.setResg(resg);
            }
        }
        assert appsResgRefList != null;
        return new LinkedList<>(appsResgRefList);
    }
}
