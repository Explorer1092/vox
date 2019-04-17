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

package com.voxlearning.utopia.service.vendor.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.service.campaign.api.constant.CampaignType;
import com.voxlearning.utopia.service.vendor.api.VendorService;
import com.voxlearning.utopia.service.vendor.api.entity.VendorAppRewardHistory;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.utopia.service.vendor.api.entity.VendorAppsOrder;
import com.voxlearning.utopia.service.vendor.api.entity.VendorAppsUserRef;
import com.voxlearning.utopia.service.vendor.consumer.support.VendorNotifyCreator;
import lombok.Getter;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Client implementation of remote reference {@link VendorService}.
 *
 * @author Xiaohai Zhang
 * @since Feb 10, 2015
 */
public class VendorServiceClient implements VendorService {
    private static final Logger logger = LoggerFactory.getLogger(VendorServiceClient.class);

    @Getter
    @ImportService(interfaceClass = VendorService.class)
    private VendorService vendorService;

    @Inject
    protected VendorLoaderClient vendorLoaderClient;

    @Override
    public Map<Long, Set<Long>> studentBindAppParentMap(List<Long> studentIds) {
        return vendorService.studentBindAppParentMap(studentIds);
    }

    @Override
    public MapMessage deleteVendor(Long id) {
        return vendorService.deleteVendor(id);
    }

    @Override
    public MapMessage saveAppVendor(Long vendorId, String cname, String ename, String sname, String address, String webSite, String logoUrl, String contact1Name, String contact1Tel, String contact1Mob, String contact1Email, String contact2Name, String contact2Tel, String contact2Mob, String contact2Email) {
        return vendorService.saveAppVendor(vendorId, cname, ename, sname, address, webSite, logoUrl, contact1Name, contact1Tel, contact1Mob, contact1Email, contact2Name, contact2Tel, contact2Mob, contact2Email);
    }

    @Override
    public MapMessage persistVendorApp(VendorApps vendorApp) {
        return vendorService.persistVendorApp(vendorApp);
    }

    @Override
    public MapMessage deleteVendorApp(Long id) {
        return vendorService.deleteVendorApp(id);
    }

    @Override
    public MapMessage persistVenderAppUserRef(VendorAppsUserRef ref) {
        return vendorService.persistVenderAppUserRef(ref);
    }

    @Override
    public MapMessage expireSessionKey(String appKey, Long userId, String sessionKey) {
        return vendorService.expireSessionKey(appKey, userId, sessionKey);
    }

    @Override
    public MapMessage saveVendorAppResg(Long appId, String resgList) {
        return vendorService.saveVendorAppResg(appId, resgList);
    }

    @Override
    public MapMessage saveVendorResg(Long resgId, String cname, String ename, String description) {
        return vendorService.saveVendorResg(resgId, cname, ename, description);
    }

    @Override
    public MapMessage deleteVendorResgs(Long resgId) {
        return vendorService.deleteVendorResgs(resgId);
    }

    @Override
    public MapMessage deleteVendorResgContent(Long id) {
        return vendorService.deleteVendorResgContent(id);
    }

    @Override
    public MapMessage saveVendorResgContent(Long resgContentId, Long resgId, String resName) {
        return vendorService.saveVendorResgContent(resgContentId, resgId, resName);
    }

    @Override
    public MapMessage persistVendorAppOrder(VendorAppsOrder order) {
        return vendorService.persistVendorAppOrder(order);
    }

    @Override
    public MapMessage updateVendorAppOrder(VendorAppsOrder order) {
        return vendorService.updateVendorAppOrder(order);
    }

    @Override
    public MapMessage finishPaymnet(String orderId) {
        return vendorService.finishPaymnet(orderId);
    }

    @Override
    public MapMessage persistVendorAppRewardHistory(VendorAppRewardHistory history) {
        return vendorService.persistVendorAppRewardHistory(history);
    }

    @Override
    public void sendCampaignAward(String appKey, Long userId, CampaignType campaignType, Integer diamondAmount) {
        vendorService.sendCampaignAward(appKey, userId, campaignType, diamondAmount);
    }

    @Override
    public void sendVendorMessage(Message message) {
        vendorService.sendVendorMessage(message);
    }

    @Override
    public void sendJpushMessage(Message message) {
        vendorService.sendJpushMessage(message);
    }

    @Override
    public void sendMyselfstudyMessage(Message message) {
        vendorService.sendMyselfstudyMessage(message);
    }

    public MapMessage updateOrInsertAppParentSignRecord(Long userId) {
        Objects.requireNonNull(userId);
        return vendorService.updateOrInsertAppParentSignRecord(userId);
    }

    public VendorNotifyCreator createVendorNotify(String notifyId) {
        return new VendorNotifyCreator(vendorService).notifyId(notifyId);
    }

    public MapMessage sendHttpNotify(String appKey, String targetUrl, Map<String, Object> params) {
        if (StringUtils.isBlank(appKey) || StringUtils.isBlank(targetUrl) || params == null) {
            return MapMessage.errorMessage();
        }
        try {
            return vendorService.sendHttpNotify(appKey, targetUrl, params);
        } catch (Exception ex) {
            logger.error("Failed to send HTTP notify [appKey={},targetUrl={},params={}]",
                    appKey, targetUrl, params, ex);
            return MapMessage.errorMessage();
        }
    }
}
