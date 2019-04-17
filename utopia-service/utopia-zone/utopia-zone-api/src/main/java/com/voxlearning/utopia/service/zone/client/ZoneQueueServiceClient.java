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

package com.voxlearning.utopia.service.zone.client;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.spi.common.JsonStringSerializer;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.queue.zone.ZoneEvent;
import com.voxlearning.utopia.queue.zone.ZoneEventType;
import com.voxlearning.utopia.service.zone.api.ZoneQueueService;
import com.voxlearning.utopia.service.zone.support.ClazzJournalCreator;
import lombok.Getter;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class ZoneQueueServiceClient {

    @Getter
    @ImportService(interfaceClass = ZoneQueueService.class)
    private ZoneQueueService zoneQueueService;

    public void increaseStudyMasterCountByOne(Long studentId) {
        if (studentId == null) {
            return;
        }
        ZoneEvent event = new ZoneEvent();
        event.setType(ZoneEventType.IncreaseStudyMasterCountByOne);
        event.getAttributes().put("studentId", studentId);
        zoneQueueService.sendMessage(event.toMessage());
    }

    public void deleteBubbles(Collection<Long> bagIds, Long userId) {
        bagIds = CollectionUtils.toLinkedHashSet(bagIds);
        if (bagIds.isEmpty() || userId == null) {
            return;
        }
        Map<String, String> message = new LinkedHashMap<>();
        message.put("T", ZoneEventType.DELETE_BUBBLE.name());
        message.put("bagIds", StringUtils.join(bagIds, ","));
        message.put("userId", userId.toString());
        String json = JsonStringSerializer.getInstance().serialize(message);
        zoneQueueService.sendMessage(Message.newMessage().withStringBody(json));
    }

    public ClazzJournalCreator createClazzJournal(Long clazzId) {
        return new ClazzJournalCreator(zoneQueueService).withClazz(clazzId);
    }
}
