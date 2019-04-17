package com.voxlearning.utopia.service.vendor.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.service.vendor.api.DPVendorLoader;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.utopia.service.vendor.api.entity.VendorAppsUserRef;
import com.voxlearning.utopia.service.vendor.impl.loader.VendorLoaderImpl;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author fugui.chang
 * @since 2018/6/7.
 */
@Named
@Service(interfaceClass = DPVendorLoader.class)
@ExposeService(interfaceClass = DPVendorLoader.class)
public class DPVendorLoaderImpl implements DPVendorLoader {

    @Inject private VendorAppsServiceImpl vendorAppsService;
    @Inject private VendorLoaderImpl vendorLoader;

    @Override
    public VendorApps loadVendor(String appKey) {
        return vendorAppsService.getVendorAppsBuffer().loadByAk(appKey);
    }

    @Override
    public Map<String, VendorApps> loadVendorApps(Set<String> appKeys) {
        if (CollectionUtils.isEmpty(appKeys)) return Collections.emptyMap();

        return vendorAppsService.getVendorAppsBuffer().loadVendorAppsList().stream()
                .filter(v -> appKeys.contains(v.getAppKey()))
                .collect(Collectors.toMap(VendorApps::getAppKey, Function.identity()));
    }

    @Override
    public VendorAppsUserRef loadVendorAppUserRef(String appKey, Long userId) {
        return vendorLoader.loadVendorAppUserRef(appKey, userId);
    }
}
