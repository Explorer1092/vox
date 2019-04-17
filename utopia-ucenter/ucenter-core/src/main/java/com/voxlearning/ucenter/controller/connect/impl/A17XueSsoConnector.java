package com.voxlearning.ucenter.controller.connect.impl;

import com.voxlearning.alps.core.concurrent.ReentrantReadWriteLocker;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.utopia.service.vendor.consumer.VendorLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author changyuan
 * @since 2017/6/28
 */
@Named
public class A17XueSsoConnector extends AbstractPlatformSsoConnector {

    @Inject
    private VendorLoaderClient vendorLoaderClient;

    private static final String APP_KEY = "YiQiXueTeacher";

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
            VendorApps vendorApps = vendorLoaderClient.loadVendor(APP_KEY);
            secretKey = vendorApps.getSecretKey();
            return secretKey;
        });
    }

    @Override
    int getTimeLimitInSeconds() {
        return 10 * 60;
    }
}
