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

package com.voxlearning.utopia.service.vendor.api;

import com.voxlearning.alps.annotation.remote.NoResponseWait;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.service.campaign.api.constant.CampaignType;
import com.voxlearning.utopia.service.vendor.api.entity.VendorAppRewardHistory;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.utopia.service.vendor.api.entity.VendorAppsOrder;
import com.voxlearning.utopia.service.vendor.api.entity.VendorAppsUserRef;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Vendor service interface definition.
 *
 * @author Xiaohai Zhang
 * @since Feb 10, 2015
 */
@ServiceVersion(version = "1.0.DEV")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface VendorService extends IPingable {
    MapMessage deleteVendor(Long id);

    MapMessage saveAppVendor(Long vendorId, String cname, String ename, String sname, String address, String webSite, String logoUrl, String contact1Name, String contact1Tel, String contact1Mob, String contact1Email, String contact2Name, String contact2Tel, String contact2Mob, String contact2Email);

    MapMessage persistVendorApp(VendorApps vendorApp);

    MapMessage deleteVendorApp(Long id);

    MapMessage persistVenderAppUserRef(VendorAppsUserRef ref);

    MapMessage expireSessionKey(String appKey, Long userId, String sessionKey);

    MapMessage saveVendorAppResg(Long appId, String resgList);

    MapMessage saveVendorResg(Long resgId, String cname, String ename, String description);

    MapMessage deleteVendorResgs(Long resgId);

    MapMessage deleteVendorResgContent(Long id);

    MapMessage saveVendorResgContent(Long resgContentId, Long resgId, String resName);

    MapMessage persistVendorAppOrder(VendorAppsOrder order);

    MapMessage updateVendorAppOrder(VendorAppsOrder order);

    MapMessage finishPaymnet(String orderId);

    MapMessage persistVendorAppRewardHistory(VendorAppRewardHistory history);

    void sendCampaignAward(String appKey, Long userId, CampaignType campaignType, Integer diamondAmount);

    MapMessage sendHttpNotify(String appKey, String targetUrl, Map<String, Object> params);

    Map<Long, Set<Long>> studentBindAppParentMap(List<Long> studentIds);

    MapMessage updateOrInsertAppParentSignRecord(Long userId);

    // send to utopia.vendor.queue
    @NoResponseWait
    void sendVendorMessage(Message message);

    // send to utopia.jpush.queue
    @NoResponseWait
    void sendJpushMessage(Message message);

    // send to utopia.vendor.myselfstudy.queue
    @NoResponseWait
    void sendMyselfstudyMessage(Message message);


}
