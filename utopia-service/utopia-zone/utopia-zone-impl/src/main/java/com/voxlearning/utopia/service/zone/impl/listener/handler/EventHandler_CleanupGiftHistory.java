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

package com.voxlearning.utopia.service.zone.impl.listener.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.voxlearning.alps.annotation.common.Spring;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.queue.zone.ZoneEventType;
import com.voxlearning.utopia.service.zone.impl.listener.ZoneEventHandler;
import com.voxlearning.utopia.service.zone.impl.persistence.GiftHistoryPersistence;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;

@Spring
@Named
public class EventHandler_CleanupGiftHistory extends SpringContainerSupport implements ZoneEventHandler {

    @Inject private GiftHistoryPersistence giftHistoryPersistence;

    @Override
    public ZoneEventType getEventType() {
        return ZoneEventType.CLEANUP_GIFT_HISTORY;
    }

    @Override
    public void handle(JsonNode root) throws Exception {
        Date beginDate = DateUtils.nextDay(new Date(), -45);
        logger.info("We're going to clean up all gift histories which created before {}", beginDate);

        String sql = "DELETE FROM VOX_GIFT_HISTORY WHERE CREATE_DATETIME <= ?";
        int deleted = giftHistoryPersistence.getUtopiaSql().withSql(sql).useParamsArgs(beginDate).executeUpdate();
        logger.info("There are {} gift histories deleted", deleted);
    }
}
