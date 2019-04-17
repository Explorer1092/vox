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
import com.voxlearning.utopia.service.vendor.api.VendorManagement;
import com.voxlearning.utopia.service.vendor.api.entity.VendorNotify;

import java.util.List;

public class VendorManagementClient implements VendorManagement {

    @ImportService(interfaceClass = VendorManagement.class)
    private VendorManagement remoteReference;

    @Deprecated
    @Override
    public void scheduleReloadCache() {
        remoteReference.scheduleReloadCache();
    }

    @Override
    public void scheduleDeleteJpushRetryMessage() {
        remoteReference.scheduleDeleteJpushRetryMessage();
    }

    @Override
    public void scheduleDeleteVendorNotify() {
        remoteReference.scheduleDeleteVendorNotify();
    }

    @Override
    public List<VendorNotify> findUndeliveriedNotify() {
        return remoteReference.findUndeliveriedNotify();
    }

    @Override
    public int findTodayDeliveryFailedNotifyCount() {
        return remoteReference.findTodayDeliveryFailedNotifyCount();
    }

    @Override
    public List<VendorNotify> findTodayDeliveryFailedNotify() {
        return remoteReference.findTodayDeliveryFailedNotify();
    }

    @Override
    public void scheduleRemindExpireJxtNotice() {
        remoteReference.scheduleRemindExpireJxtNotice();
    }

    @Override
    public void scheduleAutoAppJpushMessageRetry() {
        remoteReference.scheduleAutoAppJpushMessageRetry();
    }

}
