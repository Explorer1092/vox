package com.voxlearning.ucenter.controller.connect.impl;

import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.utopia.service.vendor.consumer.VendorLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 翻转课堂sso connector
 *
 * @author xuesong.zhang
 * @since 2016/10/11
 */
@Named
public class XuebaSsoConnector extends AbstractPlatformSsoConnector {

    @Inject private VendorLoaderClient vendorLoaderClient;

    private static final String APP_KEY = "17xueba";
    private static final String APP_KEY_NEW = "YiQiXue";

    private String secretKey = null;

    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    @Override
    String getSecretKey() {
        lock.readLock().lock();
        try {
            if (secretKey != null) {
                return secretKey;
            }
        } finally {
            lock.readLock().unlock();
        }
        lock.writeLock().lock();
        try {
            if (secretKey != null) {
                return secretKey;
            }
            VendorApps vendorApps = vendorLoaderClient.loadVendor(APP_KEY);
            if (vendorApps == null) {
                vendorApps = vendorLoaderClient.loadVendor(APP_KEY_NEW);
            }
            secretKey = vendorApps.getSecretKey();
            return secretKey;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    int getTimeLimitInSeconds() {
        return 10 * 60;
    }
}
