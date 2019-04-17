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

package com.voxlearning.utopia.service.zone.impl.persistence;

import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.zone.api.entity.GiftHistory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
public class TestGiftHistoryPersistence {

    @Inject private GiftHistoryPersistence giftHistoryPersistence;

    @Test
    @TruncateDatabaseTable(databaseEntities = GiftHistory.class)
    public void testQueryIdsBySenderId() throws Exception {
        long senderId = 30009;
        for (int i = 0; i < 10; i++) {
            giftHistoryPersistence.persist(GiftHistory.mockInstance().withSenderId(senderId));
            Collection<Long> ids = giftHistoryPersistence.queryIdsBySenderId(senderId);
            assertEquals(i + 1, ids.size());
        }
    }

    @Test
    @TruncateDatabaseTable(databaseEntities = GiftHistory.class)
    public void testQueryIdsByReceiverId() throws Exception {
        long receiverId = 30009;
        for (int i = 0; i < 10; i++) {
            giftHistoryPersistence.persist(GiftHistory.mockInstance().withReceiverId(receiverId));
            Collection<Long> ids = giftHistoryPersistence.queryIdsByReceiverId(receiverId);
            assertEquals(i + 1, ids.size());
        }
    }
}
