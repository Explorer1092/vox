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

package com.voxlearning.utopia.service.business.impl.processor.vendorAppFilter.filterList;

import com.voxlearning.utopia.service.business.impl.processor.AbstractExecuteTask;
import com.voxlearning.utopia.service.business.impl.processor.vendorAppFilter.VendorAppFilterContext;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserBlacklistServiceClient;
import com.voxlearning.utopia.service.vendor.consumer.VendorLoaderClient;

import javax.inject.Inject;

import static com.voxlearning.utopia.api.constant.OrderProductServiceType.*;

/**
 * @author peng.zhang.a
 * @since 16-10-12
 */
abstract class FilterBase extends AbstractExecuteTask<VendorAppFilterContext> {

    @Inject protected VendorLoaderClient vendorLoaderClient;
    @Inject protected UserBlacklistServiceClient userBlacklistServiceClient;
    @Inject protected UserOrderLoaderClient userOrderLoaderClient;
    @Inject protected StudentLoaderClient studentLoaderClient;


    protected boolean isAfenti(String appKey) {
        return AfentiChinese.isEqual(appKey) || AfentiChineseImproved.isEqual(appKey)
                || AfentiMath.isEqual(appKey) || AfentiMathImproved.isEqual(appKey)
                || AfentiExam.isEqual(appKey) || AfentiExamImproved.isEqual(appKey);
    }

}
