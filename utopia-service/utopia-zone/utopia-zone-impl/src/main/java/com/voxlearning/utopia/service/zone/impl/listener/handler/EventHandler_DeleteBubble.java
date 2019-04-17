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
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.queue.zone.ZoneEventType;
import com.voxlearning.utopia.service.zone.impl.listener.ZoneEventHandler;
import com.voxlearning.utopia.service.zone.impl.persistence.ClazzZoneBagPersistence;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Set;
import java.util.stream.Collectors;

@Named
public class EventHandler_DeleteBubble extends SpringContainerSupport implements ZoneEventHandler {

    @Inject private ClazzZoneBagPersistence clazzZoneBagPersistence;

    @Override
    public ZoneEventType getEventType() {
        return ZoneEventType.DELETE_BUBBLE;
    }

    @Override
    public void handle(JsonNode root) throws Exception {
        JsonNode bagIdsNode = root.get("bagIds");
        JsonNode userIdNode = root.get("userId");

        Set<Long> bagIds = StringUtils.toLongList(bagIdsNode.asText())
                .stream()
                .collect(Collectors.toSet());
        Long userId = SafeConverter.toLong(userIdNode.asText());

        bagIds.forEach(bagId -> clazzZoneBagPersistence.delete(bagId, userId));
    }
}
