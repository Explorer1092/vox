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

package com.voxlearning.utopia.service.vendor.impl.loader;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.vendor.api.VendorLoader;
import com.voxlearning.utopia.service.vendor.api.entity.*;
import com.voxlearning.utopia.service.vendor.api.mapper.VersionedVendorAppsList;
import com.voxlearning.utopia.service.vendor.impl.dao.*;
import com.voxlearning.utopia.service.vendor.impl.service.VendorAppsResgRefServiceImpl;
import com.voxlearning.utopia.service.vendor.impl.service.VendorAppsServiceImpl;
import com.voxlearning.utopia.service.vendor.impl.service.VendorResgContentServiceImpl;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Default {@link VendorLoader} implementation.
 *
 * @author Xiaohai Zhang
 * @since Feb 11, 2015
 */
@Named("com.voxlearning.utopia.service.vendor.impl.loader.VendorLoaderImpl")
@Service(interfaceClass = VendorLoader.class)
@ExposeService(interfaceClass = VendorLoader.class)
@Deprecated
public class VendorLoaderImpl extends SpringContainerSupport implements VendorLoader {

    @Inject private AppParentSignRecordDao appParentSignRecordDao;
    @Inject private VendorAppRewardHistoryPersistence vendorAppRewardHistoryPersistence;
    @Inject private VendorAppsOrderPersistence vendorAppsOrderPersistence;
    @Inject private VendorAppsUserRefDao vendorAppsUserRefDao;
    @Inject private VendorPersistence vendorPersistence;
    @Inject private VendorResgPersistence vendorResgPersistence;

    @Inject private VendorAppsServiceImpl vendorAppsService;
    @Inject private VendorAppsResgRefServiceImpl vendorAppsResgRefService;
    @Inject private VendorResgContentServiceImpl vendorResgContentService;

    @Override
    public Map<Long, Vendor> loadVendorsIncludeDisabled() {
        return vendorPersistence.loadAll();
    }

    @Override
    public VendorAppsUserRef loadVendorAppUserRef(String appKey, Long userId) {
        if (StringUtils.isBlank(appKey) || userId == null) {
            return null;
        }
        if (!vendorAppsService.getVendorAppsBuffer().containsAk(appKey)) {
            return null;
        }
        List<VendorAppsUserRef> userRefs = vendorAppsUserRefDao.findVendorAppsUserRefList(userId);
        if (CollectionUtils.isEmpty(userRefs)) {
            return null;
        }
        // 排序
        Collections.sort(userRefs, (o1, o2) -> o2.getUpdateTime().compareTo(o1.getUpdateTime()));
        return userRefs.stream().filter(p -> Objects.equals(appKey, p.getAppKey())).findFirst().orElse(null);
    }

    @Override
    public List<VendorAppsUserRef> loadUserVendorApps(Long userId) {
        return vendorAppsUserRefDao.findVendorAppsUserRefList(userId);
    }

    @Override
    @Deprecated
    public Map<Long, VendorAppsResgRef> loadVendorAppResgRefs() {
        return vendorAppsResgRefService.getVendorAppsResgRefBuffer()
                .dump()
                .getRefList()
                .stream()
                .collect(Collectors.toMap(VendorAppsResgRef::getId, Function.identity()));
    }

    @Override
    public Map<Long, VendorResg> loadVendorResgsIncludeDisabled() {
        return vendorResgPersistence.loadAll();
    }

    @Override
    @Deprecated
    public Map<Long, VendorResgContent> loadVendorResgContents() {
        return vendorResgContentService.getVendorResgContentBuffer().loadAll();
    }

    public VendorAppsOrder loadVendorAppOrder(String id) {
        return id == null ? null : loadVendorAppOrders(Collections.singleton(id)).get(id);
    }

    @Override
    public Map<String, VendorAppsOrder> loadVendorAppOrders(Collection<String> ids) {
        return vendorAppsOrderPersistence.loads(ids);
    }

    @Override
    public VendorAppsOrder loadVendorAppOrder(String appKey, String sessionKey, Long orderSeq) {
        if (appKey == null || sessionKey == null || orderSeq == null) {
            return null;
        }
        return vendorAppsOrderPersistence.find(appKey, sessionKey, orderSeq);
    }

    @Override
    public List<VendorAppsOrder> loadVendorAppOrders(String appKey, Long userId) {
        if (appKey == null || userId == null) {
            return Collections.emptyList();
        }

        return vendorAppsOrderPersistence.find(appKey, userId);
    }

    @Override
    public Integer getUserPaidHwcoinOrderCount(String appKey, Long userId) {
        return vendorAppsOrderPersistence.findUserPaidHwcoinOrderCount(appKey, userId);
    }

    @Override
    public Integer getVendorAppRewardNum(Date createTime, Long userId, Integer appId, String rewardType) {
        List<VendorAppRewardHistory> vendorAppRewardHistoryList = vendorAppRewardHistoryPersistence.findByCreateTimeAndUserIdAndAppIdAndRewardType(createTime, userId, appId, rewardType);
        int rewardNum = 0;
        for (VendorAppRewardHistory vendorAppRewardHistory : vendorAppRewardHistoryList) {
            int rewardValue = vendorAppRewardHistory.getRewardValue() == null ? 0 : vendorAppRewardHistory.getRewardValue();
            rewardNum += rewardValue;
        }
        return rewardNum;
    }

    @Override
    public AppParentSignRecord loadAppParentRecordByUserId(Long userId) {
        return appParentSignRecordDao.loadAppParentRecordByUserId(userId);
    }

    @Override
    public List<AppParentSignRecord> loadAppParentSignRecordByUserIds(Collection<Long> userIds) {
        return appParentSignRecordDao.loadAppParentSignRecordByUserIds(userIds);
    }

    @Override
    @Deprecated
    public VersionedVendorAppsList loadVersionedVendorAppsList(long version) {
        return vendorAppsService.loadVersionedVendorAppsList(version).getUninterruptibly();
    }

    @Override
    @Deprecated
    public List<VendorApps> $loadVendorAppsList() {
        return vendorAppsService.loadAllVendorAppsFromDB().getUninterruptibly();
    }

    @Override
    public Map<Long, VendorApps> loadVendorAppsIncludeDisabled() {
        return vendorAppsService.getVendorAppsBuffer().loadVendorAppsList()
                .stream()
                .collect(Collectors.toMap(VendorApps::getId, Function.identity(),
                        (u, v) -> {
                            throw new IllegalStateException(String.format("Duplicate key %s", u));
                        },
                        LinkedHashMap::new));
    }

    @Override
    public VendorApps loadVendor(String appKey) {
        return vendorAppsService.getVendorAppsBuffer().loadByAk(appKey);
    }

    @Override
    public Map<Long, VendorAppsUserRef> loadVendorAppUserRefs(String appKey, Collection<Long> userIds) {
        if (appKey == null || appKey.trim().length() == 0) {
            return Collections.emptyMap();
        }
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        if (!vendorAppsService.getVendorAppsBuffer().containsAk(appKey)) {
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
