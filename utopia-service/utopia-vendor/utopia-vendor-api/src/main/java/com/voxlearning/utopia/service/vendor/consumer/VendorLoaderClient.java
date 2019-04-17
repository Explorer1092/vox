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
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.spi.cache.CacheObjectLoader;
import com.voxlearning.alps.spi.cache.ExternalLoader;
import com.voxlearning.alps.spi.cache.KeyGenerator;
import com.voxlearning.utopia.service.vendor.api.VendorLoader;
import com.voxlearning.utopia.service.vendor.api.entity.*;
import com.voxlearning.utopia.service.vendor.api.mapper.VersionedVendorAppsList;
import com.voxlearning.utopia.service.vendor.cache.VendorCache;
import com.voxlearning.utopia.service.vendor.client.VendorAppsServiceClient;
import com.voxlearning.utopia.service.vendor.consumer.extension.ExtensionVendorLoaderClient;
import lombok.Getter;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Client implementation of remote reference {@link VendorLoader}.
 *
 * @author Xiaohai Zhang
 * @since Feb 11, 2015
 */
@Deprecated
public class VendorLoaderClient implements VendorLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(VendorLoaderClient.class);

    @Getter
    @ImportService(interfaceClass = VendorLoader.class)
    private VendorLoader remoteReference;

    @Inject private VendorAppsServiceClient vendorAppsServiceClient;

    private ExtensionVendorLoaderClient extensionVendorLoaderClient = null;

    public synchronized ExtensionVendorLoaderClient getExtension() {
        if (extensionVendorLoaderClient == null) {
            extensionVendorLoaderClient = new ExtensionVendorLoaderClient();
            extensionVendorLoaderClient.setVendorLoaderClient(this);
        }
        return extensionVendorLoaderClient;
    }

    public Map<Long, Vendor> loadVendorsIncludeDisabled() {
        String cacheKey = Vendor.generateCacheKeyAll();
        Map<Long, Vendor> map = VendorCache.getVendorCache().load(cacheKey);
        if (map == null) {
            try {
                return remoteReference.loadVendorsIncludeDisabled();
            } catch (Exception ex) {
                LOGGER.error("Failed to load vendors", ex);
                return Collections.emptyMap();
            }
        }
        return map;
    }

    @Override
    public List<VendorAppsUserRef> loadUserVendorApps(Long userId) {
        return remoteReference.loadUserVendorApps(userId);
    }

    @Override
    public VendorAppsUserRef loadVendorAppUserRef(String appKey, Long userId) {
        if (StringUtils.isBlank(appKey) || userId == null) {
            return null;
        }
        if (!vendorAppsServiceClient.getVendorAppsBuffer().containsAk(appKey)) {
            return null;
        }

        List<VendorAppsUserRef> userRefs = remoteReference.loadUserVendorApps(userId);
        if (CollectionUtils.isEmpty(userRefs)) {
            return null;
        }
        // 排序
        Collections.sort(userRefs, (o1, o2) -> o2.getUpdateTime().compareTo(o1.getUpdateTime()));
        return userRefs.stream().filter(p -> Objects.equals(appKey, p.getAppKey())).findFirst().orElse(null);
    }

    @Override
    @Deprecated
    public Map<Long, VendorAppsResgRef> loadVendorAppResgRefs() {
        return remoteReference.loadVendorAppResgRefs();
    }

    public Map<Long, VendorResg> loadVendorResgsIncludeDisabled() {
        String cacheKey = VendorResg.generateCacheKeyAll();
        Map<Long, VendorResg> map = VendorCache.getVendorCache().load(cacheKey);
        if (map == null) {
            try {
                return remoteReference.loadVendorResgsIncludeDisabled();
            } catch (Exception ex) {
                LOGGER.error("Failed to load vendor resgs", ex);
                return Collections.emptyMap();
            }
        }
        return map;
    }

    @Override
    @Deprecated
    public Map<Long, VendorResgContent> loadVendorResgContents() {
        return remoteReference.loadVendorResgContents();
    }

    public Map<String, VendorAppsOrder> loadVendorAppOrders(final Collection<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        final CacheObjectLoader.Loader<String, VendorAppsOrder> loader = VendorCache.getVendorCache().getCacheObjectLoader()
                .createLoader(new KeyGenerator<String>() {
                    @Override
                    public String generate(String source) {
                        return VendorAppsOrder.ck_id(source);
                    }
                });
        return loader.loads(ids).loadsMissed(new ExternalLoader<String, VendorAppsOrder>() {
            @Override
            public Map<String, VendorAppsOrder> loadFromExternal(Collection<String> missedSources) {
                try {
                    return remoteReference.loadVendorAppOrders(missedSources);
                } catch (Exception ex) {
                    LOGGER.error("Failed to load vendor app orders [ids={}]", StringUtils.join(ids, ","), ex);
                    return Collections.emptyMap();
                }
            }
        }).getResult();
    }

    public VendorAppsOrder loadVendorAppOrder(String appKey, String sessionKey, Long orderSeq) {
        if (appKey == null || sessionKey == null || orderSeq == null) {
            return null;
        }
        String cacheKey = VendorAppsOrder.ck_appKey_sessionKey_orderSeq(appKey, sessionKey, orderSeq);
        VendorAppsOrder order = VendorCache.getVendorCache().load(cacheKey);
        if (order == null) {
            try {
                return remoteReference.loadVendorAppOrder(appKey, sessionKey, orderSeq);
            } catch (Exception ex) {
                LOGGER.error("Failed to load vendor app order", ex);
                return null;
            }
        }
        return order;
    }

    public AppParentSignRecord loadAppParentRecordByUserId(Long userId) {
        Objects.requireNonNull(userId);

        String cacheKey = AppParentSignRecord.generateCacheKeyById(SafeConverter.toString(userId));
        AppParentSignRecord appParentSignRecord = VendorCache.getVendorCache().load(cacheKey);
        if (appParentSignRecord != null)
            return appParentSignRecord;
        return remoteReference.loadAppParentRecordByUserId(userId);
    }

    public List<AppParentSignRecord> loadAppParentSignRecordByUserIds(Collection<Long> userIds) {
        userIds = CollectionUtils.toLinkedHashSet(userIds);
        if (userIds.isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> ids = userIds.stream().map(SafeConverter::toString).collect(Collectors.toSet());
        CacheObjectLoader cacheObjectLoader = VendorCache.getVendorCache().getCacheObjectLoader();
        CacheObjectLoader.Loader<String, AppParentSignRecord> loader = cacheObjectLoader.createLoader(AppParentSignRecord::generateCacheKeyById);
        Map<String, AppParentSignRecord> map = loader.loads(ids)
                .loadsMissed(missedSources -> {
                    Set<Long> missedUserIds = missedSources.stream()
                            .map(SafeConverter::toLong)
                            .collect(Collectors.toSet());
                    List<AppParentSignRecord> list = remoteReference.loadAppParentSignRecordByUserIds(missedUserIds);
                    return list.stream().collect(Collectors.toMap(AppParentSignRecord::getId, Function.identity()));
                })
                .getResult();
        return map.values().stream().collect(Collectors.toList());
    }

    @Override
    public List<VendorAppsOrder> loadVendorAppOrders(String appKey, Long userId) {
        return remoteReference.loadVendorAppOrders(appKey, userId);
    }

    @Override
    public Integer getUserPaidHwcoinOrderCount(String appKey, Long userId) {
        return remoteReference.getUserPaidHwcoinOrderCount(appKey, userId);
    }

    @Override
    public Integer getVendorAppRewardNum(Date createTime, Long userId, Integer appId, String rewardType) {
        return remoteReference.getVendorAppRewardNum(createTime, userId, appId, rewardType);
    }

    @Override
    @Deprecated
    public VersionedVendorAppsList loadVersionedVendorAppsList(long version) {
        return remoteReference.loadVersionedVendorAppsList(version);
    }

    @Override
    @Deprecated
    public List<VendorApps> $loadVendorAppsList() {
        return remoteReference.$loadVendorAppsList();
    }

    @Override
    public Map<Long, VendorApps> loadVendorAppsIncludeDisabled() {
        return vendorAppsServiceClient.getVendorAppsBuffer().loadVendorAppsList()
                .stream()
                .collect(Collectors.toMap(VendorApps::getId, Function.identity(),
                        (u, v) -> {
                            throw new IllegalStateException(String.format("Duplicate key %s", u));
                        },
                        LinkedHashMap::new));
    }

    @Override
    public VendorApps loadVendor(String appKey) {
        return vendorAppsServiceClient.getVendorAppsBuffer().loadByAk(appKey);
    }

    @Override
    public Map<Long, VendorAppsUserRef> loadVendorAppUserRefs(String appKey, Collection<Long> userIds) {
        if (appKey == null || appKey.trim().length() == 0) {
            return Collections.emptyMap();
        }
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        if (!vendorAppsServiceClient.getVendorAppsBuffer().containsAk(appKey)) {
            return Collections.emptyMap();
        }
        Map<Long, VendorAppsUserRef> map = new LinkedHashMap<>();
        for (Long userId : userIds) {
            VendorAppsUserRef ref = loadVendorAppUserRef(appKey, userId);
            if (ref != null) {
                map.put(userId, ref);
            }
        }
        return map;
    }
}
