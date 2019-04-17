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
import com.voxlearning.alps.test.annotation.MockBinder;
import com.voxlearning.alps.test.context.MDP;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.zone.api.entity.ClazzZoneComment;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * @author RuiBao
 * @version 0.1
 * @since 14-5-19
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
public class TestClazzZoneCommentPersistence {

    @Inject private ClazzZoneCommentPersistence clazzZoneCommentPersistence;

    @Test
    @TruncateDatabaseTable(databaseEntities = ClazzZoneComment.class)
    @MockBinder(
            type = ClazzZoneComment.class,
            jsons = {
                    "{'journalId':1,'journalOwnerId':0,'journalOwnerClazzId':0,'userId':1,'userName':'','imgComment':0}",
                    "{'journalId':1,'journalOwnerId':0,'journalOwnerClazzId':0,'userId':2,'userName':'','imgComment':0}",
                    "{'journalId':1,'journalOwnerId':0,'journalOwnerClazzId':0,'userId':3,'userName':'','imgComment':0}",
                    "{'journalId':2,'journalOwnerId':0,'journalOwnerClazzId':0,'userId':1,'userName':'','imgComment':0}",
                    "{'journalId':2,'journalOwnerId':0,'journalOwnerClazzId':0,'userId':2,'userName':'','imgComment':0}",
                    "{'journalId':2,'journalOwnerId':0,'journalOwnerClazzId':0,'userId':3,'userName':'','imgComment':0}",
                    "{'journalId':3,'journalOwnerId':0,'journalOwnerClazzId':0,'userId':1,'userName':'','imgComment':0}",
                    "{'journalId':3,'journalOwnerId':0,'journalOwnerClazzId':0,'userId':2,'userName':'','imgComment':0}",
                    "{'journalId':3,'journalOwnerId':0,'journalOwnerClazzId':0,'userId':3,'userName':'','imgComment':0}",
            },
            persistence = ClazzZoneCommentPersistence.class
    )
    public void testFindByJournalIds() throws Exception {
        Set<Long> journalIds = MDP.groupingBy(ClazzZoneComment.class, ClazzZoneComment::getJournalId).keySet();
        Map<Long, List<ClazzZoneComment>> map = clazzZoneCommentPersistence.findByJournalIds(journalIds);
        assertEquals(journalIds.size(), map.size());
        for (Long journalId : journalIds) {
            assertEquals(3, map.get(journalId).size());
            assertEquals(3, clazzZoneCommentPersistence.findByJournalId(journalId).size());
        }
    }
}
