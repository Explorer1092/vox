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

package com.voxlearning.utopia.service.action.impl.service.support;

import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.action.api.event.ActionEvent;
import com.voxlearning.utopia.service.action.api.event.ActionEventType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
public class TestActionEventDayRangeCounter {
    @Inject
    private ActionEventDayRangeCounter actionEventDayRangeCounter;

    @Test
    public void testActionEventDayRangeCounter() throws Exception {
        ActionEvent event = new ActionEvent();
        event.setUserId(30009L);
        event.setType(ActionEventType.FinishSelfLearning);
        assertEquals(1, actionEventDayRangeCounter.increase(event));
        assertEquals(2, actionEventDayRangeCounter.increase(event));
        assertEquals(3, actionEventDayRangeCounter.increase(event));
        assertEquals(4, actionEventDayRangeCounter.increase(event));
        assertEquals(5, actionEventDayRangeCounter.increase(event));
    }
}
