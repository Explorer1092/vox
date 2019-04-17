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

import com.voxlearning.alps.annotation.remote.ServiceMethod;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.vendor.api.entity.VendorNotify;

import java.util.List;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20171115")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface VendorManagement extends IPingable {

    @Deprecated
    void scheduleReloadCache();

    void scheduleDeleteJpushRetryMessage();

    void scheduleDeleteVendorNotify();

    @ServiceMethod(timeout = 5, unit = TimeUnit.MINUTES, retries = 0)
    List<VendorNotify> findUndeliveriedNotify();

    @ServiceMethod(timeout = 10, unit = TimeUnit.SECONDS, retries = 0)
    int findTodayDeliveryFailedNotifyCount();

    @ServiceMethod(timeout = 10, unit = TimeUnit.SECONDS, retries = 0)
    List<VendorNotify> findTodayDeliveryFailedNotify();

    @ServiceMethod(timeout = 60, unit = TimeUnit.SECONDS, retries = 0)
    void scheduleRemindExpireJxtNotice();

    void scheduleAutoAppJpushMessageRetry();
}
