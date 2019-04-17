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

import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.queue.zone.ZoneEvent;
import com.voxlearning.utopia.queue.zone.ZoneEventType;
import com.voxlearning.utopia.service.zone.api.entity.StudentInfo;
import com.voxlearning.utopia.service.zone.impl.listener.ZoneQueueListener;
import com.voxlearning.utopia.service.zone.impl.persistence.StudentInfoPersistence;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
public class TestIncreaseStudyMasterCountByOne {

    @Inject
    private StudentInfoPersistence studentInfoPersistence;
    @Inject
    private ZoneQueueListener zoneQueueListener;

    @Test
    @TruncateDatabaseTable(databaseEntities = StudentInfo.class)
    public void testIncreaseStudyMasterCountByOne() throws Exception {
        long studentId = 30009;
        ZoneEvent event = new ZoneEvent();
        event.setType(ZoneEventType.IncreaseStudyMasterCountByOne);
        event.getAttributes().put("studentId", studentId);
        zoneQueueListener.onMessage(event.toMessage());
        StudentInfo info = studentInfoPersistence.load(studentId);
        assertEquals(1, info.getStudyMasterCount().intValue());
    }
}
