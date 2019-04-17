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

package com.voxlearning.utopia.schedule.dropins;

import com.voxlearning.utopia.schedule.support.AbstractSweeperTask;
import com.voxlearning.utopia.schedule.support.SweeperTask;
import com.voxlearning.utopia.service.vendor.consumer.VendorManagementClient;

import java.util.Map;

/**
 * 删除消息中心重发表中非当天的数据
 *
 * @author shiwe.liao
 * @since 2016/1/25
 */
@SweeperTask
public class DeleteAppJpushRetryMessage extends AbstractSweeperTask {
    @Override
    public void execute(Map<String, Object> beans) {
        VendorManagementClient vendorManagementClient = applicationContext.getBean(VendorManagementClient.class);
        vendorManagementClient.scheduleDeleteJpushRetryMessage();
    }
}
