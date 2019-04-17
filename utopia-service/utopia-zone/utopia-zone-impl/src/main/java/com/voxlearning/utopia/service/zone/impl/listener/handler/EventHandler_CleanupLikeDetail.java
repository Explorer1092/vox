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
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.queue.zone.ZoneEventType;
import com.voxlearning.utopia.service.zone.impl.listener.ZoneEventHandler;
import com.voxlearning.utopia.service.zone.impl.persistence.LikeDetailPersistence;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;

@Spring
@Named
public class EventHandler_CleanupLikeDetail extends SpringContainerSupport implements ZoneEventHandler {

    @Inject private LikeDetailPersistence likeDetailPersistence;

    @Override
    public ZoneEventType getEventType() {
        return ZoneEventType.CLEANUP_LIKE_DETAIL;
    }

    @Override
    public void handle(JsonNode root) throws Exception {
        JsonNode timestampNode = root.get("TS");
        long timestamp = timestampNode.asLong();
        if (timestamp == 0) {
            logger.warn("Invalid 'TS' value received, ignore");
            return;
        }

        Date date = new Date(timestamp);
        logger.info("We're going to cleanup all like detail(s) which created before {}", date);

        int count = likeDetailPersistence.getUtopiaSql()
                .withSql("DELETE FROM VOX_LIKE_DETAIL WHERE CREATE_DATETIME<?")
                .useParamsArgs(date)
                .executeUpdate();

        logger.info("Total {} like detail(s) deleted", count);
    }
}
