package com.voxlearning.utopia.service.vendor.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.utopia.service.vendor.api.entity.VendorAppsUserRef;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author fugui.chang
 * @since 2018/6/7.
 */
@ServiceVersion(version = "20180607")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
public interface DPVendorLoader {

    VendorApps loadVendor(String appKey);

    Map<String, VendorApps> loadVendorApps(Set<String> appKeys);

    VendorAppsUserRef loadVendorAppUserRef(String appKey, Long userId);
}
