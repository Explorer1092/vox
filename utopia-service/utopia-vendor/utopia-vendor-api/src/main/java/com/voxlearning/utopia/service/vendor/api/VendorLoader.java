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

import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.annotation.remote.Idempotent;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.vendor.api.entity.*;
import com.voxlearning.utopia.service.vendor.api.mapper.VersionedVendorAppsList;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Vendor loader interface definition.
 *
 * @author Xiaohai Zhang
 * @since Feb 11, 2015
 */
@ServiceVersion(version = "20161209")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
@Deprecated
public interface VendorLoader extends IPingable {

    Map<Long, VendorApps> loadVendorAppsIncludeDisabled();

    VendorApps loadVendor(String appKey);

    @Idempotent
    @CacheMethod(
            type = VendorAppsUserRef.class,
            writeCache = false
    )
    List<VendorAppsUserRef> loadUserVendorApps(@CacheParameter(value = "UID") Long userId);

    VendorAppsUserRef loadVendorAppUserRef(String appKey, Long userId);

    Map<Long, VendorAppsUserRef> loadVendorAppUserRefs(String appKey, Collection<Long> userIds);

    Map<Long, Vendor> loadVendorsIncludeDisabled();

    /**
     * @deprecated use {@link VendorAppsResgRefService#loadAllVendorAppsResgRefsFromBuffer()} instead.
     */
    @Deprecated
    Map<Long, VendorAppsResgRef> loadVendorAppResgRefs();

    Map<Long, VendorResg> loadVendorResgsIncludeDisabled();

    /**
     * @deprecated use {@link VendorResgContentService} instead.
     */
    @Deprecated
    Map<Long, VendorResgContent> loadVendorResgContents();

    Map<String, VendorAppsOrder> loadVendorAppOrders(Collection<String> ids);

    VendorAppsOrder loadVendorAppOrder(String appKey, String sessionKey, Long orderSeq);

    List<VendorAppsOrder> loadVendorAppOrders(String appKey, Long userId);

    Integer getUserPaidHwcoinOrderCount(String appKey, Long userId);

    Integer getVendorAppRewardNum(Date createTime, Long userId, Integer appId, String rewardType);

    AppParentSignRecord loadAppParentRecordByUserId(Long userId);

    List<AppParentSignRecord> loadAppParentSignRecordByUserIds(Collection<Long> userId);

    /**
     * @deprecated use {@link VendorAppsService#loadVersionedVendorAppsList(long)} instead.
     */
    @Deprecated
    VersionedVendorAppsList loadVersionedVendorAppsList(long version);

    // ========================================================================
    // dollar methods for CRM
    // don't use these as business methods
    // ========================================================================

    /**
     * @deprecated use {@link VendorAppsService#loadAllVendorAppsFromDB()} instead.
     */
    @Deprecated
    List<VendorApps> $loadVendorAppsList();

}
