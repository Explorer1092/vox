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

package com.voxlearning.utopia.service.zone.impl.loader;

import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.zone.api.annotation.TruncateClazzZones;
import com.voxlearning.utopia.service.zone.api.entity.GiftHistory;
import com.voxlearning.utopia.service.zone.impl.persistence.GiftHistoryPersistence;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
public class TestGiftLoaderImpl {

    @Inject private GiftLoaderImpl giftLoader;
    @Inject private GiftHistoryPersistence giftHistoryPersistence;

    @Test
    @TruncateClazzZones
    public void testLoadGiftHistories() throws Exception {
        Long id1 = giftHistoryPersistence.persist(GiftHistory.mockInstance());
        Long id2 = giftHistoryPersistence.persist(GiftHistory.mockInstance());
        Long id3 = giftHistoryPersistence.persist(GiftHistory.mockInstance());
        Long id4 = giftHistoryPersistence.persist(GiftHistory.mockInstance());
        Long id5 = giftHistoryPersistence.persist(GiftHistory.mockInstance());
        Collection<Long> ids = Arrays.asList(id1, id2, id3, id4, id5);
        Map<Long, GiftHistory> map = giftLoader.loadGiftHistories(ids);
        assertEquals(ids.size(), map.size());
        for (Long id : ids) {
            assertNotNull(map.get(id));
        }
    }

}
