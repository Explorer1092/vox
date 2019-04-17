/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
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
import com.voxlearning.utopia.service.flower.client.FlowerServiceClient;

import java.util.Map;

/**
 * Created by Summer Yang on 2016/2/29.
 */
@SweeperTask
public class DeleteFlower extends AbstractSweeperTask {

    @Override
    public void execute(Map<String, Object> beans) {
        FlowerServiceClient flowerServiceClient;
        try {
            flowerServiceClient = applicationContext.getBean(FlowerServiceClient.class);
        } catch (Exception ex) {
            logger.warn("No FlowerServiceClient configured.", ex);
            flowerServiceClient = null;
        }
        if (flowerServiceClient == null) {
            return;
        }
        flowerServiceClient.getFlowerService().purgeExpiredFlowers();
    }
}
