package com.voxlearning.ucenter.controller.connect.impl;

import com.voxlearning.alps.core.concurrent.ReentrantReadWriteLocker;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.utopia.service.vendor.client.VendorAppsServiceClient;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * 网校app sso 主站
 * Created by Summer on 2019/3/11
 */
@Named
public class XueAppServerSsoConnector extends AbstractPlatformSsoConnector {
    @Inject
    private VendorAppsServiceClient vendorAppsServiceClient;

    private static final String APP_KEY = "17Yunketang";

    private String secretKey = null;

    private ReentrantReadWriteLocker locker = new ReentrantReadWriteLocker();

    @Override
    String getSecretKey() {
        String key = locker.withinReadLock(() -> secretKey);
        if (key != null) {
            return key;
        }
        return locker.withinWriteLock(() -> {
            if (secretKey != null) {
                return secretKey;
            }
            VendorApps vendorApps = vendorAppsServiceClient.getVendorAppsBuffer().loadByAk(APP_KEY);
            secretKey = vendorApps.getSecretKey();
            return secretKey;
        });
    }

    @Override
    int getTimeLimitInSeconds() {
        return 10 * 60;
    }
}
