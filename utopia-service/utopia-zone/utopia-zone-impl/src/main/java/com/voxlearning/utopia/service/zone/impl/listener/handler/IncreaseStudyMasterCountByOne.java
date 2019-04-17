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
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonObjectMapper;
import com.voxlearning.utopia.queue.zone.ZoneEvent;
import com.voxlearning.utopia.queue.zone.ZoneEventType;
import com.voxlearning.utopia.service.zone.impl.listener.ZoneEventHandler;
import com.voxlearning.utopia.service.zone.impl.persistence.StudentInfoPersistence;

import javax.inject.Inject;
import javax.inject.Named;

@Named("zoneEventHandler.increaseStudyMasterCountByOne")
public class IncreaseStudyMasterCountByOne implements ZoneEventHandler {

    @Inject
    private StudentInfoPersistence studentInfoPersistence;

    @Override
    public ZoneEventType getEventType() {
        return ZoneEventType.IncreaseStudyMasterCountByOne;
    }

    @Override
    public void handle(JsonNode root) throws Exception {
        ZoneEvent event = JsonObjectMapper.OBJECT_MAPPER.readValue(root.traverse(), ZoneEvent.class);
        if (event == null) return;
        if (event.getAttributes() == null) return;
        long studentId = SafeConverter.toLong(event.getAttributes().get("studentId"));
        if (studentId == 0) return;
        studentInfoPersistence.createOrIncreaseStudyMasterCountByOne(studentId);
    }
}
