/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.vendor.api.annotation;

import com.voxlearning.alps.spi.test.Truncatable;
import com.voxlearning.utopia.service.vendor.api.entity.*;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Truncatable
public @interface TruncateVendors {
    Class<?>[] databaseEntities() default {
            Vendor.class,
            VendorAppRewardHistory.class,
            VendorApps.class,
            VendorAppsOrder.class,
            VendorAppsResgRef.class,
            VendorAppsUserRef.class,
            VendorNotify.class,
            VendorResg.class,
            VendorResgContent.class
    };

    String[] databaseTables() default {};
}
